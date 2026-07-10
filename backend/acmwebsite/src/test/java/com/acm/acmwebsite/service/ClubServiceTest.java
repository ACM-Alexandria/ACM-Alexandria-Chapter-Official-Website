package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.dto.ClubCardDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.entity.ClubFormQuestion;
import com.acm.acmwebsite.feature.enums.QuestionType;
import com.acm.acmwebsite.feature.mapper.ClubMapper;
import com.acm.acmwebsite.feature.repository.ClubRepository;
import com.acm.acmwebsite.feature.repository.ClubRegistrationRepository;
import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.repository.ClubFormQuestionRepository;
import com.acm.acmwebsite.feature.service.ClubService;
import com.acm.acmwebsite.feature.service.GoogleSheetsService;
import com.acm.acmwebsite.feature.service.SubscriptionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

    @InjectMocks
    private ClubService clubService;

    private Club createClubSample(){
        Club club = new Club();
        club.setId(50L);
        club.setName("CP Club");
        club.setDescription("Programming club");
        club.setImageUrl("img");
        return club;
    }
    private ClubCardDto sampleClubCardDto() {
        return new ClubCardDto(1L, "CP Club", "img", "Programming club");
    }

    @Test
    void getClubsByPage_shouldReturnPagedClubs() {
        Club club = createClubSample();
        ClubCardDto dto = sampleClubCardDto();
        Page<Club> page = new PageImpl<>(List.of(club));

        when(clubRepository.findAll(any(PageRequest.class))).thenReturn(page);
        when(clubMapper.toClubCardDto(club)).thenReturn(dto);

        Page<ClubCardDto> result = clubService.getClubsByPage(0);

        assertEquals(1, result.getContent().size());
        assertEquals("CP Club", result.getContent().get(0).getName());
        verify(clubRepository).findAll(any(PageRequest.class));
    }

    @Test
    void getClubById_found() {
        Club club = createClubSample();
        when(clubRepository.findById(50L)).thenReturn(Optional.of(club));

        Optional<Club> result = clubService.getClubById(50L);

        assertTrue(result.isPresent());
        assertEquals("CP Club", result.get().getName());
    }

    @Test
    void getClubById_notFound() {
        when(clubRepository.findById(1L)).thenReturn(Optional.empty());

        Optional<Club> result = clubService.getClubById(1L);

        assertTrue(result.isEmpty());
    }

    @Test
    void createClub_shouldSave() {
        Club club = createClubSample();
        when(clubRepository.save(club)).thenReturn(club);

        Club saved = clubService.createClub(club);

        assertEquals("CP Club", saved.getName());
        verify(clubRepository).save(club);
        verify(subscriptionService).sendNewClubNotificationToNewsSubscribers(club);
    }

    @Test
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
    void updateClub_notFound_shouldThrow() {
        when(clubRepository.findById(50L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(
                RuntimeException.class,
                () -> clubService.updateClub(50L, new Club())
        );

        assertEquals("Club not found", ex.getMessage());
    }
    @Test
    void deleteClub_shouldCallRepository() {
        clubService.deleteClubById(1L);
        verify(clubRegistrationRepository).deleteByClubId(1L);
        verify(clubFormQuestionRepository).deleteByClubId(1L);
        verify(clubRepository).deleteById(1L);
    }

    @Test
    void getClubSocialLinks_found() {
        Club club = createClubSample();
        club.setSocialMediaLinks(List.of("https://facebook.com", "https://twitter.com"));
        when(clubRepository.findById(50L)).thenReturn(Optional.of(club));

        List<String> result = clubService.getClubSocialLinks(50L);

        assertEquals(2, result.size());
        assertEquals("https://facebook.com", result.get(0));
    }

    @Test
    void getClubSocialLinks_notFound_shouldThrow() {
        when(clubRepository.findById(50L)).thenReturn(Optional.empty());

        assertThrows(
                com.acm.acmwebsite.feature.exception.ResourceNotFoundException.class,
                () -> clubService.getClubSocialLinks(50L)
        );
    }

    @Test
    void updateClubSocialLinks_shouldSave() {
        Club club = createClubSample();
        when(clubRepository.findById(50L)).thenReturn(Optional.of(club));
        when(clubRepository.save(any(Club.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<String> result = clubService.updateClubSocialLinks(50L, List.of("https://facebook.com", "  ", "https://linkedin.com"));

        assertEquals(2, result.size());
        assertEquals("https://facebook.com", result.get(0));
        assertEquals("https://linkedin.com", result.get(1));
        verify(clubRepository).save(club);
    }

    @Test
    void updateClubSocialLinks_notFound_shouldThrow() {
        when(clubRepository.findById(50L)).thenReturn(Optional.empty());

        assertThrows(
                com.acm.acmwebsite.feature.exception.ResourceNotFoundException.class,
                () -> clubService.updateClubSocialLinks(50L, List.of("https://facebook.com"))
        );
    }
}
