package com.acm.acmwebsite.feature.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private LocalDateTime eventTime;
    private String Location;
    private String imageUrl;
    @Column(name = "google_sheet_url")
    private String googleSheetUrl;
    @Column(name = "sheet_last_updated_at")
    private LocalDateTime sheetLastUpdatedAt;

    public Event() {
    }

    public Event(long id, String name, String description, LocalDateTime eventTime, String location, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.eventTime = eventTime;
        Location = location;
        this.imageUrl = imageUrl;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
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

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
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
}
