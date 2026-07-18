package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.ClubCardDto;
import com.acm.acmwebsite.feature.dto.FormQuestionRequestDto;
import com.acm.acmwebsite.feature.dto.FormQuestionResponseDto;
import com.acm.acmwebsite.feature.dto.RegistrationAnalysisDto;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.service.ClubService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clubs")
public class ClubController {
    private final ClubService clubService;
    public ClubController(ClubService clubService) {
        this.clubService = clubService;
    }
    @GetMapping
    public ResponseEntity<Page<ClubCardDto>> getAllClubs(@RequestParam(defaultValue = "0") int page) {
        return ResponseEntity.ok(clubService.getClubsByPage(page));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Club> getClubById(@PathVariable("id") long id) {
        return clubService.getClubById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Club createClub(@RequestBody Club club) {
        return clubService.createClub(club);
    }
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Club updateClub(@PathVariable Long id,@RequestBody Club club) {
        return clubService.updateClub(id, club);
    }
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClub(@PathVariable Long id) {
        clubService.deleteClubById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/registrations/analysis")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegistrationAnalysisDto> getClubRegistrationAnalysis(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.getRegistrationAnalysis(id));
    }

    @PostMapping("/{id}/registrations/sheet")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RegistrationAnalysisDto> syncClubRegistrationsSheet(@PathVariable Long id) {
        return ResponseEntity.ok(clubService.syncRegistrationsSheet(id));
    }

    @PostMapping("/{id}/questions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FormQuestionResponseDto> createClubQuestion(
            @PathVariable("id") Long clubId,
            @RequestBody FormQuestionRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(clubService.createQuestion(clubId, request));
    }

    @PutMapping("/{id}/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<FormQuestionResponseDto> updateClubQuestion(
            @PathVariable("id") Long clubId,
            @PathVariable Long questionId,
            @RequestBody FormQuestionRequestDto request) {
        return ResponseEntity.ok(clubService.updateQuestion(clubId, questionId, request));
    }

    @DeleteMapping("/{id}/questions/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteClubQuestion(
            @PathVariable("id") Long clubId,
            @PathVariable Long questionId) {
        clubService.deleteQuestion(clubId, questionId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/social-links")
    public ResponseEntity<List<String>> getClubSocialLinks(@PathVariable("id") Long clubId) {
        return ResponseEntity.ok(clubService.getClubSocialLinks(clubId));
    }

    @PutMapping("/{id}/social-links")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<String>> updateClubSocialLinks(
            @PathVariable("id") Long clubId,
            @RequestBody List<String> socialLinks) {
        return ResponseEntity.ok(clubService.updateClubSocialLinks(clubId, socialLinks));
    }
}
