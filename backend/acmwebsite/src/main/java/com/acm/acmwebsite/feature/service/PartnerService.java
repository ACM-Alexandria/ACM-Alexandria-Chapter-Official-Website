package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.Partner;
import com.acm.acmwebsite.feature.repository.PartnerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PartnerService {

    private final PartnerRepository partnerRepository;

    public PartnerService(PartnerRepository partnerRepository) {
        this.partnerRepository = partnerRepository;
    }

    public List<Partner> getAllPartners() {
        return partnerRepository.findAll();
    }

    public Partner createPartner(Partner partner) {
        return partnerRepository.save(partner);
    }

    public Partner updatePartner(Long id, Partner updatedPartner) {
        return partnerRepository.findById(id).map(partner -> {
            partner.setName(updatedPartner.getName());
            partner.setWebsite(updatedPartner.getWebsite());
            partner.setImageUrl(updatedPartner.getImageUrl());
            return partnerRepository.save(partner);
        }).orElseThrow(() -> new RuntimeException("Partner not found with id: " + id));
    }

    public void deletePartner(Long id) {
        partnerRepository.deleteById(id);
    }
}
