package com.acm.acmwebsite.integration;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Department;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.User_Authentication.service.EmailExitanceService;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.dto.ExclusiveFormDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.ExclusiveForm;
import com.acm.acmwebsite.feature.entity.ExclusiveFormQuestion;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.repository.ExclusiveFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.ExclusiveFormRepository;
import com.acm.acmwebsite.feature.repository.ExclusiveRegistrationRepository;
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
@DisplayName("Dynamic Exclusive Forms Workflow Integration Tests")
class ExclusiveFormIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ExclusiveFormRepository exclusiveFormRepository;

    @Autowired
    private ExclusiveRegistrationRepository exclusiveRegistrationRepository;

    @Autowired
    private ExclusiveFormQuestionRepository exclusiveFormQuestionRepository;

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

    private ExclusiveForm testForm;
    private ExclusiveFormQuestion requiredQuestion;

    @BeforeEach
    void setUp() {
        when(emailExitanceService.isEmailReal(anyString())).thenReturn(true);

        // 1. Create applicant user
        applicant = User.builder()
                .email("form.applicant@example.com")
                .name("Form Submitter")
                .phoneNumber("01033332222")
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
                .email("form.admin@example.com")
                .name("Form Admin")
                .phoneNumber("01066667777")
                .isAlexEngStudent(true)
                .role(Role.SUPER_ADMIN)
                .emailConfirmed(true)
                .passwordHash("hashedPassword")
                .build();
        admin = userRepository.save(admin);
        adminToken = jwtUtil.generateToken(admin.getEmail(), admin.getRole().name());

        // 3. Create test exclusive form (initially inactive)
        testForm = ExclusiveForm.builder()
                .title("ACM Official Swag Pre-order")
                .description("Exclusive pre-order form for chapter merchandise")
                .isActive(false)
                .build();
        testForm = exclusiveFormRepository.save(testForm);

        // 4. Create required question for form
        requiredQuestion = ExclusiveFormQuestion.builder()
                .exclusiveForm(testForm)
                .questionText("T-Shirt Color and Size")
                .questionType(QuestionType.TEXT)
                .isRequired(true)
                .options(List.of())
                .build();
        requiredQuestion = exclusiveFormQuestionRepository.save(requiredQuestion);

        entityManager.flush();
    }

    @Nested
    @DisplayName("Complete Exclusive Form Lifecycle")
    class FullLifecycleTests {

        @Test
        @DisplayName("Complete flow: inactive guard -> admin activates -> retrieve questions -> submit -> analysis verify -> duplicate guard -> cancel -> analysis verify -> admin deactivates")
        void fullExclusiveFormLifecycle() throws Exception {
            Long formId = testForm.getId();
            UUID userId = applicant.getId();

            Map<Long, String> answers = new HashMap<>();
            answers.put(requiredQuestion.getId(), "Black / L");

            RegistrationRequestDto registrationDto = RegistrationRequestDto.builder()
                    .userId(userId)
                    .answers(answers)
                    .build();

            // 1. INACTIVE FORM GUARD
            assertThatThrownBy(() ->
                    mockMvc.perform(post("/api/exclusive-forms/" + formId + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto))))
                    .hasRootCauseInstanceOf(IllegalStateException.class)
                    .hasRootCauseMessage("This form is not currently active.");

            assertFalse(exclusiveRegistrationRepository.findByUserIdAndExclusiveFormId(userId, formId).isPresent());

            // 2. ADMIN ACTIVATES FORM
            ExclusiveFormDto updateDto = ExclusiveFormDto.builder()
                    .title("ACM Official Swag Pre-order")
                    .description("Exclusive pre-order form for chapter merchandise")
                    .isActive(true)
                    .build();

            mockMvc.perform(put("/api/exclusive-forms/" + formId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(true));

            entityManager.flush();

            // 3. RETRIEVE FORM QUESTIONS
            mockMvc.perform(get("/api/exclusive-forms/" + formId + "/questions"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].question_text").value("T-Shirt Color and Size"))
                    .andExpect(jsonPath("$[0].is_required").value(true));

            // 4. USER SUBMITS FORM
            mockMvc.perform(post("/api/exclusive-forms/" + formId + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isCreated());

            // Verify in Database
            assertTrue(exclusiveRegistrationRepository.findByUserIdAndExclusiveFormId(userId, formId).isPresent());

            // 5. VERIFY STATUS ENDPOINT
            mockMvc.perform(get("/api/exclusive-forms/" + formId + "/is-registered/" + userId)
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered").value(true));

            // 6. ADMIN CHECKS REGISTRATION ANALYSIS
            mockMvc.perform(get("/api/exclusive-forms/" + formId + "/registrations/analysis")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations").value(1))
                    .andExpect(jsonPath("$.alexUniStudentCount").value(1))
                    .andExpect(jsonPath("$.departmentCounts.CSED").value(1));

            // 7. DUPLICATE SUBMISSION REJECTION GUARD
            mockMvc.perform(post("/api/exclusive-forms/" + formId + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(registrationDto)))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.message").value("You have already submitted this form."));

            // 8. CANCEL SUBMISSION
            mockMvc.perform(delete("/api/exclusive-forms/" + formId + "/register/" + userId)
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isNoContent());

            // Verify Database cleanup
            assertFalse(exclusiveRegistrationRepository.findByUserIdAndExclusiveFormId(userId, formId).isPresent());

            // 9. VERIFY STATUS AFTER CANCELLATION
            mockMvc.perform(get("/api/exclusive-forms/" + formId + "/is-registered/" + userId)
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.registered").value(false));

            // 10. ADMIN ANALYSIS REFLECTS DECREMENT
            mockMvc.perform(get("/api/exclusive-forms/" + formId + "/registrations/analysis")
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalRegistrations").value(0));

            // 11. CANCELLING AGAIN REJECTS WITH 404 NOT FOUND
            mockMvc.perform(delete("/api/exclusive-forms/" + formId + "/register/" + userId)
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isNotFound());

            // 12. ADMIN DEACTIVATES FORM
            ExclusiveFormDto deactivateDto = ExclusiveFormDto.builder()
                    .title("ACM Official Swag Pre-order")
                    .description("Exclusive pre-order form for chapter merchandise")
                    .isActive(false)
                    .build();

            mockMvc.perform(put("/api/exclusive-forms/" + formId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(deactivateDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.isActive").value(false));

            entityManager.flush();
        }
    }

    @Nested
    @DisplayName("Validation & Integrity Guards")
    class ValidationGuardTests {

        @Test
        @DisplayName("Rejects form submission when user profile details are incomplete")
        void rejectsIncompleteUserProfile() throws Exception {
            testForm.setIsActive(true);
            exclusiveFormRepository.save(testForm);
            entityManager.flush();

            // User without phone number
            User incompleteUser = User.builder()
                    .email("incomplete.form@example.com")
                    .name("Incomplete Form User")
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
                    .answers(Map.of(requiredQuestion.getId(), "Black / M"))
                    .build();

            mockMvc.perform(post("/api/exclusive-forms/" + testForm.getId() + "/register")
                            .header("Authorization", "Bearer " + incompleteToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Please complete your profile details (Name & Phone Number & Student Status) before registering."));
        }

        @Test
        @DisplayName("Rejects form submission when required dynamic form answer is missing or blank")
        void rejectsMissingRequiredFormAnswer() throws Exception {
            testForm.setIsActive(true);
            exclusiveFormRepository.save(testForm);
            entityManager.flush();

            RegistrationRequestDto missingAnswerDto = RegistrationRequestDto.builder()
                    .userId(applicant.getId())
                    .answers(Map.of()) // Empty answers
                    .build();

            mockMvc.perform(post("/api/exclusive-forms/" + testForm.getId() + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(missingAnswerDto)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message").value("Answer is required for: T-Shirt Color and Size"));
        }

        @Test
        @DisplayName("Rejects form submission when userId is missing from request body")
        void rejectsMissingUserIdInRequestBody() throws Exception {
            testForm.setIsActive(true);
            exclusiveFormRepository.save(testForm);
            entityManager.flush();

            RegistrationRequestDto noUserDto = RegistrationRequestDto.builder()
                    .userId(null)
                    .build();

            mockMvc.perform(post("/api/exclusive-forms/" + testForm.getId() + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(noUserDto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Rejects invalid UUID format when querying submission status")
        void rejectsInvalidUuidFormat() throws Exception {
            mockMvc.perform(get("/api/exclusive-forms/" + testForm.getId() + "/is-registered/not-a-valid-uuid")
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Authorization & Impersonation Guards")
    class AuthorizationGuardTests {

        @Test
        @DisplayName("Rejects submission and cancellation when user attempts cross-user impersonation")
        void rejectsCrossUserImpersonation() throws Exception {
            testForm.setIsActive(true);
            exclusiveFormRepository.save(testForm);
            entityManager.flush();

            User otherUser = User.builder()
                    .email("other.form@example.com")
                    .name("Other Form User")
                    .phoneNumber("01077773333")
                    .isAlexEngStudent(true)
                    .role(Role.USER)
                    .emailConfirmed(true)
                    .passwordHash("hashed")
                    .build();
            otherUser = userRepository.save(otherUser);

            // Applicant attempts to submit for otherUser
            RegistrationRequestDto impersonatedDto = RegistrationRequestDto.builder()
                    .userId(otherUser.getId())
                    .answers(Map.of(requiredQuestion.getId(), "Impersonated answer"))
                    .build();

            mockMvc.perform(post("/api/exclusive-forms/" + testForm.getId() + "/register")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(impersonatedDto)))
                    .andExpect(status().isForbidden());

            // Applicant attempts to cancel otherUser's submission
            mockMvc.perform(delete("/api/exclusive-forms/" + testForm.getId() + "/register/" + otherUser.getId())
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Rejects unauthenticated access to form submission endpoints")
        void rejectsUnauthenticatedAccess() throws Exception {
            RegistrationRequestDto requestDto = RegistrationRequestDto.builder()
                    .userId(applicant.getId())
                    .build();

            // Unauthenticated register (blocked by Spring Security filter chain with 403)
            mockMvc.perform(post("/api/exclusive-forms/" + testForm.getId() + "/register")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isForbidden());

            // Unauthenticated cancel (handled by controller authorization check with 401)
            mockMvc.perform(delete("/api/exclusive-forms/" + testForm.getId() + "/register/" + applicant.getId()))
                    .andExpect(status().isUnauthorized());

            // Unauthenticated is-registered (blocked by Spring Security filter chain with 403)
            mockMvc.perform(get("/api/exclusive-forms/" + testForm.getId() + "/is-registered/" + applicant.getId()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Rejects regular non-admin user trying to execute administrative form operations")
        void rejectsNonAdminAdministrativeOperations() throws Exception {
            Long formId = testForm.getId();

            ExclusiveFormDto newForm = ExclusiveFormDto.builder()
                    .title("Unauthorized Form")
                    .description("Should not be created")
                    .build();

            // Non-admin creating form
            mockMvc.perform(post("/api/exclusive-forms")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newForm)))
                    .andExpect(status().isForbidden());

            // Non-admin deleting form
            mockMvc.perform(delete("/api/exclusive-forms/" + formId)
                            .header("Authorization", "Bearer " + applicantToken))
                    .andExpect(status().isForbidden());

            // Non-admin viewing analysis
            mockMvc.perform(get("/api/exclusive-forms/" + formId + "/registrations/analysis")
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
            Long formId = testForm.getId();

            // 1. Non-admin cannot create question
            FormQuestionRequestDto newQuestion = FormQuestionRequestDto.builder()
                    .questionText("Dietary Restrictions")
                    .questionType("TEXT")
                    .isRequired(false)
                    .options(List.of())
                    .build();

            mockMvc.perform(post("/api/exclusive-forms/" + formId + "/questions")
                            .header("Authorization", "Bearer " + applicantToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newQuestion)))
                    .andExpect(status().isForbidden());

            // 2. Admin creates question
            String createResult = mockMvc.perform(post("/api/exclusive-forms/" + formId + "/questions")
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(newQuestion)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.question_text").value("Dietary Restrictions"))
                    .andExpect(jsonPath("$.is_required").value(false))
                    .andReturn().getResponse().getContentAsString();

            Long createdQuestionId = objectMapper.readTree(createResult).get("id").asLong();

            // 3. Admin updates question
            FormQuestionRequestDto updateRequest = FormQuestionRequestDto.builder()
                    .questionText("Updated: Specific Allergies or Preferences")
                    .questionType("TEXT")
                    .isRequired(true)
                    .options(List.of())
                    .build();

            mockMvc.perform(put("/api/exclusive-forms/" + formId + "/questions/" + createdQuestionId)
                            .header("Authorization", "Bearer " + adminToken)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.question_text").value("Updated: Specific Allergies or Preferences"))
                    .andExpect(jsonPath("$.is_required").value(true));

            // 4. Admin deletes question
            mockMvc.perform(delete("/api/exclusive-forms/" + formId + "/questions/" + createdQuestionId)
                            .header("Authorization", "Bearer " + adminToken))
                    .andExpect(status().isNoContent());

            assertFalse(exclusiveFormQuestionRepository.existsById(createdQuestionId));
        }
    }
}
