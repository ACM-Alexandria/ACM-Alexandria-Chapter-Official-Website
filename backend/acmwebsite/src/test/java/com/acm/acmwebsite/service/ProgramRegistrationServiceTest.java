package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.Program;
import com.acm.acmwebsite.feature.entity.ProgramFormQuestion;
import com.acm.acmwebsite.feature.entity.ProgramRegistration;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.enums.RegistrationEntityType;
import com.acm.acmwebsite.feature.event.SheetSyncEvent;
import com.acm.acmwebsite.feature.exception.DuplicateRegistrationException;
import com.acm.acmwebsite.feature.exception.MissingRequiredAnswerException;
import com.acm.acmwebsite.feature.exception.ProfileIncompleteException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.repository.ProgramFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.ProgramRegistrationRepository;
import com.acm.acmwebsite.feature.repository.ProgramRepository;
import com.acm.acmwebsite.feature.service.ProgramRegistrationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

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
public class ProgramRegistrationServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ProgramRepository programRepository;
    @Mock
    private ProgramRegistrationRepository programRegistrationRepository;
    @Mock
    private ProgramFormQuestionRepository programFormQuestionRepository;
    @Mock
    private EmailService coreEmailService;

    @InjectMocks
    private ProgramRegistrationService registrationService;

    private User createCompleteUser(UUID userId) {
        return User.builder()
                .id(userId)
                .email("user@acm.org")
                .name("Alex Student")
                .phoneNumber("01012345678")
                .isAlexEngStudent(true)
                .build();
    }

    private Program createProgram(Long id, String name, boolean open) {
        Program program = new Program();
        program.setId(id);
        program.setName(name);
        program.setRegistrationOpen(open);
        return program;
    }

    @Nested
    @DisplayName("getQuestions Tests")
    class GetQuestionsTests {

        @Test
        @DisplayName("getQuestions should return mapped questions when program exists")
        void getQuestions_shouldReturnQuestions() {
            when(programRepository.existsById(1L)).thenReturn(true);
            ProgramFormQuestion question = ProgramFormQuestion.builder()
                    .id(10L)
                    .questionText("Your experience level?")
                    .questionType(QuestionType.TEXT)
                    .isRequired(true)
                    .options(List.of("Beginner", "Advanced"))
                    .build();
            when(programFormQuestionRepository.findByProgramId(1L)).thenReturn(List.of(question));

            List<FormQuestionResponseDto> result = registrationService.getQuestions(1L);

            assertFalse(result.isEmpty());
            assertEquals(10L, result.getFirst().getId());
            assertEquals("Your experience level?", result.getFirst().getQuestionText());
            assertEquals("TEXT", result.getFirst().getQuestionType());
            assertTrue(result.getFirst().getIsRequired());
            assertEquals(List.of("Beginner", "Advanced"), result.getFirst().getOptions());
        }

        @Test
        @DisplayName("getQuestions program not found should throw ResourceNotFoundException")
        void getQuestions_programNotFound_shouldThrow() {
            when(programRepository.existsById(1L)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.getQuestions(1L));
            assertEquals("Program not found with id 1", ex.getMessage());
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
            Program program = createProgram(1L, "Competitive Programming", true);

            RegistrationRequestDto request = new RegistrationRequestDto();
            request.setUserId(userId);
            Map<Long, String> answers = new HashMap<>();
            answers.put(100L, "Intermediate");
            request.setAnswers(answers);

            ProgramFormQuestion question = ProgramFormQuestion.builder()
                    .id(100L)
                    .questionText("Skill Level?")
                    .isRequired(true)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(programRepository.findById(1L)).thenReturn(Optional.of(program));
            when(programRegistrationRepository.existsByUserIdAndProgramId(userId, 1L)).thenReturn(false);
            when(programFormQuestionRepository.findByProgramId(1L)).thenReturn(List.of(question));

            registrationService.registerUser(userId, 1L, request);

            verify(programRegistrationRepository).save(any(ProgramRegistration.class));
            verify(coreEmailService).sendRegistrationConfirmationEmail(eq("user@acm.org"), eq("Competitive Programming"), eq("Alex Student"));
            ArgumentCaptor<SheetSyncEvent> eventCaptor = ArgumentCaptor.forClass(SheetSyncEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertEquals(RegistrationEntityType.PROGRAM, eventCaptor.getValue().getEntityType());
            assertEquals(1L, eventCaptor.getValue().getEntityId());
        }

        @Test
        @DisplayName("registerUser email failure does not block registration")
        void registerUser_emailFailure_doesNotThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            Program program = createProgram(1L, "Competitive Programming", true);

            RegistrationRequestDto request = new RegistrationRequestDto();
            request.setUserId(userId);
            request.setAnswers(Map.of());

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(programRepository.findById(1L)).thenReturn(Optional.of(program));
            when(programRegistrationRepository.existsByUserIdAndProgramId(userId, 1L)).thenReturn(false);
            when(programFormQuestionRepository.findByProgramId(1L)).thenReturn(List.of());
            doThrow(new RuntimeException("Mail server down")).when(coreEmailService)
                    .sendRegistrationConfirmationEmail(any(), any(), any());

            assertDoesNotThrow(() -> registrationService.registerUser(userId, 1L, request));
            verify(programRegistrationRepository).save(any(ProgramRegistration.class));
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
        @DisplayName("registerUser program not found should throw ResourceNotFoundException")
        void registerUser_programNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(programRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.registerUser(userId, 1L, new RegistrationRequestDto()));
            assertEquals("Program not found", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser when registration closed should throw IllegalStateException")
        void registerUser_registrationClosed_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            Program program = createProgram(1L, "Competitive Programming", false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(programRepository.findById(1L)).thenReturn(Optional.of(program));

            RegistrationRequestDto request = new RegistrationRequestDto();
            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> registrationService.registerUser(userId, 1L, request));
            assertEquals("Registration for this program is currently closed.", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser incomplete profile should throw ProfileIncompleteException")
        void registerUser_incompleteProfile_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User incompleteUser = User.builder().id(userId).name("").build();
            Program program = createProgram(1L, "Competitive Programming", true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(incompleteUser));
            when(programRepository.findById(1L)).thenReturn(Optional.of(program));

            RegistrationRequestDto request = new RegistrationRequestDto();
            assertThrows(ProfileIncompleteException.class, () -> registrationService.registerUser(userId, 1L, request));
        }

        @Test
        @DisplayName("registerUser duplicate registration should throw DuplicateRegistrationException")
        void registerUser_duplicateRegistration_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            Program program = createProgram(1L, "Competitive Programming", true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(programRepository.findById(1L)).thenReturn(Optional.of(program));
            when(programRegistrationRepository.existsByUserIdAndProgramId(userId, 1L)).thenReturn(true);

            RegistrationRequestDto request = new RegistrationRequestDto();
            DuplicateRegistrationException ex = assertThrows(DuplicateRegistrationException.class,
                    () -> registrationService.registerUser(userId, 1L, request));
            assertEquals("You are already registered for this program.", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser missing required answer should throw MissingRequiredAnswerException")
        void registerUser_missingRequiredAnswer_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            Program program = createProgram(1L, "Competitive Programming", true);

            ProgramFormQuestion question = ProgramFormQuestion.builder()
                    .id(200L)
                    .questionText("GitHub Profile URL")
                    .isRequired(true)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(programRepository.findById(1L)).thenReturn(Optional.of(program));
            when(programRegistrationRepository.existsByUserIdAndProgramId(userId, 1L)).thenReturn(false);
            when(programFormQuestionRepository.findByProgramId(1L)).thenReturn(List.of(question));

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
            when(programRegistrationRepository.existsByUserIdAndProgramId(userId, 1L)).thenReturn(true);

            assertTrue(registrationService.isUserRegistered(userId, 1L));
        }

        @Test
        @DisplayName("isUserRegistered should return false when not registered")
        void isUserRegistered_false() {
            UUID userId = UUID.randomUUID();
            when(programRegistrationRepository.existsByUserIdAndProgramId(userId, 1L)).thenReturn(false);

            assertFalse(registrationService.isUserRegistered(userId, 1L));
        }
    }

    @Nested
    @DisplayName("cancelRegistration Tests")
    class CancelRegistrationTests {

        @Test
        @DisplayName("cancelRegistration program not found should throw ResourceNotFoundException")
        void cancelRegistration_programNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            when(programRepository.existsById(1L)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.cancelRegistration(userId, 1L));
            assertEquals("Program not found", ex.getMessage());
            verify(programRegistrationRepository, never()).deleteByUserIdAndProgramId(any(), any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("cancelRegistration registration not found should throw ResourceNotFoundException")
        void cancelRegistration_registrationNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            when(programRepository.existsById(1L)).thenReturn(true);
            when(programRegistrationRepository.deleteByUserIdAndProgramId(userId, 1L)).thenReturn(0L);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.cancelRegistration(userId, 1L));
            assertEquals("Program registration not found", ex.getMessage());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("cancelRegistration success should delete and publish SheetSyncEvent")
        void cancelRegistration_success() {
            UUID userId = UUID.randomUUID();
            when(programRepository.existsById(1L)).thenReturn(true);
            when(programRegistrationRepository.deleteByUserIdAndProgramId(userId, 1L)).thenReturn(1L);

            registrationService.cancelRegistration(userId, 1L);

            verify(programRegistrationRepository).deleteByUserIdAndProgramId(userId, 1L);
            ArgumentCaptor<SheetSyncEvent> eventCaptor = ArgumentCaptor.forClass(SheetSyncEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertEquals(RegistrationEntityType.PROGRAM, eventCaptor.getValue().getEntityType());
            assertEquals(1L, eventCaptor.getValue().getEntityId());
        }
    }
}
