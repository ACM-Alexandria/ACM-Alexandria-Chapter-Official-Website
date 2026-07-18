package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.FeatureSuggestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface FeatureSuggestionRepository extends JpaRepository<FeatureSuggestion, Long> {
    @Query("SELECT fs FROM FeatureSuggestion fs ORDER BY CASE WHEN fs.status = 'NEW' THEN 0 ELSE 1 END, fs.createdAt DESC")
    List<FeatureSuggestion> findAllSorted();
}
