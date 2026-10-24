package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.BoardArchiveRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.BoardArchiveSummary;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.BoardSeasonsResponse;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastBoardResponse;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastClubBoardRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastClubBoardResponse;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastClubRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastClubResponse;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastCommitteeBoardRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastCommitteeBoardResponse;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastCommitteeRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastCommitteeResponse;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastHighBoardRequest;
import com.acm.acmwebsite.feature.dto.PastSeasonsDtos.PastHighBoardResponse;
import com.acm.acmwebsite.feature.service.PastSeasonsService;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

// All past-board endpoints. Public board views first, then the SUPER_ADMIN management endpoints.
@RestController
@RequiredArgsConstructor
public class PastSeasonsController {
    private final PastSeasonsService service;
    private final SystemSettingsService systemSettingsService;

    // ----- public board views -----

    @GetMapping("/api/boards/seasons")
    public BoardSeasonsResponse getBoardSeasons() {
        return BoardSeasonsResponse.builder().currentSeason(systemSettingsService.getCurrentSeason())
                .pastSeasons(service.getSeasons()).build();
    }

    @GetMapping("/api/boards/past/{season}")
    public PastBoardResponse getPastBoards(@PathVariable Integer season) {
        return service.getBySeason(season);
    }

    // ----- archive and season info (admin) -----

    @PostMapping("/api/admin/boards/archive")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public BoardArchiveSummary archive(@Valid @RequestBody BoardArchiveRequest request) {
        return service.archive(request.getSeason());
    }

    @GetMapping("/api/admin/boards/past/seasons")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public List<Integer> listPastSeasons() {
        return service.getSeasons();
    }

    @GetMapping("/api/admin/boards/current-season")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public Map<String, Integer> currentSeason() {
        return Map.of("currentSeason", systemSettingsService.getCurrentSeason());
    }

    // ----- past clubs (admin) -----

    @GetMapping("/api/admin/boards/past/clubs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public List<PastClubResponse> listPastClubs(@RequestParam Integer season) {
        return service.listPastClubs(season);
    }

    @PostMapping("/api/admin/boards/past/clubs")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public PastClubResponse createPastClub(@Valid @RequestBody PastClubRequest request) {
        return service.createPastClub(request);
    }

    @PutMapping("/api/admin/boards/past/clubs/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public PastClubResponse updatePastClub(@PathVariable Long id, @Valid @RequestBody PastClubRequest request) {
        return service.updatePastClub(id, request);
    }

    @DeleteMapping("/api/admin/boards/past/clubs/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public void deletePastClub(@PathVariable Long id) {
        service.deletePastClub(id);
    }

    // ----- past club board members (admin) -----

    @GetMapping("/api/admin/boards/past/club-board")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public List<PastClubBoardResponse> listPastClubBoards(@RequestParam Integer season) {
        return service.listPastClubBoards(season);
    }

    @PostMapping("/api/admin/boards/past/club-board")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public PastClubBoardResponse createPastClubBoard(@Valid @RequestBody PastClubBoardRequest request) {
        return service.createPastClubBoard(request);
    }

    @PutMapping("/api/admin/boards/past/club-board/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public PastClubBoardResponse updatePastClubBoard(@PathVariable Long id, @Valid @RequestBody PastClubBoardRequest request) {
        return service.updatePastClubBoard(id, request);
    }

    @DeleteMapping("/api/admin/boards/past/club-board/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public void deletePastClubBoard(@PathVariable Long id) {
        service.deletePastClubBoard(id);
    }

    // ----- past committees (admin) -----

    @GetMapping("/api/admin/boards/past/committees")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public List<PastCommitteeResponse> listPastCommittees(@RequestParam Integer season) {
        return service.listPastCommittees(season);
    }

    @PostMapping("/api/admin/boards/past/committees")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public PastCommitteeResponse createPastCommittee(@Valid @RequestBody PastCommitteeRequest request) {
        return service.createPastCommittee(request);
    }

    @PutMapping("/api/admin/boards/past/committees/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public PastCommitteeResponse updatePastCommittee(@PathVariable Long id, @Valid @RequestBody PastCommitteeRequest request) {
        return service.updatePastCommittee(id, request);
    }

    @DeleteMapping("/api/admin/boards/past/committees/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public void deletePastCommittee(@PathVariable Long id) {
        service.deletePastCommittee(id);
    }

    // ----- past committee board members (admin) -----

    @GetMapping("/api/admin/boards/past/committee-board")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public List<PastCommitteeBoardResponse> listPastCommitteeBoards(@RequestParam Integer season) {
        return service.listPastCommitteeBoards(season);
    }

    @PostMapping("/api/admin/boards/past/committee-board")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public PastCommitteeBoardResponse createPastCommitteeBoard(@Valid @RequestBody PastCommitteeBoardRequest request) {
        return service.createPastCommitteeBoard(request);
    }

    @PutMapping("/api/admin/boards/past/committee-board/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public PastCommitteeBoardResponse updatePastCommitteeBoard(@PathVariable Long id, @Valid @RequestBody PastCommitteeBoardRequest request) {
        return service.updatePastCommitteeBoard(id, request);
    }

    @DeleteMapping("/api/admin/boards/past/committee-board/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public void deletePastCommitteeBoard(@PathVariable Long id) {
        service.deletePastCommitteeBoard(id);
    }

    // ----- past high board (admin) -----

    @GetMapping("/api/admin/boards/past/high-board")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public List<PastHighBoardResponse> listPastHighBoards(@RequestParam Integer season) {
        return service.listPastHighBoards(season);
    }

    @PostMapping("/api/admin/boards/past/high-board")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public PastHighBoardResponse createPastHighBoard(@Valid @RequestBody PastHighBoardRequest request) {
        return service.createPastHighBoard(request);
    }

    @PutMapping("/api/admin/boards/past/high-board/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public PastHighBoardResponse updatePastHighBoard(@PathVariable Long id, @Valid @RequestBody PastHighBoardRequest request) {
        return service.updatePastHighBoard(id, request);
    }

    @DeleteMapping("/api/admin/boards/past/high-board/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public void deletePastHighBoard(@PathVariable Long id) {
        service.deletePastHighBoard(id);
    }
}
