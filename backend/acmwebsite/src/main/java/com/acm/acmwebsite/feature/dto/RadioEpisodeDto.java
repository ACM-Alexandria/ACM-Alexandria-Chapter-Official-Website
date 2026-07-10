package com.acm.acmwebsite.feature.dto;

public class RadioEpisodeDto {
    private Long id;
    private Integer episodeNumber;
    private String title;
    private String imageUrl;
    private String url;
    private String guest;
    private String host;
    private Long radioSeasonId;

    public RadioEpisodeDto() {
    }

    public RadioEpisodeDto(Long id, Integer episodeNumber, String title, String imageUrl, String url, String guest, String host, Long radioSeasonId) {
        this.id = id;
        this.episodeNumber = episodeNumber;
        this.title = title;
        this.imageUrl = imageUrl;
        this.url = url;
        this.guest = guest;
        this.host = host;
        this.radioSeasonId = radioSeasonId;
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

    public Long getRadioSeasonId() {
        return radioSeasonId;
    }

    public void setRadioSeasonId(Long radioSeasonId) {
        this.radioSeasonId = radioSeasonId;
    }
}
