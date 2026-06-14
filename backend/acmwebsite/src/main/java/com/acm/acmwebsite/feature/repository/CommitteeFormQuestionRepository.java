package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.CommitteeFormQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommitteeFormQuestionRepository extends JpaRepository<CommitteeFormQuestion, Long> {
    List<CommitteeFormQuestion> findByCommitteeId(Long committeeId);
}
