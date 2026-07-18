package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.service.SubscriptionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    public SubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/news/subscribe")
    public ResponseEntity<?> subscribeToNews(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }
        try {
            subscriptionService.subscribeToNews(authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Successfully subscribed to news"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/news/unsubscribe")
    public ResponseEntity<?> unsubscribeFromNews(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("message", "Unauthorized"));
        }
        try {
            subscriptionService.unsubscribeFromNews(authentication.getName());
            return ResponseEntity.ok(Map.of("message", "Successfully unsubscribed from news"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    @GetMapping("/news/subscription-status")
    public ResponseEntity<?> getNewsSubscriptionStatus(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() || authentication.getName() == null) {
            return ResponseEntity.ok(Map.of("subscribed", false));
        }
        try {
            boolean isSubscribed = subscriptionService.isSubscribedToNews(authentication.getName());
            return ResponseEntity.ok(Map.of("subscribed", isSubscribed));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("subscribed", false));
        }
    }
}
