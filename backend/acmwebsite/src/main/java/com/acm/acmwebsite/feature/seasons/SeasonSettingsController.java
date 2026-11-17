package com.acm.acmwebsite.feature.seasons;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acm.acmwebsite.feature.seasons.dto.SeasonDtos.CurrentSeasonResponse;
import com.acm.acmwebsite.feature.seasons.dto.SeasonDtos.MonthRequest;
import com.acm.acmwebsite.feature.seasons.dto.SeasonDtos.PeriodRequest;
import com.acm.acmwebsite.feature.seasons.dto.SeasonDtos.PeriodResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Season settings. Every admin-page role can read the calendar (the badge Award screen needs it);
// only the super admin can change it. Archiving (POST /api/admin/boards/archive) ends the current season
// and starts the next one.
@RestController
@RequestMapping("/api/admin/seasons")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
@RequiredArgsConstructor
public class SeasonSettingsController {

    private final SeasonPeriodService periodService;
    private final CurrentSeasonService currentSeasonService;

    // The latest season: what boards, past boards and archiving treat as current.
    @GetMapping("/current")
    public CurrentSeasonResponse current() {
        return currentSeasonService.currentSeasonResponse();
    }

    @GetMapping("/periods")
    public List<PeriodResponse> listPeriods() {
        return periodService.listSeasonsAndQuarters();
    }

    @PostMapping("/periods")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<PeriodResponse> createPeriod(@Valid @RequestBody PeriodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(periodService.create(request));
    }

    @PutMapping("/periods/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public PeriodResponse updatePeriod(@PathVariable Long id, @Valid @RequestBody PeriodRequest request) {
        return periodService.update(id, request);
    }

    @DeleteMapping("/periods/{id}")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<Void> deletePeriod(@PathVariable Long id) {
        periodService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // The period for a calendar month ("2026-11"), created on first use.
    @PostMapping("/months")
    public PeriodResponse resolveMonth(@Valid @RequestBody MonthRequest request) {
        return PeriodResponse.of(periodService.resolveMonth(request.month()));
    }
}
