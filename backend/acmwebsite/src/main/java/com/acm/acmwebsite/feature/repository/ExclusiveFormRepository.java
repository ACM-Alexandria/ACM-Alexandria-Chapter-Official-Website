package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.ExclusiveForm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ExclusiveFormRepository extends JpaRepository<ExclusiveForm, Long> {
    List<ExclusiveForm> findByIsActiveTrueOrderByCreatedAtDesc();
    List<ExclusiveForm> findAllByOrderByCreatedAtDesc();
}
