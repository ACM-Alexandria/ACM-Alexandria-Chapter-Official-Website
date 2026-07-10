package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.dto.CommitteeCallResponseDto;
import com.acm.acmwebsite.feature.entity.Committee;
import com.acm.acmwebsite.feature.entity.CommitteeCall;
import com.acm.acmwebsite.feature.entity.CommitteeFormQuestion;
import com.acm.acmwebsite.feature.entity.CommitteeRegistration;
import com.acm.acmwebsite.feature.exception.DuplicateRegistrationException;
import com.acm.acmwebsite.feature.exception.GoogleSheetsNotFoundException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.repository.CommitteeCallRepository;
import com.acm.acmwebsite.feature.repository.CommitteeFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.CommitteeRegistrationRepository;
import com.acm.acmwebsite.feature.repository.CommitteeRepository;
import com.acm.acmwebsite.feature.util.QuestionValidationUtil;
import com.acm.acmwebsite.feature.util.RegistrationValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommitteeRegistrationService implements RegistrationService {

    private final UserRepository userRepository;
    private final CommitteeRepository committeeRepository;
    private final CommitteeCallRepository committeeCallRepository;
    private final CommitteeRegistrationRepository committeeRegistrationRepository;
    private final CommitteeFormQuestionRepository committeeFormQuestionRepository;
    private final com.acm.acmwebsite.core.service.EmailService coreEmailService;
    private final GoogleSheetsService googleSheetsService;

    @Value("${google.sheets.committees-registrations-folder-id:}")
    private String committeesRegistrationsFolderId;

    @Override
    public List<FormQuestionResponseDto> getQuestions(Long committeeId) {
        if (!committeeRepository.existsById(committeeId)) {
            throw new ResourceNotFoundException("Committee not found with id " + committeeId);
        }

        List<CommitteeFormQuestion> questions = committeeFormQuestionRepository.findByCommitteeId(committeeId);
        return questions.stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public void registerUser(UUID userId, Long committeeId, RegistrationRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Committee committee = committeeRepository.findById(committeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Committee not found"));

        if (!committee.isOpen()) {
            throw new IllegalStateException("The call for this committee is currently closed.");
        }

        CommitteeCall activeCall = committeeCallRepository.findActiveCallByCommitteeId(committeeId)
                .orElseThrow(() -> new ResourceNotFoundException("No active call found for this committee."));

        RegistrationValidationUtil.validateUserProfile(user);

        if (committeeRegistrationRepository.existsByUserIdAndCommitteeCallId(user.getId(), activeCall.getId())) {
            throw new DuplicateRegistrationException("You have already applied for this committee call.");
        }

        List<CommitteeFormQuestion> formQuestions = committeeFormQuestionRepository.findByCommitteeId(committeeId);
        RegistrationValidationUtil.validateAnswers(formQuestions, request.getAnswers());

        CommitteeRegistration registration = CommitteeRegistration.builder()
                .user(user)
                .committeeCall(activeCall)
                .answers(request.getAnswers())
                .build();

        committeeRegistrationRepository.save(registration);

        try {
            coreEmailService.sendCommitteeRegistrationConfirmationEmail(user.getEmail(), committee.getName(), user.getName());
        } catch (Exception e) {
            // Non-blocking
        }
    }

    @Override
    public boolean isUserRegistered(UUID userId, Long committeeId) {
        CommitteeCall activeCall = committeeCallRepository.findActiveCallByCommitteeId(committeeId).orElse(null);
        if (activeCall == null) {
            return false;
        }
        return committeeRegistrationRepository.existsByUserIdAndCommitteeCallId(userId, activeCall.getId());
    }

    @Transactional
    public FormQuestionResponseDto createQuestion(Long committeeId, FormQuestionRequestDto request) {
        Committee committee = committeeRepository.findById(committeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Committee not found with id " + committeeId));

        CommitteeFormQuestion question = CommitteeFormQuestion.builder()
                .committee(committee)
                .questionText(QuestionValidationUtil.validateQuestionText(request))
                .questionType(QuestionValidationUtil.parseQuestionType(request))
                .isRequired(Boolean.TRUE.equals(request.getIsRequired()))
                .options(QuestionValidationUtil.normalizeOptions(request.getOptions()))
                .build();

        return toResponseDto(committeeFormQuestionRepository.save(question));
    }

    @Transactional
    public FormQuestionResponseDto updateQuestion(Long committeeId, Long questionId, FormQuestionRequestDto request) {
        CommitteeFormQuestion question = committeeFormQuestionRepository.findById(questionId)
                .filter(q -> q.getCommittee() != null && q.getCommittee().getId() == committeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Committee question not found with id " + questionId));

        question.setQuestionText(QuestionValidationUtil.validateQuestionText(request));
        question.setQuestionType(QuestionValidationUtil.parseQuestionType(request));
        question.setIsRequired(Boolean.TRUE.equals(request.getIsRequired()));
        question.setOptions(QuestionValidationUtil.normalizeOptions(request.getOptions()));

        return toResponseDto(committeeFormQuestionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long committeeId, Long questionId) {
        CommitteeFormQuestion question = committeeFormQuestionRepository.findById(questionId)
                .filter(q -> q.getCommittee() != null && q.getCommittee().getId() == committeeId)
                .orElseThrow(() -> new ResourceNotFoundException("Committee question not found with id " + questionId));

        committeeFormQuestionRepository.delete(question);
    }

    public List<CommitteeCallResponseDto> getCallsForCommittee(Long committeeId) {
        if (!committeeRepository.existsById(committeeId)) {
            throw new ResourceNotFoundException("Committee not found with id " + committeeId);
        }
        List<CommitteeCall> calls = committeeCallRepository.findByCommitteeIdOrderByOpenedAtDesc(committeeId);
        return calls.stream()
                .map(c -> CommitteeCallResponseDto.builder()
                        .id(c.getId())
                        .openedAt(c.getOpenedAt())
                        .closedAt(c.getClosedAt())
                        .googleSheetUrl(c.getGoogleSheetUrl())
                        .sheetLastUpdatedAt(c.getSheetLastUpdatedAt())
                        .registrationsCount(committeeRegistrationRepository.countByCommitteeCallId(c.getId()))
                        .build())
                .toList();
    }

    public RegistrationAnalysisDto getRegistrationAnalysis(Long committeeCallId) {
        CommitteeCall call = committeeCallRepository.findById(committeeCallId)
                .orElseThrow(() -> new ResourceNotFoundException("Committee call not found with id " + committeeCallId));

        List<CommitteeRegistration> registrations = committeeRegistrationRepository.findByCommitteeCallId(committeeCallId);

        long alexCount = registrations.stream()
                .filter(r -> r.getUser() != null && Boolean.TRUE.equals(r.getUser().getIsAlexEngStudent()))
                .count();

        Map<String, Long> departments = registrations.stream()
                .filter(r -> r.getUser() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getUser().getDepartment() != null ? r.getUser().getDepartment().name() : "N/A",
                        Collectors.counting()
                ));

        Map<String, Long> batches = registrations.stream()
                .filter(r -> r.getUser() != null)
                .collect(Collectors.groupingBy(
                        r -> r.getUser().getBatch() != null && !r.getUser().getBatch().trim().isEmpty() ? r.getUser().getBatch() : "N/A",
                        Collectors.counting()
                ));

        return RegistrationAnalysisDto.builder()
                .totalRegistrations(registrations.size())
                .alexUniStudentCount(alexCount)
                .nonAlexUniStudentCount(registrations.size() - alexCount)
                .departmentCounts(departments)
                .batchCounts(batches)
                .googleSheetUrl(call.getGoogleSheetUrl())
                .sheetLastUpdatedAt(call.getSheetLastUpdatedAt())
                .build();
    }

    @Transactional
    public RegistrationAnalysisDto syncRegistrationsSheet(Long committeeCallId) {
        CommitteeCall call = committeeCallRepository.findById(committeeCallId)
                .orElseThrow(() -> new ResourceNotFoundException("Committee call not found with id " + committeeCallId));

        Committee committee = call.getCommittee();
        List<CommitteeRegistration> registrations = committeeRegistrationRepository.findByCommitteeCallId(committeeCallId);
        List<CommitteeFormQuestion> questions = committeeFormQuestionRepository.findByCommitteeId(committee.getId());

        List<String> prefixHeaders = Arrays.asList(
                "Committee Name:", committee.getName(),
                "Call Opened At:", call.getOpenedAt() != null ? call.getOpenedAt().toString() : "N/A",
                "Call Closed At:", call.getClosedAt() != null ? call.getClosedAt().toString() : "N/A"
        );
        String title = "ACM Alexandria - Committee: " + committee.getName() + " - Call " + call.getOpenedAt().toLocalDate() + " - Registrations";

        String url = googleSheetsService.syncRegistrationData(
                title,
                committeesRegistrationsFolderId,
                call.getGoogleSheetUrl(),
                prefixHeaders,
                registrations,
                questions,
                CommitteeRegistration::getUser,
                CommitteeRegistration::getId,
                CommitteeRegistration::getAnswers,
                CommitteeFormQuestion::getId,
                CommitteeFormQuestion::getQuestionText
        );

        call.setGoogleSheetUrl(url);
        call.setSheetLastUpdatedAt(LocalDateTime.now());
        committeeCallRepository.save(call);

        return getRegistrationAnalysis(committeeCallId);
    }

    private FormQuestionResponseDto toResponseDto(CommitteeFormQuestion question) {
        return FormQuestionResponseDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType().name())
                .isRequired(question.getIsRequired())
                .options(question.getOptions())
                .build();
    }
}
