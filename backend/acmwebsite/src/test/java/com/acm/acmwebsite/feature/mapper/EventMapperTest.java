package com.acm.acmwebsite.feature.mapper;

import com.acm.acmwebsite.feature.dto.EventCardDto;
import com.acm.acmwebsite.feature.entity.Event;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class EventMapperTest {

    private final EventMapper mapper = Mappers.getMapper(EventMapper.class);

    @Test
    @DisplayName("Should map Event entity to EventCardDto")
    void testToEventCardDto() {
        Event event = new Event();
        event.setId(5L);
        event.setName("Hackathon 2026");
        event.setDescription("Annual coding contest");
        event.setImageUrl("https://example.com/banner.png");
        event.setLocation("Main Hall");
        LocalDateTime now = LocalDateTime.now();
        event.setEventTime(now);
        event.setEndTime(now.plusHours(4));
        event.setRegistrationOpen(true);

        EventCardDto dto = mapper.toEventCardDto(event);

        assertThat(dto).isNotNull();
        assertThat(dto.getId()).isEqualTo(5L);
        assertThat(dto.getName()).isEqualTo("Hackathon 2026");
        assertThat(dto.getDescription()).isEqualTo("Annual coding contest");
        assertThat(dto.getImageUrl()).isEqualTo("https://example.com/banner.png");
        assertThat(dto.getLocation()).isEqualTo("Main Hall");
        assertThat(dto.getEventTime()).isEqualTo(now);
        assertThat(dto.getEndTime()).isEqualTo(now.plusHours(4));
        assertThat(dto.isRegistrationOpen()).isTrue();
    }

    @Test
    @DisplayName("Should return null when event is null")
    void testNullEvent() {
        assertThat(mapper.toEventCardDto(null)).isNull();
    }
}
