package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.FAQ;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FAQRepository extends JpaRepository<FAQ,Integer> {
    List<FAQ> findAllByOrderByCategoryAscOrderAscIdAsc();
    Page<FAQ> findByQuestionContainingIgnoreCaseOrAnswerContainingIgnoreCase(String question, String answer, Pageable pageable);
}
