package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.ExclusiveFormDto;
import com.acm.acmwebsite.feature.entity.ExclusiveForm;
import com.acm.acmwebsite.feature.repository.ExclusiveFormRepository;
import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExclusiveFormService {

    private final ExclusiveFormRepository formRepository;
    static final Logger logger = LoggerFactory.getLogger(ExclusiveFormService.class);
    public ExclusiveFormService(ExclusiveFormRepository formRepository) {
        this.formRepository = formRepository;
    }

    @Transactional
    @CacheEvict(value = "homepageData", allEntries = true)
    public ExclusiveForm saveForm(ExclusiveForm form) {
        if (form.getIsActive() == null) {
            form.setIsActive(false);
        }
        return formRepository.save(form);
    }

    @Transactional
    @CacheEvict(value = "homepageData", allEntries = true)
    public ExclusiveForm updateForm(Long id, ExclusiveFormDto formDto) {
        ExclusiveForm form = formRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Form not found"));
                
        form.setTitle(formDto.getTitle());
        form.setDescription(formDto.getDescription());
        if (formDto.getImageUrl() != null) {
            form.setImageUrl(formDto.getImageUrl());
        }
        if (formDto.getSheetId() != null) {
            form.setSheetId(formDto.getSheetId());
        }
        if (formDto.getIsActive() != null) {
            form.setIsActive(formDto.getIsActive());
        }
        
        return formRepository.save(form);
    }

    @Transactional
    @CacheEvict(value = "homepageData", allEntries = true)
    public void deleteForm(Long id) {
        ExclusiveForm form = formRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Form not found"));
        formRepository.delete(form);
    }

    @Cacheable(value = "homepageData", key = "'ExclusiveForm_' + #id")
    public ExclusiveForm getFormById(Long id) {
        logger.info("Fetching form with id " + id +"from database...");
        return formRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Form not found"));
    }

    @Cacheable(value = "homepageData", key = "'ExclusiveForms_all'")
    public List<ExclusiveForm> getAllForms() {
        logger.info("Fetching all forms from database...");
        return formRepository.findAllByOrderByCreatedAtDesc();
    }

    @Cacheable(value = "homepageData", key = "'ExclusiveForms_active'")
    public List<ExclusiveForm> getActiveForms() {
        logger.info("Fetching all active forms from database...");
        return formRepository.findByIsActiveTrueOrderByCreatedAtDesc();
    }
}
