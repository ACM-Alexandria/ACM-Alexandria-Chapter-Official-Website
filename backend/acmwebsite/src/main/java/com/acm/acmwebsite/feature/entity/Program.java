package com.acm.acmwebsite.feature.entity;

import jakarta.persistence.*;
import org.springframework.boot.autoconfigure.web.WebProperties;

import java.time.LocalDateTime;

@Entity
public class Program {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column (nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String googleFormUrl;
    private String imageUrl;
    private LocalDateTime eventTime;

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

    public String getGoogleFormUrl() {
        return googleFormUrl;
    }

    public void setGoogleFormUrl(String googleFormUrl) {
        this.googleFormUrl = googleFormUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public LocalDateTime getEventTime() {
        return eventTime;
    }

    public void setEventTime(LocalDateTime eventTime) {
        this.eventTime = eventTime;
    }

    public Program(Long id, String name, String description, String googleFormUrl, String imageUrl, LocalDateTime eventTime) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.googleFormUrl = googleFormUrl;
        this.imageUrl = imageUrl;
        this.eventTime = eventTime;
    }
}
