package com.acm.acmwebsite.feature.badges;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.AwardView;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.MemberBadgesResponse;

import lombok.RequiredArgsConstructor;

// Badges as members and visitors see them. `/api/public/**` is open to everyone (SecurityConfig);
// `/api/badges/me` needs a signed-in user.
@RestController
@RequiredArgsConstructor
public class BadgeController {

    private final BadgeAwardService awardService;

    @GetMapping("/api/badges/me")
    public List<AwardView> myBadges(Authentication authentication) {
        return awardService.listMine(authentication.getName());
    }

    // Clears the "new" marker on all of the signed-in member's badges.
    @PostMapping("/api/badges/me/seen")
    public ResponseEntity<Void> markSeen(Authentication authentication) {
        awardService.markMineSeen(authentication.getName());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/public/members/{userId}/badges")
    public MemberBadgesResponse memberBadges(@PathVariable UUID userId) {
        return awardService.memberBadges(userId);
    }

    @GetMapping("/api/public/badges/{publicId}")
    public AwardView publicBadge(@PathVariable UUID publicId) {
        return awardService.publicBadge(publicId);
    }
}
