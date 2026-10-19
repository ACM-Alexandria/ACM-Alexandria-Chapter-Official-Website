package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Department;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.feature.dto.CommitteeCallResponseDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.CommitteeCall;
import com.acm.acmwebsite.feature.entity.CommitteeFormQuestion;
import com.acm.acmwebsite.feature.entity.CommitteeRegistration;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.enums.RegistrationEntityType;
import com.acm.acmwebsite.feature.event.SheetSyncEvent;
import com.acm.acmwebsite.feature.exception.DuplicateRegistrationException;
import com.acm.acmwebsite.feature.exception.MissingRequiredAnswerException;
import com.acm.acmwebsite.feature.exception.ProfileIncompleteException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.repository.CommitteeCallRepository;
import com.acm.acmwebsite.feature.repository.CommitteeFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.CommitteeRegistrationRepository;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.service.CommitteeRegistrationService;
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
public class CommitteeRegistrationServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CommitteeRepository committeeRepository;
    @Mock
    private CommitteeCallRepository committeeCallRepository;
    @Mock
    private CommitteeRegistrationRepository committeeRegistrationRepository;
    @Mock
    private CommitteeFormQuestionRepository committeeFormQuestionRepository;
    @Mock
    private EmailService coreEmailService;
    @Mock
    private GoogleSheetsService googleSheetsService;

    @InjectMocks
    private CommitteeRegistrationService registrationService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(registrationService, "committeesRegistrationsFolderId", "test-folder-id");
    }

    private User createCompleteUser(UUID userId) {
        return User.builder()
                .id(userId)
                .email("member@acm.org")
                .name("Jane Member")
                .phoneNumber("0123456789")
                .isAlexEngStudent(true)
                .department(Department.CSED)
                .batch("2026")
                .build();
    }

    private Committee createCommittee(Long id, String name, boolean open) {
        Committee c = new Committee();
        c.setId(id);
        c.setName(name);
        c.setOpen(open);
        return c;
    }

    @Nested
    @DisplayName("getQuestions Tests")
    class GetQuestionsTests {

        @Test
        @DisplayName("getQuestions should return mapped questions when committee exists")
        void getQuestions_shouldReturnQuestions() {
            when(committeeRepository.existsById(1L)).thenReturn(true);
            CommitteeFormQuestion question = CommitteeFormQuestion.builder()
                    .id(10L)
                    .questionText("Stack?")
                    .questionType(QuestionType.TEXT)
                    .isRequired(true)
                    .options(List.of("Option A", "Option B"))
                    .build();
            when(committeeFormQuestionRepository.findByCommitteeId(1L)).thenReturn(List.of(question));

            List<FormQuestionResponseDto> result = registrationService.getQuestions(1L);

            assertFalse(result.isEmpty());
            assertEquals(10L, result.getFirst().getId());
            assertEquals("Stack?", result.getFirst().getQuestionText());
            assertEquals("TEXT", result.getFirst().getQuestionType());
            assertTrue(result.getFirst().getIsRequired());
            assertEquals(List.of("Option A", "Option B"), result.getFirst().getOptions());
        }

        @Test
        @DisplayName("getQuestions committee not found should throw ResourceNotFoundException")
        void getQuestions_committeeNotFound_shouldThrow() {
            when(committeeRepository.existsById(1L)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.getQuestions(1L));
            assertEquals("Committee not found with id 1", ex.getMessage());
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
            Committee committee = createCommittee(1L, "Tech", true);
            CommitteeCall activeCall = CommitteeCall.builder().id(10L).committee(committee).build();

            RegistrationRequestDto request = new RegistrationRequestDto();
            request.setUserId(userId);
            Map<Long, String> answers = new HashMap<>();
            answers.put(100L, "Java");
            request.setAnswers(answers);

            CommitteeFormQuestion question = CommitteeFormQuestion.builder()
                    .id(100L)
                    .questionText("Lang?")
                    .isRequired(true)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));
            when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(activeCall));
            when(committeeRegistrationRepository.existsByUserIdAndCommitteeCallId(userId, 10L)).thenReturn(false);
            when(committeeFormQuestionRepository.findByCommitteeId(1L)).thenReturn(List.of(question));

            registrationService.registerUser(userId, 1L, request);

            verify(committeeRegistrationRepository).save(any(CommitteeRegistration.class));
            verify(coreEmailService).sendCommitteeRegistrationConfirmationEmail(eq("member@acm.org"), eq("Tech"), eq("Jane Member"));
            ArgumentCaptor<SheetSyncEvent> eventCaptor = ArgumentCaptor.forClass(SheetSyncEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertEquals(RegistrationEntityType.COMMITTEE_CALL, eventCaptor.getValue().getEntityType());
            assertEquals(10L, eventCaptor.getValue().getEntityId());
        }

        @Test
        @DisplayName("registerUser email failure does not block registration")
        void registerUser_emailFailure_doesNotThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            Committee committee = createCommittee(1L, "Tech", true);
            CommitteeCall activeCall = CommitteeCall.builder().id(10L).committee(committee).build();

            RegistrationRequestDto request = new RegistrationRequestDto();
            request.setUserId(userId);
            request.setAnswers(Map.of());

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));
            when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(activeCall));
            when(committeeRegistrationRepository.existsByUserIdAndCommitteeCallId(userId, 10L)).thenReturn(false);
            when(committeeFormQuestionRepository.findByCommitteeId(1L)).thenReturn(List.of());
            doThrow(new RuntimeException("Mail down")).when(coreEmailService)
                    .sendCommitteeRegistrationConfirmationEmail(any(), any(), any());

            assertDoesNotThrow(() -> registrationService.registerUser(userId, 1L, request));
            verify(committeeRegistrationRepository).save(any(CommitteeRegistration.class));
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
        @DisplayName("registerUser committee not found should throw ResourceNotFoundException")
        void registerUser_committeeNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(committeeRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.registerUser(userId, 1L, new RegistrationRequestDto()));
            assertEquals("Committee not found", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser committee closed should throw IllegalStateException")
        void registerUser_committeeClosed_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            Committee committee = createCommittee(1L, "Tech", false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));

            RegistrationRequestDto request = new RegistrationRequestDto();
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> registrationService.registerUser(userId, 1L, request));
            assertEquals("The call for this committee is currently closed.", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser active call missing should throw ResourceNotFoundException")
        void registerUser_activeCallNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            Committee committee = createCommittee(1L, "Tech", true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));
            when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.empty());

            RegistrationRequestDto request = new RegistrationRequestDto();
            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.registerUser(userId, 1L, request));
            assertEquals("No active call found for this committee.", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser incomplete profile should throw ProfileIncompleteException")
        void registerUser_incompleteProfile_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = User.builder().id(userId).name("").build();
            Committee committee = createCommittee(1L, "Tech", true);
            CommitteeCall activeCall = CommitteeCall.builder().id(10L).committee(committee).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));
            when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(activeCall));

            RegistrationRequestDto request = new RegistrationRequestDto();
            assertThrows(ProfileIncompleteException.class, () -> registrationService.registerUser(userId, 1L, request));
        }

        @Test
        @DisplayName("registerUser duplicate registration should throw DuplicateRegistrationException")
        void registerUser_duplicateRegistration_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            Committee committee = createCommittee(1L, "Tech", true);
            CommitteeCall activeCall = CommitteeCall.builder().id(10L).committee(committee).build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));
            when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(activeCall));
            when(committeeRegistrationRepository.existsByUserIdAndCommitteeCallId(userId, 10L)).thenReturn(true);

            RegistrationRequestDto request = new RegistrationRequestDto();
            DuplicateRegistrationException ex = assertThrows(DuplicateRegistrationException.class,
                    () -> registrationService.registerUser(userId, 1L, request));
            assertEquals("You have already applied for this committee call.", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser missing required answer should throw MissingRequiredAnswerException")
        void registerUser_missingRequiredAnswer_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            Committee committee = createCommittee(1L, "Tech", true);
            CommitteeCall activeCall = CommitteeCall.builder().id(10L).committee(committee).build();

            CommitteeFormQuestion question = CommitteeFormQuestion.builder()
                    .id(200L)
                    .questionText("Required Answer")
                    .isRequired(true)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));
            when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(activeCall));
            when(committeeRegistrationRepository.existsByUserIdAndCommitteeCallId(userId, 10L)).thenReturn(false);
            when(committeeFormQuestionRepository.findByCommitteeId(1L)).thenReturn(List.of(question));

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
        @DisplayName("isUserRegistered when active call is empty should return false")
        void isUserRegistered_noActiveCall_returnsFalse() {
            UUID userId = UUID.randomUUID();
            when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.empty());

            assertFalse(registrationService.isUserRegistered(userId, 1L));
        }

        @Test
        @DisplayName("isUserRegistered when registered should return true")
        void isUserRegistered_registered_returnsTrue() {
            UUID userId = UUID.randomUUID();
            CommitteeCall activeCall = CommitteeCall.builder().id(10L).build();

            when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(activeCall));
            when(committeeRegistrationRepository.existsByUserIdAndCommitteeCallId(userId, 10L)).thenReturn(true);

            assertTrue(registrationService.isUserRegistered(userId, 1L));
        }

        @Test
        @DisplayName("isUserRegistered when not registered should return false")
        void isUserRegistered_notRegistered_returnsFalse() {
            UUID userId = UUID.randomUUID();
            CommitteeCall activeCall = CommitteeCall.builder().id(10L).build();

            when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(activeCall));
            when(committeeRegistrationRepository.existsByUserIdAndCommitteeCallId(userId, 10L)).thenReturn(false);

            assertFalse(registrationService.isUserRegistered(userId, 1L));
        }
    }

    @Nested
    @DisplayName("cancelRegistration Tests")
    class CancelRegistrationTests {

        @Test
        @DisplayName("cancelRegistration active call not found should throw ResourceNotFoundException")
        void cancelRegistration_noActiveCall_shouldThrow() {
            UUID userId = UUID.randomUUID();
            when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.cancelRegistration(userId, 1L));
            assertEquals("No active call found for this committee.", ex.getMessage());
            verify(committeeRegistrationRepository, never()).deleteByUserIdAndCommitteeCallId(any(), any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("cancelRegistration registration not found should throw ResourceNotFoundException")
        void cancelRegistration_registrationNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            CommitteeCall activeCall = CommitteeCall.builder().id(10L).build();

            when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(activeCall));
            when(committeeRegistrationRepository.deleteByUserIdAndCommitteeCallId(userId, 10L)).thenReturn(0L);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.cancelRegistration(userId, 1L));
            assertEquals("Committee application not found", ex.getMessage());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("cancelRegistration success should delete and publish SheetSyncEvent")
        void cancelRegistration_success() {
            UUID userId = UUID.randomUUID();
            CommitteeCall activeCall = CommitteeCall.builder().id(10L).build();

            when(committeeCallRepository.findActiveCallByCommitteeId(1L)).thenReturn(Optional.of(activeCall));
            when(committeeRegistrationRepository.deleteByUserIdAndCommitteeCallId(userId, 10L)).thenReturn(1L);

            registrationService.cancelRegistration(userId, 1L);

            verify(committeeRegistrationRepository).deleteByUserIdAndCommitteeCallId(userId, 10L);
            ArgumentCaptor<SheetSyncEvent> eventCaptor = ArgumentCaptor.forClass(SheetSyncEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertEquals(RegistrationEntityType.COMMITTEE_CALL, eventCaptor.getValue().getEntityType());
            assertEquals(10L, eventCaptor.getValue().getEntityId());
        }
    }

    @Nested
    @DisplayName("Question Management Tests")
    class QuestionManagementTests {

        @Test
        @DisplayName("createQuestion committee not found should throw ResourceNotFoundException")
        void createQuestion_committeeNotFound_shouldThrow() {
            when(committeeRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.createQuestion(1L, new FormQuestionRequestDto("Text", "TEXT", true, List.of())));
            assertEquals("Committee not found with id 1", ex.getMessage());
        }

        @Test
        @DisplayName("createQuestion success should save and return question DTO")
        void createQuestion_success() {
            Committee committee = createCommittee(1L, "Tech", true);
            FormQuestionRequestDto req = new FormQuestionRequestDto("Favorite framework?", "MULTIPLE_CHOICE", true, List.of("Spring", "Quarkus"));

            when(committeeRepository.findById(1L)).thenReturn(Optional.of(committee));
            when(committeeFormQuestionRepository.save(any(CommitteeFormQuestion.class))).thenAnswer(i -> {
                CommitteeFormQuestion q = i.getArgument(0);
                q.setId(55L);
                return q;
            });

            FormQuestionResponseDto result = registrationService.createQuestion(1L, req);

            assertNotNull(result);
            assertEquals(55L, result.getId());
            assertEquals("Favorite framework?", result.getQuestionText());
            assertEquals("MULTIPLE_CHOICE", result.getQuestionType());
            assertTrue(result.getIsRequired());
            assertEquals(List.of("Spring", "Quarkus"), result.getOptions());
        }

        @Test
        @DisplayName("updateQuestion question not found or mismatch should throw ResourceNotFoundException")
        void updateQuestion_notFoundOrMismatch_shouldThrow() {
            when(committeeFormQuestionRepository.findById(55L)).thenReturn(Optional.empty());

            FormQuestionRequestDto req = new FormQuestionRequestDto("Updated?", "TEXT", true, List.of());
            ResourceNotFoundException ex1 = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.updateQuestion(1L, 55L, req));
            assertEquals("Committee question not found with id 55", ex1.getMessage());

            // Question has null committee
            CommitteeFormQuestion qWithNullCommittee = CommitteeFormQuestion.builder().id(55L).committee(null).build();
            when(committeeFormQuestionRepository.findById(55L)).thenReturn(Optional.of(qWithNullCommittee));
            assertThrows(ResourceNotFoundException.class, () -> registrationService.updateQuestion(1L, 55L, req));

            // Question belongs to a different committee
            Committee other = createCommittee(2L, "Different", true);
            CommitteeFormQuestion qOther = CommitteeFormQuestion.builder().id(55L).committee(other).build();
            when(committeeFormQuestionRepository.findById(55L)).thenReturn(Optional.of(qOther));
            assertThrows(ResourceNotFoundException.class, () -> registrationService.updateQuestion(1L, 55L, req));
        }

        @Test
        @DisplayName("updateQuestion success should update fields and save")
        void updateQuestion_success() {
            Committee committee = createCommittee(1L, "Tech", true);
            CommitteeFormQuestion existing = CommitteeFormQuestion.builder()
                    .id(55L)
                    .committee(committee)
                    .questionText("Old Text")
                    .questionType(QuestionType.TEXT)
                    .isRequired(false)
                    .options(List.of())
                    .build();

            when(committeeFormQuestionRepository.findById(55L)).thenReturn(Optional.of(existing));
            when(committeeFormQuestionRepository.save(any(CommitteeFormQuestion.class))).thenAnswer(i -> i.getArgument(0));

            FormQuestionRequestDto req = new FormQuestionRequestDto("New Text", "CHECKBOX", true, List.of("Opt1", "Opt2"));
            FormQuestionResponseDto result = registrationService.updateQuestion(1L, 55L, req);

            assertEquals("New Text", result.getQuestionText());
            assertEquals("CHECKBOX", result.getQuestionType());
            assertTrue(result.getIsRequired());
            assertEquals(List.of("Opt1", "Opt2"), result.getOptions());
        }

        @Test
        @DisplayName("deleteQuestion question not found or mismatch should throw ResourceNotFoundException")
        void deleteQuestion_notFoundOrMismatch_shouldThrow() {
            when(committeeFormQuestionRepository.findById(55L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.deleteQuestion(1L, 55L));
            assertEquals("Committee question not found with id 55", ex.getMessage());

            // Question committee mismatch
            Committee other = createCommittee(99L, "Other", true);
            CommitteeFormQuestion qOther = CommitteeFormQuestion.builder().id(55L).committee(other).build();
            when(committeeFormQuestionRepository.findById(55L)).thenReturn(Optional.of(qOther));
            assertThrows(ResourceNotFoundException.class, () -> registrationService.deleteQuestion(1L, 55L));

            // Question has null committee
            CommitteeFormQuestion qNullCommittee = CommitteeFormQuestion.builder().id(55L).committee(null).build();
            when(committeeFormQuestionRepository.findById(55L)).thenReturn(Optional.of(qNullCommittee));
            assertThrows(ResourceNotFoundException.class, () -> registrationService.deleteQuestion(1L, 55L));
        }

        @Test
        @DisplayName("deleteQuestion success should delete question")
        void deleteQuestion_success() {
            Committee committee = createCommittee(1L, "Tech", true);
            CommitteeFormQuestion existing = CommitteeFormQuestion.builder()
                    .id(55L)
                    .committee(committee)
                    .build();

            when(committeeFormQuestionRepository.findById(55L)).thenReturn(Optional.of(existing));

            registrationService.deleteQuestion(1L, 55L);

            verify(committeeFormQuestionRepository).delete(existing);
        }
    }

    @Nested
    @DisplayName("getCallsForCommittee Tests")
    class GetCallsForCommitteeTests {

        @Test
        @DisplayName("getCallsForCommittee committee not found should throw ResourceNotFoundException")
        void getCallsForCommittee_committeeNotFound_shouldThrow() {
            when(committeeRepository.existsById(1L)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.getCallsForCommittee(1L));
            assertEquals("Committee not found with id 1", ex.getMessage());
        }

        @Test
        @DisplayName("getCallsForCommittee success should map calls and counts")
        void getCallsForCommittee_success() {
            LocalDateTime now = LocalDateTime.now();
            CommitteeCall call = CommitteeCall.builder()
                    .id(10L)
                    .openedAt(now.minusDays(5))
                    .closedAt(now.minusDays(1))
                    .googleSheetUrl("https://docs.google.com/test")
                    .sheetLastUpdatedAt(now)
                    .build();

            when(committeeRepository.existsById(1L)).thenReturn(true);
            when(committeeCallRepository.findByCommitteeIdOrderByOpenedAtDesc(1L)).thenReturn(List.of(call));
            when(committeeRegistrationRepository.countByCommitteeCallId(10L)).thenReturn(7L);

            List<CommitteeCallResponseDto> result = registrationService.getCallsForCommittee(1L);

            assertEquals(1, result.size());
            assertEquals(10L, result.getFirst().getId());
            assertEquals("https://docs.google.com/test", result.getFirst().getGoogleSheetUrl());
            assertEquals(7L, result.getFirst().getRegistrationsCount());
            assertEquals(now, result.getFirst().getSheetLastUpdatedAt());
        }
    }

    @Nested
    @DisplayName("getRegistrationAnalysis Tests")
    class GetRegistrationAnalysisTests {

        @Test
        @DisplayName("getRegistrationAnalysis call not found should throw ResourceNotFoundException")
        void getRegistrationAnalysis_callNotFound_shouldThrow() {
            when(committeeCallRepository.findById(10L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.getRegistrationAnalysis(10L));
            assertEquals("Committee call not found with id 10", ex.getMessage());
        }

        @Test
        @DisplayName("getRegistrationAnalysis success with diverse user demographics")
        void getRegistrationAnalysis_success() {
            LocalDateTime now = LocalDateTime.now();
            CommitteeCall call = CommitteeCall.builder()
                    .id(10L)
                    .googleSheetUrl("https://docs.google.com/sheet")
                    .sheetLastUpdatedAt(now)
                    .build();

            User userAlex = User.builder()
                    .id(UUID.randomUUID())
                    .isAlexEngStudent(true)
                    .department(Department.CSED)
                    .batch("2026")
                    .build();

            User userNonAlex = User.builder()
                    .id(UUID.randomUUID())
                    .isAlexEngStudent(false)
                    .department(null) // null department fallback "N/A"
                    .batch(null) // null batch fallback "N/A"
                    .build();

            User userBlankBatch = User.builder()
                    .id(UUID.randomUUID())
                    .isAlexEngStudent(null) // null Alex status counted as non-Alex
                    .department(Department.CCE)
                    .batch("   ") // blank batch fallback "N/A"
                    .build();

            CommitteeRegistration r1 = CommitteeRegistration.builder().id(1L).user(userAlex).build();
            CommitteeRegistration r2 = CommitteeRegistration.builder().id(2L).user(userNonAlex).build();
            CommitteeRegistration r3 = CommitteeRegistration.builder().id(3L).user(userBlankBatch).build();
            CommitteeRegistration rNullUser = CommitteeRegistration.builder().id(4L).user(null).build();

            when(committeeCallRepository.findById(10L)).thenReturn(Optional.of(call));
            when(committeeRegistrationRepository.findByCommitteeCallId(10L)).thenReturn(List.of(r1, r2, r3, rNullUser));

            RegistrationAnalysisDto analysis = registrationService.getRegistrationAnalysis(10L);

            assertEquals(4, analysis.getTotalRegistrations());
            assertEquals(1, analysis.getAlexUniStudentCount());
            assertEquals(3, analysis.getNonAlexUniStudentCount());
            assertEquals("https://docs.google.com/sheet", analysis.getGoogleSheetUrl());
            assertEquals(now, analysis.getSheetLastUpdatedAt());

            Map<String, Long> depts = analysis.getDepartmentCounts();
            assertEquals(1L, depts.get("CSED"));
            assertEquals(1L, depts.get("CCE"));
            assertEquals(1L, depts.get("N/A"));

            Map<String, Long> batches = analysis.getBatchCounts();
            assertEquals(1L, batches.get("2026"));
            assertEquals(2L, batches.get("N/A"));
        }
    }

    @Nested
    @DisplayName("syncRegistrationsSheet Tests")
    class SyncRegistrationsSheetTests {

        @Test
        @DisplayName("syncRegistrationsSheet call not found should throw ResourceNotFoundException")
        void syncRegistrationsSheet_callNotFound_shouldThrow() {
            when(committeeCallRepository.findById(10L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.syncRegistrationsSheet(10L));
            assertEquals("Committee call not found with id 10", ex.getMessage());
        }

        @Test
        @DisplayName("syncRegistrationsSheet success should sync and update call sheet metadata")
        void syncRegistrationsSheet_success() {
            LocalDateTime opened = LocalDateTime.of(2026, 1, 1, 10, 0);
            LocalDateTime closed = LocalDateTime.of(2026, 1, 10, 10, 0);
            Committee committee = createCommittee(1L, "Tech", true);
            CommitteeCall call = CommitteeCall.builder()
                    .id(10L)
                    .committee(committee)
                    .openedAt(opened)
                    .closedAt(closed)
                    .googleSheetUrl("https://docs.google.com/old")
                    .build();

            when(committeeCallRepository.findById(10L)).thenReturn(Optional.of(call));
            when(committeeRegistrationRepository.findByCommitteeCallId(10L)).thenReturn(List.of());
            when(committeeFormQuestionRepository.findByCommitteeId(1L)).thenReturn(List.of());
            when(googleSheetsService.syncRegistrationData(
                    anyString(),
                    eq("test-folder-id"),
                    eq("https://docs.google.com/old"),
                    anyList(),
                    anyList(),
                    anyList(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
            )).thenReturn("https://docs.google.com/new");

            RegistrationAnalysisDto result = registrationService.syncRegistrationsSheet(10L);

            verify(googleSheetsService).syncRegistrationData(
                    contains("Tech"),
                    eq("test-folder-id"),
                    eq("https://docs.google.com/old"),
                    anyList(),
                    anyList(),
                    anyList(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any(),
                    any()
            );

            assertEquals("https://docs.google.com/new", call.getGoogleSheetUrl());
            assertNotNull(call.getSheetLastUpdatedAt());
            verify(committeeCallRepository).save(call);
            assertNotNull(result);
        }

        @Test
        @DisplayName("syncRegistrationsSheet with null closed date handles N/A fallbacks")
        void syncRegistrationsSheet_nullClosedDate_fallback() {
            LocalDateTime opened = LocalDateTime.of(2026, 2, 1, 12, 0);
            Committee committee = createCommittee(1L, "Media", true);
            CommitteeCall call = CommitteeCall.builder()
                    .id(20L)
                    .committee(committee)
                    .openedAt(opened)
                    .closedAt(null)
                    .build();

            when(committeeCallRepository.findById(20L)).thenReturn(Optional.of(call));
            when(committeeRegistrationRepository.findByCommitteeCallId(20L)).thenReturn(List.of());
            when(committeeFormQuestionRepository.findByCommitteeId(1L)).thenReturn(List.of());
            when(googleSheetsService.syncRegistrationData(any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn("https://docs.google.com/sheet-url");

            assertDoesNotThrow(() -> registrationService.syncRegistrationsSheet(20L));
            verify(committeeCallRepository).save(call);
        }
    }
}
