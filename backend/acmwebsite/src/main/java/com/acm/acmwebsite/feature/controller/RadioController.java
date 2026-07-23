package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.RadioEpisodeDto;
import com.acm.acmwebsite.feature.dto.RadioSeasonDto;
import com.acm.acmwebsite.feature.service.RadioService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.data.domain.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@RestController
@RequestMapping("/api/radio")
public class RadioController {

    private static final Logger logger = LoggerFactory.getLogger(RadioController.class);
    private final RadioService radioService;

    public RadioController(RadioService radioService) {
        this.radioService = radioService;
    }

    // ── Public Endpoints ──

    @GetMapping("/seasons")
    public ResponseEntity<Page<RadioSeasonDto>> getAllSeasons(@RequestParam(value = "page", defaultValue = "0") int page) {
        logger.info("Request received: GET /api/radio/seasons with page={}", page);
        return ResponseEntity.ok(radioService.getSeasonsByPage(page));
    }

    @GetMapping("/seasons/{id}")
    public ResponseEntity<RadioSeasonDto> getSeasonById(@PathVariable Long id) {
        logger.info("Request received: GET /api/radio/seasons/{}", id);
        return radioService.getSeasonById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/seasons/{id}/episodes")
    public ResponseEntity<Page<RadioEpisodeDto>> getEpisodesBySeason(
            @PathVariable Long id,
            @RequestParam(value = "page", defaultValue = "0") int page) {
        logger.info("Request received: GET /api/radio/seasons/{}/episodes with page={}", id, page);
        return ResponseEntity.ok(radioService.getEpisodesBySeason(id, page));
    }

    @GetMapping("/seasons/{id}/episodes/page/{page}")
    public ResponseEntity<Page<RadioEpisodeDto>> getEpisodesBySeasonPage(
            @PathVariable Long id,
            @PathVariable int page) {
        logger.info("Request received: GET /api/radio/seasons/{}/episodes/page/{}", id, page);
        return ResponseEntity.ok(radioService.getEpisodesBySeason(id, page));
    }

    // ── Admin-only Season Operations ──

    @PostMapping("/seasons")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RadioSeasonDto> createSeason(@RequestBody RadioSeasonDto seasonDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(radioService.createSeason(seasonDto));
    }

    @PutMapping("/seasons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RadioSeasonDto> updateSeason(@PathVariable Long id, @RequestBody RadioSeasonDto seasonDto) {
        return ResponseEntity.ok(radioService.updateSeason(id, seasonDto));
    }

    @DeleteMapping("/seasons/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSeason(@PathVariable Long id) {
        radioService.deleteSeason(id);
        return ResponseEntity.noContent().build();
    }

    // ── Admin-only Episode Operations ──

    @PostMapping("/episodes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RadioEpisodeDto> createEpisode(@RequestBody RadioEpisodeDto episodeDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(radioService.createEpisode(episodeDto));
    }

    @PutMapping("/episodes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<RadioEpisodeDto> updateEpisode(@PathVariable Long id, @RequestBody RadioEpisodeDto episodeDto) {
        return ResponseEntity.ok(radioService.updateEpisode(id, episodeDto));
    }

    @DeleteMapping("/episodes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEpisode(@PathVariable Long id) {
        radioService.deleteEpisode(id);
        return ResponseEntity.noContent().build();
    }
}
