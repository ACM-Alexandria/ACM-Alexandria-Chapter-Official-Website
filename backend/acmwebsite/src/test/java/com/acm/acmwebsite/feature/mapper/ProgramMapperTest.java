package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.ProgramDto;
import com.acm.acmwebsite.feature.entity.Program;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class ProgramMapperTest {

    private ProgramMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ProgramMapper();
    }

    @Test
    @DisplayName("Should map Program entity to ProgramDto")
    void testToProgramDto() {
        Program program = new Program();
        program.setId(10L);
        program.setName("Mentorship 2026");
        program.setDescription("Mentorship program");
        program.setStartDate(LocalDateTime.of(2026, 11, 1, 0, 0));
        program.setEndDate(LocalDateTime.of(2026, 12, 1, 0, 0));
        program.setTime("18:00");
        program.setImageUrl("https://example.com/prog.png");
        program.setRegistrationOpen(true);
        program.setGoogleSheetUrl("https://sheets.google.com/xyz");
        LocalDateTime lastUpdated = LocalDateTime.now();
        program.setSheetLastUpdatedAt(lastUpdated);

        ProgramDto dto = mapper.toProgramDto(program);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(10L);
        assertThat(dto.getName()).isEqualTo("Mentorship 2026");
        assertThat(dto.getDescription()).isEqualTo("Mentorship program");
        assertThat(dto.getStartDate()).isEqualTo(LocalDateTime.of(2026, 11, 1, 0, 0));
        assertThat(dto.getEndDate()).isEqualTo(LocalDateTime.of(2026, 12, 1, 0, 0));
        assertThat(dto.getTime()).isEqualTo("18:00");
        assertThat(dto.getImageUrl()).isEqualTo("https://example.com/prog.png");
        assertThat(dto.isRegistrationOpen()).isTrue();
        assertThat(dto.getGoogleSheetUrl()).isEqualTo("https://sheets.google.com/xyz");
        assertThat(dto.getSheetLastUpdatedAt()).isEqualTo(lastUpdated);
    }

    @Test
    @DisplayName("Should map ProgramDto to Program entity")
    void testToProgram() {
        ProgramDto dto = new ProgramDto();
        dto.setId(20L);
        dto.setName("Training Camp");
        dto.setDescription("Camp for beginners");
        dto.setStartDate(LocalDateTime.of(2026, 10, 15, 0, 0));
        dto.setEndDate(LocalDateTime.of(2026, 10, 30, 0, 0));
        dto.setTime("10:00");
        dto.setImageUrl("https://example.com/camp.png");
        dto.setRegistrationOpen(false);

        Program entity = mapper.toProgram(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(20L);
        assertThat(entity.getName()).isEqualTo("Training Camp");
        assertThat(entity.getDescription()).isEqualTo("Camp for beginners");
        assertThat(entity.getStartDate()).isEqualTo(LocalDateTime.of(2026, 10, 15, 0, 0));
        assertThat(entity.getEndDate()).isEqualTo(LocalDateTime.of(2026, 10, 30, 0, 0));
        assertThat(entity.getTime()).isEqualTo("10:00");
        assertThat(entity.getImageUrl()).isEqualTo("https://example.com/camp.png");
        assertThat(entity.isRegistrationOpen()).isFalse();
    }
}
