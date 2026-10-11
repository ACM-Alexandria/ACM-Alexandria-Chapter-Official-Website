package com.acm.acmwebsite.feature.dto;

import java.util.UUID;

import com.acm.acmwebsite.feature.entity.PointsLog;

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
    private String reason;
    private int points;

    public static PointsLogDto convertToDto(PointsLog log) {
        return PointsLogDto.builder()
                .id(log.getId())
                .member(log.getMember().getId())
                .assigner(log.getAssigner().getId())
                .reason(log.getReason())
                .points(log.getPoints())
                .build();
    }
}