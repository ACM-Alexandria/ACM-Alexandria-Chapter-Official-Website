package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.ClubCardDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.entity.ClubRegistration;
import com.acm.acmwebsite.feature.entity.ClubFormQuestion;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.mapper.ClubMapper;
import com.acm.acmwebsite.feature.util.QuestionValidationUtil;
import com.acm.acmwebsite.feature.repository.ClubRepository;
import com.acm.acmwebsite.feature.repository.ClubRegistrationRepository;
import com.acm.acmwebsite.feature.repository.ClubFormQuestionRepository;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.exception.GoogleSheetsNotFoundException;
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
public class ClubService {
    private final ClubRepository clubRepository;
    private final ClubMapper clubMapper;
    private final ClubRegistrationRepository clubRegistrationRepository;
    private final ClubFormQuestionRepository clubFormQuestionRepository;
    private final GoogleSheetsService googleSheetsService;

    @Value("${google.sheets.clubs-folder-id:}")
    private String clubsFolderId;

    public ClubService(ClubRepository clubRepository, ClubMapper clubMapper,
                       ClubRegistrationRepository clubRegistrationRepository,
                       ClubFormQuestionRepository clubFormQuestionRepository,
                       GoogleSheetsService googleSheetsService) {
        this.clubRepository = clubRepository;
        this.clubMapper = clubMapper;
        this.clubRegistrationRepository = clubRegistrationRepository;
        this.clubFormQuestionRepository = clubFormQuestionRepository;
        this.googleSheetsService = googleSheetsService;
    }


    public Page<ClubCardDto> getClubsByPage(int pageNumber) {
        pageNumber = Math.max(0, pageNumber);
        Pageable pageable = PageRequest.of(pageNumber, 4, Sort.by("name").ascending());
        return clubRepository.findAll(pageable).map(clubMapper::toClubCardDto);
    }
    public Optional<Club> getClubById(long id) {
        return clubRepository.findById(id);
    }
    public Club createClub(Club club) {
        if (club.getName() == null || club.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Club name is required");
        }
        return clubRepository.save(club);
    }

    @Transactional
    public FormQuestionResponseDto createQuestion(Long clubId, FormQuestionRequestDto request) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id " + clubId));

        ClubFormQuestion question = ClubFormQuestion.builder()
                .club(club)
                .questionText(QuestionValidationUtil.validateQuestionText(request))
                .questionType(QuestionValidationUtil.parseQuestionType(request))
                .isRequired(Boolean.TRUE.equals(request.getIsRequired()))
                .options(QuestionValidationUtil.normalizeOptions(request.getOptions()))
                .build();

