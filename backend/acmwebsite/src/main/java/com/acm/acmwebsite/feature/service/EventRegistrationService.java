package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.entity.EventFormQuestion;
import com.acm.acmwebsite.feature.entity.EventRegistration;
import com.acm.acmwebsite.feature.entity.FormQuestion;
import com.acm.acmwebsite.feature.exception.DuplicateRegistrationException;
import com.acm.acmwebsite.feature.exception.MissingRequiredAnswerException;
import com.acm.acmwebsite.feature.exception.ProfileIncompleteException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.repository.EventFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.EventRegistrationRepository;
import com.acm.acmwebsite.feature.repository.EventRepository;
import com.acm.acmwebsite.feature.util.RegistrationValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventRegistrationService implements RegistrationService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventFormQuestionRepository eventFormQuestionRepository;
    private final com.acm.acmwebsite.core.service.EmailService coreEmailService;

    @Override
    public List<FormQuestionResponseDto> getQuestions(Long eventId) {
        if (!eventRepository.existsById(eventId)) {
            throw new ResourceNotFoundException("Event not found with id " + eventId);
        }
        
        List<EventFormQuestion> questions = eventFormQuestionRepository.findByEventId(eventId);
        return questions.stream()
                .map(q -> FormQuestionResponseDto.builder()
                        .id(q.getId())
                        .questionText(q.getQuestionText())
                        .questionType(q.getQuestionType().name())
                        .isRequired(q.getIsRequired())
                        .options(q.getOptions())
                        .build())
                .toList();
    }

    @Override
    public void registerUser(UUID userId, Long eventId, RegistrationRequestDto request) {
        // 1. Get entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // 2. Check profile integrity
        RegistrationValidationUtil.validateUserProfile(user);

        // 3. Prevent double registrations
        if (eventRegistrationRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            throw new DuplicateRegistrationException("You are already registered for this event.");
        }

        // 4. Validate answers for questions
        List<EventFormQuestion> formQuestions = eventFormQuestionRepository.findByEventId(eventId);
        RegistrationValidationUtil.validateAnswers(formQuestions, request.getAnswers());

        // 5. Store registration
        EventRegistration registration = EventRegistration.builder()
                .user(user)
                .event(event)
                .answers(request.getAnswers())
                .build();

        eventRegistrationRepository.save(registration);

        // 6. Trigger Confirmation Email
        try {
            coreEmailService.sendRegistrationConfirmationEmail(user.getEmail(), event.getName(), user.getName());
        } catch (Exception e) {
            // Non-blocking
        }
    }

    @Override
    public boolean isUserRegistered(UUID userId, Long eventId) {
        return eventRegistrationRepository.existsByUserIdAndEventId(userId, eventId);
    }
}
