package com.acm.acmwebsite.feature.dto;

import com.acm.acmwebsite.feature.enums.HRFeedbackType;
import java.time.LocalDateTime;

public class HRFeedbackResponse {
    private Long id;
    private HRFeedbackType type;
    private String content;
    private LocalDateTime createdAt;
    private Boolean isAnonymous;
    private String reporterName;
    private String reporterEmail;

    public HRFeedbackResponse() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public HRFeedbackType getType() {
        return type;
    }

    public void setType(HRFeedbackType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
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
}
