package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.entity.ClubFormQuestion;
import com.acm.acmwebsite.feature.entity.ClubRegistration;
import com.acm.acmwebsite.feature.entity.FormQuestion;
import com.acm.acmwebsite.feature.exception.DuplicateRegistrationException;
import com.acm.acmwebsite.feature.exception.MissingRequiredAnswerException;
import com.acm.acmwebsite.feature.exception.ProfileIncompleteException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.repository.ClubFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.ClubRegistrationRepository;
import com.acm.acmwebsite.feature.repository.ClubRepository;
import com.acm.acmwebsite.feature.util.RegistrationValidationUtil;
import com.acm.acmwebsite.core.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClubRegistrationService implements RegistrationService {

    private final UserRepository userRepository;
    private final ClubRepository clubRepository;
    private final ClubRegistrationRepository clubRegistrationRepository;
    private final ClubFormQuestionRepository clubFormQuestionRepository;
    private final EmailService coreEmailService;

    @Override
    public List<FormQuestionResponseDto> getQuestions(Long clubId) {
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

    @Override
    public void registerUser(UUID userId, Long clubId, RegistrationRequestDto request) {
        // 1. Get entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found"));

        // 2. Check profile integrity
        RegistrationValidationUtil.validateUserProfile(user);

        // 3. Prevent double registrations
        if (clubRegistrationRepository.existsByUserIdAndClubId(user.getId(), clubId)) {
            throw new DuplicateRegistrationException("You have already registered for this club.");
        }

        // 4. Validate answers for questions
        List<ClubFormQuestion> formQuestions = clubFormQuestionRepository.findByClubId(clubId);
        RegistrationValidationUtil.validateAnswers(formQuestions, request.getAnswers());

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

    @Override
    public boolean isUserRegistered(UUID userId, Long clubId) {
        return clubRegistrationRepository.existsByUserIdAndClubId(userId, clubId);
    }
}
