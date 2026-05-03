package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.ClubCardDto;
import com.acm.acmwebsite.feature.entity.Club;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ClubMapper {
    ClubCardDto toClubCardDto(Club club);
}
