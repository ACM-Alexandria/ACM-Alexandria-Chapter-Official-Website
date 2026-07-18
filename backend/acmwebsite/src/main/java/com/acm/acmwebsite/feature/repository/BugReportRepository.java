package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.BugReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import org.springframework.data.jpa.repository.Query;
import java.util.List;

@Repository
public interface BugReportRepository extends JpaRepository<BugReport, Long> {
    @Query("SELECT br FROM BugReport br ORDER BY CASE WHEN br.status = 'NEW' THEN 0 ELSE 1 END, br.createdAt DESC")
    List<BugReport> findAllSorted();
}
