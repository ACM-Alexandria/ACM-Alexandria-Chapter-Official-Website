package com.acm.acmwebsite.feature.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public class ProgramDto {
    private Long id;
    private String name;
    private String description;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String time;
    private String imageUrl;
    private boolean registrationOpen;
    private String googleSheetUrl;
    private LocalDateTime sheetLastUpdatedAt;
    // Request-only flag: when false, creating the program skips the subscriber announcement
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Boolean sendAnnouncement;

    public ProgramDto(Long id, String name, String description, LocalDateTime startDate, LocalDateTime endDate, String time, String imageUrl,
                      boolean registrationOpen, String googleSheetUrl, LocalDateTime sheetLastUpdatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.time = time;
        this.imageUrl = imageUrl;
        this.registrationOpen = registrationOpen;
        this.googleSheetUrl = googleSheetUrl;
        this.sheetLastUpdatedAt = sheetLastUpdatedAt;
    }

    public ProgramDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }

    public LocalDateTime getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isRegistrationOpen() {
        return registrationOpen;
    }

    public void setRegistrationOpen(boolean registrationOpen) {
        this.registrationOpen = registrationOpen;
    }

    public String getGoogleSheetUrl() {
        return googleSheetUrl;
    }

    public void setGoogleSheetUrl(String googleSheetUrl) {
        this.googleSheetUrl = googleSheetUrl;
    }

    public LocalDateTime getSheetLastUpdatedAt() {
        return sheetLastUpdatedAt;
    }

    public void setSheetLastUpdatedAt(LocalDateTime sheetLastUpdatedAt) {
        this.sheetLastUpdatedAt = sheetLastUpdatedAt;
    }

    public Boolean getSendAnnouncement() {
        return sendAnnouncement;
    }

    public void setSendAnnouncement(Boolean sendAnnouncement) {
        this.sendAnnouncement = sendAnnouncement;
    }
}
