package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.User_Authentication.repository.UserRepository;
import com.acm.acmwebsite.feature.dto.HRFeedbackRequest;
import com.acm.acmwebsite.feature.dto.HRFeedbackResponse;
import com.acm.acmwebsite.feature.service.HRFeedbackService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/hr-feedback")
public class HRFeedbackController {

    private final HRFeedbackService hrFeedbackService;
    private final UserRepository userRepository;

    public HRFeedbackController(HRFeedbackService hrFeedbackService, UserRepository userRepository) {
        this.hrFeedbackService = hrFeedbackService;
        this.userRepository = userRepository;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated() and !hasRole('USER')")
    public ResponseEntity<HRFeedbackResponse> createFeedback(
            @RequestBody HRFeedbackRequest request,
            Authentication authentication) {
        String email = (authentication != null && authentication.isAuthenticated()) ? authentication.getName() : null;
        HRFeedbackResponse saved = hrFeedbackService.saveFeedback(request, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> getAllFeedback(Authentication authentication) {
        String email = authentication.getName();
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User not found.");
        }

        User user = userOpt.get();
        boolean isSuperAdmin = "SUPER_ADMIN".equals(user.getRole().name());
        boolean isHRBoard = "ACM_COMMITTEE_BOARD".equals(user.getRole().name()) &&
                user.getCommittee() != null &&
                "HR Committee".equalsIgnoreCase(user.getCommittee().getName());

        if (!isSuperAdmin && !isHRBoard) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Access Denied: Only HR Board members can view this data.");
        }

        List<HRFeedbackResponse> feedbacks = hrFeedbackService.getAllFeedback();
        return ResponseEntity.ok(feedbacks);
    }
}
