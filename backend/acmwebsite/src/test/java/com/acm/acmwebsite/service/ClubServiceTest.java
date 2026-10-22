package com.acm.acmwebsite.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Department;
import com.acm.acmwebsite.feature.dto.ClubCardDto;
import com.acm.acmwebsite.feature.dto.EmailSettingsDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.dto.commiteedtos.CommitteeBoardMemberDto;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.entity.ClubBoard;
import com.acm.acmwebsite.feature.entity.ClubFormQuestion;
import com.acm.acmwebsite.feature.entity.ClubRegistration;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.exception.AnnouncementAlreadySentException;
import com.acm.acmwebsite.feature.exception.EmailsLockedException;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.mapper.ClubMapper;
import com.acm.acmwebsite.feature.repository.ClubBoardRepository;
import com.acm.acmwebsite.feature.repository.ClubFormQuestionRepository;
import com.acm.acmwebsite.feature.repository.ClubRegistrationRepository;
import com.acm.acmwebsite.feature.repository.ClubRepository;
import com.acm.acmwebsite.feature.service.ClubService;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import com.acm.acmwebsite.feature.service.SubscriptionService;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private ClubMapper clubMapper;

    @Mock
    private ClubRegistrationRepository clubRegistrationRepository;

    @Mock
    private ClubFormQuestionRepository clubFormQuestionRepository;

    @Mock
    private GoogleSheetsService googleSheetsService;

    @Mock
    private SubscriptionService subscriptionService;

    @Mock
    private ClubBoardRepository clubBoardRepository;

    @Mock
    private SystemSettingsService systemSettingsService;

    @InjectMocks
    private ClubService clubService;

    private Club createClubSample() {
        Club club = new Club();
        club.setId(50L);
        club.setName("CP Club");
        club.setDescription("Programming club");
        club.setImageUrl("img");
        return club;
    }

    private ClubCardDto sampleClubCardDto() {
        return new ClubCardDto(50L, "CP Club", "img", "Programming club");
    }

    @Nested
    @DisplayName("getAllClubs & toClubCardDto Tests")
    class GetAllClubsTests {

        @Test
        @DisplayName("getAllClubs should map board entities with non-null and null user fields")
        void getAllClubs_shouldMapBoardEntitiesProperly() {
            Club club1 = createClubSample();
            club1.setId(1L);
            Club club2 = createClubSample();
            club2.setId(2L);

            ClubCardDto dto1 = new ClubCardDto(1L, "CP Club", "img", "Desc");
            ClubCardDto dto2 = new ClubCardDto(2L, "AI Club", "img2", "Desc2");

            User boardUser = new User();
            UUID userId = UUID.randomUUID();
            boardUser.setId(userId);
            boardUser.setName("Alice");
            boardUser.setProfileImageUrl("http://alice.png");
            boardUser.setLinkedinUrl("http://linkedin.com/alice");

            ClubBoard boardWithUser = new ClubBoard(10L, "Lead", 1, club1, boardUser);
            ClubBoard boardWithoutUser = new ClubBoard(11L, "Co-Lead", 2, club1, null);

            Club club3 = createClubSample();
            club3.setId(3L);
            ClubCardDto dto3 = new ClubCardDto(3L, "Web Club", "img3", "Desc3");

            when(clubRepository.findAll()).thenReturn(List.of(club1, club2, club3));
            when(clubMapper.toClubCardDto(club1)).thenReturn(dto1);
            when(clubMapper.toClubCardDto(club2)).thenReturn(dto2);
            when(clubMapper.toClubCardDto(club3)).thenReturn(dto3);
            when(clubBoardRepository.findByClubId(1L)).thenReturn(List.of(boardWithUser, boardWithoutUser));
            when(clubBoardRepository.findByClubId(2L)).thenReturn(Collections.emptyList());
            when(clubBoardRepository.findByClubId(3L)).thenReturn(null);

            List<ClubCardDto> result = clubService.getAllClubs();

            assertEquals(3, result.size());
            assertNotNull(dto1.getBoardRoles());
            assertEquals(2, dto1.getBoardRoles().size());

            CommitteeBoardMemberDto role1 = dto1.getBoardRoles().getFirst();
            assertEquals(10L, role1.getId());
            assertEquals("Lead", role1.getRole());
            assertEquals(1, role1.getOrder());
            assertEquals(userId, role1.getUserId());
            assertEquals("Alice", role1.getUserName());
            assertEquals("http://alice.png", role1.getProfileImageUrl());
            assertEquals("http://linkedin.com/alice", role1.getLinkedinUrl());

            CommitteeBoardMemberDto role2 = dto1.getBoardRoles().get(1);
            assertEquals(11L, role2.getId());
            assertNull(role2.getUserId());
            assertNull(role2.getUserName());
            assertNull(role2.getProfileImageUrl());
            assertNull(role2.getLinkedinUrl());

            assertNull(dto2.getBoardRoles());
            verify(clubRepository).findAll();
            verify(clubBoardRepository).findByClubId(1L);
            verify(clubBoardRepository).findByClubId(2L);
        }
    }

    @Nested
    @DisplayName("getClubsByPage Tests")
    class GetClubsByPageTests {

        @Test
        @DisplayName("getClubsByPage should return paged clubs")
        void getClubsByPage_shouldReturnPagedClubs() {
            Club club = createClubSample();
            ClubCardDto dto = sampleClubCardDto();
            Page<Club> page = new PageImpl<>(List.of(club));

            when(clubRepository.findAll(any(PageRequest.class))).thenReturn(page);
            when(clubMapper.toClubCardDto(club)).thenReturn(dto);

            Page<ClubCardDto> result = clubService.getClubsByPage(0);

            assertEquals(1, result.getContent().size());
            assertEquals("CP Club", result.getContent().getFirst().getName());
            verify(clubRepository).findAll(any(PageRequest.class));
        }

        @Test
        @DisplayName("getClubsByPage with negative page number should clamp to 0")
        void getClubsByPage_negativePage_shouldClampToZero() {
            Club club = createClubSample();
            ClubCardDto dto = sampleClubCardDto();
            Page<Club> page = new PageImpl<>(List.of(club));

            when(clubRepository.findAll(argThat((PageRequest pr) -> pr.getPageNumber() == 0))).thenReturn(page);
            when(clubMapper.toClubCardDto(club)).thenReturn(dto);

            Page<ClubCardDto> result = clubService.getClubsByPage(-5);

            assertEquals(1, result.getContent().size());
            verify(clubRepository).findAll(argThat((PageRequest pr) -> pr.getPageNumber() == 0));
        }
    }

    @Nested
    @DisplayName("getClubById Tests")
    class GetClubByIdTests {

        @Test
        @DisplayName("getClubById found")
        void getClubById_found() {
            Club club = createClubSample();
            when(clubRepository.findById(50L)).thenReturn(Optional.of(club));

            Optional<Club> result = clubService.getClubById(50L);

            assertTrue(result.isPresent());
            assertEquals("CP Club", result.get().getName());
        }

        @Test
        @DisplayName("getClubById not found")
        void getClubById_notFound() {
            when(clubRepository.findById(1L)).thenReturn(Optional.empty());

            Optional<Club> result = clubService.getClubById(1L);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("createClub Tests")
    class CreateClubTests {

        @Test
        @DisplayName("createClub with null name should throw IllegalArgumentException")
        void createClub_nullName_shouldThrow() {
            Club club = createClubSample();
            club.setName(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> clubService.createClub(club));

            assertEquals("Club name is required", ex.getMessage());
            verify(clubRepository, never()).save(any());
        }

        @Test
        @DisplayName("createClub with blank name should throw IllegalArgumentException")
        void createClub_blankName_shouldThrow() {
            Club club = createClubSample();
            club.setName("   ");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> clubService.createClub(club));

            assertEquals("Club name is required", ex.getMessage());
            verify(clubRepository, never()).save(any());
        }

        @Test
        @DisplayName("createClub should save and announce when emails enabled")
        void createClub_shouldSave() {
            Club club = createClubSample();
            when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
            when(clubRepository.save(club)).thenReturn(club);

            Club saved = clubService.createClub(club);

            assertEquals("CP Club", saved.getName());
            assertNotNull(club.getAnnouncementSentAt());
        // Saved once on create, then again to record the announcement time
            verify(clubRepository, times(2)).save(club);
            verify(subscriptionService).sendNewClubNotificationToNewsSubscribers(club);
        }

        @Test
        @DisplayName("createClub announcement failure should still create without sentAt")
        void createClub_announcementFails_shouldStillCreateWithoutSentAt() {
            Club club = createClubSample();
            when(systemSettingsService.isEmailsEnabled()).thenReturn(true);
            when(clubRepository.save(club)).thenReturn(club);
            doThrow(new RuntimeException("db down")).when(subscriptionService).sendNewClubNotificationToNewsSubscribers(club);

            Club saved = clubService.createClub(club);

        // The club exists, but stays sendable because the announcement never went out
            assertSame(club, saved);
            assertNull(club.getAnnouncementSentAt());
            verify(clubRepository, times(1)).save(club);
        }

        @Test
        @DisplayName("createClub while emails locked should not announce")
        void createClub_whileEmailsLocked_shouldNotAnnounce() {
            Club club = createClubSample();
            when(systemSettingsService.isEmailsEnabled()).thenReturn(false);
            when(clubRepository.save(club)).thenReturn(club);

            clubService.createClub(club);

        // The announcement was never sent, so it must still be sendable later
            assertNull(club.getAnnouncementSentAt());
            verify(subscriptionService, never()).sendNewClubNotificationToNewsSubscribers(any());
        }

        @Test
        @DisplayName("createClub with sendAnnouncement false should skip announcement")
        void createClub_withSendAnnouncementFalse_shouldSkipAnnouncement() {
            Club club = createClubSample();
            club.setSendAnnouncement(false);
            when(clubRepository.save(club)).thenReturn(club);

            clubService.createClub(club);

            verify(clubRepository).save(club);
            verify(subscriptionService, never()).sendNewClubNotificationToNewsSubscribers(any());
        }
    }

    @Nested
    @DisplayName("getEmailSettings Tests")
    class GetEmailSettingsTests {

        @Test
        @DisplayName("getEmailSettings found should return EmailSettingsDto")
        void getEmailSettings_found_shouldReturnDto() {
            Club club = createClubSample();
            LocalDateTime sentAt = LocalDateTime.of(2026, 10, 1, 12, 0);
            club.setAnnouncementSentAt(sentAt);
            when(clubRepository.findById(50L)).thenReturn(Optional.of(club));

            EmailSettingsDto dto = clubService.getEmailSettings(50L);

            assertNotNull(dto);
            assertEquals(sentAt, dto.getAnnouncementSentAt());
        }

        @Test
        @DisplayName("getEmailSettings not found should throw ResourceNotFoundException")
        void getEmailSettings_notFound_shouldThrow() {
            when(clubRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> clubService.getEmailSettings(99L));
        }
    }

    @Nested
    @DisplayName("announceClub Tests")
    class AnnounceClubTests {

        @Test
        @DisplayName("announceClub when emails locked should throw EmailsLockedException")
        void announceClub_emailsLocked_shouldThrow() {
            doThrow(new EmailsLockedException()).when(systemSettingsService).assertEmailsEnabled();

            assertThrows(EmailsLockedException.class, () -> clubService.announceClub(50L, false));
            verify(clubRepository, never()).findById(any());
        }

        @Test
        @DisplayName("announceClub not found should throw ResourceNotFoundException")
        void announceClub_notFound_shouldThrow() {
            when(clubRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> clubService.announceClub(99L, false));
        }

        @Test
        @DisplayName("announceClub should notify subscribers on successful claim")
        void announceClub_shouldNotifySubscribers() {
            Club club = createClubSample();
            when(clubRepository.findById(1L)).thenReturn(Optional.of(club));
            when(clubRepository.claimAnnouncement(eq(1L), any())).thenReturn(1);

            clubService.announceClub(1L, false);

            verify(subscriptionService).sendNewClubNotificationToNewsSubscribers(club);
        }

        @Test
        @DisplayName("announceClub already sent should throw AnnouncementAlreadySentException")
        void announceClub_alreadySent_shouldRefuseWithoutNotifying() {
            Club club = createClubSample();
            LocalDateTime sentAt = LocalDateTime.of(2026, 9, 15, 10, 0);
            club.setAnnouncementSentAt(sentAt);
            when(clubRepository.findById(1L)).thenReturn(Optional.of(club));
            when(clubRepository.claimAnnouncement(eq(1L), any())).thenReturn(0);

            AnnouncementAlreadySentException ex = assertThrows(AnnouncementAlreadySentException.class,
                    () -> clubService.announceClub(1L, false));

            assertEquals(sentAt, ex.getSentAt());
            verify(subscriptionService, never()).sendNewClubNotificationToNewsSubscribers(any());
        }

        @Test
        @DisplayName("announceClub with force=true should resend and update sentAt")
        void announceClub_forceTrue_shouldResendAndUpdateSentAt() {
            Club club = createClubSample();
            LocalDateTime oldSent = LocalDateTime.of(2026, 8, 1, 10, 0);
            club.setAnnouncementSentAt(oldSent);
            when(clubRepository.findById(1L)).thenReturn(Optional.of(club));

            clubService.announceClub(1L, true);

            assertTrue(club.getAnnouncementSentAt().isAfter(oldSent));
            verify(clubRepository).save(club);
            verify(clubRepository, never()).claimAnnouncement(any(), any());
            verify(subscriptionService).sendNewClubNotificationToNewsSubscribers(club);
        }
    }

    @Nested
    @DisplayName("Question Management Tests")
    class QuestionManagementTests {

        @Test
        @DisplayName("createQuestion club not found should throw ResourceNotFoundException")
        void createQuestion_clubNotFound_shouldThrow() {
            when(clubRepository.findById(99L)).thenReturn(Optional.empty());

            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Question?")
                    .questionType("TEXT")
                    .build();

            assertThrows(ResourceNotFoundException.class, () -> clubService.createQuestion(99L, req));
        }

        @Test
        @DisplayName("createQuestion should save question and return response DTO")
        void createQuestion_shouldSaveClubQuestion() {
            Club club = createClubSample();
            FormQuestionRequestDto request = FormQuestionRequestDto.builder()
                    .questionText("Why do you want to join?")
                    .questionType("multiple_choice")
                    .isRequired(true)
                    .options(List.of("Practice", "  ", "Community"))
                    .build();

            when(clubRepository.findById(50L)).thenReturn(Optional.of(club));
            when(clubFormQuestionRepository.save(any(ClubFormQuestion.class))).thenAnswer(invocation -> {
                ClubFormQuestion question = invocation.getArgument(0);
                question.setId(30L);
                return question;
            });

            FormQuestionResponseDto result = clubService.createQuestion(50L, request);

            assertEquals(30L, result.getId());
            assertEquals("Why do you want to join?", result.getQuestionText());
            assertEquals(QuestionType.MULTIPLE_CHOICE.name(), result.getQuestionType());
            assertTrue(result.getIsRequired());
            verify(clubFormQuestionRepository).save(argThat(question ->
                    question.getClub() == club
                            && question.getQuestionType() == QuestionType.MULTIPLE_CHOICE
                            && question.getOptions().equals(List.of("Practice", "Community"))
            ));
        }

        @Test
        @DisplayName("updateQuestion question not found should throw ResourceNotFoundException")
        void updateQuestion_questionNotFound_shouldThrow() {
            when(clubFormQuestionRepository.findById(100L)).thenReturn(Optional.empty());

            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Updated?")
                    .questionType("TEXT")
                    .build();

            assertThrows(ResourceNotFoundException.class, () -> clubService.updateQuestion(50L, 100L, req));
        }

        @Test
        @DisplayName("updateQuestion club mismatch should throw ResourceNotFoundException")
        void updateQuestion_clubMismatch_shouldThrow() {
            Club otherClub = createClubSample();
            otherClub.setId(999L);
            ClubFormQuestion existingWithOtherClub = ClubFormQuestion.builder()
                    .id(100L)
                    .club(otherClub)
                    .questionText("Old text")
                    .build();

            when(clubFormQuestionRepository.findById(100L)).thenReturn(Optional.of(existingWithOtherClub));

            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("Updated?")
                    .questionType("TEXT")
                    .build();

            assertThrows(ResourceNotFoundException.class, () -> clubService.updateQuestion(50L, 100L, req));

            // Also test null club
            ClubFormQuestion existingWithNullClub = ClubFormQuestion.builder()
                    .id(101L)
                    .club(null)
                    .questionText("Old text")
                    .build();
            when(clubFormQuestionRepository.findById(101L)).thenReturn(Optional.of(existingWithNullClub));

            assertThrows(ResourceNotFoundException.class, () -> clubService.updateQuestion(50L, 101L, req));
        }

        @Test
        @DisplayName("updateQuestion success should update fields and save")
        void updateQuestion_success_shouldUpdateAndSave() {
            Club club = createClubSample();
            ClubFormQuestion existing = ClubFormQuestion.builder()
                    .id(100L)
                    .club(club)
                    .questionText("Old text")
                    .questionType(QuestionType.TEXT)
                    .isRequired(false)
                    .build();

            when(clubFormQuestionRepository.findById(100L)).thenReturn(Optional.of(existing));
            when(clubFormQuestionRepository.save(any(ClubFormQuestion.class))).thenAnswer(invocation -> invocation.getArgument(0));

            FormQuestionRequestDto req = FormQuestionRequestDto.builder()
                    .questionText("New text?")
                    .questionType("CHECKBOX")
                    .isRequired(true)
                    .options(List.of("Option A", "Option B"))
                    .build();

            FormQuestionResponseDto result = clubService.updateQuestion(50L, 100L, req);

            assertEquals(100L, result.getId());
            assertEquals("New text?", result.getQuestionText());
            assertEquals(QuestionType.CHECKBOX.name(), result.getQuestionType());
            assertTrue(result.getIsRequired());
            assertEquals(List.of("Option A", "Option B"), result.getOptions());
            verify(clubFormQuestionRepository).save(existing);
        }

        @Test
        @DisplayName("deleteQuestion question not found should throw ResourceNotFoundException")
        void deleteQuestion_questionNotFound_shouldThrow() {
            when(clubFormQuestionRepository.findById(100L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> clubService.deleteQuestion(50L, 100L));
        }

        @Test
        @DisplayName("deleteQuestion club mismatch should throw ResourceNotFoundException")
        void deleteQuestion_clubMismatch_shouldThrow() {
            Club otherClub = createClubSample();
            otherClub.setId(999L);
            ClubFormQuestion existingWithOther = ClubFormQuestion.builder()
                    .id(100L)
                    .club(otherClub)
                    .build();

            when(clubFormQuestionRepository.findById(100L)).thenReturn(Optional.of(existingWithOther));

            assertThrows(ResourceNotFoundException.class, () -> clubService.deleteQuestion(50L, 100L));

            // Also test null club
            ClubFormQuestion existingWithNull = ClubFormQuestion.builder()
                    .id(101L)
                    .club(null)
                    .build();
            when(clubFormQuestionRepository.findById(101L)).thenReturn(Optional.of(existingWithNull));

            assertThrows(ResourceNotFoundException.class, () -> clubService.deleteQuestion(50L, 101L));
            verify(clubFormQuestionRepository, never()).delete(any());
        }

        @Test
        @DisplayName("deleteQuestion success should delete entity")
        void deleteQuestion_success_shouldDelete() {
            Club club = createClubSample();
            ClubFormQuestion existing = ClubFormQuestion.builder()
                    .id(100L)
                    .club(club)
                    .build();

            when(clubFormQuestionRepository.findById(100L)).thenReturn(Optional.of(existing));

            clubService.deleteQuestion(50L, 100L);

            verify(clubFormQuestionRepository).delete(existing);
        }
    }

    @Nested
    @DisplayName("updateClub Tests")
    class UpdateClubTests {

        @Test
        @DisplayName("updateClub not found should throw RuntimeException")
        void updateClub_notFound_shouldThrow() {
            when(clubRepository.findById(50L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> clubService.updateClub(50L, new Club())
        );

            assertEquals("Club not found", ex.getMessage());
        }

        @Test
        @DisplayName("updateClub null name should throw IllegalArgumentException")
        void updateClub_nullName_shouldThrow() {
            Club existing = createClubSample();
            when(clubRepository.findById(50L)).thenReturn(Optional.of(existing));

            Club updateReq = new Club();
            updateReq.setName(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> clubService.updateClub(50L, updateReq));

            assertEquals("Club name is required", ex.getMessage());
            verify(clubRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateClub blank name should throw IllegalArgumentException")
        void updateClub_blankName_shouldThrow() {
            Club existing = createClubSample();
            when(clubRepository.findById(50L)).thenReturn(Optional.of(existing));

            Club updateReq = new Club();
            updateReq.setName("   ");

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> clubService.updateClub(50L, updateReq));

            assertEquals("Club name is required", ex.getMessage());
            verify(clubRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateClub success should update all fields and save")
        void updateClub_success_shouldUpdateFields() {
            Club existing = createClubSample();
            when(clubRepository.findById(50L)).thenReturn(Optional.of(existing));
            when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Club updateReq = new Club();
            updateReq.setName("Updated Club");
            updateReq.setDescription("Updated Desc");
            updateReq.setImageUrl("updated.png");
            updateReq.setSocialMediaLinks(List.of("https://facebook.com/club"));
            updateReq.setIsExternal(true);
            updateReq.setRegistrationOpen(true);

            Club result = clubService.updateClub(50L, updateReq);

            assertEquals("Updated Club", result.getName());
            assertEquals("Updated Desc", result.getDescription());
            assertEquals("updated.png", result.getImageUrl());
            assertEquals(List.of("https://facebook.com/club"), result.getSocialMediaLinks());
            assertTrue(result.getIsExternal());
            assertTrue(result.getRegistrationOpen());
            verify(clubRepository).save(existing);
        }
    }

    @Nested
    @DisplayName("openRegistration & closeRegistration Tests")
    class RegistrationStatusTests {

        @Test
        @DisplayName("openRegistration success should set registrationOpen to true")
        void openRegistration_success() {
            Club club = createClubSample();
            club.setRegistrationOpen(false);
            when(clubRepository.findById(50L)).thenReturn(Optional.of(club));
            when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Club result = clubService.openRegistration(50L);

            assertTrue(result.getRegistrationOpen());
            verify(clubRepository).save(club);
        }

        @Test
        @DisplayName("openRegistration not found should throw ResourceNotFoundException")
        void openRegistration_notFound_shouldThrow() {
            when(clubRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> clubService.openRegistration(99L));
        }

        @Test
        @DisplayName("closeRegistration success should set registrationOpen to false")
        void closeRegistration_success() {
            Club club = createClubSample();
            club.setRegistrationOpen(true);
            when(clubRepository.findById(50L)).thenReturn(Optional.of(club));
            when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

            Club result = clubService.closeRegistration(50L);

            assertFalse(result.getRegistrationOpen());
            verify(clubRepository).save(club);
        }

        @Test
        @DisplayName("closeRegistration not found should throw ResourceNotFoundException")
        void closeRegistration_notFound_shouldThrow() {
            when(clubRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> clubService.closeRegistration(99L));
        }
    }

    @Nested
    @DisplayName("Social Links Tests")
    class SocialLinksTests {

        @Test
        @DisplayName("getClubSocialLinks found should return list")
        void getClubSocialLinks_found() {
            Club club = createClubSample();
            club.setSocialMediaLinks(List.of("https://facebook.com", "https://twitter.com"));
            when(clubRepository.findById(50L)).thenReturn(Optional.of(club));

            List<String> result = clubService.getClubSocialLinks(50L);

            assertEquals(2, result.size());
            assertEquals("https://facebook.com", result.getFirst());
        }

        @Test
        @DisplayName("getClubSocialLinks null social media links should return empty list")
        void getClubSocialLinks_nullLinks_shouldReturnEmptyList() {
            Club club = createClubSample();
            club.setSocialMediaLinks(null);
            when(clubRepository.findById(50L)).thenReturn(Optional.of(club));

            List<String> result = clubService.getClubSocialLinks(50L);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("getClubSocialLinks not found should throw ResourceNotFoundException")
        void getClubSocialLinks_notFound_shouldThrow() {
            when(clubRepository.findById(50L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> clubService.getClubSocialLinks(50L));
        }

        @Test
        @DisplayName("updateClubSocialLinks should trim, filter blanks, and save")
        void updateClubSocialLinks_shouldSave() {
            Club club = createClubSample();
            when(clubRepository.findById(50L)).thenReturn(Optional.of(club));
            when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

            List<String> result = clubService.updateClubSocialLinks(50L,
                    Arrays.asList("https://facebook.com", null, "  ", "https://linkedin.com"));

            assertEquals(2, result.size());
            assertEquals("https://facebook.com", result.get(0));
            assertEquals("https://linkedin.com", result.get(1));
            verify(clubRepository).save(club);
        }

        @Test
        @DisplayName("updateClubSocialLinks null list should set empty list and save")
        void updateClubSocialLinks_nullList_shouldSaveEmpty() {
            Club club = createClubSample();
            when(clubRepository.findById(50L)).thenReturn(Optional.of(club));
            when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

            List<String> result = clubService.updateClubSocialLinks(50L, null);

            assertNotNull(result);
            assertTrue(result.isEmpty());
            assertEquals(Collections.emptyList(), club.getSocialMediaLinks());
            verify(clubRepository).save(club);
        }

        @Test
        @DisplayName("updateClubSocialLinks not found should throw ResourceNotFoundException")
        void updateClubSocialLinks_notFound_shouldThrow() {
            when(clubRepository.findById(50L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> clubService.updateClubSocialLinks(50L, List.of("https://facebook.com")));
        }
    }

    @Nested
    @DisplayName("deleteClubById Tests")
    class DeleteClubTests {

        @Test
        @DisplayName("deleteClubById should delete cascade references and club")
        void deleteClub_shouldCallRepository() {
            clubService.deleteClubById(1L);
            verify(clubRegistrationRepository).deleteByClubId(1L);
            verify(clubFormQuestionRepository).deleteByClubId(1L);
            verify(clubRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("Registration Analysis & Google Sheets Sync Tests")
    class RegistrationAnalysisTests {

        @Test
        @DisplayName("getRegistrationAnalysis not found should throw ResourceNotFoundException")
        void getRegistrationAnalysis_notFound_shouldThrow() {
            when(clubRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> clubService.getRegistrationAnalysis(99L));
        }

        @Test
        @DisplayName("getRegistrationAnalysis should compute statistics correctly across diverse registrations")
        void getRegistrationAnalysis_success_shouldComputeStats() {
            Club club = createClubSample();
            club.setGoogleSheetUrl("https://docs.google.com/sheet1");
            LocalDateTime sheetTime = LocalDateTime.of(2026, 9, 20, 15, 0);
            club.setSheetLastUpdatedAt(sheetTime);

            User userAlexCsed = new User();
            userAlexCsed.setIsAlexEngStudent(true);
            userAlexCsed.setDepartment(Department.CSED);
            userAlexCsed.setBatch("2026");

            User userNonAlexCce = new User();
            userNonAlexCce.setIsAlexEngStudent(false);
            userNonAlexCce.setDepartment(Department.CCE);
            userNonAlexCce.setBatch("2027");

            User userNullDeptAndBatch = new User();
            userNullDeptAndBatch.setIsAlexEngStudent(null);
            userNullDeptAndBatch.setDepartment(null);
            userNullDeptAndBatch.setBatch(null);

            User userBlankBatch = new User();
            userBlankBatch.setIsAlexEngStudent(false);
            userBlankBatch.setDepartment(Department.OTHER);
            userBlankBatch.setBatch("   ");

            ClubRegistration reg1 = ClubRegistration.builder().id(1L).user(userAlexCsed).club(club).build();
            ClubRegistration reg2 = ClubRegistration.builder().id(2L).user(userNonAlexCce).club(club).build();
            ClubRegistration reg3 = ClubRegistration.builder().id(3L).user(userNullDeptAndBatch).club(club).build();
            ClubRegistration reg4 = ClubRegistration.builder().id(4L).user(userBlankBatch).club(club).build();
            ClubRegistration reg5 = ClubRegistration.builder().id(5L).user(null).club(club).build();

            when(clubRepository.findById(50L)).thenReturn(Optional.of(club));
            when(clubRegistrationRepository.findByClubId(50L))
                    .thenReturn(List.of(reg1, reg2, reg3, reg4, reg5));

            RegistrationAnalysisDto analysis = clubService.getRegistrationAnalysis(50L);

            assertEquals(5, analysis.getTotalRegistrations());
            assertEquals(1L, analysis.getAlexUniStudentCount());
            assertEquals(4L, analysis.getNonAlexUniStudentCount());

            assertEquals(1L, analysis.getDepartmentCounts().get("CSED"));
            assertEquals(1L, analysis.getDepartmentCounts().get("CCE"));
            assertEquals(1L, analysis.getDepartmentCounts().get("OTHER"));
            assertEquals(1L, analysis.getDepartmentCounts().get("N/A"));

            assertEquals(1L, analysis.getBatchCounts().get("2026"));
            assertEquals(1L, analysis.getBatchCounts().get("2027"));
            assertEquals(2L, analysis.getBatchCounts().get("N/A"));

            assertEquals("https://docs.google.com/sheet1", analysis.getGoogleSheetUrl());
            assertEquals(sheetTime, analysis.getSheetLastUpdatedAt());
        }

        @Test
        @DisplayName("syncRegistrationsSheet not found should throw ResourceNotFoundException")
        void syncRegistrationsSheet_notFound_shouldThrow() {
            when(clubRepository.findById(99L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> clubService.syncRegistrationsSheet(99L));
        }

        @Test
        @DisplayName("syncRegistrationsSheet should invoke GoogleSheetsService and update sheet metadata")
        void syncRegistrationsSheet_success_shouldSync() {
            Club club = createClubSample();
            club.setGoogleSheetUrl("https://docs.google.com/old-url");

            ClubRegistration reg = ClubRegistration.builder().id(1L).club(club).build();
            ClubFormQuestion q = ClubFormQuestion.builder().id(10L).club(club).questionText("Why?").build();

            when(clubRepository.findById(50L)).thenReturn(Optional.of(club));
            when(clubRegistrationRepository.findByClubId(50L)).thenReturn(List.of(reg));
            when(clubFormQuestionRepository.findByClubId(50L)).thenReturn(List.of(q));
            when(googleSheetsService.syncRegistrationData(
                    any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn("https://docs.google.com/new-url");
            when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

            RegistrationAnalysisDto result = clubService.syncRegistrationsSheet(50L);

            assertNotNull(result);
            assertEquals("https://docs.google.com/new-url", club.getGoogleSheetUrl());
            assertNotNull(club.getSheetLastUpdatedAt());
            verify(clubRepository).save(club);
            verify(googleSheetsService).syncRegistrationData(
                    eq("ACM Alexandria - Club: CP Club - Registrations"),
                    any(),
                    eq("https://docs.google.com/old-url"),
                    eq(List.of("Club Name:", "CP Club")),
                    eq(List.of(reg)),
                    eq(List.of(q)),
                    any(), any(), any(), any(), any(), any()
            );
        }
    }
}
