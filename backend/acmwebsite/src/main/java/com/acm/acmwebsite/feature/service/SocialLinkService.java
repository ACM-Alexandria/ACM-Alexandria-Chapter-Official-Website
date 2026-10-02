package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.core.constants.CacheNames;
import com.acm.acmwebsite.feature.entity.SocialLink;
import com.acm.acmwebsite.feature.repository.SocialLinkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SocialLinkService {
    private final SocialLinkRepository socialLinkRepository;
    private static final Logger logger = LoggerFactory.getLogger(SocialLinkService.class);

    public SocialLinkService(SocialLinkRepository socialLinkRepository) {
        this.socialLinkRepository = socialLinkRepository;
    }
    @Cacheable(value = CacheNames.SOCIAL_LINKS,key = "'AllLinks'")
    public List<SocialLink> getAllLinks(){
        logger.info("Fetching All Links from Database...");
        return socialLinkRepository.findAll();
    }

    @Cacheable(value = CacheNames.SOCIAL_LINKS,key = "'Link'+#id")
    public Optional<SocialLink> getLinkById(Long id){
        logger.info("Fetching Link from Database...");
        return socialLinkRepository.findById(id);
    }
    @CacheEvict(value = CacheNames.SOCIAL_LINKS, allEntries = true)
    public SocialLink createSocialLink(SocialLink socialLink){
        return socialLinkRepository.save(socialLink);
    }
    @CacheEvict(value = CacheNames.SOCIAL_LINKS, allEntries = true)
    public SocialLink updateSocialLink(Long id,SocialLink updatedSocialLink){
        return socialLinkRepository.findById(id).map(socialLink->{
            socialLink.setUrl(updatedSocialLink.getUrl());
            socialLink.setPlatform(updatedSocialLink.getPlatform());
            return  socialLinkRepository.save(socialLink);
                }
        ).orElseThrow(()->new RuntimeException("Social Link not found"));
    }
    @CacheEvict(value = CacheNames.SOCIAL_LINKS, allEntries = true)
    public void deleteSocialLink(Long id){
        socialLinkRepository.deleteById(id);
    }
}
