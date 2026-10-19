package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.ClubCardDto;
import com.acm.acmwebsite.feature.entity.Club;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.assertj.core.api.Assertions.assertThat;

class ClubMapperTest {

    private final ClubMapper clubMapper = Mappers.getMapper(ClubMapper.class);

    @Test
    @DisplayName("Should map Club entity to ClubCardDto")
    void testToClubCardDto() {
        Club club = new Club();
        club.setId(1L);
        club.setName("CP Club");
        club.setDescription("Competitive Programming");
        club.setImageUrl("https://example.com/logo.png");
        club.setIsExternal(true);

        ClubCardDto dto = clubMapper.toClubCardDto(club);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getName()).isEqualTo("CP Club");
        assertThat(dto.getDescription()).isEqualTo("Competitive Programming");
        assertThat(dto.getImageUrl()).isEqualTo("https://example.com/logo.png");
        assertThat(dto.isExternal()).isTrue();
    }

    @Test
    @DisplayName("Should return null when club is null")
    void testNullMapping() {
        assertThat(clubMapper.toClubCardDto(null)).isNull();
    }
}
