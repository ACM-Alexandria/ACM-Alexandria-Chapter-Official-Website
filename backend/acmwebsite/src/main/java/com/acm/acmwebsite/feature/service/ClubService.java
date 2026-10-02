package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.core.constants.CacheNames;
import com.acm.acmwebsite.feature.dto.ClubCardDto;
import com.acm.acmwebsite.feature.dto.EmailSettingsDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.entity.ClubRegistration;
import com.acm.acmwebsite.feature.entity.ClubFormQuestion;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.mapper.ClubMapper;
import com.acm.acmwebsite.feature.util.QuestionValidationUtil;
import com.acm.acmwebsite.feature.repository.ClubRepository;
import com.acm.acmwebsite.feature.repository.ClubRegistrationRepository;
import com.acm.acmwebsite.feature.repository.ClubFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.ClubBoardRepository;
import com.acm.acmwebsite.feature.exception.AnnouncementAlreadySentException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.exception.GoogleSheetsNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ClubService {
    private final ClubRepository clubRepository;
    private final ClubMapper clubMapper;
    private final ClubRegistrationRepository clubRegistrationRepository;
    private final ClubFormQuestionRepository clubFormQuestionRepository;
    private final GoogleSheetsService googleSheetsService;
    private final SubscriptionService subscriptionService;
    private final SystemSettingsService systemSettingsService;
    private final ClubBoardRepository clubBoardRepository;
    private static final Logger logger = LoggerFactory.getLogger(ClubService.class);

    @Value("${google.sheets.clubs-folder-id:}")
    private String clubsFolderId;

    public ClubService(ClubRepository clubRepository, ClubMapper clubMapper,
                       ClubRegistrationRepository clubRegistrationRepository,
                       ClubFormQuestionRepository clubFormQuestionRepository,
                       GoogleSheetsService googleSheetsService,
                       SubscriptionService subscriptionService,
                       ClubBoardRepository clubBoardRepository,
                       SystemSettingsService systemSettingsService) {
        this.clubRepository = clubRepository;
        this.clubMapper = clubMapper;
        this.clubRegistrationRepository = clubRegistrationRepository;
        this.clubFormQuestionRepository = clubFormQuestionRepository;
        this.googleSheetsService = googleSheetsService;
        this.subscriptionService = subscriptionService;
        this.clubBoardRepository = clubBoardRepository;
        this.systemSettingsService = systemSettingsService;
    }

    @Cacheable(value = CacheNames.CLUBS, key = "'AllClubs_page_' + #pageNumber")   
     public List<ClubCardDto> getAllClubs() {
        return clubRepository.findAll().stream()
                .map(this::toClubCardDto)
                .collect(Collectors.toList());
    }

    public Page<ClubCardDto> getClubsByPage(int pageNumber) {
        logger.info("Fetching Clubs from Database...");
        pageNumber = Math.max(0, pageNumber);
        Pageable pageable = PageRequest.of(pageNumber, 100, Sort.by("name").ascending());
        return clubRepository.findAll(pageable).map(this::toClubCardDto);
    }
    
    private ClubCardDto toClubCardDto(Club club) {
            ClubCardDto dto = clubMapper.toClubCardDto(club);
            var boardEntities = clubBoardRepository.findByClubId(club.getId());
            if (boardEntities != null && !boardEntities.isEmpty()) {
                var boardRoles = boardEntities.stream().map(b -> new com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeBoardMemberDto(
                    b.getId(),
                    b.getRole(),
                    b.getOrder(),
                    b.getUser() != null ? b.getUser().getId() : null,
                    b.getUser() != null ? b.getUser().getName() : null,
                    b.getUser() != null ? b.getUser().getProfileImageUrl() : null,
                    b.getUser() != null ? b.getUser().getLinkedinUrl() : null
                )).collect(Collectors.toList());
                dto.setBoardRoles(boardRoles);
            }
            return dto;
    }
    @Cacheable(value = CacheNames.CLUBS, key = "'club_' + #id")
    public Optional<Club> getClubById(long id) {
        logger.info("Fetching One Club from Database...");
        return clubRepository.findById(id);
    }
    @CacheEvict(value = CacheNames.CLUBS, allEntries = true)
    public Club createClub(Club club) {
        if (club.getName() == null || club.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Club name is required");
        }
        // Decided once so the announcement only goes out when it can really be sent (emails unlocked)
        boolean announce = !Boolean.FALSE.equals(club.getSendAnnouncement())
                && systemSettingsService.isEmailsEnabled();
        Club savedClub = clubRepository.save(club);
        // Only recorded as announced once the send went through, so a failure leaves it sendable later
        if (announce && notifySubscribersAboutNewClub(savedClub)) {
            savedClub.setAnnouncementSentAt(LocalDateTime.now());
            savedClub = clubRepository.save(savedClub);
        }
        return savedClub;
    }

    public EmailSettingsDto getEmailSettings(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id " + id));
        return new EmailSettingsDto(club.getAnnouncementSentAt());
    }

    // force = true resends even if it was already announced; otherwise a second send is refused
    @Transactional
    public void announceClub(Long id, boolean force) {
        systemSettingsService.assertEmailsEnabled();
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id " + id));
        LocalDateTime now = LocalDateTime.now();
        if (force) {
            club.setAnnouncementSentAt(now);
            clubRepository.save(club);
        } else if (clubRepository.claimAnnouncement(id, now) == 0) {
            throw new AnnouncementAlreadySentException(club.getAnnouncementSentAt());
        }
        subscriptionService.sendNewClubNotificationToNewsSubscribers(club);
    }

    // Returns false when the send failed, so the club isn't marked as announced
    private boolean notifySubscribersAboutNewClub(Club club) {
        try {
            subscriptionService.sendNewClubNotificationToNewsSubscribers(club);
            return true;
        } catch (Exception e) {
            log.warn("Announcement for new club {} failed; it can still be sent manually", club.getId(), e);
            return false;
        }
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
    @CacheEvict(value = CacheNames.CLUBS, allEntries = true)
    public Club updateClub(Long id,Club updatedClub) {
        return clubRepository.findById(id).map(club -> {
            if (updatedClub.getName() == null || updatedClub.getName().trim().isEmpty()) {
                throw new IllegalArgumentException("Club name is required");
            }
            club.setName(updatedClub.getName());
            club.setDescription(updatedClub.getDescription());
            club.setImageUrl(updatedClub.getImageUrl());
            club.setSocialMediaLinks(updatedClub.getSocialMediaLinks());
            club.setIsExternal(updatedClub.getIsExternal());
            club.setRegistrationOpen(updatedClub.getRegistrationOpen());
            return clubRepository.save(club);
                }
        ).orElseThrow(()->new RuntimeException("Club not found"));
    }

    @CacheEvict(value = CacheNames.CLUBS, allEntries = true)
    public Club openRegistration(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id " + id));
        club.setRegistrationOpen(true);
        return clubRepository.save(club);
    }

    @CacheEvict(value = CacheNames.CLUBS, allEntries = true)
    public Club closeRegistration(Long id) {
        Club club = clubRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id " + id));
        club.setRegistrationOpen(false);
        return clubRepository.save(club);
    }

    @Transactional(readOnly = true)
    public List<String> getClubSocialLinks(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id " + clubId));
        return club.getSocialMediaLinks() != null ? club.getSocialMediaLinks() : new ArrayList<>();
    }

    @Transactional
    @CacheEvict(value = CacheNames.CLUBS, allEntries = true)
    public List<String> updateClubSocialLinks(Long clubId, List<String> socialLinks) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id " + clubId));

        List<String> cleanLinks = socialLinks == null ? new ArrayList<>() : socialLinks.stream()
                .filter(link -> link != null && !link.trim().isEmpty())
                .map(String::trim)
                .collect(Collectors.toList());

        club.setSocialMediaLinks(cleanLinks);
        clubRepository.save(club);
        return cleanLinks;
    }
    @Transactional
    @CacheEvict(value = CacheNames.CLUBS, allEntries = true)
    public void deleteClubById(long id) {
        clubRegistrationRepository.deleteByClubId(id);
        clubFormQuestionRepository.deleteByClubId(id);
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
    @CacheEvict(value = CacheNames.CLUBS, allEntries = true)
    public RegistrationAnalysisDto syncRegistrationsSheet(Long clubId) {
        Club club = clubRepository.findById(clubId)
                .orElseThrow(() -> new ResourceNotFoundException("Club not found with id " + clubId));

        List<ClubRegistration> registrations = clubRegistrationRepository.findByClubId(clubId);
        List<ClubFormQuestion> questions = clubFormQuestionRepository.findByClubId(clubId);

        List<String> prefixHeaders = Arrays.asList(
                "Club Name:", club.getName()
        );
        String title = "ACM Alexandria - Club: " + club.getName() + " - Registrations";

        String url = googleSheetsService.syncRegistrationData(
                title,
                clubsFolderId,
                club.getGoogleSheetUrl(),
                prefixHeaders,
                registrations,
                questions,
                ClubRegistration::getUser,
                ClubRegistration::getId,
                ClubRegistration::getAnswers,
                ClubFormQuestion::getId,
                ClubFormQuestion::getQuestionText,
                ClubRegistration::getRegisteredAt
        );

        club.setGoogleSheetUrl(url);
        club.setSheetLastUpdatedAt(LocalDateTime.now());
        clubRepository.save(club);

        return getRegistrationAnalysis(clubId);
    }
}
