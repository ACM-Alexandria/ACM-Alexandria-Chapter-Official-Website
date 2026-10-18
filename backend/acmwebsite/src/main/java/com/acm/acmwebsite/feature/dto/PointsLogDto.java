package com.acm.acmwebsite.feature.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import com.acm.acmwebsite.feature.entity.PointsLog;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointsLogDto {

    private UUID id;
    private UUID member;
    private UUID assigner;
    private String assignerName;
    private UUID lastUpdater;
    private String lastUpdaterName;

    private String reason;
    private int points;


    @JsonProperty("created_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss") // Ensures React can parse it easily
    private LocalDateTime updatedAt;


    public static PointsLogDto convertToDto(PointsLog log) {
        return PointsLogDto.builder()
                .id(log.getId())
                .member(log.getMember().getId())

                // Safe check for Assigner
                .assigner(log.getAssigner() != null ? log.getAssigner().getId() : null)
                .assignerName(log.getAssigner() != null ? log.getAssigner().getName() : "System")

                // 👇 THE CRITICAL FIX: Safe check for Last Updater
                .lastUpdater(log.getLastUpdater() != null ? log.getLastUpdater().getId() : null)
                .lastUpdaterName(log.getLastUpdater() != null ? log.getLastUpdater().getName() : null)

                .reason(log.getReason())
                .points(log.getPoints())
                .createdAt(log.getCreatedAt())
                .updatedAt(log.getUpdatedAt())
                .build();
    }
}