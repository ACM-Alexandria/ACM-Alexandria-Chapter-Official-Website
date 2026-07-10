package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.Program;
import com.acm.acmwebsite.feature.entity.ProgramFormQuestion;
import com.acm.acmwebsite.feature.entity.ProgramRegistration;
import com.acm.acmwebsite.feature.exception.DuplicateRegistrationException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.repository.ProgramFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.ProgramRegistrationRepository;
import com.acm.acmwebsite.feature.repository.ProgramRepository;
import com.acm.acmwebsite.feature.util.RegistrationValidationUtil;
import com.acm.acmwebsite.core.service.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProgramRegistrationService implements RegistrationService {

    private final UserRepository userRepository;
    private final ProgramRepository programRepository;
    private final ProgramRegistrationRepository programRegistrationRepository;
    private final ProgramFormQuestionRepository programFormQuestionRepository;
    private final EmailService coreEmailService;

    @Override
    public List<FormQuestionResponseDto> getQuestions(Long programId) {
        if (!programRepository.existsById(programId)) {
            throw new ResourceNotFoundException("Program not found with id " + programId);
        }

        List<ProgramFormQuestion> questions = programFormQuestionRepository.findByProgramId(programId);
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
    public void registerUser(UUID userId, Long programId, RegistrationRequestDto request) {
        // 1. Get entities
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found"));

        // 2. Check registration is open
        if (!program.isRegistrationOpen()) {
            throw new IllegalStateException("Registration for this program is currently closed.");
        }

        // 3. Check profile integrity
        RegistrationValidationUtil.validateUserProfile(user);

        // 4. Prevent double registrations
        if (programRegistrationRepository.existsByUserIdAndProgramId(user.getId(), programId)) {
            throw new DuplicateRegistrationException("You are already registered for this program.");
        }

        // 5. Validate answers for questions
        List<ProgramFormQuestion> formQuestions = programFormQuestionRepository.findByProgramId(programId);
        RegistrationValidationUtil.validateAnswers(formQuestions, request.getAnswers());

        // 6. Store registration
        ProgramRegistration registration = ProgramRegistration.builder()
                .user(user)
                .program(program)
                .answers(request.getAnswers())
                .build();

        programRegistrationRepository.save(registration);

        // 7. Trigger Confirmation Email
        try {
            coreEmailService.sendRegistrationConfirmationEmail(user.getEmail(), program.getName(), user.getName());
        } catch (Exception e) {
            // Non-blocking
        }
    }

    @Override
    public boolean isUserRegistered(UUID userId, Long programId) {
        return programRegistrationRepository.existsByUserIdAndProgramId(userId, programId);
    }
}
