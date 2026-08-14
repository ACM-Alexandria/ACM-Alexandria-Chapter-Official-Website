package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.ProgramDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.entity.Program;
import com.acm.acmwebsite.feature.entity.ProgramFormQuestion;
import com.acm.acmwebsite.feature.entity.ProgramRegistration;
import com.acm.acmwebsite.feature.exception.GoogleSheetsNotFoundException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.mapper.ProgramMapper;
import com.acm.acmwebsite.feature.repository.ProgramFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.ProgramRegistrationRepository;
import com.acm.acmwebsite.feature.repository.ProgramRepository;
import com.acm.acmwebsite.feature.util.QuestionValidationUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProgramService {
    private final ProgramRepository programRepository;
    private final ProgramMapper programMapper;
    private final ProgramFormQuestionRepository programFormQuestionRepository;
    private final ProgramRegistrationRepository programRegistrationRepository;
    private final GoogleSheetsService googleSheetsService;
    private final SubscriptionService subscriptionService;

    @Value("${google.sheets.programs-folder-id:}")
    private String programsFolderId;

    public ProgramService(ProgramRepository programRepository,
                          ProgramMapper programMapper,
                          ProgramFormQuestionRepository programFormQuestionRepository,
                          ProgramRegistrationRepository programRegistrationRepository,
                          GoogleSheetsService googleSheetsService,
                          SubscriptionService subscriptionService) {
        this.programRepository = programRepository;
        this.programMapper = programMapper;
        this.programFormQuestionRepository = programFormQuestionRepository;
        this.programRegistrationRepository = programRegistrationRepository;
        this.googleSheetsService = googleSheetsService;
        this.subscriptionService = subscriptionService;
    }

    public List<ProgramDto> getAllPrograms() {
        return programRepository.findAll(Sort.by("startDate").descending()).stream()
                .map(programMapper::toProgramDto)
                .toList();
    }

    public Page<ProgramDto> getProgramsByPage(int pageNumber) {
        pageNumber = Math.max(0, pageNumber);
        Pageable page = PageRequest.of(pageNumber, 4, Sort.by("startDate").descending());
        return programRepository.findAll(page).map(programMapper::toProgramDto);
    }

    public Optional<ProgramDto> getProgramById(long id) {
        return programRepository.findById(id).map(programMapper::toProgramDto);
    }

    @Transactional
    public void deleteProgram(long id) {
        programRegistrationRepository.deleteByProgramId(id);
        programFormQuestionRepository.deleteByProgramId(id);
        programRepository.deleteById(id);
    }

    @Transactional
    public ProgramDto updateProgram(Long id, ProgramDto updatedProgram) {
        if (updatedProgram.getName() == null || updatedProgram.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Program name is required");
        }
        Program program = programRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Program not found!"));
        program.setName(updatedProgram.getName());
        program.setDescription(updatedProgram.getDescription());
        program.setImageUrl(updatedProgram.getImageUrl());
        program.setStartDate(updatedProgram.getStartDate());
        program.setEndDate(updatedProgram.getEndDate());
        program.setTime(updatedProgram.getTime());
        program.setRegistrationOpen(updatedProgram.isRegistrationOpen());
        return programMapper.toProgramDto(programRepository.save(program));
    }

    public List<ProgramDto> findProgramByName(String name) {
        return programRepository.findByName(name).stream()
                .map(programMapper::toProgramDto)
                .toList();
    }

    public ProgramDto createProgram(ProgramDto programDto) {
        if (programDto.getName() == null || programDto.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Program name is required");
        }
        Program program = programMapper.toProgram(programDto);
        Program saved = programRepository.save(program);
        subscriptionService.sendNewProgramNotificationToNewsSubscribers(saved);
        return programMapper.toProgramDto(saved);
    }

    // ── Registration Toggle ──

    @Transactional
    public ProgramDto toggleRegistration(Long programId, boolean open) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with id " + programId));
        program.setRegistrationOpen(open);
        return programMapper.toProgramDto(programRepository.save(program));
    }

    // ── Form Questions ──

    @Transactional
    public FormQuestionResponseDto createQuestion(Long programId, FormQuestionRequestDto request) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with id " + programId));

        ProgramFormQuestion question = ProgramFormQuestion.builder()
                .program(program)
                .questionText(QuestionValidationUtil.validateQuestionText(request))
                .questionType(QuestionValidationUtil.parseQuestionType(request))
                .isRequired(Boolean.TRUE.equals(request.getIsRequired()))
                .options(QuestionValidationUtil.normalizeOptions(request.getOptions()))
                .build();

        return toResponseDto(programFormQuestionRepository.save(question));
    }

    @Transactional
    public FormQuestionResponseDto updateQuestion(Long programId, Long questionId, FormQuestionRequestDto request) {
        ProgramFormQuestion question = programFormQuestionRepository.findById(questionId)
                .filter(q -> q.getProgram() != null && q.getProgram().getId().equals(programId))
                .orElseThrow(() -> new ResourceNotFoundException("Program question not found with id " + questionId));

        question.setQuestionText(QuestionValidationUtil.validateQuestionText(request));
        question.setQuestionType(QuestionValidationUtil.parseQuestionType(request));
        question.setIsRequired(Boolean.TRUE.equals(request.getIsRequired()));
        question.setOptions(QuestionValidationUtil.normalizeOptions(request.getOptions()));

        return toResponseDto(programFormQuestionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long programId, Long questionId) {
        ProgramFormQuestion question = programFormQuestionRepository.findById(questionId)
                .filter(q -> q.getProgram() != null && q.getProgram().getId().equals(programId))
                .orElseThrow(() -> new ResourceNotFoundException("Program question not found with id " + questionId));

        programFormQuestionRepository.delete(question);
    }

    // ── Registration Analytics ──

    public RegistrationAnalysisDto getRegistrationAnalysis(Long programId) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with id " + programId));

        List<ProgramRegistration> registrations = programRegistrationRepository.findByProgramId(programId);

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
                        r -> r.getUser().getBatch() != null && !r.getUser().getBatch().trim().isEmpty()
                                ? r.getUser().getBatch() : "N/A",
                        Collectors.counting()
                ));

        return RegistrationAnalysisDto.builder()
                .totalRegistrations(registrations.size())
                .alexUniStudentCount(alexCount)
                .nonAlexUniStudentCount(registrations.size() - alexCount)
                .departmentCounts(departments)
                .batchCounts(batches)
                .googleSheetUrl(program.getGoogleSheetUrl())
                .sheetLastUpdatedAt(program.getSheetLastUpdatedAt())
                .build();
    }

    @Transactional
    public RegistrationAnalysisDto syncRegistrationsSheet(Long programId) {
        Program program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with id " + programId));

        List<ProgramRegistration> registrations = programRegistrationRepository.findByProgramId(programId);
        List<ProgramFormQuestion> questions = programFormQuestionRepository.findByProgramId(programId);

        List<String> prefixHeaders = Arrays.asList(
                "Program Name:", program.getName(),
                "Start/End Date:", (program.getStartDate() != null ? program.getStartDate().toString() : "N/A") + " - " + (program.getEndDate() != null ? program.getEndDate().toString() : "N/A"),
                "Schedule Time:", program.getTime() != null ? program.getTime() : "N/A"
        );
        String title = "ACM Alexandria - Program: " + program.getName() + " - Registrations";

        String url = googleSheetsService.syncRegistrationData(
                title,
                programsFolderId,
                program.getGoogleSheetUrl(),
                prefixHeaders,
                registrations,
                questions,
                ProgramRegistration::getUser,
                ProgramRegistration::getId,
                ProgramRegistration::getAnswers,
                ProgramFormQuestion::getId,
                ProgramFormQuestion::getQuestionText,
                ProgramRegistration::getRegisteredAt
        );

        program.setGoogleSheetUrl(url);
        program.setSheetLastUpdatedAt(LocalDateTime.now());
        programRepository.save(program);

        return getRegistrationAnalysis(programId);
    }

    // ── Helpers ──

    private FormQuestionResponseDto toResponseDto(ProgramFormQuestion question) {
        return FormQuestionResponseDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType().name())
                .isRequired(question.getIsRequired())
                .options(question.getOptions())
                .build();
    }
}
