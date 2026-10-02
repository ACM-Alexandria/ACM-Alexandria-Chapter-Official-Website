package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.core.constants.CacheNames;
import com.acm.acmwebsite.feature.entity.Partner;
import com.acm.acmwebsite.feature.repository.PartnerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartnerService {

    private final PartnerRepository partnerRepository;
    private static final Logger logger = LoggerFactory.getLogger(PartnerService.class);
    public PartnerService(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    @Cacheable(value = CacheNames.PARTNERS, key = "'Partners'")
    public List<Partner> getAllPartners() {
        logger.info("fetching all partners from database...");
        return partnerRepository.findAll();
    }

    @CacheEvict(value = CacheNames.PARTNERS, allEntries = true)
    public Partner createPartner(Partner partner) {
        return partnerRepository.save(partner);
    }
    @CacheEvict(value = CacheNames.PARTNERS, allEntries = true)
    public Partner updatePartner(Long id, Partner updatedPartner) {
        return partnerRepository.findById(id).map(partner -> {
            partner.setName(updatedPartner.getName());
            partner.setWebsite(updatedPartner.getWebsite());
            partner.setImageUrl(updatedPartner.getImageUrl());
            return partnerRepository.save(partner);
        }).orElseThrow(() -> new RuntimeException("Partner not found with id: " + id));
    }
    @CacheEvict(value = CacheNames.PARTNERS, allEntries = true)
    public void deletePartner(Long id) {
        partnerRepository.deleteById(id);
    }
}
