package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.HRFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface HRFeedbackRepository extends JpaRepository<HRFeedback, Long> {
}
