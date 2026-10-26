package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.RadioEpisodeDto;
import com.acm.acmwebsite.feature.dto.RadioSeasonDto;
import com.acm.acmwebsite.feature.entity.RadioEpisode;
import com.acm.acmwebsite.feature.entity.RadioSeason;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RadioMapperTest {

    private RadioMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new RadioMapper();
    }

    @Test
    @DisplayName("Should map RadioEpisode to RadioEpisodeDto")
    void testToEpisodeDto() {
        RadioSeason season = new RadioSeason();
        season.setId(1L);

        RadioEpisode episode = new RadioEpisode();
        episode.setId(10L);
        episode.setEpisodeNumber(1);
        episode.setTitle("Pilot Episode");
        episode.setImageUrl("https://image.com/ep1.png");
        episode.setUrl("https://spotify.com/ep1");
        episode.setGuest("Dr. Smith");
        episode.setHost("Alice");
        episode.setRadioSeason(season);

        RadioEpisodeDto dto = mapper.toEpisodeDto(episode);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getEpisodeNumber()).isEqualTo(1);
        assertThat(dto.getTitle()).isEqualTo("Pilot Episode");
        assertThat(dto.getImageUrl()).isEqualTo("https://image.com/ep1.png");
        assertThat(dto.getUrl()).isEqualTo("https://spotify.com/ep1");
        assertThat(dto.getGuest()).isEqualTo("Dr. Smith");
        assertThat(dto.getHost()).isEqualTo("Alice");
        assertThat(dto.getRadioSeasonId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return null when episode is null")
    void testToEpisodeDtoNull() {
        assertThat(mapper.toEpisodeDto(null)).isNull();
    }

    @Test
    @DisplayName("Should map RadioEpisodeDto to RadioEpisode entity with season")
    void testToEpisode() {
        RadioSeason season = new RadioSeason();
        season.setId(2L);

        RadioEpisodeDto dto = new RadioEpisodeDto();
        dto.setId(20L);
        dto.setEpisodeNumber(2);
        dto.setTitle("Episode 2");
        dto.setImageUrl("https://image.com/ep2.png");
        dto.setUrl("https://spotify.com/ep2");
        dto.setGuest("Jane");
        dto.setHost("Bob");

        RadioEpisode entity = mapper.toEpisode(dto, season);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(20L);
        assertThat(entity.getEpisodeNumber()).isEqualTo(2);
        assertThat(entity.getTitle()).isEqualTo("Episode 2");
        assertThat(entity.getImageUrl()).isEqualTo("https://image.com/ep2.png");
        assertThat(entity.getUrl()).isEqualTo("https://spotify.com/ep2");
        assertThat(entity.getGuest()).isEqualTo("Jane");
        assertThat(entity.getHost()).isEqualTo("Bob");
        assertThat(entity.getRadioSeason()).isEqualTo(season);
    }

    @Test
    @DisplayName("Should return null when episode DTO is null")
    void testToEpisodeNull() {
        assertThat(mapper.toEpisode(null, new RadioSeason())).isNull();
    }

    @Test
    @DisplayName("Should map RadioSeason to RadioSeasonDto with episodes")
    void testToSeasonDto() {
        RadioSeason season = new RadioSeason();
        season.setId(3L);
        season.setSeasonNumber(1);
        season.setImageUrl("https://image.com/season1.png");
        LocalDateTime created = LocalDateTime.now();
        season.setCreatedAt(created);

        RadioEpisode ep = new RadioEpisode();
        ep.setId(30L);
        ep.setTitle("Ep 1");
        season.setEpisodes(List.of(ep));

        RadioSeasonDto dto = mapper.toSeasonDto(season);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(3L);
        assertThat(dto.getSeasonNumber()).isEqualTo(1);
        assertThat(dto.getImageUrl()).isEqualTo("https://image.com/season1.png");
        assertThat(dto.getCreatedAt()).isEqualTo(created);
        assertThat(dto.getEpisodes()).hasSize(1);
        assertThat(dto.getEpisodes().get(0).getTitle()).isEqualTo("Ep 1");
    }

    @Test
    @DisplayName("Should return null when season is null")
    void testToSeasonDtoNull() {
        assertThat(mapper.toSeasonDto(null)).isNull();
    }

    @Test
    @DisplayName("Should map RadioSeasonDto to RadioSeason entity")
    void testToSeason() {
        RadioSeasonDto dto = new RadioSeasonDto();
        dto.setId(4L);
        dto.setSeasonNumber(2);
        dto.setImageUrl("https://image.com/s2.png");

        RadioSeason entity = mapper.toSeason(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(4L);
        assertThat(entity.getSeasonNumber()).isEqualTo(2);
        assertThat(entity.getImageUrl()).isEqualTo("https://image.com/s2.png");
    }

    @Test
    @DisplayName("Should return null when season DTO is null")
    void testToSeasonNull() {
        assertThat(mapper.toSeason(null)).isNull();
    }
}
