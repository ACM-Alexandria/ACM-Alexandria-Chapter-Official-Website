package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.ClubCardDto;
import com.acm.acmwebsite.feature.entity.Club;
import com.acm.acmwebsite.feature.service.ClubService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
