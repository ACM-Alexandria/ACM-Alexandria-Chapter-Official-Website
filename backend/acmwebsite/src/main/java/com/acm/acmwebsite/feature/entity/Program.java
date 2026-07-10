package com.acm.acmwebsite.feature.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String imageUrl;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String time;

    @Column(name = "registration_open", nullable = false)
    private boolean registrationOpen = false;

    @Column(name = "google_sheet_url")
    private String googleSheetUrl;

    @Column(name = "sheet_last_updated_at")
    private LocalDateTime sheetLastUpdatedAt;

    public Program() {
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

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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

    public Program(Long id, String name, String description, String imageUrl, LocalDateTime startDate, LocalDateTime endDate, String time) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.startDate = startDate;
        this.endDate = endDate;
        this.time = time;
    }
}
