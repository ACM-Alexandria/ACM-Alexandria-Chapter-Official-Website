package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.EventCardDto;
import com.acm.acmwebsite.feature.entity.Event;
import org.springframework.stereotype.Component;

@Component
public class EventMapper {
  public EventCardDto toEventCardDto(Event event) {
    EventCardDto dto = new EventCardDto();
    dto.setId(event.getId());
    dto.setName(event.getName());
    dto.setImageUrl(event.getImageUrl());
    return dto;
  }
}