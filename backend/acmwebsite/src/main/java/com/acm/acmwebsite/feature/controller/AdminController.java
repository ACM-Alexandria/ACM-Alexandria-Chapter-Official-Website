package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.AdminInsightsDto;
import com.acm.acmwebsite.feature.service.AdminInsightsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
public class AdminController {

    private final AdminInsightsService adminInsightsService;

    @GetMapping("/insights")
    public ResponseEntity<AdminInsightsDto> getInsights() {
        return ResponseEntity.ok(adminInsightsService.getInsights());
    }
}
