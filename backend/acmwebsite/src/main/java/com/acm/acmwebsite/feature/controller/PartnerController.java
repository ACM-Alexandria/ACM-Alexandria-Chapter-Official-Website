package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.entity.Partner;
import com.acm.acmwebsite.feature.service.PartnerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/partners")
public class PartnerController {

    private final PartnerService partnerService;

    public PartnerController(PartnerService partnerService) {
        this.partnerService = partnerService;
    }

    @GetMapping
    public ResponseEntity<List<Partner>> getAllPartners() {
        return ResponseEntity.ok(partnerService.getAllPartners());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public ResponseEntity<Partner> createPartner(@RequestBody Partner partner) {
        return ResponseEntity.status(HttpStatus.CREATED).body(partnerService.createPartner(partner));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public ResponseEntity<?> updatePartner(@PathVariable Long id, @RequestBody Partner partner) {
        try {
            return ResponseEntity.ok(partnerService.updatePartner(id, partner));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public ResponseEntity<Void> deletePartner(@PathVariable Long id) {
        partnerService.deletePartner(id);
        return ResponseEntity.noContent().build();
    }
}
