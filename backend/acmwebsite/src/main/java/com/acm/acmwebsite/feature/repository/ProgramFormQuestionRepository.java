package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.ProgramFormQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProgramFormQuestionRepository extends JpaRepository<ProgramFormQuestion, Long> {
    List<ProgramFormQuestion> findByProgramId(Long programId);

    void deleteByProgramId(Long programId);
}
