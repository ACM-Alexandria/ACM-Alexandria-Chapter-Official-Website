package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.ClubFormQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClubFormQuestionRepository extends JpaRepository<ClubFormQuestion, Long> {
    List<ClubFormQuestion> findByClubId(Long clubId);

    void deleteByClubId(Long clubId);
}
