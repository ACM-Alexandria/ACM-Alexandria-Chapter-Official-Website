package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.dto.EventReviewDto;
import com.acm.acmwebsite.feature.service.EventReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/event-reviews")
@RequiredArgsConstructor
public class EventReviewController {

    private final EventReviewService eventReviewService;


    @PostMapping("/add")
    public ResponseEntity<Void> addReview(@Valid @RequestBody EventReviewDto dto,
                                          @AuthenticationPrincipal User currentUser) {
        require(dto.getEventId(), "eventId");
        require(dto.getRating(), "rating");
        eventReviewService.addReview(dto.getEventId(), currentUser.getId(),
                dto.getRating(), dto.getComment());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }


    @PostMapping("/summary")
    public EventReviewDto getRatingSummary(@RequestBody EventReviewDto dto) {
        require(dto.getEventId(), "eventId");
        var summary = eventReviewService.getRatingSummary(dto.getEventId());
        return EventReviewDto.builder()
                .eventId(dto.getEventId())
                .averageRating(summary.averageRating())
                .reviewCount(summary.reviewCount())
                .build();
    }


    @PostMapping("/comments/public")
    public List<EventReviewDto> getPublicComments(@RequestBody EventReviewDto dto) {
        require(dto.getEventId(), "eventId");
        return eventReviewService.getPublicComments(dto.getEventId()).stream()
                .map(c -> EventReviewDto.builder().comment(c).build())
                .toList();
    }

    // Admins only. Body: { "eventId": 1 }
    @PostMapping("/comments")
    public List<EventReviewDto> getAllComments(@RequestBody EventReviewDto dto,
                                               @AuthenticationPrincipal User currentUser) {
        require(dto.getEventId(), "eventId");
        return eventReviewService.getAllComments(dto.getEventId(), currentUser.getRole()).stream()
                .map(c -> EventReviewDto.builder().comment(c).build())
                .toList();
    }

    // Admins only: reviews with ids so they can be toggled. Body: { "eventId": 1 }
    @PostMapping("/admin")
    public List<String> getAllReviewsForAdmin(@RequestBody EventReviewDto dto,
                                                      @AuthenticationPrincipal User currentUser) {
        require(dto.getEventId(), "eventId");
        return eventReviewService.getAllComments(dto.getEventId(), currentUser.getRole());
    }

    // Admins only. Body: { "reviewId": 7 }
    @PatchMapping("/visibility")
    public EventReviewDto toggleVisibility(@RequestBody EventReviewDto dto,
                                           @AuthenticationPrincipal User currentUser) {
        require(dto.getReviewId(), "reviewId");
        var result = eventReviewService.toggleVisibility(dto.getReviewId(), currentUser.getRole());
        return EventReviewDto.builder()
                .reviewId(result.getId())
                .status(result.getReviewStatus())
                .build();
    }

    private void require(Object value, String name) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, name + " is required");
        }
    }
}