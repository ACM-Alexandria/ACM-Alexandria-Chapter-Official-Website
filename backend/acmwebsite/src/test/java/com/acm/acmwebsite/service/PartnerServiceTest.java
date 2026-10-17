package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.entity.Partner;
import com.acm.acmwebsite.feature.repository.PartnerRepository;
import com.acm.acmwebsite.feature.service.PartnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PartnerServiceTest {

    @Mock
    private PartnerRepository partnerRepository;

    @InjectMocks
    private PartnerService partnerService;

    private Partner samplePartner;

    @BeforeEach
    void setUp() {
        samplePartner = new Partner(1L, "TechCorp", "https://techcorp.example.com", "https://techcorp.example.com/logo.png");
    }

    @Test
    @DisplayName("getAllPartners - should return all partners from repository")
    void getAllPartners_shouldReturnList() {
        when(partnerRepository.findAll()).thenReturn(List.of(samplePartner));

        List<Partner> result = partnerService.getAllPartners();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("TechCorp", result.get(0).getName());
        verify(partnerRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("createPartner - should save and return new partner")
    void createPartner_shouldSave() {
        when(partnerRepository.save(any(Partner.class))).thenReturn(samplePartner);

        Partner result = partnerService.createPartner(samplePartner);

        assertNotNull(result);
        assertEquals(samplePartner.getId(), result.getId());
        assertEquals(samplePartner.getName(), result.getName());
        verify(partnerRepository, times(1)).save(samplePartner);
    }

    @Test
    @DisplayName("updatePartner - successfully update existing partner")
    void updatePartner_found_shouldUpdateFieldsAndSave() {
        Partner existing = new Partner(1L, "OldCorp", "https://old.com", "https://old.com/logo.png");
        Partner updated = new Partner(null, "NewCorp", "https://new.com", "https://new.com/logo.png");

        when(partnerRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(partnerRepository.save(existing)).thenReturn(existing);

        Partner result = partnerService.updatePartner(1L, updated);

        assertNotNull(result);
        assertEquals("NewCorp", result.getName());
        assertEquals("https://new.com", result.getWebsite());
        assertEquals("https://new.com/logo.png", result.getImageUrl());
        verify(partnerRepository, times(1)).findById(1L);
        verify(partnerRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("updatePartner - should throw RuntimeException when partner not found")
    void updatePartner_notFound_shouldThrow() {
        Partner updated = new Partner(null, "NewCorp", "https://new.com", "https://new.com/logo.png");
        when(partnerRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> partnerService.updatePartner(99L, updated));
        assertEquals("Partner not found with id: 99", ex.getMessage());
        verify(partnerRepository, times(1)).findById(99L);
        verify(partnerRepository, never()).save(any());
    }

    @Test
    @DisplayName("deletePartner - should call repository deleteById")
    void deletePartner_shouldCallRepository() {
        partnerService.deletePartner(1L);

        verify(partnerRepository, times(1)).deleteById(1L);
    }
}
