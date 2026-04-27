package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.dto.ClubCardDto;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.mapper.ClubMapper;
import com.acm.acmwebsite.feature.repository.ClubRepository;
import com.acm.acmwebsite.feature.service.ClubService;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClubServiceTest {

    @Mock
    private ClubRepository clubRepository;

    @Mock
    private ClubMapper clubMapper;

    @InjectMocks
    private ClubService clubService;

    private Club createClubSample(){
        Club club = new Club();
        club.setId(50L);
        club.setName("CP Club");
        club.setDescription("Programming club");
        club.setImageUrl("img");
        club.setGoogleFormUrl("form");
        return club;
    }
    private ClubCardDto sampleClubCardDto() {
        ClubCardDto dto = new ClubCardDto();
        dto.setId(1L);
        dto.setName("CP Club");
        dto.setImageUrl("img");
        return dto;
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
        verify(clubRepository).deleteById(1L);
    }
}
