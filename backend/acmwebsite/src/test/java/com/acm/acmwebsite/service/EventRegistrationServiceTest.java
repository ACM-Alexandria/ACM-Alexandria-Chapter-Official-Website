package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.*;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.exception.*;
import com.acm.acmwebsite.feature.repository.*;
import com.acm.acmwebsite.feature.service.EventRegistrationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EventRegistrationServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private EventRepository eventRepository;
    @Mock
    private EventRegistrationRepository eventRegistrationRepository;
    @Mock
    private EventFormQuestionRepository eventFormQuestionRepository;
    @Mock
    private ClubRepository clubRepository;
    @Mock
    private ClubRegistrationRepository clubRegistrationRepository;
    @Mock
    private ClubFormQuestionRepository clubFormQuestionRepository;
    @Mock
    private com.acm.acmwebsite.core.service.EmailService coreEmailService;

    @InjectMocks
    private EventRegistrationService registrationService;

    @Test
    void getEventQuestions_shouldReturnQuestions() {
        when(eventRepository.existsById(1L)).thenReturn(true);
        EventFormQuestion question = EventFormQuestion.builder()
                .id(10L)
                .questionText("What's your name?")
                .questionType(QuestionType.TEXT)
                .isRequired(true)
                .build();
        when(eventFormQuestionRepository.findByEventId(1L)).thenReturn(List.of(question));

        List<FormQuestionResponseDto> result = registrationService.getQuestions(1L);

        assertFalse(result.isEmpty());
        assertEquals("What's your name?", result.get(0).getQuestionText());
        assertEquals("TEXT", result.get(0).getQuestionType());
    }

    @Test
    void getEventQuestions_eventNotFound_shouldThrow() {
        when(eventRepository.existsById(1L)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> registrationService.getQuestions(1L));
    }

    @Test
    void registerUserForEvent_successful() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("test@acm.org")
                .name("ACM Member")
                .phoneNumber("123456")
                .isAlexEngStudent(true)
                .build();
        Event event = new Event();
        event.setId(1L);
        event.setName("ACM Hackathon");

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
        verify(coreEmailService).sendRegistrationConfirmationEmail(eq("test@acm.org"), eq("ACM Hackathon"), eq("ACM Member"));
    }

    @Test
    void registerUserForEvent_incompleteProfile_shouldThrow() {
        UUID userId = UUID.randomUUID();
        User incompleteUser = User.builder()
                .id(userId)
                .name("") // empty name
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(incompleteUser));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(new Event()));

        RegistrationRequestDto request = new RegistrationRequestDto();
        assertThrows(ProfileIncompleteException.class, () -> registrationService.registerUser(userId, 1L, request));
    }

    @Test
    void registerUserForEvent_duplicateRegistration_shouldThrow() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("John")
                .phoneNumber("1111")
                .isAlexEngStudent(false)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(new Event()));
        when(eventRegistrationRepository.existsByUserIdAndEventId(userId, 1L)).thenReturn(true);

        RegistrationRequestDto request = new RegistrationRequestDto();
        assertThrows(DuplicateRegistrationException.class, () -> registrationService.registerUser(userId, 1L, request));
    }

    @Test
    void registerUserForEvent_missingRequiredAnswer_shouldThrow() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .name("John")
                .phoneNumber("1111")
                .isAlexEngStudent(false)
                .build();

        EventFormQuestion requiredQuestion = EventFormQuestion.builder()
                .id(200L)
                .questionText("Necessary answer")
                .isRequired(true)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(eventRepository.findById(1L)).thenReturn(Optional.of(new Event()));
        when(eventRegistrationRepository.existsByUserIdAndEventId(userId, 1L)).thenReturn(false);
        when(eventFormQuestionRepository.findByEventId(1L)).thenReturn(List.of(requiredQuestion));

        RegistrationRequestDto request = new RegistrationRequestDto();
        request.setAnswers(new HashMap<>()); // No answer provided for question 200

        assertThrows(MissingRequiredAnswerException.class, () -> registrationService.registerUser(userId, 1L, request));
    }
}
