package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.ExclusiveFormDto;
import com.acm.acmwebsite.feature.entity.ExclusiveForm;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ExclusiveFormMapperTest {

    private ExclusiveFormMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ExclusiveFormMapper();
    }

    @Test
    @DisplayName("Should map ExclusiveForm to ExclusiveFormDto")
    void testToDto() {
        LocalDateTime now = LocalDateTime.now();
        ExclusiveForm entity = ExclusiveForm.builder()
                .id(1L)
                .title("T-shirt Form")
                .description("Sizes and colors")
                .imageUrl("https://image.com/tshirt.png")
                .sheetId("sheet-123")
                .isActive(true)
                .createdAt(now)
                .updatedAt(now)
                .build();

        ExclusiveFormDto dto = mapper.toDto(entity);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getTitle()).isEqualTo("T-shirt Form");
        assertThat(dto.getDescription()).isEqualTo("Sizes and colors");
        assertThat(dto.getImageUrl()).isEqualTo("https://image.com/tshirt.png");
        assertThat(dto.getSheetId()).isEqualTo("sheet-123");
        assertThat(dto.getIsActive()).isTrue();
        assertThat(dto.getCreatedAt()).isEqualTo(now);
        assertThat(dto.getUpdatedAt()).isEqualTo(now);
    }

    @Test
    @DisplayName("Should return null when ExclusiveForm entity is null")
    void testToDtoNull() {
        assertThat(mapper.toDto(null)).isNull();
    }

    @Test
    @DisplayName("Should map ExclusiveFormDto to ExclusiveForm entity")
    void testToEntity() {
        ExclusiveFormDto dto = ExclusiveFormDto.builder()
                .id(2L)
                .title("Workshop Survey")
                .description("Feedback survey")
                .imageUrl("https://image.com/survey.png")
                .sheetId("sheet-456")
                .isActive(true)
                .build();

        ExclusiveForm entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(2L);
        assertThat(entity.getTitle()).isEqualTo("Workshop Survey");
        assertThat(entity.getDescription()).isEqualTo("Feedback survey");
        assertThat(entity.getImageUrl()).isEqualTo("https://image.com/survey.png");
        assertThat(entity.getSheetId()).isEqualTo("sheet-456");
        assertThat(entity.getIsActive()).isTrue();
    }

    @Test
    @DisplayName("Should default isActive to false when DTO isActive is null")
    void testToEntityWithNullIsActive() {
        ExclusiveFormDto dto = ExclusiveFormDto.builder()
                .id(3L)
                .isActive(null)
                .build();

        ExclusiveForm entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getIsActive()).isFalse();
    }

    @Test
    @DisplayName("Should return null when ExclusiveFormDto is null")
    void testToEntityNull() {
        assertThat(mapper.toEntity(null)).isNull();
    }
}
