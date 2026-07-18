package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.EventRegistration;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;
import java.util.UUID;

@Repository
public interface EventRegistrationRepository extends JpaRepository<EventRegistration, Long> {
    boolean existsByUserIdAndEventId(UUID userId, Long eventId);

    List<EventRegistration> findByEventId(Long eventId);

    void deleteByEventId(Long eventId);

    @Query("SELECT r.event.id, r.event.name, COUNT(r) FROM EventRegistration r GROUP BY r.event.id, r.event.name")
    List<Object[]> countRegistrationsByEvent();
}
