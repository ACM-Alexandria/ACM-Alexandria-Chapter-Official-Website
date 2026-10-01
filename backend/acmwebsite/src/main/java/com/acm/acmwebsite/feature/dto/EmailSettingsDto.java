package com.acm.acmwebsite.feature.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmailSettingsDto {
    // Read-only: when the subscriber announcement last went out (null = never)
    private LocalDateTime announcementSentAt;
}
