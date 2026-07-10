package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.RadioEpisodeDto;
import com.acm.acmwebsite.feature.dto.RadioSeasonDto;
import com.acm.acmwebsite.feature.entity.RadioEpisode;
import com.acm.acmwebsite.feature.entity.RadioSeason;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Component
public class RadioMapper {

    public RadioEpisodeDto toEpisodeDto(RadioEpisode episode) {
        if (episode == null) return null;
        RadioEpisodeDto dto = new RadioEpisodeDto();
        dto.setId(episode.getId());
        dto.setEpisodeNumber(episode.getEpisodeNumber());
        dto.setTitle(episode.getTitle());
        dto.setImageUrl(episode.getImageUrl());
        dto.setUrl(episode.getUrl());
        dto.setGuest(episode.getGuest());
        dto.setHost(episode.getHost());
        if (episode.getRadioSeason() != null) {
            dto.setRadioSeasonId(episode.getRadioSeason().getId());
        }
        return dto;
    }

    public RadioEpisode toEpisode(RadioEpisodeDto dto, RadioSeason season) {
        if (dto == null) return null;
        RadioEpisode episode = new RadioEpisode();
        episode.setId(dto.getId());
        episode.setEpisodeNumber(dto.getEpisodeNumber());
        episode.setTitle(dto.getTitle());
        episode.setImageUrl(dto.getImageUrl());
        episode.setUrl(dto.getUrl());
        episode.setGuest(dto.getGuest());
        episode.setHost(dto.getHost());
        episode.setRadioSeason(season);
        return episode;
    }

    public RadioSeasonDto toSeasonDto(RadioSeason season) {
        if (season == null) return null;
        RadioSeasonDto dto = new RadioSeasonDto();
        dto.setId(season.getId());
        dto.setSeasonNumber(season.getSeasonNumber());
        dto.setImageUrl(season.getImageUrl());
        dto.setCreatedAt(season.getCreatedAt());
        if (season.getEpisodes() != null) {
            dto.setEpisodes(season.getEpisodes().stream()
                    .map(this::toEpisodeDto)
                    .collect(Collectors.toList()));
        } else {
            dto.setEpisodes(new ArrayList<>());
        }
        return dto;
    }

    public RadioSeason toSeason(RadioSeasonDto dto) {
        if (dto == null) return null;
        RadioSeason season = new RadioSeason();
        season.setId(dto.getId());
        season.setSeasonNumber(dto.getSeasonNumber());
        season.setImageUrl(dto.getImageUrl());
        return season;
    }
}
