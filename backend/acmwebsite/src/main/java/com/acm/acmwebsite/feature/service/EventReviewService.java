package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.enums.Role;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.entity.Event;
import com.acm.acmwebsite.feature.entity.EventRegistration;
import com.acm.acmwebsite.feature.entity.EventReview;
import com.acm.acmwebsite.feature.enums.ReviewStatus;
import com.acm.acmwebsite.feature.repository.EventRepository;
import com.acm.acmwebsite.feature.repository.EventReviewRepository;
import com.acm.acmwebsite.feature.repository.EventReviewRepository.EventRatingSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventReviewService {

    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final EventReviewRepository eventReviewRepository;
    private final EventRegistrationService eventRegistrationService;


    @Transactional
    public EventReview addReview(Long eventId, UUID userId, int rating, String comment) {
        if (rating < 1 || rating > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Rating must be between 1 and 5");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        if (eventRegistrationService.isUserRegistered(userId,eventId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "You already reviewed this event");
        }

        EventReview review = new EventReview();
        review.setEvent(event);
        review.setUser(user);
        review.setRating(rating);
        review.setComment(comment == null || comment.isBlank() ? null : comment.trim());
        review.setReviewStatus(ReviewStatus.PRIVATE);
        return eventReviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public EventRatingSummaryResponse getRatingSummary(Long eventId) {
        EventRatingSummary summary = eventReviewRepository.getRatingSummary(eventId);

        double average = summary.getAverageRating() == null
                ? 0.0
                : Math.round(summary.getAverageRating() * 10.0) / 10.0;   // 4.37 -> 4.4
        long count = summary.getReviewCount() == null ? 0 : summary.getReviewCount();

        return new EventRatingSummaryResponse(average, count);
    }

    // Public comments (what any visitor can see)
    @Transactional(readOnly = true)
    public List<String> getPublicComments(Long eventId) {
        return eventReviewRepository.findCommentsByEventIdAndStatus(eventId, ReviewStatus.PUBLIC);
    }

    // Private comments (only for admins/board, restrict this in the controller)
    @Transactional(readOnly = true)
    public List<String> getAllComments(Long eventId,Role role) {
        return eventReviewRepository.findCommentsByEventId(eventId);
    }

    private static final Set<Role> ADMIN_ROLES = Set.of(Role.SUPER_ADMIN, Role.ACM_HIGH_BOARD);

    private boolean isAdmin(Role role) {
        return ADMIN_ROLES.contains(role);
    }

    @Transactional
    public EventReview toggleVisibility(Long reviewId, Role role) {
        if (!isAdmin(role)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can change review visibility");
        }

        EventReview review = eventReviewRepository.findById(reviewId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Review not found"));

        review.setReviewStatus(
                review.getReviewStatus() == ReviewStatus.PUBLIC ? ReviewStatus.PRIVATE : ReviewStatus.PUBLIC);
        return review;
    }

    public record EventRatingSummaryResponse(double averageRating, long reviewCount) {}
}