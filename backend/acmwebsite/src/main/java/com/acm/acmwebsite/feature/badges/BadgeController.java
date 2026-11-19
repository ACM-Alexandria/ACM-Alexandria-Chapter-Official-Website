package com.acm.acmwebsite.feature.badges;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.AwardView;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.MemberBadgesResponse;

import lombok.RequiredArgsConstructor;

// Badges as members, admins and visitors see them. The single badge page (`/api/public/badges/...`) is open
// to everyone (SecurityConfig); the rest needs a signed-in user.
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

    // A member's badges for the admin and HR member view: the same people who can open a member's profile
    // there (see UserController#adminGetUserProfile).
    @GetMapping("/api/badges/members/{userId}")
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'ACM_HIGH_BOARD', 'ACM_COMMITTEE_BOARD', 'ACM_CLUB_BOARD')"
            + " or @authorizationServiceImpl.isHrCommitteeMember(authentication)")
    public MemberBadgesResponse memberBadges(@PathVariable UUID userId) {
        return awardService.memberBadges(userId);
    }

    @GetMapping("/api/public/badges/{publicId}")
    public AwardView publicBadge(@PathVariable UUID publicId) {
        return awardService.publicBadge(publicId);
    }
}
