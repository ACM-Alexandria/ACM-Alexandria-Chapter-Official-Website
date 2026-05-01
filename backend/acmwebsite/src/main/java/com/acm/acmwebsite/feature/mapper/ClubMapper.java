package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.ClubCardDto;
import com.acm.acmwebsite.feature.entity.Club;
import org.springframework.stereotype.Component;

@Component
public class ClubMapper {
    public ClubCardDto toClubCardDto(Club club) {
        ClubCardDto dto = new ClubCardDto();
        dto.setId(club.getId());
        dto.setName(club.getName());
        dto.setImageUrl(club.getImageUrl());
        dto.setDescription(club.getDescription());
        return dto;
    }
}
