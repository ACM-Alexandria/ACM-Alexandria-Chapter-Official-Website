package com.acm.acmwebsite.feature.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberExportPreviewDto {
    private long totalMembers;
    private long alexUniStudentCount;
    private Map<String, Long> roleCounts;
    private Map<String, Long> associationCounts;
}
