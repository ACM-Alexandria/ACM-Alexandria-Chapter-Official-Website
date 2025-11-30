package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.entity.Message;
import com.acm.acmwebsite.feature.entity.Subscription;
import com.acm.acmwebsite.feature.service.SubscriptionService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subscription")
public class TestController {
    private final SubscriptionService subscriptionService;
    public TestController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }
    @PostMapping
    public ResponseEntity<?> createSubscription() {
        var subscription = new Subscription();
        subscriptionService.addSubscription(subscription);
        subscriptionService.sendConfirmationEmail(subscription,new Message());
        return ResponseEntity.ok().build();
    }
}

