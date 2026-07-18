package com.acm.acmwebsite.feature.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class CommitteeCallResponseDto {
    private Long id;
    private LocalDateTime openedAt;
    private LocalDateTime closedAt;
    private String googleSheetUrl;
    private LocalDateTime sheetLastUpdatedAt;
    private long registrationsCount;
}
