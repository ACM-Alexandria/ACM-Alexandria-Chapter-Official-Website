package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.dto.ExclusiveFormDto;
import com.acm.acmwebsite.feature.entity.ExclusiveForm;
import com.acm.acmwebsite.feature.repository.ExclusiveFormRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExclusiveFormService {

    private final ExclusiveFormRepository formRepository;

    public ExclusiveFormService(ExclusiveFormRepository formRepository) {
        this.formRepository = formRepository;
    }

    @Transactional
    public ExclusiveForm saveForm(ExclusiveForm form) {
        if (form.getIsActive() == null) {
            form.setIsActive(false);
        }
        return formRepository.save(form);
    }

    @Transactional
    public ExclusiveForm updateForm(Long id, ExclusiveFormDto formDto) {
        ExclusiveForm form = formRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Form not found"));
                
        form.setTitle(formDto.getTitle());
        form.setDescription(formDto.getDescription());
        if (formDto.getIsActive() != null) {
            form.setIsActive(formDto.getIsActive());
        }
        
        return formRepository.save(form);
    }

    @Transactional
    public void deleteForm(Long id) {
        ExclusiveForm form = formRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Form not found"));
        formRepository.delete(form);
    }

    public ExclusiveForm getFormById(Long id) {
        return formRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Form not found"));
    }

    public List<ExclusiveForm> getAllForms() {
        return formRepository.findAll();
    }

    public List<ExclusiveForm> getActiveForms() {
        return formRepository.findByIsActiveTrue();
    }
}
