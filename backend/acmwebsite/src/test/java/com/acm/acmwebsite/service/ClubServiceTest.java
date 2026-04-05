package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.repository.ClubRepository;
import com.acm.acmwebsite.feature.service.ClubService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.w3c.dom.stylesheets.LinkStyle;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ClubServiceTest {
    @Mock
    private ClubRepository clubRepository;

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

    @Test
    void getAllClubs_returnList(){
        List<Club> clubs = List.of(createClubSample());
        when (clubRepository.findAll())
                .thenReturn(clubs);
        List<Club> result = clubService.getAllClubs();
        assertEquals(1,result.size());
        verify(clubRepository).findAll();
    }

    @Test
    void getClubById_found() {
        Club club = createClubSample();
        when (clubRepository.findById(50L)).thenReturn(Optional.of(club));
        Optional<Club> result = clubService.getClubById(50L);
        assertTrue(result.isPresent());
        assertEquals("CP Club", result.get().getName());
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
