package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Department;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.ExclusiveForm;
import com.acm.acmwebsite.feature.entity.ExclusiveFormQuestion;
import com.acm.acmwebsite.feature.entity.ExclusiveRegistration;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.enums.RegistrationEntityType;
import com.acm.acmwebsite.feature.event.SheetSyncEvent;
import com.acm.acmwebsite.feature.exception.DuplicateRegistrationException;
import com.acm.acmwebsite.feature.exception.MissingRequiredAnswerException;
import com.acm.acmwebsite.feature.exception.ProfileIncompleteException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.repository.ExclusiveFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.ExclusiveFormRepository;
import com.acm.acmwebsite.feature.repository.ExclusiveRegistrationRepository;
import com.acm.acmwebsite.feature.service.ExclusiveFormRegistrationService;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ExclusiveFormRegistrationServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ExclusiveFormRepository exclusiveFormRepository;
    @Mock
    private ExclusiveFormQuestionRepository exclusiveFormQuestionRepository;
    @Mock
    private ExclusiveRegistrationRepository exclusiveRegistrationRepository;
    @Mock
    private EmailService coreEmailService;
    @Mock
    private GoogleSheetsService googleSheetsService;

    @InjectMocks
    private ExclusiveFormRegistrationService registrationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(registrationService, "exclusiveFormsFolderId", "test-exclusive-folder-id");
    }

    private User createCompleteUser(UUID userId) {
        return User.builder()
                .id(userId)
                .email("student@acm.org")
                .name("Sarah Member")
                .phoneNumber("01112223334")
                .isAlexEngStudent(true)
                .department(Department.CCE)
                .batch("2025")
                .build();
    }

    private ExclusiveForm createForm(Long id, String title, boolean active) {
        ExclusiveForm form = new ExclusiveForm();
        form.setId(id);
        form.setTitle(title);
        form.setIsActive(active);
        return form;
    }

    @Nested
    @DisplayName("getQuestions Tests")
    class GetQuestionsTests {

        @Test
        @DisplayName("getQuestions should return mapped questions when form exists")
        void getQuestions_shouldReturnQuestions() {
            when(exclusiveFormRepository.existsById(1L)).thenReturn(true);
            ExclusiveFormQuestion question = ExclusiveFormQuestion.builder()
                    .id(10L)
                    .questionText("Feedback?")
                    .questionType(QuestionType.TEXT)
                    .isRequired(true)
                    .options(List.of("Good", "Great"))
                    .build();
            when(exclusiveFormQuestionRepository.findByExclusiveFormId(1L)).thenReturn(List.of(question));

            List<FormQuestionResponseDto> result = registrationService.getQuestions(1L);

            assertFalse(result.isEmpty());
            assertEquals(10L, result.getFirst().getId());
            assertEquals("Feedback?", result.getFirst().getQuestionText());
            assertEquals("TEXT", result.getFirst().getQuestionType());
            assertTrue(result.getFirst().getIsRequired());
            assertEquals(List.of("Good", "Great"), result.getFirst().getOptions());
        }

        @Test
        @DisplayName("getQuestions form not found should throw ResourceNotFoundException")
        void getQuestions_formNotFound_shouldThrow() {
            when(exclusiveFormRepository.existsById(1L)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.getQuestions(1L));
            assertEquals("Exclusive form not found with id 1", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("registerUser Tests")
    class RegisterUserTests {

        @Test
        @DisplayName("registerUser successful registration")
        void registerUser_successful() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            ExclusiveForm form = createForm(1L, "T-Shirt Order", true);

            RegistrationRequestDto request = new RegistrationRequestDto();
            request.setUserId(userId);
            Map<Long, String> answers = new HashMap<>();
            answers.put(100L, "Large");
            request.setAnswers(answers);

            ExclusiveFormQuestion question = ExclusiveFormQuestion.builder()
                    .id(100L)
                    .questionText("Size?")
                    .isRequired(true)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.of(form));
            when(exclusiveRegistrationRepository.findByUserIdAndExclusiveFormId(userId, 1L)).thenReturn(Optional.empty());
            when(exclusiveFormQuestionRepository.findByExclusiveFormId(1L)).thenReturn(List.of(question));

            registrationService.registerUser(userId, 1L, request);

            verify(exclusiveRegistrationRepository).save(any(ExclusiveRegistration.class));
            verify(coreEmailService).sendGenericFormSubmissionConfirmationEmail(eq("student@acm.org"), eq("T-Shirt Order"), eq("Sarah Member"));
            ArgumentCaptor<SheetSyncEvent> eventCaptor = ArgumentCaptor.forClass(SheetSyncEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertEquals(RegistrationEntityType.EXCLUSIVE_FORM, eventCaptor.getValue().getEntityType());
            assertEquals(1L, eventCaptor.getValue().getEntityId());
        }

        @Test
        @DisplayName("registerUser email failure does not block registration")
        void registerUser_emailFailure_doesNotThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            ExclusiveForm form = createForm(1L, "Btngan", true);

            RegistrationRequestDto request = new RegistrationRequestDto();
            request.setUserId(userId);
            request.setAnswers(Map.of());

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.of(form));
            when(exclusiveRegistrationRepository.findByUserIdAndExclusiveFormId(userId, 1L)).thenReturn(Optional.empty());
            when(exclusiveFormQuestionRepository.findByExclusiveFormId(1L)).thenReturn(List.of());
            doThrow(new RuntimeException("Email failed")).when(coreEmailService)
                    .sendGenericFormSubmissionConfirmationEmail(any(), any(), any());

            assertDoesNotThrow(() -> registrationService.registerUser(userId, 1L, request));
            verify(exclusiveRegistrationRepository).save(any(ExclusiveRegistration.class));
            verify(eventPublisher).publishEvent(any(SheetSyncEvent.class));
        }

        @Test
        @DisplayName("registerUser user not found should throw ResourceNotFoundException")
        void registerUser_userNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            when(userRepository.findById(userId)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.registerUser(userId, 1L, new RegistrationRequestDto()));
            assertEquals("User not found", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser form not found should throw ResourceNotFoundException")
        void registerUser_formNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.registerUser(userId, 1L, new RegistrationRequestDto()));
            assertEquals("Exclusive form not found", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser inactive form should throw IllegalStateException")
        void registerUser_formInactive_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            ExclusiveForm form = createForm(1L, "Inactive Form", false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.of(form));

            RegistrationRequestDto request = new RegistrationRequestDto();
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> registrationService.registerUser(userId, 1L, request));
            assertEquals("This form is not currently active.", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser incomplete profile should throw ProfileIncompleteException")
        void registerUser_incompleteProfile_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = User.builder().id(userId).name("").build();
            ExclusiveForm form = createForm(1L, "T-Shirt Order", true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.of(form));

            RegistrationRequestDto request = new RegistrationRequestDto();
            assertThrows(ProfileIncompleteException.class, () -> registrationService.registerUser(userId, 1L, request));
        }

        @Test
        @DisplayName("registerUser duplicate submission should throw DuplicateRegistrationException")
        void registerUser_duplicateRegistration_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            ExclusiveForm form = createForm(1L, "T-Shirt Order", true);
            ExclusiveRegistration existing = ExclusiveRegistration.builder().id(5L).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.of(form));
            when(exclusiveRegistrationRepository.findByUserIdAndExclusiveFormId(userId, 1L)).thenReturn(Optional.of(existing));

            RegistrationRequestDto request = new RegistrationRequestDto();
            DuplicateRegistrationException ex = assertThrows(DuplicateRegistrationException.class,
                    () -> registrationService.registerUser(userId, 1L, request));
            assertEquals("You have already submitted this form.", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser missing required answer should throw MissingRequiredAnswerException")
        void registerUser_missingRequiredAnswer_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            ExclusiveForm form = createForm(1L, "T-Shirt Order", true);

            ExclusiveFormQuestion question = ExclusiveFormQuestion.builder()
                    .id(200L)
                    .questionText("Color preference")
                    .isRequired(true)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.of(form));
            when(exclusiveRegistrationRepository.findByUserIdAndExclusiveFormId(userId, 1L)).thenReturn(Optional.empty());
            when(exclusiveFormQuestionRepository.findByExclusiveFormId(1L)).thenReturn(List.of(question));

            RegistrationRequestDto request = new RegistrationRequestDto();
            request.setAnswers(Map.of());

            assertThrows(MissingRequiredAnswerException.class,
                    () -> registrationService.registerUser(userId, 1L, request));
        }
    }

    @Nested
    @DisplayName("isUserRegistered Tests")
    class IsUserRegisteredTests {

        @Test
        @DisplayName("isUserRegistered should return true when registered")
        void isUserRegistered_true() {
            UUID userId = UUID.randomUUID();
            ExclusiveRegistration existing = ExclusiveRegistration.builder().id(5L).build();
            when(exclusiveRegistrationRepository.findByUserIdAndExclusiveFormId(userId, 1L)).thenReturn(Optional.of(existing));

            assertTrue(registrationService.isUserRegistered(userId, 1L));
        }

        @Test
        @DisplayName("isUserRegistered should return false when not registered")
        void isUserRegistered_false() {
            UUID userId = UUID.randomUUID();
            when(exclusiveRegistrationRepository.findByUserIdAndExclusiveFormId(userId, 1L)).thenReturn(Optional.empty());

            assertFalse(registrationService.isUserRegistered(userId, 1L));
        }
    }

    @Nested
    @DisplayName("cancelRegistration Tests")
    class CancelRegistrationTests {

        @Test
        @DisplayName("cancelRegistration form not found should throw ResourceNotFoundException")
        void cancelRegistration_formNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            when(exclusiveFormRepository.existsById(1L)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.cancelRegistration(userId, 1L));
            assertEquals("Exclusive form not found", ex.getMessage());
            verify(exclusiveRegistrationRepository, never()).deleteByUserIdAndExclusiveFormId(any(), any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("cancelRegistration registration not found should throw ResourceNotFoundException")
        void cancelRegistration_registrationNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            when(exclusiveFormRepository.existsById(1L)).thenReturn(true);
            when(exclusiveRegistrationRepository.deleteByUserIdAndExclusiveFormId(userId, 1L)).thenReturn(0L);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.cancelRegistration(userId, 1L));
            assertEquals("Form submission not found", ex.getMessage());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("cancelRegistration success should delete and publish SheetSyncEvent")
        void cancelRegistration_success() {
            UUID userId = UUID.randomUUID();
            when(exclusiveFormRepository.existsById(1L)).thenReturn(true);
            when(exclusiveRegistrationRepository.deleteByUserIdAndExclusiveFormId(userId, 1L)).thenReturn(1L);

            registrationService.cancelRegistration(userId, 1L);

            verify(exclusiveRegistrationRepository).deleteByUserIdAndExclusiveFormId(userId, 1L);
            ArgumentCaptor<SheetSyncEvent> eventCaptor = ArgumentCaptor.forClass(SheetSyncEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertEquals(RegistrationEntityType.EXCLUSIVE_FORM, eventCaptor.getValue().getEntityType());
            assertEquals(1L, eventCaptor.getValue().getEntityId());
        }
    }

    @Nested
    @DisplayName("Question Management Tests")
    class QuestionManagementTests {

        @Test
        @DisplayName("createQuestion form not found should throw ResourceNotFoundException")
        void createQuestion_formNotFound_shouldThrow() {
            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.createQuestion(1L, new FormQuestionRequestDto("Question?", "TEXT", true, List.of())));
            assertEquals("Exclusive form not found with id 1", ex.getMessage());
        }

        @Test
        @DisplayName("createQuestion success should save and return question DTO")
        void createQuestion_success() {
            ExclusiveForm form = createForm(1L, "Form 1", true);
            FormQuestionRequestDto req = new FormQuestionRequestDto("Which track?", "MULTIPLE_CHOICE", true, List.of("Web", "Mobile"));

            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.of(form));
            when(exclusiveFormQuestionRepository.save(any(ExclusiveFormQuestion.class))).thenAnswer(i -> {
                ExclusiveFormQuestion q = i.getArgument(0);
                q.setId(88L);
                return q;
            });

            FormQuestionResponseDto result = registrationService.createQuestion(1L, req);

            assertNotNull(result);
            assertEquals(88L, result.getId());
            assertEquals("Which track?", result.getQuestionText());
            assertEquals("MULTIPLE_CHOICE", result.getQuestionType());
            assertTrue(result.getIsRequired());
            assertEquals(List.of("Web", "Mobile"), result.getOptions());
        }

        @Test
        @DisplayName("updateQuestion question not found or mismatch should throw ResourceNotFoundException")
        void updateQuestion_notFoundOrMismatch_shouldThrow() {
            when(exclusiveFormQuestionRepository.findById(88L)).thenReturn(Optional.empty());

            FormQuestionRequestDto req = new FormQuestionRequestDto("Updated?", "TEXT", true, List.of());
            ResourceNotFoundException ex1 = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.updateQuestion(1L, 88L, req));
            assertEquals("Form question not found with id 88", ex1.getMessage());

            // Question has null form
            ExclusiveFormQuestion qNullForm = ExclusiveFormQuestion.builder().id(88L).exclusiveForm(null).build();
            when(exclusiveFormQuestionRepository.findById(88L)).thenReturn(Optional.of(qNullForm));
            assertThrows(ResourceNotFoundException.class, () -> registrationService.updateQuestion(1L, 88L, req));

            // Question has different form id
            ExclusiveForm otherForm = createForm(99L, "Other Form", true);
            ExclusiveFormQuestion qOther = ExclusiveFormQuestion.builder().id(88L).exclusiveForm(otherForm).build();
            when(exclusiveFormQuestionRepository.findById(88L)).thenReturn(Optional.of(qOther));
            assertThrows(ResourceNotFoundException.class, () -> registrationService.updateQuestion(1L, 88L, req));
        }

        @Test
        @DisplayName("updateQuestion success should update fields and save")
        void updateQuestion_success() {
            ExclusiveForm form = createForm(1L, "Form 1", true);
            ExclusiveFormQuestion existing = ExclusiveFormQuestion.builder()
                    .id(88L)
                    .exclusiveForm(form)
                    .questionText("Old Question")
                    .questionType(QuestionType.TEXT)
                    .isRequired(false)
                    .options(List.of())
                    .build();

            when(exclusiveFormQuestionRepository.findById(88L)).thenReturn(Optional.of(existing));
            when(exclusiveFormQuestionRepository.save(any(ExclusiveFormQuestion.class))).thenAnswer(i -> i.getArgument(0));

            FormQuestionRequestDto req = new FormQuestionRequestDto("New Question", "CHECKBOX", true, List.of("Opt A", "Opt B"));
            FormQuestionResponseDto result = registrationService.updateQuestion(1L, 88L, req);

            assertEquals("New Question", result.getQuestionText());
            assertEquals("CHECKBOX", result.getQuestionType());
            assertTrue(result.getIsRequired());
            assertEquals(List.of("Opt A", "Opt B"), result.getOptions());
        }

        @Test
        @DisplayName("deleteQuestion question not found or mismatch should throw ResourceNotFoundException")
        void deleteQuestion_notFoundOrMismatch_shouldThrow() {
            when(exclusiveFormQuestionRepository.findById(88L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.deleteQuestion(1L, 88L));
            assertEquals("Form question not found with id 88", ex.getMessage());

            // Question has different form id
            ExclusiveForm otherForm = createForm(99L, "Other Form", true);
            ExclusiveFormQuestion qOther = ExclusiveFormQuestion.builder().id(88L).exclusiveForm(otherForm).build();
            when(exclusiveFormQuestionRepository.findById(88L)).thenReturn(Optional.of(qOther));
            assertThrows(ResourceNotFoundException.class, () -> registrationService.deleteQuestion(1L, 88L));

            // Question has null form
            ExclusiveFormQuestion qNullForm = ExclusiveFormQuestion.builder().id(88L).exclusiveForm(null).build();
            when(exclusiveFormQuestionRepository.findById(88L)).thenReturn(Optional.of(qNullForm));
            assertThrows(ResourceNotFoundException.class, () -> registrationService.deleteQuestion(1L, 88L));
        }

        @Test
        @DisplayName("deleteQuestion success should delete question")
        void deleteQuestion_success() {
            ExclusiveForm form = createForm(1L, "Form 1", true);
            ExclusiveFormQuestion existing = ExclusiveFormQuestion.builder()
                    .id(88L)
                    .exclusiveForm(form)
                    .build();

            when(exclusiveFormQuestionRepository.findById(88L)).thenReturn(Optional.of(existing));

            registrationService.deleteQuestion(1L, 88L);

            verify(exclusiveFormQuestionRepository).delete(existing);
        }
    }

    @Nested
    @DisplayName("getRegistrationAnalysis Tests")
    class GetRegistrationAnalysisTests {

        @Test
        @DisplayName("getRegistrationAnalysis form not found should throw ResourceNotFoundException")
        void getRegistrationAnalysis_formNotFound_shouldThrow() {
            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.getRegistrationAnalysis(1L));
            assertEquals("Exclusive form not found with id 1", ex.getMessage());
        }

        @Test
        @DisplayName("getRegistrationAnalysis success with diverse user demographics")
        void getRegistrationAnalysis_success() {
            LocalDateTime updatedAt = LocalDateTime.now();
            ExclusiveForm form = createForm(1L, "Merch Form", true);
            form.setSheetId("sheet-xyz-123");
            form.setUpdatedAt(updatedAt);

            User userAlex = User.builder()
                    .id(UUID.randomUUID())
                    .isAlexEngStudent(true)
                    .department(Department.CCE)
                    .batch("2025")
                    .build();

            User userNonAlex = User.builder()
                    .id(UUID.randomUUID())
                    .isAlexEngStudent(false)
                    .department(null) // "N/A"
                    .batch(null) // "N/A"
                    .build();

            User userBlankBatch = User.builder()
                    .id(UUID.randomUUID())
                    .isAlexEngStudent(null) // non-Alex
                    .department(Department.CSED)
                    .batch("   ") // "N/A"
                    .build();

            ExclusiveRegistration r1 = ExclusiveRegistration.builder().id(1L).user(userAlex).build();
            ExclusiveRegistration r2 = ExclusiveRegistration.builder().id(2L).user(userNonAlex).build();
            ExclusiveRegistration r3 = ExclusiveRegistration.builder().id(3L).user(userBlankBatch).build();
            ExclusiveRegistration rNullUser = ExclusiveRegistration.builder().id(4L).user(null).build();

            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.of(form));
            when(exclusiveRegistrationRepository.findByExclusiveFormIdOrderByRegisteredAtAsc(1L))
                    .thenReturn(List.of(r1, r2, r3, rNullUser));

            RegistrationAnalysisDto analysis = registrationService.getRegistrationAnalysis(1L);

            assertEquals(4, analysis.getTotalRegistrations());
            assertEquals(1, analysis.getAlexUniStudentCount());
            assertEquals(3, analysis.getNonAlexUniStudentCount());
            assertEquals("https://docs.google.com/spreadsheets/d/sheet-xyz-123/edit", analysis.getGoogleSheetUrl());
            assertEquals(updatedAt, analysis.getSheetLastUpdatedAt());

            Map<String, Long> depts = analysis.getDepartmentCounts();
            assertEquals(1L, depts.get("CCE"));
            assertEquals(1L, depts.get("CSED"));
            assertEquals(1L, depts.get("N/A"));

            Map<String, Long> batches = analysis.getBatchCounts();
            assertEquals(1L, batches.get("2025"));
            assertEquals(2L, batches.get("N/A"));
        }

        @Test
        @DisplayName("getRegistrationAnalysis with null sheetId returns null sheet URL")
        void getRegistrationAnalysis_nullSheetId_returnsNullUrl() {
            ExclusiveForm form = createForm(1L, "Merch Form", true);
            form.setSheetId(null);

            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.of(form));
            when(exclusiveRegistrationRepository.findByExclusiveFormIdOrderByRegisteredAtAsc(1L)).thenReturn(List.of());

            RegistrationAnalysisDto analysis = registrationService.getRegistrationAnalysis(1L);

            assertNull(analysis.getGoogleSheetUrl());
        }
    }

    @Nested
    @DisplayName("syncRegistrationsSheet Tests")
    class SyncRegistrationsSheetTests {

        @Test
        @DisplayName("syncRegistrationsSheet form not found should throw ResourceNotFoundException")
        void syncRegistrationsSheet_formNotFound_shouldThrow() {
            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.syncRegistrationsSheet(1L));
            assertEquals("Exclusive form not found with id 1", ex.getMessage());
        }

        @Test
        @DisplayName("syncRegistrationsSheet success updates form sheetId when newly extracted")
        void syncRegistrationsSheet_success_updatesSheetId() {
            LocalDateTime createdAt = LocalDateTime.of(2026, 3, 1, 10, 0);
            ExclusiveForm form = createForm(1L, "T-Shirt Form", true);
            form.setSheetId("old-sheet-id");
            form.setCreatedAt(createdAt);

            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.of(form));
            when(exclusiveRegistrationRepository.findByExclusiveFormIdOrderByRegisteredAtAsc(1L)).thenReturn(List.of());
            when(exclusiveFormQuestionRepository.findByExclusiveFormId(1L)).thenReturn(List.of());
            when(googleSheetsService.syncRegistrationData(
                    contains("T-Shirt Form"),
                    eq("test-exclusive-folder-id"),
                    eq("https://docs.google.com/spreadsheets/d/old-sheet-id/edit"),
                    anyList(),
                    anyList(),
                    anyList(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
            )).thenReturn("https://docs.google.com/spreadsheets/d/new-sheet-id/edit");

            when(googleSheetsService.extractSpreadsheetId("https://docs.google.com/spreadsheets/d/new-sheet-id/edit"))
                    .thenReturn("new-sheet-id");

            RegistrationAnalysisDto result = registrationService.syncRegistrationsSheet(1L);

            assertEquals("new-sheet-id", form.getSheetId());
            verify(exclusiveFormRepository).save(form);
            assertNotNull(result);
        }

        @Test
        @DisplayName("syncRegistrationsSheet handles null createdAt and does not re-save if sheetId unchanged")
        void syncRegistrationsSheet_nullCreatedAt_andUnchangedSheetId() {
            ExclusiveForm form = createForm(1L, "Form No Date", true);
            form.setSheetId("same-sheet-id");
            form.setCreatedAt(null);

            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.of(form));
            when(exclusiveRegistrationRepository.findByExclusiveFormIdOrderByRegisteredAtAsc(1L)).thenReturn(List.of());
            when(exclusiveFormQuestionRepository.findByExclusiveFormId(1L)).thenReturn(List.of());
            when(googleSheetsService.syncRegistrationData(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn("https://docs.google.com/spreadsheets/d/same-sheet-id/edit");

            when(googleSheetsService.extractSpreadsheetId("https://docs.google.com/spreadsheets/d/same-sheet-id/edit"))
                    .thenReturn("same-sheet-id");

            RegistrationAnalysisDto result = registrationService.syncRegistrationsSheet(1L);

            verify(exclusiveFormRepository, never()).save(any());
            assertNotNull(result);
        }

        @Test
        @DisplayName("syncRegistrationsSheet handles null extracted sheetId without saving")
        void syncRegistrationsSheet_nullExtractedSheetId() {
            ExclusiveForm form = createForm(1L, "Form", true);
            form.setSheetId(null);

            when(exclusiveFormRepository.findById(1L)).thenReturn(Optional.of(form));
            when(exclusiveRegistrationRepository.findByExclusiveFormIdOrderByRegisteredAtAsc(1L)).thenReturn(List.of());
            when(exclusiveFormQuestionRepository.findByExclusiveFormId(1L)).thenReturn(List.of());
            when(googleSheetsService.syncRegistrationData(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn("invalid-url");
            when(googleSheetsService.extractSpreadsheetId("invalid-url")).thenReturn(null);

            RegistrationAnalysisDto result = registrationService.syncRegistrationsSheet(1L);

            verify(exclusiveFormRepository, never()).save(any());
            assertNotNull(result);
        }
    }
}
