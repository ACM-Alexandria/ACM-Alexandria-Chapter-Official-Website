package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.EventReview;
import com.acm.acmwebsite.feature.enums.ReviewStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface EventReviewRepository extends JpaRepository<EventReview, Long> {

    // All reviews of an event
    List<EventReview> findByEventId(Long eventId);

    // Average rating and number if ratings for an event with given id
    @Query("""
           SELECT AVG(r.rating) AS averageRating, COUNT(r) AS reviewCount
           FROM EventReview r
           WHERE r.event.id = :eventId
           """)
    EventRatingSummary getRatingSummary(@Param("eventId") Long eventId);

    // Only the comments that exist (not null, not empty)
    List<EventReview> findByEventIdAndReviewStatus(Long eventId, ReviewStatus status);

    @Query("""
       SELECT r.comment
       FROM EventReview r
       WHERE r.event.id = :eventId
         AND r.reviewStatus = :status
         AND r.comment IS NOT NULL
         AND r.comment <> ''
       """)
    List<String> findCommentsByEventIdAndStatus(@Param("eventId") Long eventId,
                                                @Param("status") ReviewStatus status);

    @Query("""
       SELECT r.comment
       FROM EventReview r
       WHERE r.event.id = :eventId
         AND r.comment IS NOT NULL
         AND r.comment <> ''
       """)
    List<String> findCommentsByEventId(@Param("eventId") Long eventId);

    boolean existsByUserIdAndEventId(UUID userId, Long eventId);

    // Projection for the summary query
    interface EventRatingSummary {
        Double getAverageRating();
        Long getReviewCount();
    }
}
