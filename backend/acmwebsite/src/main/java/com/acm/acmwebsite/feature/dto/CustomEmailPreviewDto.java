package com.acm.acmwebsite.feature.dto;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class CustomEmailPreviewDto {
    private long totalRecipients;
    private Map<String, Long> recipientCounts;
}