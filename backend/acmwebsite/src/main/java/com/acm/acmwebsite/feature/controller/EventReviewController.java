package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.EventReviewDto;
import com.acm.acmwebsite.feature.service.EventReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.spec.PSource;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/event-reviews")
@RequiredArgsConstructor
public class EventReviewController {

    private final EventReviewService eventReviewService;
    private final UserRepository userRepository;

    @PostMapping("/add")
    public ResponseEntity<Void> addReview(@Valid @RequestBody EventReviewDto dto,
                                          Authentication authentication) {
        User currentUser = requireUser(authentication);
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

    @PostMapping("/comments")
    public List<EventReviewDto> getAllComments(@RequestBody EventReviewDto dto,
                                               Authentication authentication) {
        User currentUser = requireUser(authentication);
        require(dto.getEventId(), "eventId");
        return eventReviewService.getAllComments(dto.getEventId(), currentUser.getRole()).stream()
                .map(c -> EventReviewDto.builder().comment(c).build())
                .toList();
    }

    /** Admins only: anonymous reviews with ids so visibility can be toggled. Body: { "eventId": 1 } */
    @PostMapping("/admin")
    public List<EventReviewDto> getAllReviewsForAdmin(@RequestBody EventReviewDto dto,
                                                      Authentication authentication) {
        User currentUser = requireUser(authentication);
        require(dto.getEventId(), "eventId");
        return eventReviewService.getReviewsForAdmin(dto.getEventId(), currentUser.getRole()).stream()
                .map(r -> EventReviewDto.builder()
                        .reviewId(r.getId())
                        .rating(r.getRating())
                        .comment(r.getComment())
                        .status(r.getReviewStatus())
                        .build())
                .toList();
    }

    /** Whether the current user already submitted feedback for this event. Body: { "eventId": 1 } */
    @PostMapping("/mine")
    public Map<String, Boolean> hasReviewed(@RequestBody EventReviewDto dto,
                                            Authentication authentication) {
        User currentUser = requireUser(authentication);
        require(dto.getEventId(), "eventId");
        boolean reviewed = eventReviewService.hasUserReviewed(dto.getEventId(), currentUser.getId());
        return Map.of("reviewed", reviewed);
    }

    @PatchMapping("/visibility")
    public EventReviewDto toggleVisibility(@RequestBody EventReviewDto dto,
                                           Authentication authentication) {
        User currentUser = requireUser(authentication);
        require(dto.getReviewId(), "reviewId");
        var result = eventReviewService.toggleVisibility(dto.getReviewId(), currentUser.getRole());
        return EventReviewDto.builder()
                .reviewId(result.getId())
                .status(result.getReviewStatus())
                .build();
    }
    
    private User requireUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in to continue");
        }
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in to continue"));
    }

    private void require(Object value, String name) {
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, name + " is required");
        }
    }
}
