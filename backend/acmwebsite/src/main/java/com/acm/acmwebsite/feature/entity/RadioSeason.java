package com.acm.acmwebsite.feature.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "radio_season")
public class RadioSeason {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "season_number", nullable = false, unique = true)
    private Integer seasonNumber;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "radioSeason", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RadioEpisode> episodes = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public RadioSeason() {
    }

    public RadioSeason(Long id, Integer seasonNumber, String imageUrl) {
        this.id = id;
        this.seasonNumber = seasonNumber;
        this.imageUrl = imageUrl;
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

    public List<RadioEpisode> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<RadioEpisode> episodes) {
        this.episodes = episodes;
    }
}
