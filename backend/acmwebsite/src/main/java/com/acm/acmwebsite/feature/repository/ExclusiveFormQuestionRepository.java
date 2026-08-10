package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.ExclusiveFormQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExclusiveFormQuestionRepository extends JpaRepository<ExclusiveFormQuestion, Long> {
    List<ExclusiveFormQuestion> findByExclusiveFormId(Long formId);
}
