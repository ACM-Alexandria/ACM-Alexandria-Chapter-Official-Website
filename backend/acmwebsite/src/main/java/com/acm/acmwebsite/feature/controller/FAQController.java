package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.dto.FAQDto;
import com.acm.acmwebsite.feature.dto.FAQReorderDto;
import com.acm.acmwebsite.feature.service.FAQService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/faq")
public class FAQController {
    private final FAQService faqService;

    public FAQController (FAQService faqService) {
        this.faqService=faqService;
    }

    @GetMapping
    public ResponseEntity<Page<FAQDto>> getAllFAQs(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String query) {
        if (page < 0 || size < 1 || size > 100) {
            return ResponseEntity.badRequest().build();
        }
        PageRequest pageable = PageRequest.of(page, size, Sort.by("category").ascending().and(Sort.by("order").ascending()).and(Sort.by("id").ascending()));
        return ResponseEntity.ok(faqService.getFAQs(query, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FAQDto> getFAQ(@PathVariable int id) {
        return ResponseEntity.ok(faqService.getFAQ(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public ResponseEntity<FAQDto> addFAQ(@Valid @RequestBody FAQDto faqDto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(faqService.addFAQ(faqDto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public ResponseEntity<FAQDto> updateFAQ(@PathVariable int id, @Valid @RequestBody FAQDto faqDto) {
        return ResponseEntity.ok(faqService.updateFAQ(id, faqDto));
    }

    @PutMapping("/reorder")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public ResponseEntity<List<FAQDto>> reorderFAQs(@Valid @RequestBody List<FAQReorderDto> items) {
        return ResponseEntity.ok(faqService.reorderFAQs(items));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')")
    public ResponseEntity<Void> deleteFAQ(@PathVariable int id) {
        faqService.deleteFAQ(id);
        return ResponseEntity.noContent().build();
    }
}
