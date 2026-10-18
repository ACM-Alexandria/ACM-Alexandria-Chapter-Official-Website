package com.acm.acmwebsite.integration;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Department;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.Program;
import com.acm.acmwebsite.feature.entity.ProgramFormQuestion;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.repository.ProgramFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.ProgramRegistrationRepository;
import com.acm.acmwebsite.feature.repository.ProgramRepository;
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
@DisplayName("Program Registration Lifecycle Integration Tests")
class ProgramRegistrationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private ProgramRegistrationRepository programRegistrationRepository;

    @Autowired
    private ProgramFormQuestionRepository programFormQuestionRepository;

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

    private Program testProgram;
    private ProgramFormQuestion requiredQuestion;

    @BeforeEach
    void setUp() {
        when(emailExitanceService.isEmailReal(anyString())).thenReturn(true);

        // 1. Create applicant user
        applicant = User.builder()
                .email("program.applicant@example.com")
                .name("Program Applicant")
                .phoneNumber("01022221111")
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
                .email("program.admin@example.com")
                .name("Program Admin")
                .phoneNumber("01088889999")
                .isAlexEngStudent(true)
                .role(Role.SUPER_ADMIN)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build();
        admin = userRepository.save(admin);
        adminToken = jwtUtil.generateToken(admin.getEmail(), admin.getRole().name());

        // 3. Create test program (registration initially closed)
        testProgram = new Program();
        testProgram.setName("Competitive Programming Camp 2026");
        testProgram.setDescription("Training program for national problem solving contests");
        testProgram.setRegistrationOpen(false);
        testProgram = programRepository.save(testProgram);

        // 4. Create required question for program
        requiredQuestion = ProgramFormQuestion.builder()
                .program(testProgram)
                .questionText("Codeforces Handle & Rating")
                .questionType(QuestionType.TEXT)
                .isRequired(true)
                .options(List.of())
                .build();
        requiredQuestion = programFormQuestionRepository.save(requiredQuestion);

        entityManager.flush();
    }

    @Nested
    @DisplayName("Complete Program Registration & Cancellation Lifecycle")
    class FullLifecycleTests {

        @Test
        @DisplayName("Complete flow: closed guard -> toggle open -> retrieve questions -> register -> analysis verify -> duplicate guard -> cancel -> analysis verify -> toggle close")
        void fullProgramRegistrationLifecycle() throws Exception {
            Long programId = testProgram.getId();
            UUID userId = applicant.getId();

            Map<Long, String> answers = new HashMap<>();
            answers.put(requiredQuestion.getId(), "tourist / 3000");

            RegistrationRequestDto registrationDto = RegistrationRequestDto.builder()
                    .userId(userId)
                    .answers(answers)
                    .build();

            // 1. REGISTRATION CLOSED GUARD
            assertThatThrownBy(() ->
                    mockMvc.perform(post("/api/program/" + programId + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto))))
                    .hasRootCauseInstanceOf(IllegalStateException.class)
                    .hasRootCauseMessage("Registration for this program is currently closed.");

            assertFalse(programRegistrationRepository.existsByUserIdAndProgramId(userId, programId));

            // 2. ADMIN OPENS PROGRAM REGISTRATION
            mockMvc.perform(post("/api/program/" + programId + "/toggle-registration?open=true")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registrationOpen").value(true));

            entityManager.flush();

            // 3. RETRIEVE PROGRAM QUESTIONS
            mockMvc.perform(get("/api/program/" + programId + "/questions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].question_text").value("Codeforces Handle & Rating"))
                    .andExpect(jsonPath("$[0].is_required").value(true));

            // 4. APPLICANT REGISTERS
            mockMvc.perform(post("/api/program/" + programId + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isCreated());

            // Verify in Database
            assertTrue(programRegistrationRepository.existsByUserIdAndProgramId(userId, programId));

            // 5. VERIFY REGISTRATION STATUS ENDPOINT
            mockMvc.perform(get("/api/program/" + programId + "/is-registered/" + userId)
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered").value(true));

            // 6. ADMIN CHECKS REGISTRATION ANALYSIS
            mockMvc.perform(get("/api/program/" + programId + "/registrations/analysis")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations").value(1))
                    .andExpect(jsonPath("$.alexUniStudentCount").value(1))
                    .andExpect(jsonPath("$.departmentCounts.CSED").value(1));

            // 7. DUPLICATE REGISTRATION REJECTION GUARD
            mockMvc.perform(post("/api/program/" + programId + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("You are already registered for this program."));

            // 8. CANCEL REGISTRATION
            mockMvc.perform(delete("/api/program/" + programId + "/register/" + userId)
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isNoContent());

            // Verify Database cleanup
            assertFalse(programRegistrationRepository.existsByUserIdAndProgramId(userId, programId));

            // 9. VERIFY STATUS AFTER CANCELLATION
            mockMvc.perform(get("/api/program/" + programId + "/is-registered/" + userId)
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered").value(false));

            // 10. ADMIN ANALYSIS REFLECTS DECREMENT
            mockMvc.perform(get("/api/program/" + programId + "/registrations/analysis")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations").value(0));

            // 11. CANCELLING AGAIN REJECTS WITH 404 NOT FOUND
            mockMvc.perform(delete("/api/program/" + programId + "/register/" + userId)
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isNotFound());

            // 12. ADMIN CLOSES PROGRAM REGISTRATION
            mockMvc.perform(post("/api/program/" + programId + "/toggle-registration?open=false")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registrationOpen").value(false));

            entityManager.flush();
        }
    }

    @Nested
    @DisplayName("Validation & Integrity Guards")
    class ValidationGuardTests {

        @Test
        @DisplayName("Rejects registration when applicant profile details are incomplete")
        void rejectsIncompleteUserProfile() throws Exception {
            testProgram.setRegistrationOpen(true);
            programRepository.save(testProgram);
            entityManager.flush();

            // User without phone number
            User incompleteUser = User.builder()
                    .email("incomplete.program@example.com")
                    .name("Incomplete Candidate")
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
                    .answers(Map.of(requiredQuestion.getId(), "Rating 1500"))
                    .build();

            mockMvc.perform(post("/api/program/" + testProgram.getId() + "/register")
                            .header("Authorization", "Bearer " + incompleteToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Please complete your profile details (Name & Phone Number & Student Status) before registering."));
        }

        @Test
        @DisplayName("Rejects registration when required dynamic form answer is missing or blank")
        void rejectsMissingRequiredFormAnswer() throws Exception {
            testProgram.setRegistrationOpen(true);
            programRepository.save(testProgram);
            entityManager.flush();

            RegistrationRequestDto missingAnswerDto = RegistrationRequestDto.builder()
                    .userId(applicant.getId())
                    .answers(Map.of()) // Empty answers
                    .build();

            mockMvc.perform(post("/api/program/" + testProgram.getId() + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(missingAnswerDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Answer is required for: Codeforces Handle & Rating"));
        }

        @Test
        @DisplayName("Rejects registration when userId is missing from request body")
        void rejectsMissingUserIdInRequestBody() throws Exception {
            testProgram.setRegistrationOpen(true);
            programRepository.save(testProgram);
            entityManager.flush();

            RegistrationRequestDto noUserDto = RegistrationRequestDto.builder()
                    .userId(null)
                    .build();

            mockMvc.perform(post("/api/program/" + testProgram.getId() + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(noUserDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Rejects invalid UUID format when querying registration status")
        void rejectsInvalidUuidFormat() throws Exception {
            mockMvc.perform(get("/api/program/" + testProgram.getId() + "/is-registered/not-a-valid-uuid")
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Authorization & Impersonation Guards")
    class AuthorizationGuardTests {

        @Test
        @DisplayName("Rejects registration and cancellation when user attempts cross-user impersonation")
        void rejectsCrossUserImpersonation() throws Exception {
            testProgram.setRegistrationOpen(true);
            programRepository.save(testProgram);
            entityManager.flush();

            User otherUser = User.builder()
                    .email("other.program@example.com")
                    .name("Other Program User")
                    .phoneNumber("01044445555")
                    .isAlexEngStudent(true)
                    .role(Role.USER)
                    .emailConfirmed(true)
                    .passwordHash("hashed")
                    .build();
            otherUser = userRepository.save(otherUser);

            // Applicant attempts to register for otherUser
            RegistrationRequestDto impersonatedDto = RegistrationRequestDto.builder()
                    .userId(otherUser.getId())
                    .answers(Map.of(requiredQuestion.getId(), "Impersonated rating"))
                    .build();

            mockMvc.perform(post("/api/program/" + testProgram.getId() + "/register")
                            .header("Authorization", "Bearer " + applicantToken) // token is for applicant, not otherUser
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(impersonatedDto)))
                    .andExpect(status().isForbidden());

            // Applicant attempts to cancel otherUser's registration
            mockMvc.perform(delete("/api/program/" + testProgram.getId() + "/register/" + otherUser.getId())
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
            mockMvc.perform(post("/api/program/" + testProgram.getId() + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isForbidden());

            // Unauthenticated cancel (handled by controller authorization check with 401)
            mockMvc.perform(delete("/api/program/" + testProgram.getId() + "/register/" + applicant.getId()))
                    .andExpect(status().isUnauthorized());

            // Unauthenticated is-registered (blocked by Spring Security filter chain with 403)
            mockMvc.perform(get("/api/program/" + testProgram.getId() + "/is-registered/" + applicant.getId()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Rejects regular non-admin user trying to execute administrative program operations")
        void rejectsNonAdminAdministrativeOperations() throws Exception {
            Long programId = testProgram.getId();

            // Non-admin toggling registration
            mockMvc.perform(post("/api/program/" + programId + "/toggle-registration?open=true")
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isForbidden());

            // Non-admin announcing program
            mockMvc.perform(post("/api/program/" + programId + "/announce")
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isForbidden());

            // Non-admin viewing analysis
            mockMvc.perform(get("/api/program/" + programId + "/registrations/analysis")
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
            Long programId = testProgram.getId();

            // 1. Non-admin cannot create question
            FormQuestionRequestDto newQuestion = FormQuestionRequestDto.builder()
                    .questionText("Preferred Training Track")
                    .questionType("TEXT")
                    .isRequired(false)
                    .options(List.of())
                    .build();

            mockMvc.perform(post("/api/program/" + programId + "/questions")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newQuestion)))
                    .andExpect(status().isForbidden());

            // 2. Admin creates question
            String createResult = mockMvc.perform(post("/api/program/" + programId + "/questions")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newQuestion)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.question_text").value("Preferred Training Track"))
                    .andExpect(jsonPath("$.is_required").value(false))
                    .andReturn().getResponse().getContentAsString();

            Long createdQuestionId = objectMapper.readTree(createResult).get("id").asLong();

            // 3. Admin updates question
            FormQuestionRequestDto updateRequest = FormQuestionRequestDto.builder()
                    .questionText("Updated: Preferred Track & Level")
                    .questionType("TEXT")
                    .isRequired(true)
                    .options(List.of())
                    .build();

            mockMvc.perform(put("/api/program/" + programId + "/questions/" + createdQuestionId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.question_text").value("Updated: Preferred Track & Level"))
                    .andExpect(jsonPath("$.is_required").value(true));

            // 4. Admin deletes question
            mockMvc.perform(delete("/api/program/" + programId + "/questions/" + createdQuestionId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());

            assertFalse(programFormQuestionRepository.existsById(createdQuestionId));
        }
    }
}
