package com.acm.acmwebsite.integration;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Department;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.OpenCallRequestDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.CommitteeCall;
import com.acm.acmwebsite.feature.entity.CommitteeFormQuestion;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.repository.CommitteeCallRepository;
import com.acm.acmwebsite.feature.repository.CommitteeFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.CommitteeRegistrationRepository;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
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
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
@DisplayName("Committee Application Lifecycle Integration Tests")
class CommitteeRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommitteeRepository committeeRepository;

    @Autowired
    private CommitteeCallRepository committeeCallRepository;

    @Autowired
    private CommitteeRegistrationRepository committeeRegistrationRepository;

    @Autowired
    private CommitteeFormQuestionRepository committeeFormQuestionRepository;

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

    private User applicant;
    private User admin;
    private String applicantToken;
    private String adminToken;

    private Committee testCommittee;
    private CommitteeFormQuestion requiredQuestion;

    @BeforeEach
    void setUp() {
        when(emailExitanceService.isEmailReal(anyString())).thenReturn(true);

        // 1. Create applicant user with complete profile
        applicant = User.builder()
                .email("committee.applicant@example.com")
                .name("Applicant Candidate")
                .phoneNumber("01055556666")
                .isAlexEngStudent(true)
                .department(Department.CSED)
                .batch("2026")
                .role(Role.USER)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build();
        applicant = userRepository.save(applicant);
        applicantToken = jwtUtil.generateToken(applicant.getEmail(), applicant.getRole().name());

        // 2. Create admin user
        admin = User.builder()
                .email("committee.admin@example.com")
                .name("Committee Admin")
                .phoneNumber("01077778888")
                .isAlexEngStudent(true)
                .role(Role.SUPER_ADMIN)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build();
        admin = userRepository.save(admin);
        adminToken = jwtUtil.generateToken(admin.getEmail(), admin.getRole().name());

        // 3. Create test committee with call message (starts with call closed)
        testCommittee = new Committee();
        testCommittee.setName("Technical Committee");
        testCommittee.setDescription("Conducts workshops, competitive programming sessions, and hackathons");
        testCommittee.setOpen(false);

        Message callMessage = new Message("Technical Committee Call Open", "Applications for Technical Committee are now open!");
        testCommittee.setCallMessage(callMessage);

        testCommittee = committeeRepository.save(testCommittee);

        // 4. Create required question for the committee
        requiredQuestion = CommitteeFormQuestion.builder()
                .committee(testCommittee)
                .questionText("Why do you want to join our committee?")
                .questionType(QuestionType.TEXT)
                .isRequired(true)
                .options(List.of())
                .build();
        requiredQuestion = committeeFormQuestionRepository.save(requiredQuestion);

        entityManager.flush();
    }

    @Nested
    @DisplayName("Complete Committee Call & Application Lifecycle")
    class FullLifecycleTests {

        @Test
        @DisplayName("Complete flow: closed guard -> open call -> announce call -> retrieve questions -> submit application -> analysis verify -> duplicate guard -> cancel -> analysis verify -> close call")
        void fullCommitteeApplicationLifecycle() throws Exception {
            Long committeeId = testCommittee.getId();
            UUID userId = applicant.getId();

            Map<Long, String> answers = new HashMap<>();
            answers.put(requiredQuestion.getId(), "Passionate about backend algorithms and teaching");

            RegistrationRequestDto registrationDto = RegistrationRequestDto.builder()
                    .userId(userId)
                    .answers(answers)
                    .build();

            // 1. REGISTRATION CLOSED GUARD
            assertThatThrownBy(() ->
                    mockMvc.perform(post("/api/committee/" + committeeId + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto))))
                    .hasRootCauseInstanceOf(IllegalStateException.class)
                    .hasRootCauseMessage("The call for this committee is currently closed.");

            // 2. ADMIN OPENS COMMITTEE CALL
            OpenCallRequestDto openCallDto = new OpenCallRequestDto(true);

            mockMvc.perform(post("/api/committee/" + committeeId + "/open-call")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(openCallDto)))
                    .andExpect(status().isOk());

            entityManager.flush();

            CommitteeCall activeCall = committeeCallRepository.findActiveCallByCommitteeId(committeeId)
                    .orElseThrow(() -> new AssertionError("Active committee call should be present"));
            Long callId = activeCall.getId();
            assertNotNull(callId);

            // 3. ADMIN ANNOUNCEMENT & DEDUPLICATION GUARD
            // Second announcement attempt without force should conflict
            mockMvc.perform(post("/api/committee/" + committeeId + "/announce-call")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.code").value("ANNOUNCEMENT_ALREADY_SENT"));

            // Force announcement succeeds
            mockMvc.perform(post("/api/committee/" + committeeId + "/announce-call?force=true")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            // 4. RETRIEVE COMMITTEE QUESTIONS
            mockMvc.perform(get("/api/committee/" + committeeId + "/questions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].question_text").value("Why do you want to join our committee?"))
                    .andExpect(jsonPath("$[0].is_required").value(true));

            // 5. APPLICANT SUBMITS APPLICATION
            mockMvc.perform(post("/api/committee/" + committeeId + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isCreated());

            // Verify in Database
            assertTrue(committeeRegistrationRepository.existsByUserIdAndCommitteeCallId(userId, callId));

            // 6. APPLICANT VERIFIES REGISTRATION STATUS
            mockMvc.perform(get("/api/committee/" + committeeId + "/is-registered/" + userId)
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered").value(true));

            // 7. ADMIN CHECKS APPLICATION ANALYSIS
            mockMvc.perform(get("/api/committee/calls/" + callId + "/registrations/analysis")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations").value(1))
                    .andExpect(jsonPath("$.alexUniStudentCount").value(1))
                    .andExpect(jsonPath("$.departmentCounts.CSED").value(1));

            // 8. DUPLICATE APPLICATION REJECTION GUARD
            mockMvc.perform(post("/api/committee/" + committeeId + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("You have already applied for this committee call."));

            // 9. CANCEL APPLICATION
            mockMvc.perform(delete("/api/committee/" + committeeId + "/register/" + userId)
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isNoContent());

            // Verify Database cleanup
            assertFalse(committeeRegistrationRepository.existsByUserIdAndCommitteeCallId(userId, callId));

            // 10. STATUS AFTER CANCELLATION
            mockMvc.perform(get("/api/committee/" + committeeId + "/is-registered/" + userId)
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered").value(false));

            // 11. ADMIN ANALYSIS REFLECTS DECREMENT
            mockMvc.perform(get("/api/committee/calls/" + callId + "/registrations/analysis")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations").value(0));

            // 12. CANCELLING AGAIN REJECTS WITH 404 NOT FOUND
            mockMvc.perform(delete("/api/committee/" + committeeId + "/register/" + userId)
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isNotFound());

            // 13. ADMIN CLOSES COMMITTEE CALL
            mockMvc.perform(post("/api/committee/" + committeeId + "/close-call")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk());

            entityManager.flush();

            // Verify active call is closed
            assertTrue(committeeCallRepository.findActiveCallByCommitteeId(committeeId).isEmpty());
        }
    }

    @Nested
    @DisplayName("Validation & Integrity Guards")
    class ValidationGuardTests {

        @Test
        @DisplayName("Rejects application when applicant profile details are incomplete")
        void rejectsIncompleteUserProfile() throws Exception {
            // Open call
            testCommittee.setOpen(true);
            committeeRepository.save(testCommittee);
            CommitteeCall call = CommitteeCall.builder()
                    .committee(testCommittee)
                    .openedAt(java.time.LocalDateTime.now())
                    .build();
            committeeCallRepository.save(call);
            entityManager.flush();

            // User without phone number
            User incompleteUser = User.builder()
                    .email("incomplete.committee@example.com")
                    .name("Incomplete Committee Candidate")
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
                    .answers(Map.of(requiredQuestion.getId(), "My Answer"))
                    .build();

            mockMvc.perform(post("/api/committee/" + testCommittee.getId() + "/register")
                            .header("Authorization", "Bearer " + incompleteToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Please complete your profile details (Name & Phone Number & Student Status) before registering."));
        }

        @Test
        @DisplayName("Rejects application when required dynamic form answer is missing or blank")
        void rejectsMissingRequiredFormAnswer() throws Exception {
            testCommittee.setOpen(true);
            committeeRepository.save(testCommittee);
            CommitteeCall call = CommitteeCall.builder()
                    .committee(testCommittee)
                    .openedAt(java.time.LocalDateTime.now())
                    .build();
            committeeCallRepository.save(call);
            entityManager.flush();

            RegistrationRequestDto missingAnswerDto = RegistrationRequestDto.builder()
                    .userId(applicant.getId())
                    .answers(Map.of()) // Empty answers
                    .build();

            mockMvc.perform(post("/api/committee/" + testCommittee.getId() + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(missingAnswerDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Answer is required for: Why do you want to join our committee?"));
        }

        @Test
        @DisplayName("Rejects application when userId is missing from request body")
        void rejectsMissingUserIdInRequestBody() throws Exception {
            testCommittee.setOpen(true);
            committeeRepository.save(testCommittee);

            RegistrationRequestDto noUserDto = RegistrationRequestDto.builder()
                    .userId(null)
                    .build();

            mockMvc.perform(post("/api/committee/" + testCommittee.getId() + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(noUserDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Rejects invalid UUID format when querying application status")
        void rejectsInvalidUuidFormat() throws Exception {
            mockMvc.perform(get("/api/committee/" + testCommittee.getId() + "/is-registered/not-a-valid-uuid")
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Authorization & Impersonation Guards")
    class AuthorizationGuardTests {

        @Test
        @DisplayName("Rejects application and cancellation when user attempts cross-user impersonation")
        void rejectsCrossUserImpersonation() throws Exception {
            testCommittee.setOpen(true);
            committeeRepository.save(testCommittee);
            CommitteeCall call = CommitteeCall.builder()
                    .committee(testCommittee)
                    .openedAt(java.time.LocalDateTime.now())
                    .build();
            committeeCallRepository.save(call);
            entityManager.flush();

            User otherUser = User.builder()
                    .email("other.committee@example.com")
                    .name("Other Committee User")
                    .phoneNumber("01033334444")
                    .isAlexEngStudent(true)
                    .role(Role.USER)
                    .emailConfirmed(true)
                    .passwordHash("hashed")
                    .build();
            otherUser = userRepository.save(otherUser);

            // Applicant attempts to register for otherUser
            RegistrationRequestDto impersonatedDto = RegistrationRequestDto.builder()
                    .userId(otherUser.getId())
                    .answers(Map.of(requiredQuestion.getId(), "Impersonated answer"))
                    .build();

            mockMvc.perform(post("/api/committee/" + testCommittee.getId() + "/register")
                            .header("Authorization", "Bearer " + applicantToken) // token is for applicant, not otherUser
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(impersonatedDto)))
                    .andExpect(status().isForbidden());

            // Applicant attempts to cancel otherUser's registration
            mockMvc.perform(delete("/api/committee/" + testCommittee.getId() + "/register/" + otherUser.getId())
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Rejects unauthenticated access to registration endpoints")
        void rejectsUnauthenticatedAccess() throws Exception {
            RegistrationRequestDto requestDto = RegistrationRequestDto.builder()
                    .userId(applicant.getId())
                    .build();

            // Unauthenticated register (blocked by Spring Security filter chain with 403)
            mockMvc.perform(post("/api/committee/" + testCommittee.getId() + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isForbidden());

            // Unauthenticated cancel (handled by controller authorization check with 401)
            mockMvc.perform(delete("/api/committee/" + testCommittee.getId() + "/register/" + applicant.getId()))
                    .andExpect(status().isUnauthorized());

            // Unauthenticated is-registered (blocked by Spring Security filter chain with 403)
            mockMvc.perform(get("/api/committee/" + testCommittee.getId() + "/is-registered/" + applicant.getId()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Rejects regular non-admin user trying to execute administrative committee operations")
        void rejectsNonAdminAdministrativeOperations() throws Exception {
            Long committeeId = testCommittee.getId();

            // Non-admin opening call
            mockMvc.perform(post("/api/committee/" + committeeId + "/open-call")
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isForbidden());

            // Non-admin closing call
            mockMvc.perform(post("/api/committee/" + committeeId + "/close-call")
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isForbidden());

            // Non-admin announcing call
            mockMvc.perform(post("/api/committee/" + committeeId + "/announce-call")
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isForbidden());

            // Non-admin viewing analysis
            mockMvc.perform(get("/api/committee/calls/1/registrations/analysis")
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("Admin Dynamic Question Management")
    class QuestionManagementTests {

        @Test
        @DisplayName("Full question management lifecycle: create -> update -> delete and non-admin guard")
        void fullQuestionManagementFlow() throws Exception {
            Long committeeId = testCommittee.getId();

            // 1. Non-admin cannot create question
            FormQuestionRequestDto newQuestion = FormQuestionRequestDto.builder()
                    .questionText("What is your GitHub profile?")
                    .questionType("TEXT")
                    .isRequired(false)
                    .options(List.of())
                    .build();

            mockMvc.perform(post("/api/committee/" + committeeId + "/questions")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newQuestion)))
                    .andExpect(status().isForbidden());

            // 2. Admin creates question
            String createResult = mockMvc.perform(post("/api/committee/" + committeeId + "/questions")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newQuestion)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.question_text").value("What is your GitHub profile?"))
                    .andExpect(jsonPath("$.is_required").value(false))
                    .andReturn().getResponse().getContentAsString();

            Long createdQuestionId = objectMapper.readTree(createResult).get("id").asLong();

            // 3. Admin updates question
            FormQuestionRequestDto updateRequest = FormQuestionRequestDto.builder()
                    .questionText("Updated: What is your GitHub URL?")
                    .questionType("TEXT")
                    .isRequired(true)
                    .options(List.of())
                    .build();

            mockMvc.perform(put("/api/committee/" + committeeId + "/questions/" + createdQuestionId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.question_text").value("Updated: What is your GitHub URL?"))
                    .andExpect(jsonPath("$.is_required").value(true));

            // 4. Admin deletes question
            mockMvc.perform(delete("/api/committee/" + committeeId + "/questions/" + createdQuestionId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());

            assertFalse(committeeFormQuestionRepository.existsById(createdQuestionId));
        }
    }
}
