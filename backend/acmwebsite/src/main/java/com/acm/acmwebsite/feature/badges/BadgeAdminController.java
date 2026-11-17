package com.acm.acmwebsite.feature.badges;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.AwardNoteRequest;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.AwardRequest;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.AwardView;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.BadgeTypeResponse;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.CommitteeRankingRequest;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.CommitteeRankingResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// Badge management for the admin page (periods live in Season settings, /api/admin/seasons): everyone who can open it (super admin, high board,
// committee boards and club boards), matching the /api/admin/** rule in SecurityConfig.
@RestController
@RequestMapping("/api/admin/badges")
@PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
@RequiredArgsConstructor
public class BadgeAdminController {

    private final BadgeTypeService typeService;
    private final BadgeRankingService rankingService;
    private final BadgeAwardService awardService;

    // ---- Badge types (fixed, read-only) ----

    @GetMapping("/types")
    public List<BadgeTypeResponse> listTypes() {
        return typeService.list();
    }

    // ---- Suggestions and awards ----

    // Read-only: one committee's members ranked by points for the badge's period, with who already has it.
    // POST because it takes a body.
    @PostMapping("/committee-ranking")
    public CommitteeRankingResponse committeeRanking(@Valid @RequestBody CommitteeRankingRequest request) {
        return rankingService.rank(request);
    }

    // Every badge awarded for a period.
    @GetMapping("/awards")
    public List<AwardView> listAwards(@RequestParam Long periodId) {
        return awardService.listForPeriod(periodId);
    }

    @PostMapping("/awards")
    public ResponseEntity<List<AwardView>> award(@Valid @RequestBody AwardRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(awardService.award(request, authentication.getName()));
    }

    // The member's description on an awarded badge.
    @PatchMapping("/awards/{id}")
    public AwardView updateNote(@PathVariable Long id, @Valid @RequestBody AwardNoteRequest request) {
        return awardService.updateNote(id, request.note());
    }

    @DeleteMapping("/awards/{id}")
    public ResponseEntity<Void> revoke(@PathVariable Long id, Authentication authentication) {
        awardService.revoke(id, authentication.getName());
        return ResponseEntity.noContent().build();
    }
}
