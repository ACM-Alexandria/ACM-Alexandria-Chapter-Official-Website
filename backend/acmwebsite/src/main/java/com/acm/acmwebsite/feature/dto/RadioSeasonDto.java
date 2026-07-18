package com.acm.acmwebsite.feature.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class RadioSeasonDto {
    private Long id;
    private Integer seasonNumber;
    private String imageUrl;
    private LocalDateTime createdAt;
    private List<RadioEpisodeDto> episodes = new ArrayList<>();

    public RadioSeasonDto() {
    }

    public RadioSeasonDto(Long id, Integer seasonNumber, String imageUrl, LocalDateTime createdAt, List<RadioEpisodeDto> episodes) {
        this.id = id;
        this.seasonNumber = seasonNumber;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
        this.episodes = episodes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(Integer seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<RadioEpisodeDto> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<RadioEpisodeDto> episodes) {
        this.episodes = episodes;
    }
}
