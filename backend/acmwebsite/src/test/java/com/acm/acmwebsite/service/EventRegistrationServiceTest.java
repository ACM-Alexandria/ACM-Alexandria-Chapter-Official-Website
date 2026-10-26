package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.core.service.EmailService;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.entity.EventFormQuestion;
import com.acm.acmwebsite.feature.entity.EventRegistration;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.enums.RegistrationEntityType;
import com.acm.acmwebsite.feature.event.SheetSyncEvent;
import com.acm.acmwebsite.feature.exception.DuplicateRegistrationException;
import com.acm.acmwebsite.feature.exception.MissingRequiredAnswerException;
import com.acm.acmwebsite.feature.exception.ProfileIncompleteException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.repository.EventFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.EventRegistrationRepository;
import com.acm.acmwebsite.feature.repository.EventRepository;
import com.acm.acmwebsite.feature.service.EventRegistrationService;
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
public class EventRegistrationServiceTest {

    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventRegistrationRepository eventRegistrationRepository;
    @Mock
    private EventFormQuestionRepository eventFormQuestionRepository;
    @Mock
    private EmailService coreEmailService;

    @InjectMocks
    private EventRegistrationService registrationService;

    @Nested
    @DisplayName("getQuestions Tests")
    class GetQuestionsTests {

        @Test
        @DisplayName("getQuestions should return mapped DTOs when event exists")
        void getQuestions_shouldReturnQuestions() {
            when(eventRepository.existsById(1L)).thenReturn(true);
            EventFormQuestion question = EventFormQuestion.builder()
                    .id(10L)
                    .questionText("What's your name?")
                    .questionType(QuestionType.TEXT)
                    .isRequired(true)
                    .options(List.of("Option1", "Option2"))
                    .build();
            when(eventFormQuestionRepository.findByEventId(1L)).thenReturn(List.of(question));

            List<FormQuestionResponseDto> result = registrationService.getQuestions(1L);

            assertFalse(result.isEmpty());
            assertEquals(10L, result.getFirst().getId());
            assertEquals("What's your name?", result.getFirst().getQuestionText());
            assertEquals("TEXT", result.getFirst().getQuestionType());
            assertTrue(result.getFirst().getIsRequired());
            assertEquals(List.of("Option1", "Option2"), result.getFirst().getOptions());
        }

        @Test
        @DisplayName("getQuestions event not found should throw ResourceNotFoundException")
        void getQuestions_eventNotFound_shouldThrow() {
            when(eventRepository.existsById(1L)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.getQuestions(1L));
            assertEquals("Event not found with id 1", ex.getMessage());
        }
    }

    @Nested
    @DisplayName("registerUser Tests")
    class RegisterUserTests {

        private User createCompleteUser(UUID userId) {
            return User.builder()
                    .id(userId)
                    .email("test@acm.org")
                    .name("ACM Member")
                    .phoneNumber("123456")
                    .isAlexEngStudent(true)
                    .build();
        }

        private Event createEvent(Long id, boolean open) {
            Event event = new Event();
            event.setId(id);
            event.setName("ACM Hackathon");
            event.setRegistrationOpen(open);
            return event;
        }

        @Test
        @DisplayName("registerUser successful registration")
        void registerUserForEvent_successful() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            Event event = createEvent(1L, true);

            RegistrationRequestDto request = new RegistrationRequestDto();
            request.setUserId(userId);
            Map<Long, String> answers = new HashMap<>();
            answers.put(100L, "Java");
            request.setAnswers(answers);

            EventFormQuestion question = EventFormQuestion.builder()
                    .id(100L)
                    .questionText("Your fav Language?")
                    .isRequired(true)
                    .build();

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRegistrationRepository.existsByUserIdAndEventId(userId, 1L)).thenReturn(false);
            when(eventFormQuestionRepository.findByEventId(1L)).thenReturn(List.of(question));

            registrationService.registerUser(userId, 1L, request);

            verify(eventRegistrationRepository).save(any(EventRegistration.class));
            verify(coreEmailService).sendEventRegistrationConfirmationEmail(eq("test@acm.org"), eq(event), eq("ACM Member"));
            ArgumentCaptor<SheetSyncEvent> eventCaptor = ArgumentCaptor.forClass(SheetSyncEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertEquals(RegistrationEntityType.EVENT, eventCaptor.getValue().getEntityType());
            assertEquals(1L, eventCaptor.getValue().getEntityId());
        }

        @Test
        @DisplayName("registerUser email failure does not block registration")
        void registerUser_emailFailure_doesNotThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            Event event = createEvent(1L, true);

