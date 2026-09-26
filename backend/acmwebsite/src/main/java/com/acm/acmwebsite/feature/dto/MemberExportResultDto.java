package com.acm.acmwebsite.feature.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberExportResultDto {
    private String googleSheetUrl;
    private String title;
    private long totalMembers;
    private LocalDateTime exportedAt;
}
