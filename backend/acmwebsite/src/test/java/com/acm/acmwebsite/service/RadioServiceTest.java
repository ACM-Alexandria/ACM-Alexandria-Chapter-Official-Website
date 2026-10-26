package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.dto.RadioEpisodeDto;
import com.acm.acmwebsite.feature.dto.RadioSeasonDto;
import com.acm.acmwebsite.feature.entity.RadioEpisode;
import com.acm.acmwebsite.feature.entity.RadioSeason;
import com.acm.acmwebsite.feature.exception.ResourceNotFoundException;
import com.acm.acmwebsite.feature.mapper.RadioMapper;
import com.acm.acmwebsite.feature.repository.RadioEpisodeRepository;
import com.acm.acmwebsite.feature.repository.RadioSeasonRepository;
import com.acm.acmwebsite.feature.service.RadioService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RadioServiceTest {

    @Mock
    private RadioSeasonRepository radioSeasonRepository;

    @Mock
    private RadioEpisodeRepository radioEpisodeRepository;

    @Spy
    private RadioMapper radioMapper = new RadioMapper();

    @InjectMocks
    private RadioService radioService;

    private RadioSeason sampleSeason;
    private RadioEpisode sampleEpisode;

    @BeforeEach
    void setUp() {
        sampleSeason = new RadioSeason();
        sampleSeason.setId(1L);
        sampleSeason.setSeasonNumber(1);
        sampleSeason.setImageUrl("https://example.com/season1.png");
        sampleSeason.setEpisodes(new ArrayList<>());

        sampleEpisode = new RadioEpisode();
        sampleEpisode.setId(10L);
        sampleEpisode.setEpisodeNumber(1);
        sampleEpisode.setTitle("Tech in 2026");
        sampleEpisode.setUrl("https://spotify.com/ep1");
        sampleEpisode.setImageUrl("https://example.com/ep1.png");
        sampleEpisode.setGuest("Dr. Smith");
        sampleEpisode.setHost("Alice");
        sampleEpisode.setRadioSeason(sampleSeason);
    }

    @Nested
    @DisplayName("Season Tests")
    class SeasonTests {

        @Test
        @DisplayName("getSeasonsByPage - clamps negative page number to 0 and sorts by seasonNumber desc")
        void getSeasonsByPage_clampsNegativePage() {
            Page<RadioSeason> page = new PageImpl<>(List.of(sampleSeason));
            when(radioSeasonRepository.findAll(any(Pageable.class))).thenReturn(page);

            Page<RadioSeasonDto> result = radioService.getSeasonsByPage(-5);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(radioSeasonRepository).findAll(captor.capture());
            Pageable pageable = captor.getValue();
            assertEquals(0, pageable.getPageNumber());
            assertEquals(4, pageable.getPageSize());
            assertTrue(pageable.getSort().getOrderFor("seasonNumber").isDescending());
        }

        @Test
        @DisplayName("getSeasonById - returns mapped DTO when season found")
        void getSeasonById_found() {
            when(radioSeasonRepository.findById(1L)).thenReturn(Optional.of(sampleSeason));

            Optional<RadioSeasonDto> result = radioService.getSeasonById(1L);

            assertTrue(result.isPresent());
            assertEquals(1L, result.get().getId());
            assertEquals(1, result.get().getSeasonNumber());
        }

        @Test
        @DisplayName("getSeasonById - returns empty optional when season not found")
        void getSeasonById_notFound() {
            when(radioSeasonRepository.findById(99L)).thenReturn(Optional.empty());

            Optional<RadioSeasonDto> result = radioService.getSeasonById(99L);

            assertTrue(result.isEmpty());
        }

        @Test
        @DisplayName("createSeason - throws IllegalArgumentException when seasonNumber is null")
        void createSeason_nullSeasonNumber_throws() {
            RadioSeasonDto dto = new RadioSeasonDto();
            dto.setSeasonNumber(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> radioService.createSeason(dto));
            assertEquals("Season number is required", ex.getMessage());
            verify(radioSeasonRepository, never()).save(any());
        }

        @Test
        @DisplayName("createSeason - throws IllegalArgumentException when seasonNumber already exists")
        void createSeason_duplicateSeasonNumber_throws() {
            RadioSeasonDto dto = new RadioSeasonDto();
            dto.setSeasonNumber(1);

            when(radioSeasonRepository.findBySeasonNumber(1)).thenReturn(Optional.of(sampleSeason));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> radioService.createSeason(dto));
            assertEquals("Season number already exists: 1", ex.getMessage());
            verify(radioSeasonRepository, never()).save(any());
        }

        @Test
        @DisplayName("createSeason - success saves and returns season DTO")
        void createSeason_success() {
            RadioSeasonDto dto = new RadioSeasonDto();
            dto.setSeasonNumber(2);
            dto.setImageUrl("https://example.com/s2.png");

            when(radioSeasonRepository.findBySeasonNumber(2)).thenReturn(Optional.empty());
            when(radioSeasonRepository.save(any(RadioSeason.class))).thenAnswer(i -> {
                RadioSeason s = i.getArgument(0);
                s.setId(2L);
                return s;
            });

            RadioSeasonDto result = radioService.createSeason(dto);

            assertNotNull(result);
            assertEquals(2L, result.getId());
            assertEquals(2, result.getSeasonNumber());
            verify(radioSeasonRepository).save(any(RadioSeason.class));
        }

        @Test
        @DisplayName("updateSeason - throws IllegalArgumentException when seasonNumber is null")
        void updateSeason_nullSeasonNumber_throws() {
            RadioSeasonDto dto = new RadioSeasonDto();
            dto.setSeasonNumber(null);

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> radioService.updateSeason(1L, dto));
            assertEquals("Season number is required", ex.getMessage());
        }

        @Test
        @DisplayName("updateSeason - throws ResourceNotFoundException when season id not found")
        void updateSeason_notFound_throws() {
            RadioSeasonDto dto = new RadioSeasonDto();
            dto.setSeasonNumber(2);

            when(radioSeasonRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> radioService.updateSeason(99L, dto));
            assertEquals("Season not found with id: 99", ex.getMessage());
        }

        @Test
        @DisplayName("updateSeason - throws IllegalArgumentException when updated seasonNumber already exists on another season")
        void updateSeason_duplicateSeasonNumber_throws() {
            RadioSeason existing = new RadioSeason();
            existing.setId(1L);
            existing.setSeasonNumber(1);

            RadioSeason other = new RadioSeason();
            other.setId(2L);
            other.setSeasonNumber(2);

            RadioSeasonDto dto = new RadioSeasonDto();
            dto.setSeasonNumber(2);

            when(radioSeasonRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(radioSeasonRepository.findBySeasonNumber(2)).thenReturn(Optional.of(other));

            IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                    () -> radioService.updateSeason(1L, dto));
            assertEquals("Season number already exists: 2", ex.getMessage());
            verify(radioSeasonRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateSeason - success when seasonNumber is changed to an available number")
        void updateSeason_changedSeasonNumber_success() {
            RadioSeason existing = new RadioSeason();
            existing.setId(1L);
            existing.setSeasonNumber(1);

            RadioSeasonDto dto = new RadioSeasonDto();
            dto.setSeasonNumber(3);
            dto.setImageUrl("https://example.com/s3.png");

            when(radioSeasonRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(radioSeasonRepository.findBySeasonNumber(3)).thenReturn(Optional.empty());
            when(radioSeasonRepository.save(existing)).thenReturn(existing);

            RadioSeasonDto result = radioService.updateSeason(1L, dto);

            assertNotNull(result);
            assertEquals(3, result.getSeasonNumber());
            assertEquals("https://example.com/s3.png", result.getImageUrl());
            verify(radioSeasonRepository).save(existing);
        }

        @Test
        @DisplayName("updateSeason - success when seasonNumber is unchanged (does not check duplicate)")
        void updateSeason_unchangedSeasonNumber_success() {
            RadioSeason existing = new RadioSeason();
            existing.setId(1L);
            existing.setSeasonNumber(1);

            RadioSeasonDto dto = new RadioSeasonDto();
            dto.setSeasonNumber(1);
            dto.setImageUrl("https://example.com/s1-updated.png");

            when(radioSeasonRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(radioSeasonRepository.save(existing)).thenReturn(existing);

            RadioSeasonDto result = radioService.updateSeason(1L, dto);

            assertNotNull(result);
            assertEquals(1, result.getSeasonNumber());
            assertEquals("https://example.com/s1-updated.png", result.getImageUrl());
            verify(radioSeasonRepository, never()).findBySeasonNumber(any());
            verify(radioSeasonRepository).save(existing);
        }

        @Test
        @DisplayName("deleteSeason - throws ResourceNotFoundException when season does not exist")
        void deleteSeason_notFound_throws() {
            when(radioSeasonRepository.existsById(99L)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> radioService.deleteSeason(99L));
            assertEquals("Season not found with id: 99", ex.getMessage());
            verify(radioSeasonRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("deleteSeason - success deletes by id")
        void deleteSeason_success() {
            when(radioSeasonRepository.existsById(1L)).thenReturn(true);

            radioService.deleteSeason(1L);

            verify(radioSeasonRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("Episode Tests")
    class EpisodeTests {

        @Test
        @DisplayName("getEpisodesBySeason - clamps negative page and sorts by episodeNumber asc")
        void getEpisodesBySeason_clampsNegativePage() {
            Page<RadioEpisode> page = new PageImpl<>(List.of(sampleEpisode));
            when(radioEpisodeRepository.findByRadioSeasonId(eq(1L), any(Pageable.class))).thenReturn(page);

            Page<RadioEpisodeDto> result = radioService.getEpisodesBySeason(1L, -2);

            assertNotNull(result);
            assertEquals(1, result.getContent().size());

            ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
            verify(radioEpisodeRepository).findByRadioSeasonId(eq(1L), captor.capture());
            Pageable pageable = captor.getValue();
            assertEquals(0, pageable.getPageNumber());
            assertEquals(5, pageable.getPageSize());
            assertTrue(pageable.getSort().getOrderFor("episodeNumber").isAscending());
        }

        @Test
        @DisplayName("createEpisode - validation failures for missing fields")
        void createEpisode_validations() {
            RadioEpisodeDto dto = new RadioEpisodeDto();

            // Null season ID
            IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> radioService.createEpisode(dto));
            assertEquals("Season ID is required", ex1.getMessage());

            // Null episode number
            dto.setRadioSeasonId(1L);
            IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> radioService.createEpisode(dto));
            assertEquals("Episode number is required", ex2.getMessage());

            // Null and blank title
            dto.setEpisodeNumber(1);
            IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class, () -> radioService.createEpisode(dto));
            assertEquals("Episode title is required", ex3.getMessage());
            dto.setTitle("   ");
            IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class, () -> radioService.createEpisode(dto));
            assertEquals("Episode title is required", ex4.getMessage());

            // Null and blank url
            dto.setTitle("Title");
            IllegalArgumentException ex5 = assertThrows(IllegalArgumentException.class, () -> radioService.createEpisode(dto));
            assertEquals("Episode URL is required", ex5.getMessage());
            dto.setUrl("   ");
            IllegalArgumentException ex6 = assertThrows(IllegalArgumentException.class, () -> radioService.createEpisode(dto));
            assertEquals("Episode URL is required", ex6.getMessage());

            // Null and blank imageUrl
            dto.setUrl("https://url.com");
            IllegalArgumentException ex7 = assertThrows(IllegalArgumentException.class, () -> radioService.createEpisode(dto));
            assertEquals("Episode image URL is required", ex7.getMessage());
            dto.setImageUrl("   ");
            IllegalArgumentException ex8 = assertThrows(IllegalArgumentException.class, () -> radioService.createEpisode(dto));
            assertEquals("Episode image URL is required", ex8.getMessage());
        }

        @Test
        @DisplayName("createEpisode - throws ResourceNotFoundException when season does not exist")
        void createEpisode_seasonNotFound_throws() {
            RadioEpisodeDto dto = new RadioEpisodeDto();
            dto.setRadioSeasonId(99L);
            dto.setEpisodeNumber(1);
            dto.setTitle("Episode 1");
            dto.setUrl("https://spotify.com/ep1");
            dto.setImageUrl("https://example.com/ep1.png");

            when(radioSeasonRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> radioService.createEpisode(dto));
            assertEquals("Season not found with id: 99", ex.getMessage());
            verify(radioEpisodeRepository, never()).save(any());
        }

        @Test
        @DisplayName("createEpisode - success saves and returns episode DTO")
        void createEpisode_success() {
            RadioEpisodeDto dto = new RadioEpisodeDto();
            dto.setRadioSeasonId(1L);
            dto.setEpisodeNumber(2);
            dto.setTitle("Future of AI");
            dto.setUrl("https://spotify.com/ep2");
            dto.setImageUrl("https://example.com/ep2.png");
            dto.setGuest("Bob");
            dto.setHost("Alice");

            when(radioSeasonRepository.findById(1L)).thenReturn(Optional.of(sampleSeason));
            when(radioEpisodeRepository.save(any(RadioEpisode.class))).thenAnswer(i -> {
                RadioEpisode ep = i.getArgument(0);
                ep.setId(20L);
                return ep;
            });

            RadioEpisodeDto result = radioService.createEpisode(dto);

            assertNotNull(result);
            assertEquals(20L, result.getId());
            assertEquals("Future of AI", result.getTitle());
            assertEquals(1L, result.getRadioSeasonId());
            verify(radioEpisodeRepository).save(any(RadioEpisode.class));
        }

        @Test
        @DisplayName("updateEpisode - validation failures for missing fields")
        void updateEpisode_validations() {
            RadioEpisodeDto dto = new RadioEpisodeDto();

            // Null episode number
            IllegalArgumentException ex1 = assertThrows(IllegalArgumentException.class, () -> radioService.updateEpisode(10L, dto));
            assertEquals("Episode number is required", ex1.getMessage());

            // Null and blank title
            dto.setEpisodeNumber(1);
            IllegalArgumentException ex2 = assertThrows(IllegalArgumentException.class, () -> radioService.updateEpisode(10L, dto));
            assertEquals("Episode title is required", ex2.getMessage());
            dto.setTitle("   ");
            IllegalArgumentException ex3 = assertThrows(IllegalArgumentException.class, () -> radioService.updateEpisode(10L, dto));
            assertEquals("Episode title is required", ex3.getMessage());

            // Null and blank url
            dto.setTitle("Title");
            IllegalArgumentException ex4 = assertThrows(IllegalArgumentException.class, () -> radioService.updateEpisode(10L, dto));
            assertEquals("Episode URL is required", ex4.getMessage());
            dto.setUrl("   ");
            IllegalArgumentException ex5 = assertThrows(IllegalArgumentException.class, () -> radioService.updateEpisode(10L, dto));
            assertEquals("Episode URL is required", ex5.getMessage());

            // Null and blank imageUrl
            dto.setUrl("https://url.com");
            IllegalArgumentException ex6 = assertThrows(IllegalArgumentException.class, () -> radioService.updateEpisode(10L, dto));
            assertEquals("Episode image URL is required", ex6.getMessage());
            dto.setImageUrl("   ");
            IllegalArgumentException ex7 = assertThrows(IllegalArgumentException.class, () -> radioService.updateEpisode(10L, dto));
            assertEquals("Episode image URL is required", ex7.getMessage());
        }

        @Test
        @DisplayName("updateEpisode - throws ResourceNotFoundException when episode not found")
        void updateEpisode_notFound_throws() {
            RadioEpisodeDto dto = new RadioEpisodeDto();
            dto.setEpisodeNumber(1);
            dto.setTitle("Episode 1");
            dto.setUrl("https://spotify.com/ep1");
            dto.setImageUrl("https://example.com/ep1.png");

            when(radioEpisodeRepository.findById(99L)).thenReturn(Optional.empty());

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> radioService.updateEpisode(99L, dto));
            assertEquals("Episode not found with id: 99", ex.getMessage());
            verify(radioEpisodeRepository, never()).save(any());
        }

        @Test
        @DisplayName("updateEpisode - success updates fields and returns episode DTO")
        void updateEpisode_success() {
            RadioEpisode existing = new RadioEpisode();
            existing.setId(10L);
            existing.setEpisodeNumber(1);
            existing.setTitle("Old Title");
            existing.setRadioSeason(sampleSeason);

            RadioEpisodeDto dto = new RadioEpisodeDto();
            dto.setEpisodeNumber(5);
            dto.setTitle("New Title");
            dto.setUrl("https://spotify.com/new");
            dto.setImageUrl("https://example.com/new.png");
            dto.setGuest("New Guest");
            dto.setHost("New Host");

            when(radioEpisodeRepository.findById(10L)).thenReturn(Optional.of(existing));
            when(radioEpisodeRepository.save(existing)).thenReturn(existing);

            RadioEpisodeDto result = radioService.updateEpisode(10L, dto);

            assertNotNull(result);
            assertEquals(5, result.getEpisodeNumber());
            assertEquals("New Title", result.getTitle());
            assertEquals("https://spotify.com/new", result.getUrl());
            assertEquals("https://example.com/new.png", result.getImageUrl());
            assertEquals("New Guest", result.getGuest());
            assertEquals("New Host", result.getHost());
            verify(radioEpisodeRepository).save(existing);
        }

        @Test
        @DisplayName("deleteEpisode - throws ResourceNotFoundException when episode does not exist")
        void deleteEpisode_notFound_throws() {
            when(radioEpisodeRepository.existsById(99L)).thenReturn(false);

            ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class,
                    () -> radioService.deleteEpisode(99L));
            assertEquals("Episode not found with id: 99", ex.getMessage());
            verify(radioEpisodeRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("deleteEpisode - success deletes by id")
        void deleteEpisode_success() {
            when(radioEpisodeRepository.existsById(10L)).thenReturn(true);

            radioService.deleteEpisode(10L);

            verify(radioEpisodeRepository).deleteById(10L);
        }
    }
}
