package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.*;
import com.acm.acmwebsite.feature.entity.FormQuestion;
import com.acm.acmwebsite.feature.exception.*;
import com.acm.acmwebsite.feature.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RegistrationService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventRegistrationRepository eventRegistrationRepository;
    private final EventFormQuestionRepository eventFormQuestionRepository;
    private final ClubRepository clubRepository;
    private final ClubRegistrationRepository clubRegistrationRepository;
    private final ClubFormQuestionRepository clubFormQuestionRepository;
    private final com.acm.acmwebsite.core.service.EmailService coreEmailService;

    public List<FormQuestionResponseDto> getEventQuestions(Long eventId) {
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

    public void registerUserForEvent(UUID userId, Long eventId, RegistrationRequestDto request) {
        // 1. Get entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResourceNotFoundException("Event not found"));

        // 2. Check profile integrity
        validateUserProfile(user);

        // 3. Prevent double registrations
        if (eventRegistrationRepository.existsByUserIdAndEventId(user.getId(), eventId)) {
            throw new DuplicateRegistrationException("You are already registered for this event.");
        }

        // 4. Validate answers for questions
        List<EventFormQuestion> formQuestions = eventFormQuestionRepository.findByEventId(eventId);
        validateAnswers(formQuestions, request.getAnswers());

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

    public List<FormQuestionResponseDto> getClubQuestions(Long clubId) {
        if (!clubRepository.existsById(clubId)) {
            throw new ResourceNotFoundException("Club not found with id " + clubId);
        }

        List<ClubFormQuestion> questions = clubFormQuestionRepository.findByClubId(clubId);
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

    public void registerUserForClub(UUID userId, Long clubId, RegistrationRequestDto request) {
        // 1. Get entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));

        // 2. Check profile integrity
        validateUserProfile(user);

        // 3. Prevent double registrations
        if (clubRegistrationRepository.existsByUserIdAndClubId(user.getId(), clubId)) {
            throw new DuplicateRegistrationException("You have already registered for this club.");
        }

        // 4. Validate answers for questions
        List<ClubFormQuestion> formQuestions = clubFormQuestionRepository.findByClubId(clubId);
        validateAnswers(formQuestions, request.getAnswers());

        // 5. Store registration
        ClubRegistration registration = ClubRegistration.builder()
                .user(user)
                .club(club)
                .answers(request.getAnswers())
                .build();

        clubRegistrationRepository.save(registration);

        // 6. Trigger Confirmation Email
        try {
            coreEmailService.sendRegistrationConfirmationEmail(user.getEmail(), club.getName(), user.getName());
        } catch (Exception e) {
            // Non-blocking
        }
    }

    // Helper to ensure basic user details are complete
    private void validateUserProfile(User user) {
        if (user.getName() == null || user.getName().isBlank() ||
            user.getPhoneNumber() == null || user.getPhoneNumber().isBlank()||
            user.getIsAlexEngStudent() == null) {
            throw new ProfileIncompleteException("Please complete your profile details (Name & Phone Number & Student Status) before registering.");
        }
    }

    // Event & Club question forms validator
    private void validateAnswers(List<? extends FormQuestion> questions, Map<Long, String> answers) {
        for (FormQuestion question : questions) {
            if (Boolean.TRUE.equals(question.getIsRequired())) {
                String answer = answers.get(question.getId());
                if (answer == null || answer.isBlank()) {
                    throw new MissingRequiredAnswerException("Answer is required for: " + question.getQuestionText());
                }
            }
        }
    }

    public boolean isUserRegisteredForEvent(UUID userId, Long eventId) {
        return eventRegistrationRepository.existsByUserIdAndEventId(userId, eventId);
    }

    public boolean isUserRegisteredForClub(UUID userId, Long clubId) {
        return clubRegistrationRepository.existsByUserIdAndClubId(userId, clubId);
    }
}
