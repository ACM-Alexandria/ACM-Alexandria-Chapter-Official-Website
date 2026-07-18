package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.entity.BugReport;
import com.acm.acmwebsite.feature.entity.FeatureSuggestion;
import com.acm.acmwebsite.feature.service.FeedbackService;
import com.acm.acmwebsite.feature.service.ImageUploadService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.acm.acmwebsite.feature.dto.BugReportResponse;
import com.acm.acmwebsite.feature.dto.FeatureSuggestionResponse;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;
    private final ImageUploadService imageUploadService;

    public FeedbackController(FeedbackService feedbackService, ImageUploadService imageUploadService) {
        this.feedbackService = feedbackService;
        this.imageUploadService = imageUploadService;
    }

    @PostMapping("/features")
    public ResponseEntity<?> createFeatureSuggestion(
            @RequestBody FeatureSuggestion featureSuggestion,
            Authentication authentication) {
        String email = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : null;
        FeatureSuggestion saved = feedbackService.saveFeatureSuggestion(featureSuggestion, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/bugs")
    public ResponseEntity<?> createBugReport(
            @RequestBody BugReportRequest request,
            Authentication authentication) {
        BugReport br = new BugReport();
        br.setName(request.getName());
        br.setDescription(request.getDescription());
        String email = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : null;
        BugReport saved = feedbackService.saveBugReport(br, request.getImageUrls(), email);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PostMapping("/upload-screenshot")
    public ResponseEntity<?> uploadScreenshot(@RequestParam("file") MultipartFile file) {
        try {
            String url = imageUploadService.uploadImage(file);
            return ResponseEntity.ok(Map.of("url", url));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(Map.of("error", "Failed to upload image: " + e.getMessage()));
        }
    }

    @GetMapping("/features")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<FeatureSuggestionResponse>> getAllFeatures() {
        return ResponseEntity.ok(feedbackService.getAllFeatureSuggestions());
    }

    @GetMapping("/bugs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<BugReportResponse>> getAllBugs() {
        return ResponseEntity.ok(feedbackService.getAllBugReports());
    }

    @PutMapping("/features/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleFeatureStatus(@PathVariable Long id) {
        try {
            feedbackService.toggleFeatureSuggestionStatus(id);
            return ResponseEntity.ok(Map.of("message", "Feature suggestion status toggled successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/bugs/{id}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> toggleBugStatus(@PathVariable Long id) {
        try {
            feedbackService.toggleBugReportStatus(id);
            return ResponseEntity.ok(Map.of("message", "Bug report status toggled successfully"));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", e.getMessage()));
        }
    }

    // Static request DTO for bug reporting
    public static class BugReportRequest {
        private String name;
        private String description;
        private List<String> imageUrls;

        public BugReportRequest() {
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public List<String> getImageUrls() {
            return imageUrls;
        }

        public void setImageUrls(List<String> imageUrls) {
            this.imageUrls = imageUrls;
        }
    }
}
