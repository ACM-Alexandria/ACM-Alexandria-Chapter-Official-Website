package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.entity.GalleryImage;
import com.acm.acmwebsite.feature.service.GalleryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/gallery")
public class GalleryController {

    private final GalleryService service;

    public GalleryController(GalleryService service) {
        this.service = service;
    }

    @GetMapping
    public List<GalleryImage> getAll() {
        return service.getAll();
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public ResponseEntity<GalleryImage> add(@RequestBody GalleryImage image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.add(image));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody GalleryImage image) {
        try {
            return ResponseEntity.ok(service.update(id, image));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
