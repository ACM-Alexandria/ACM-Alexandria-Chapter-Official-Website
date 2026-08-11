package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.ExclusiveFormDto;
import com.acm.acmwebsite.feature.entity.ExclusiveForm;
import org.springframework.stereotype.Component;

@Component
public class ExclusiveFormMapper {

    public ExclusiveFormDto toDto(ExclusiveForm entity) {
        if (entity == null) {
            return null;
        }

        return ExclusiveFormDto.builder()
                .id(entity.getId())
                .title(entity.getTitle())
                .description(entity.getDescription())
                .imageUrl(entity.getImageUrl())
                .sheetId(entity.getSheetId())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public ExclusiveForm toEntity(ExclusiveFormDto dto) {
        if (dto == null) {
            return null;
        }

        return ExclusiveForm.builder()
                .id(dto.getId())
                .title(dto.getTitle())
                .description(dto.getDescription())
                .imageUrl(dto.getImageUrl())
                .sheetId(dto.getSheetId())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : false)
                .build();
    }
}
