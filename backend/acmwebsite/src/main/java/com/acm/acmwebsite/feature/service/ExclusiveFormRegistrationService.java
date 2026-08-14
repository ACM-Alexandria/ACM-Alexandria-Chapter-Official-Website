package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.dto.RegistrationRequestDto;
import com.acm.acmwebsite.feature.entity.ExclusiveForm;
import com.acm.acmwebsite.feature.entity.ExclusiveFormQuestion;
import com.acm.acmwebsite.feature.entity.ExclusiveRegistration;
import com.acm.acmwebsite.feature.exception.DuplicateRegistrationException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.repository.ExclusiveFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.ExclusiveFormRepository;
import com.acm.acmwebsite.feature.repository.ExclusiveRegistrationRepository;
import com.acm.acmwebsite.feature.util.QuestionValidationUtil;
import com.acm.acmwebsite.feature.util.RegistrationValidationUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExclusiveFormRegistrationService implements RegistrationService {

    private final UserRepository userRepository;
    private final ExclusiveFormRepository exclusiveFormRepository;
    private final ExclusiveFormQuestionRepository exclusiveFormQuestionRepository;
    private final ExclusiveRegistrationRepository exclusiveRegistrationRepository;
    private final com.acm.acmwebsite.core.service.EmailService coreEmailService;
    private final GoogleSheetsService googleSheetsService;

    @Value("${google.sheets.exclusive-forms-folder-id:}")
    private String exclusiveFormsFolderId;

    @Override
    public List<FormQuestionResponseDto> getQuestions(Long formId) {
        if (!exclusiveFormRepository.existsById(formId)) {
            throw new ResourceNotFoundException("Exclusive form not found with id " + formId);
        }

        List<ExclusiveFormQuestion> questions = exclusiveFormQuestionRepository.findByExclusiveFormId(formId);
        return questions.stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Override
    public void registerUser(UUID userId, Long formId, RegistrationRequestDto request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        ExclusiveForm form = exclusiveFormRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Exclusive form not found"));

        if (!form.getIsActive()) {
            throw new IllegalStateException("This form is not currently active.");
        }

        RegistrationValidationUtil.validateUserProfile(user);

        if (exclusiveRegistrationRepository.findByUserIdAndExclusiveFormId(user.getId(), form.getId()).isPresent()) {
            throw new DuplicateRegistrationException("You have already submitted this form.");
        }

        List<ExclusiveFormQuestion> formQuestions = exclusiveFormQuestionRepository.findByExclusiveFormId(formId);
        RegistrationValidationUtil.validateAnswers(formQuestions, request.getAnswers());

        ExclusiveRegistration registration = ExclusiveRegistration.builder()
                .user(user)
                .exclusiveForm(form)
                .answers(request.getAnswers())
                .build();

        exclusiveRegistrationRepository.save(registration);

        try {
            coreEmailService.sendGenericFormSubmissionConfirmationEmail(user.getEmail(), form.getTitle(), user.getName());
        } catch (Exception e) {
            // Non-blocking
        }
    }

    @Override
    public boolean isUserRegistered(UUID userId, Long formId) {
        return exclusiveRegistrationRepository.findByUserIdAndExclusiveFormId(userId, formId).isPresent();
    }

    @Transactional
    public FormQuestionResponseDto createQuestion(Long formId, FormQuestionRequestDto request) {
        ExclusiveForm form = exclusiveFormRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Exclusive form not found with id " + formId));

        ExclusiveFormQuestion question = ExclusiveFormQuestion.builder()
                .exclusiveForm(form)
                .questionText(QuestionValidationUtil.validateQuestionText(request))
                .questionType(QuestionValidationUtil.parseQuestionType(request))
                .isRequired(Boolean.TRUE.equals(request.getIsRequired()))
                .options(QuestionValidationUtil.normalizeOptions(request.getOptions()))
                .build();

        return toResponseDto(exclusiveFormQuestionRepository.save(question));
    }

    @Transactional
    public FormQuestionResponseDto updateQuestion(Long formId, Long questionId, FormQuestionRequestDto request) {
        ExclusiveFormQuestion question = exclusiveFormQuestionRepository.findById(questionId)
                .filter(q -> q.getExclusiveForm() != null && q.getExclusiveForm().getId().equals(formId))
                .orElseThrow(() -> new ResourceNotFoundException("Form question not found with id " + questionId));

        question.setQuestionText(QuestionValidationUtil.validateQuestionText(request));
        question.setQuestionType(QuestionValidationUtil.parseQuestionType(request));
        question.setIsRequired(Boolean.TRUE.equals(request.getIsRequired()));
        question.setOptions(QuestionValidationUtil.normalizeOptions(request.getOptions()));

        return toResponseDto(exclusiveFormQuestionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long formId, Long questionId) {
        ExclusiveFormQuestion question = exclusiveFormQuestionRepository.findById(questionId)
                .filter(q -> q.getExclusiveForm() != null && q.getExclusiveForm().getId().equals(formId))
                .orElseThrow(() -> new ResourceNotFoundException("Form question not found with id " + questionId));

        exclusiveFormQuestionRepository.delete(question);
    }

    public RegistrationAnalysisDto getRegistrationAnalysis(Long formId) {
        ExclusiveForm form = exclusiveFormRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Exclusive form not found with id " + formId));

        List<ExclusiveRegistration> registrations = exclusiveRegistrationRepository.findByExclusiveFormIdOrderByRegisteredAtAsc(formId);

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
                .googleSheetUrl(form.getSheetId() != null ? "https://docs.google.com/spreadsheets/d/" + form.getSheetId() + "/edit" : null)
                .sheetLastUpdatedAt(form.getUpdatedAt()) // Or map accordingly
                .build();
    }

    @Transactional
    public RegistrationAnalysisDto syncRegistrationsSheet(Long formId) {
        ExclusiveForm form = exclusiveFormRepository.findById(formId)
                .orElseThrow(() -> new ResourceNotFoundException("Exclusive form not found with id " + formId));

        List<ExclusiveRegistration> registrations = exclusiveRegistrationRepository.findByExclusiveFormIdOrderByRegisteredAtAsc(formId);
        List<ExclusiveFormQuestion> questions = exclusiveFormQuestionRepository.findByExclusiveFormId(formId);

        List<String> prefixHeaders = Arrays.asList(
                "Form Title:", form.getTitle(),
                "Form Created At:", form.getCreatedAt() != null ? form.getCreatedAt().toString() : "N/A"
        );
        String title = "ACM Alexandria - Form: " + form.getTitle() + " - Registrations";
        String currentUrl = form.getSheetId() != null ? "https://docs.google.com/spreadsheets/d/" + form.getSheetId() + "/edit" : null;

        String newUrl = googleSheetsService.syncRegistrationData(
                title,
                exclusiveFormsFolderId,
                currentUrl,
                prefixHeaders,
                registrations,
                questions,
                ExclusiveRegistration::getUser,
                ExclusiveRegistration::getId,
                ExclusiveRegistration::getAnswers,
                ExclusiveFormQuestion::getId,
                ExclusiveFormQuestion::getQuestionText,
                ExclusiveRegistration::getRegisteredAt
        );

        String newSheetId = googleSheetsService.extractSpreadsheetId(newUrl);
        if (newSheetId != null && !newSheetId.equals(form.getSheetId())) {
            form.setSheetId(newSheetId);
            exclusiveFormRepository.save(form);
        }

        return getRegistrationAnalysis(formId);
    }

    private FormQuestionResponseDto toResponseDto(ExclusiveFormQuestion question) {
        return FormQuestionResponseDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType().name())
                .isRequired(question.getIsRequired())
                .options(question.getOptions())
                .build();
    }
}
