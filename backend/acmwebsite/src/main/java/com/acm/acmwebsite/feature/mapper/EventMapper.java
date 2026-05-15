package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.EventCardDto;
import com.acm.acmwebsite.feature.entity.Event;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventMapper {
    EventCardDto toEventCardDto(Event event);
}