package com.acm.acmwebsite.integration;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Department;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.entity.EventFormQuestion;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.repository.EventFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.EventRegistrationRepository;
import com.acm.acmwebsite.feature.repository.EventRepository;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Event Registration & Attendance Workflow Integration Tests")
class EventRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private EventRegistrationRepository eventRegistrationRepository;

    @Autowired
    private EventFormQuestionRepository eventFormQuestionRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JwtUtil jwtUtil;

    @MockitoBean
    private EmailService emailService;

    @MockitoBean
    private EmailExitanceService emailExitanceService;

    @MockitoBean
    private GoogleSheetsService googleSheetsService;

    private User attendee;
    private User admin;
    private String attendeeToken;
    private String adminToken;

    private Event testEvent;
    private EventFormQuestion requiredQuestion;

    @BeforeEach
    void setUp() {
        when(emailExitanceService.isEmailReal(anyString())).thenReturn(true);

        // 1. Create attendee user with complete profile
        attendee = User.builder()
                .email("attendee@example.com")
                .name("Attendee Name")
                .phoneNumber("01011112222")
                .isAlexEngStudent(true)
                .department(Department.CSED)
                .batch("2026")
                .role(Role.USER)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build();
        attendee = userRepository.save(attendee);
        attendeeToken = jwtUtil.generateToken(attendee.getEmail(), attendee.getRole().name());

        // 2. Create admin user
        admin = User.builder()
                .email("eventadmin@example.com")
                .name("Admin User")
                .phoneNumber("01099998888")
                .isAlexEngStudent(true)
                .role(Role.SUPER_ADMIN)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build();
        admin = userRepository.save(admin);
        adminToken = jwtUtil.generateToken(admin.getEmail(), admin.getRole().name());

        // 3. Create test event (starts with registration closed)
        testEvent = new Event();
        testEvent.setName("Hackathon 2026");
        testEvent.setDescription("Annual Competitive Programming Hackathon");
        testEvent.setLocation("Engineering Hall A");
        testEvent.setEventTime(LocalDateTime.now().plusDays(7));
        testEvent.setRegistrationOpen(false);
        testEvent = eventRepository.save(testEvent);

        // 4. Create required question for event
        requiredQuestion = EventFormQuestion.builder()
                .event(testEvent)
                .questionText("T-Shirt Size")
                .questionType(QuestionType.TEXT)
                .isRequired(true)
                .options(List.of("S", "M", "L", "XL"))
                .build();
        requiredQuestion = eventFormQuestionRepository.save(requiredQuestion);

        entityManager.flush();
    }

    @Nested
    @DisplayName("Complete Registration & Cancellation Lifecycle")
    class FullLifecycleTests {

        @Test
        @DisplayName("Complete flow: closed guard -> admin opens -> retrieve questions -> register -> analysis -> duplicate guard -> cancel -> analysis verify")
        void fullEventRegistrationAndCancellationLifecycle() throws Exception {
            Long eventId = testEvent.getId();
            UUID userId = attendee.getId();

            Map<Long, String> answers = new HashMap<>();
            answers.put(requiredQuestion.getId(), "L");

            RegistrationRequestDto registrationDto = RegistrationRequestDto.builder()
                    .userId(userId)
                    .answers(answers)
                    .build();

            // 1. REGISTRATION CLOSED GUARD
            mockMvc.perform(post("/api/events/" + eventId + "/register")
                            .header("Authorization", "Bearer " + attendeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Registration is closed for this event."));

            assertFalse(eventRegistrationRepository.existsByUserIdAndEventId(userId, eventId));

            // 2. ADMIN OPENS REGISTRATION
            mockMvc.perform(post("/api/events/" + eventId + "/open-call")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registrationOpen").value(true));

            entityManager.flush();

            // 3. RETRIEVE EVENT QUESTIONS
            mockMvc.perform(get("/api/events/" + eventId + "/questions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].question_text").value("T-Shirt Size"))
                    .andExpect(jsonPath("$[0].is_required").value(true));

            // 4. REGISTER USER
            mockMvc.perform(post("/api/events/" + eventId + "/register")
                            .header("Authorization", "Bearer " + attendeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isCreated());

            // Verify in Database
            assertTrue(eventRegistrationRepository.existsByUserIdAndEventId(userId, eventId));

            // 5. VERIFY REGISTRATION STATUS ENDPOINT
            mockMvc.perform(get("/api/events/" + eventId + "/is-registered/" + userId)
                            .header("Authorization", "Bearer " + attendeeToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered").value(true));

            // 6. ADMIN CHECKS REGISTRATION ANALYSIS
            mockMvc.perform(get("/api/events/" + eventId + "/registrations/analysis")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations").value(1))
                    .andExpect(jsonPath("$.alexUniStudentCount").value(1));

            // 7. DUPLICATE REGISTRATION REJECTION GUARD
            mockMvc.perform(post("/api/events/" + eventId + "/register")
                            .header("Authorization", "Bearer " + attendeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isConflict());

            // 8. CANCEL REGISTRATION
            mockMvc.perform(delete("/api/events/" + eventId + "/register/" + userId)
                            .header("Authorization", "Bearer " + attendeeToken))
                    .andExpect(status().isNoContent());

            // Verify Database cleanup
            assertFalse(eventRegistrationRepository.existsByUserIdAndEventId(userId, eventId));

            // 9. VERIFY STATUS AFTER CANCELLATION
            mockMvc.perform(get("/api/events/" + eventId + "/is-registered/" + userId)
                            .header("Authorization", "Bearer " + attendeeToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered").value(false));

            // 10. ADMIN ANALYSIS REFLECTS DECREMENT
            mockMvc.perform(get("/api/events/" + eventId + "/registrations/analysis")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations").value(0));

            // 11. CANCELLING AGAIN REJECTS WITH 404 NOT FOUND
            mockMvc.perform(delete("/api/events/" + eventId + "/register/" + userId)
                            .header("Authorization", "Bearer " + attendeeToken))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Validation & Integrity Guards")
    class ValidationGuardTests {

        @Test
        @DisplayName("Rejects registration when user profile details are incomplete")
        void rejectsIncompleteUserProfile() throws Exception {
            // Open registration
            testEvent.setRegistrationOpen(true);
            eventRepository.save(testEvent);
            entityManager.flush();

            // User without phone number
            User incompleteUser = User.builder()
                    .email("incomplete@example.com")
                    .name("Incomplete User")
                    .phoneNumber(null)
                    .isAlexEngStudent(true)
                    .role(Role.USER)
                    .emailConfirmed(true)
                    .passwordHash("hashed")
                    .build();
            incompleteUser = userRepository.save(incompleteUser);
            String incompleteToken = jwtUtil.generateToken(incompleteUser.getEmail(), incompleteUser.getRole().name());

            RegistrationRequestDto requestDto = RegistrationRequestDto.builder()
                    .userId(incompleteUser.getId())
                    .answers(Map.of(requiredQuestion.getId(), "M"))
                    .build();

            mockMvc.perform(post("/api/events/" + testEvent.getId() + "/register")
                            .header("Authorization", "Bearer " + incompleteToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Please complete your profile details (Name & Phone Number & Student Status) before registering."));
        }

        @Test
        @DisplayName("Rejects registration when required dynamic form answer is missing or blank")
        void rejectsMissingRequiredFormAnswer() throws Exception {
            testEvent.setRegistrationOpen(true);
            eventRepository.save(testEvent);
            entityManager.flush();

            RegistrationRequestDto missingAnswerDto = RegistrationRequestDto.builder()
                    .userId(attendee.getId())
                    .answers(Map.of()) // Empty answers
                    .build();

            mockMvc.perform(post("/api/events/" + testEvent.getId() + "/register")
                            .header("Authorization", "Bearer " + attendeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(missingAnswerDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Answer is required for: T-Shirt Size"));
        }

        @Test
        @DisplayName("Rejects registration when userId is missing from request body")
        void rejectsMissingUserIdInRequestBody() throws Exception {
            testEvent.setRegistrationOpen(true);
            eventRepository.save(testEvent);
            entityManager.flush();

            RegistrationRequestDto noUserDto = RegistrationRequestDto.builder()
                    .userId(null)
                    .build();

            mockMvc.perform(post("/api/events/" + testEvent.getId() + "/register")
                            .header("Authorization", "Bearer " + attendeeToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(noUserDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Rejects invalid UUID format when checking registration status")
        void rejectsInvalidUuidFormat() throws Exception {
            mockMvc.perform(get("/api/events/" + testEvent.getId() + "/is-registered/not-a-valid-uuid")
                            .header("Authorization", "Bearer " + attendeeToken))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Authorization & Impersonation Guards")
    class AuthorizationGuardTests {

        @Test
        @DisplayName("Rejects registration and cancellation when user attempts to act on behalf of another user")
        void rejectsCrossUserImpersonation() throws Exception {
            testEvent.setRegistrationOpen(true);
            eventRepository.save(testEvent);
            entityManager.flush();

            User otherUser = User.builder()
                    .email("other@example.com")
                    .name("Other User")
                    .phoneNumber("01022223333")
                    .isAlexEngStudent(true)
                    .role(Role.USER)
                    .emailConfirmed(true)
                    .passwordHash("hashed")
                    .build();
            otherUser = userRepository.save(otherUser);

            // Attendee attempts to register for otherUser
            RegistrationRequestDto impersonatedDto = RegistrationRequestDto.builder()
                    .userId(otherUser.getId())
                    .answers(Map.of(requiredQuestion.getId(), "XL"))
                    .build();

            mockMvc.perform(post("/api/events/" + testEvent.getId() + "/register")
                            .header("Authorization", "Bearer " + attendeeToken) // token is for attendee, not otherUser
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(impersonatedDto)))
                    .andExpect(status().isForbidden());

            // Attendee attempts to cancel otherUser's registration
            mockMvc.perform(delete("/api/events/" + testEvent.getId() + "/register/" + otherUser.getId())
                            .header("Authorization", "Bearer " + attendeeToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Rejects unauthenticated access to registration endpoints")
        void rejectsUnauthenticatedAccess() throws Exception {
            RegistrationRequestDto requestDto = RegistrationRequestDto.builder()
                    .userId(attendee.getId())
                    .build();

            // Unauthenticated register (blocked by Spring Security filter chain with 403)
            mockMvc.perform(post("/api/events/" + testEvent.getId() + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isForbidden());

            // Unauthenticated cancel (handled by controller authorization check with 401)
            mockMvc.perform(delete("/api/events/" + testEvent.getId() + "/register/" + attendee.getId()))
                    .andExpect(status().isUnauthorized());

            // Unauthenticated is-registered (blocked by Spring Security filter chain with 403)
            mockMvc.perform(get("/api/events/" + testEvent.getId() + "/is-registered/" + attendee.getId()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Rejects non-admin user trying to open event registration")
        void rejectsNonAdminOpeningRegistration() throws Exception {
            mockMvc.perform(post("/api/events/" + testEvent.getId() + "/open-call")
                            .header("Authorization", "Bearer " + attendeeToken))
                    .andExpect(status().isForbidden());
        }
    }
}