            RegistrationRequestDto request = new RegistrationRequestDto();
            request.setUserId(userId);
            request.setAnswers(Map.of());

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRegistrationRepository.existsByUserIdAndEventId(userId, 1L)).thenReturn(false);
            when(eventFormQuestionRepository.findByEventId(1L)).thenReturn(List.of());
            doThrow(new RuntimeException("Mail server down")).when(coreEmailService)
                    .sendEventRegistrationConfirmationEmail(any(), any(), any());

            assertDoesNotThrow(() -> registrationService.registerUser(userId, 1L, request));
            verify(eventRegistrationRepository).save(any(EventRegistration.class));
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
        @DisplayName("registerUser event not found should throw ResourceNotFoundException")
        void registerUser_eventNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(eventRepository.findById(1L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.registerUser(userId, 1L, new RegistrationRequestDto()));
            assertEquals("Event not found", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser when registration closed should throw IllegalArgumentException")
        void registerUser_registrationClosed_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            Event event = createEvent(1L, false);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> registrationService.registerUser(userId, 1L, new RegistrationRequestDto()));
            assertEquals("Registration is closed for this event.", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser incomplete profile should throw ProfileIncompleteException")
        void registerUserForEvent_incompleteProfile_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User incompleteUser = User.builder()
                    .id(userId)
                    .name("") // empty name
                    .build();

            Event event = createEvent(1L, true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(incompleteUser));
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));

            RegistrationRequestDto request = new RegistrationRequestDto();
            assertThrows(ProfileIncompleteException.class, () -> registrationService.registerUser(userId, 1L, request));
        }

        @Test
        @DisplayName("registerUser duplicate registration should throw DuplicateRegistrationException")
        void registerUserForEvent_duplicateRegistration_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);
            Event event = createEvent(1L, true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRegistrationRepository.existsByUserIdAndEventId(userId, 1L)).thenReturn(true);

            RegistrationRequestDto request = new RegistrationRequestDto();
            DuplicateRegistrationException ex = assertThrows(DuplicateRegistrationException.class,
                    () -> registrationService.registerUser(userId, 1L, request));
            assertEquals("You are already registered for this event.", ex.getMessage());
        }

        @Test
        @DisplayName("registerUser missing required answer should throw MissingRequiredAnswerException")
        void registerUserForEvent_missingRequiredAnswer_shouldThrow() {
            UUID userId = UUID.randomUUID();
            User user = createCompleteUser(userId);

            EventFormQuestion requiredQuestion = EventFormQuestion.builder()
                    .id(200L)
                    .questionText("Necessary answer")
                    .isRequired(true)
                    .build();

            Event event = createEvent(1L, true);

            when(userRepository.findById(userId)).thenReturn(Optional.of(user));
            when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
            when(eventRegistrationRepository.existsByUserIdAndEventId(userId, 1L)).thenReturn(false);
            when(eventFormQuestionRepository.findByEventId(1L)).thenReturn(List.of(requiredQuestion));

            RegistrationRequestDto request = new RegistrationRequestDto();
            request.setAnswers(new HashMap<>());

            assertThrows(MissingRequiredAnswerException.class, () -> registrationService.registerUser(userId, 1L, request));
        }
    }

    @Nested
    @DisplayName("isUserRegistered Tests")
    class IsUserRegisteredTests {

        @Test
        @DisplayName("isUserRegistered should return true when registered")
        void isUserRegistered_true() {
            UUID userId = UUID.randomUUID();
            when(eventRegistrationRepository.existsByUserIdAndEventId(userId, 1L)).thenReturn(true);

            assertTrue(registrationService.isUserRegistered(userId, 1L));
        }

        @Test
        @DisplayName("isUserRegistered should return false when not registered")
        void isUserRegistered_false() {
            UUID userId = UUID.randomUUID();
            when(eventRegistrationRepository.existsByUserIdAndEventId(userId, 1L)).thenReturn(false);

            assertFalse(registrationService.isUserRegistered(userId, 1L));
        }
    }

    @Nested
    @DisplayName("cancelRegistration Tests")
    class CancelRegistrationTests {

        @Test
        @DisplayName("cancelRegistration event not found should throw ResourceNotFoundException")
        void cancelRegistration_eventNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            when(eventRepository.existsById(1L)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.cancelRegistration(userId, 1L));
            assertEquals("Event not found", ex.getMessage());
            verify(eventRegistrationRepository, never()).deleteByUserIdAndEventId(any(), any());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("cancelRegistration registration not found should throw ResourceNotFoundException")
        void cancelRegistration_registrationNotFound_shouldThrow() {
            UUID userId = UUID.randomUUID();
            when(eventRepository.existsById(1L)).thenReturn(true);
            when(eventRegistrationRepository.deleteByUserIdAndEventId(userId, 1L)).thenReturn(0L);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> registrationService.cancelRegistration(userId, 1L));
            assertEquals("Event registration not found", ex.getMessage());
            verify(eventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("cancelRegistration success should delete and publish SheetSyncEvent")
        void cancelRegistration_success() {
            UUID userId = UUID.randomUUID();
            when(eventRepository.existsById(1L)).thenReturn(true);
            when(eventRegistrationRepository.deleteByUserIdAndEventId(userId, 1L)).thenReturn(1L);

            registrationService.cancelRegistration(userId, 1L);

            verify(eventRegistrationRepository).deleteByUserIdAndEventId(userId, 1L);
            ArgumentCaptor<SheetSyncEvent> eventCaptor = ArgumentCaptor.forClass(SheetSyncEvent.class);
            verify(eventPublisher).publishEvent(eventCaptor.capture());
            assertEquals(RegistrationEntityType.EVENT, eventCaptor.getValue().getEntityType());
            assertEquals(1L, eventCaptor.getValue().getEntityId());
        }
    }
}
