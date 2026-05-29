package com.acm.acmwebsite.feature.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistrationAnalysisDto {
    private long totalRegistrations;
    private long alexUniStudentCount;
    private long nonAlexUniStudentCount;
    private Map<String, Long> departmentCounts;
    private Map<String, Long> batchCounts;
    private String googleSheetUrl;
    private LocalDateTime sheetLastUpdatedAt;
}
