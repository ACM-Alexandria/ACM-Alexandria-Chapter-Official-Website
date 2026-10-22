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

    private Long id;
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

                .assigner(log.getAssigner().getId())
                .assignerName(log.getAssigner().getName())

                .lastUpdater(log.getLastUpdater().getId())
                .lastUpdaterName(log.getLastUpdater().getName())

                .reason(log.getReason())
                .points(log.getPoints())
                .createdAt(log.getCreatedAt())
                .updatedAt(log.getUpdatedAt())
                .build();
    }
}