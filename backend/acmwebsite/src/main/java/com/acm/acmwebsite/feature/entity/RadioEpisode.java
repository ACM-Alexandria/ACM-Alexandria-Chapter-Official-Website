package com.acm.acmwebsite.feature.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "radio_episode")
public class RadioEpisode {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "episode_number", nullable = false)
    private Integer episodeNumber;

    @Column(nullable = false)
    private String title;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(nullable = false)
    private String url;

    private String guest;
    private String host;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "radio_season_id", nullable = false)
    private RadioSeason radioSeason;

    public RadioEpisode() {
    }

    public RadioEpisode(Long id, Integer episodeNumber, String title, String imageUrl, String url, String guest, String host, RadioSeason radioSeason) {
        this.id = id;
        this.episodeNumber = episodeNumber;
        this.title = title;
        this.imageUrl = imageUrl;
        this.url = url;
        this.guest = guest;
        this.host = host;
        this.radioSeason = radioSeason;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(Integer episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getGuest() {
        return guest;
    }

    public void setGuest(String guest) {
        this.guest = guest;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public RadioSeason getRadioSeason() {
        return radioSeason;
    }

    public void setRadioSeason(RadioSeason radioSeason) {
        this.radioSeason = radioSeason;
    }
}
