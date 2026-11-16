package com.acm.acmwebsite.feature.badges;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.AwardRequest;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.AwardView;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.BadgeTypeRequest;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.BadgeTypeResponse;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.MonthRequest;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.PeriodRequest;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.PeriodResponse;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.SuggestionRequest;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.SuggestionResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Badge management for the admin page. Super admins and the high board only.
@RestController
@RequestMapping("/api/admin/badges")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD')")
@RequiredArgsConstructor
public class BadgeAdminController {

    private final BadgePeriodService periodService;
    private final BadgeTypeService typeService;
    private final BadgeSuggestionService suggestionService;
    private final BadgeAwardService awardService;

    // ---- Periods ----

    @GetMapping("/periods")
    public List<PeriodResponse> listPeriods() {
        return periodService.listSeasonsAndQuarters();
    }

    @PostMapping("/periods")
    public ResponseEntity<PeriodResponse> createPeriod(@Valid @RequestBody PeriodRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(periodService.create(request));
    }

    @PutMapping("/periods/{id}")
    public PeriodResponse updatePeriod(@PathVariable Long id, @Valid @RequestBody PeriodRequest request) {
        return periodService.update(id, request);
    }

    @DeleteMapping("/periods/{id}")
    public ResponseEntity<Void> deletePeriod(@PathVariable Long id) {
        periodService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // The period for a calendar month ("2026-11"), created on first use.
    @PostMapping("/periods/months")
    public PeriodResponse resolveMonth(@Valid @RequestBody MonthRequest request) {
        return BadgeMapper.toResponse(periodService.resolveMonth(request.month()));
    }

    // ---- Badge types ----

    @GetMapping("/types")
    public List<BadgeTypeResponse> listTypes() {
        return typeService.list();
    }

    @PostMapping("/types")
    public ResponseEntity<BadgeTypeResponse> createType(@Valid @RequestBody BadgeTypeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(typeService.create(request));
    }

    @PutMapping("/types/{id}")
    public BadgeTypeResponse updateType(@PathVariable Long id, @Valid @RequestBody BadgeTypeRequest request) {
        return typeService.update(id, request);
    }

    @DeleteMapping("/types/{id}")
    public ResponseEntity<Void> deleteType(@PathVariable Long id) {
        typeService.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ---- Suggestions and awards ----

    // Read-only: ranks each committee's members for the period. POST because it takes a per-committee map.
    @PostMapping("/suggestions")
    public SuggestionResponse suggest(@Valid @RequestBody SuggestionRequest request) {
        return suggestionService.suggest(request);
    }

    // Awards of a period; without `periodId`, the manual badges that have no period.
    @GetMapping("/awards")
    public List<AwardView> listAwards(@RequestParam(required = false) Long periodId) {
        return awardService.listForPeriod(periodId);
    }

    @PostMapping("/awards")
    public ResponseEntity<List<AwardView>> award(@Valid @RequestBody AwardRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(awardService.award(request, authentication.getName()));
    }

    @DeleteMapping("/awards/{id}")
    public ResponseEntity<Void> revoke(@PathVariable Long id, Authentication authentication) {
        awardService.revoke(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