        return toResponseDto(clubFormQuestionRepository.save(question));
    }

    @Transactional
    public FormQuestionResponseDto updateQuestion(Long clubId, Long questionId, FormQuestionRequestDto request) {
        ClubFormQuestion question = clubFormQuestionRepository.findById(questionId)
                .filter(q -> q.getClub() != null && Objects.equals(q.getClub().getId(), clubId))
                .orElseThrow(() -> new ResourceNotFoundException("Club question not found with id " + questionId));

        question.setQuestionText(QuestionValidationUtil.validateQuestionText(request));
        question.setQuestionType(QuestionValidationUtil.parseQuestionType(request));
        question.setIsRequired(Boolean.TRUE.equals(request.getIsRequired()));
        question.setOptions(QuestionValidationUtil.normalizeOptions(request.getOptions()));

        return toResponseDto(clubFormQuestionRepository.save(question));
    }

    @Transactional
    public void deleteQuestion(Long clubId, Long questionId) {
        ClubFormQuestion question = clubFormQuestionRepository.findById(questionId)
                .filter(q -> q.getClub() != null && Objects.equals(q.getClub().getId(), clubId))
                .orElseThrow(() -> new ResourceNotFoundException("Club question not found with id " + questionId));

        clubFormQuestionRepository.delete(question);
    }

    public Club updateClub(Long id,Club updatedClub) {
        return clubRepository.findById(id).map(club -> {
            if (updatedClub.getName() == null || updatedClub.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Club name is required");
            }
            club.setName(updatedClub.getName());
            club.setDescription(updatedClub.getDescription());
            club.setImageUrl(updatedClub.getImageUrl());
            club.setSocialMediaLinks(updatedClub.getSocialMediaLinks());
            return clubRepository.save(club);
                }
        ).orElseThrow(()->new RuntimeException("Club not found"));
    }
    public void deleteClubById(long id) {
        clubRepository.deleteById(id);
    }

    private FormQuestionResponseDto toResponseDto(ClubFormQuestion question) {
        return FormQuestionResponseDto.builder()
                .id(question.getId())
                .questionText(question.getQuestionText())
                .questionType(question.getQuestionType().name())
                .isRequired(question.getIsRequired())
                .options(question.getOptions())
                .build();
    }


    public RegistrationAnalysisDto getRegistrationAnalysis(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id " + clubId));

        List<ClubRegistration> registrations = clubRegistrationRepository.findByClubId(clubId);

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
                .googleSheetUrl(club.getGoogleSheetUrl())
                .sheetLastUpdatedAt(club.getSheetLastUpdatedAt())
                .build();
    }

    @Transactional
    public RegistrationAnalysisDto syncRegistrationsSheet(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id " + clubId));

        List<ClubRegistration> registrations = clubRegistrationRepository.findByClubId(clubId);
        List<ClubFormQuestion> questions = clubFormQuestionRepository.findByClubId(clubId);

        List<List<Object>> rows = new ArrayList<>();
        // Row 1: Header/Title info
        rows.add(Arrays.asList("Club Name:", club.getName()));
        rows.add(Collections.emptyList()); // empty row spacer

        // Row 3: Column Headers
        List<Object> headers = new ArrayList<>(Arrays.asList(
                "#", "Registeration ID", "Name", "Email", "Phone Number", "Is Alex Eng Student", "Batch", "Department"
        ));
        for (ClubFormQuestion question : questions) {
            headers.add(question.getQuestionText());
        }
        rows.add(headers);

        // Rows 4+: Registrants Data
        int seqNum = 1;
        for (ClubRegistration reg : registrations) {
            User user = reg.getUser();
            if (user == null) continue;

            List<Object> row = new ArrayList<>(Arrays.asList(
                    seqNum++,
                    reg.getId(),
                    user.getName() != null ? user.getName() : "",
                    user.getEmail() != null ? user.getEmail() : "",
                    user.getPhoneNumber() != null ? user.getPhoneNumber() : "",
                    user.getIsAlexEngStudent() != null && user.getIsAlexEngStudent() ? "Yes" : "No",
                    user.getBatch() != null ? user.getBatch() : "",
                    user.getDepartment() != null ? user.getDepartment().name() : ""
            ));

            for (ClubFormQuestion question : questions) {
                String answer = reg.getAnswers() != null ? reg.getAnswers().getOrDefault(question.getId(), "") : "";
                row.add(answer);
            }
            rows.add(row);
        }

        String spreadsheetUrl = club.getGoogleSheetUrl();
        String spreadsheetId = googleSheetsService.extractSpreadsheetId(spreadsheetUrl);

        boolean needsNewSheet = (spreadsheetId == null);

        if (!needsNewSheet) {
            try {
                // Try clearing and writing to existing sheet
                googleSheetsService.clearSpreadsheet(spreadsheetId);
                googleSheetsService.writeSpreadsheetData(spreadsheetId, rows);
                club.setSheetLastUpdatedAt(LocalDateTime.now());
                clubRepository.save(club);
            } catch (GoogleSheetsNotFoundException e) {
                needsNewSheet = true;
            }
        }

        if (needsNewSheet) {
            String title = "ACM Alexandria - Club: " + club.getName() + " - Registrations";
            String newUrl = googleSheetsService.createSpreadsheet(title, clubsFolderId);
            String newId = googleSheetsService.extractSpreadsheetId(newUrl);
            googleSheetsService.writeSpreadsheetData(newId, rows);

            club.setGoogleSheetUrl(newUrl);
            club.setSheetLastUpdatedAt(LocalDateTime.now());
            clubRepository.save(club);
        }

        return getRegistrationAnalysis(clubId);
    }
}
