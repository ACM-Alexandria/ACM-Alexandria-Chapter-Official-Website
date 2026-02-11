package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.SocialLink;
import com.acm.acmwebsite.feature.repository.SocialLinkRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SocialLinkService {
    private final SocialLinkRepository socialLinkRepository;
    public SocialLinkService(SocialLinkRepository socialLinkRepository) {
        this.socialLinkRepository = socialLinkRepository;
    }
    public List<SocialLink> getAllLinks(){
        return socialLinkRepository.findAll();
    }
    public Optional<SocialLink> getLinkById(Long id){
        return socialLinkRepository.findById(id);
    }
    public SocialLink createSocialLink(SocialLink socialLink){
        return socialLinkRepository.save(socialLink);
    }
    public SocialLink updateSocialLink(Long id,SocialLink updatedSocialLink){
        return socialLinkRepository.findById(id).map(socialLink->{
            socialLink.setUrl(updatedSocialLink.getUrl());
            socialLink.setPlatform(updatedSocialLink.getPlatform());
            return  socialLinkRepository.save(socialLink);
                }
        ).orElseThrow(()->new RuntimeException("Social Link not found"));
    }
    public void deleteSocialLink(Long id){
        socialLinkRepository.deleteById(id);
    }
}
