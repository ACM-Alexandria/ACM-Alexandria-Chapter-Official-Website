package com.acm.acmwebsite.feature.controller;

import com.acm.acmwebsite.feature.entity.SocialLink;
import com.acm.acmwebsite.feature.service.SocialLinkService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/socialLinks")
public class SocialLinkController {
    private final SocialLinkService socialLinkService;
    public SocialLinkController(SocialLinkService socialLinkService) {
        this.socialLinkService = socialLinkService;
    }
    @GetMapping
    public List<SocialLink> getSocialLinks() {
        return socialLinkService.getAllLinks();
    }
    @GetMapping("/{id}")
    public ResponseEntity getSocialLinkById(@PathVariable Long id) {
        return socialLinkService.getLinkById(id).map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping
    public SocialLink createSocialLink(@RequestBody SocialLink socialLink) {
        return socialLinkService.createSocialLink(socialLink);
    }
    @PutMapping("{id}")
    public SocialLink updateSocialLink(@PathVariable Long id, @RequestBody SocialLink socialLink) {
        return socialLinkService.updateSocialLink(id, socialLink);
    }
    @DeleteMapping ("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id){
        socialLinkService.deleteSocialLink(id);
        return ResponseEntity.noContent().build();
    }

}
