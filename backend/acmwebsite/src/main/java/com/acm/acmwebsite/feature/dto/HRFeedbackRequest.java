package com.acm.acmwebsite.feature.dto;

import com.acm.acmwebsite.feature.enums.HRFeedbackType;

public class HRFeedbackRequest {
    private HRFeedbackType type;
    private String content;
    private Boolean isAnonymous;

    public HRFeedbackRequest() {}

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

    public Boolean getIsAnonymous() {
        return isAnonymous;
    }

    public void setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
    }
}
