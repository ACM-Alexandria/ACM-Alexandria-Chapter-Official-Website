package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.EventFormQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventFormQuestionRepository extends JpaRepository<EventFormQuestion, Long> {
    List<EventFormQuestion> findByEventId(Long eventId);

    void deleteByEventId(Long eventId);
}
