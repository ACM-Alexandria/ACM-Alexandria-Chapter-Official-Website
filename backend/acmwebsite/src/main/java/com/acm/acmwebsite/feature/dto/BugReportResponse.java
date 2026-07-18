package com.acm.acmwebsite.feature.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class BugReportResponse {
    private Long id;
    private UUID reporterId;
    private String reporterName;
    private String reporterEmail;
    private String name;
    private String description;
    private List<String> imageUrls;
    private String status;
    private LocalDateTime createdAt;

    public BugReportResponse() {
    }

    public BugReportResponse(Long id, UUID reporterId, String reporterName, String reporterEmail, String name, String description, List<String> imageUrls, String status, LocalDateTime createdAt) {
        this.id = id;
        this.reporterId = reporterId;
        this.reporterName = reporterName;
        this.reporterEmail = reporterEmail;
        this.name = name;
        this.description = description;
        this.imageUrls = imageUrls;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public UUID getReporterId() {
        return reporterId;
    }

    public void setReporterId(UUID reporterId) {
        this.reporterId = reporterId;
    }

    public String getReporterName() {
        return reporterName;
    }

    public void setReporterName(String reporterName) {
        this.reporterName = reporterName;
    }

    public String getReporterEmail() {
        return reporterEmail;
    }

    public void setReporterEmail(String reporterEmail) {
        this.reporterEmail = reporterEmail;
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

    public List<String> getImageUrls() {
        return imageUrls;
    }

    public void setImageUrls(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
