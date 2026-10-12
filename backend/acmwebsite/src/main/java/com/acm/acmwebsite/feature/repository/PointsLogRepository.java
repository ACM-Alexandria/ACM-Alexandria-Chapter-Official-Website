package com.acm.acmwebsite.feature.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.acm.acmwebsite.feature.entity.PointsLog;

public interface PointsLogRepository extends JpaRepository<PointsLog, UUID> {

    Page<PointsLog> findByMember_Id(UUID id, Pageable pageable);

    Page<PointsLog> findByAssigner_Id(UUID id, Pageable pageable);
}