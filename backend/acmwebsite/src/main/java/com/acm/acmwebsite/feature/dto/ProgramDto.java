package com.acm.acmwebsite.feature.dto;

import java.time.LocalDateTime;

public class ProgramDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime eventTime;
    private String imageUrl;
    private String googleFormUrl;

    public ProgramDto(Long id,String name, String description, LocalDateTime eventTime, String imageUrl, String googleFormUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.eventTime = eventTime;
        this.imageUrl = imageUrl;
        this.googleFormUrl = googleFormUrl;
    }

    public ProgramDto() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }


    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getGoogleFormUrl() {
        return googleFormUrl;
    }

    public void setGoogleFormUrl(String googleFormUrl) {
        this.googleFormUrl = googleFormUrl;
    }
}
