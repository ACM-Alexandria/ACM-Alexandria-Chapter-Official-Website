package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.dto.ExclusiveFormDto;
import com.acm.acmwebsite.feature.entity.ExclusiveForm;
import com.acm.acmwebsite.feature.repository.ExclusiveFormRepository;
import com.acm.acmwebsite.feature.service.ExclusiveFormService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExclusiveFormServiceTest {

    @Mock
    private ExclusiveFormRepository formRepository;

    @InjectMocks
    private ExclusiveFormService exclusiveFormService;

    private ExclusiveForm sampleForm;

    private ExclusiveForm createForm(Long id, String title, Boolean active) {
        ExclusiveForm form = new ExclusiveForm();
        form.setId(id);
        form.setTitle(title);
        form.setDescription("Exclusive workshop for members");
        form.setImageUrl("https://example.com/banner.png");
        form.setSheetId("sheet123");
        form.setIsActive(active);
        form.setQuestions(new ArrayList<>());
        form.setCreatedAt(LocalDateTime.now());
        form.setUpdatedAt(LocalDateTime.now());
        return form;
    }

    @BeforeEach
    void setUp() {
        sampleForm = createForm(1L, "Workshop Registration", true);
    }

    @Test
    @DisplayName("saveForm - should default isActive to false when isActive is null")
    void saveForm_nullIsActive_shouldDefaultToFalse() {
        ExclusiveForm form = createForm(null, "New Form", null);

        when(formRepository.save(any(ExclusiveForm.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExclusiveForm saved = exclusiveFormService.saveForm(form);

        assertNotNull(saved);
        assertFalse(saved.getIsActive());
        verify(formRepository, times(1)).save(form);
    }

    @Test
    @DisplayName("saveForm - should keep isActive true when provided as true")
    void saveForm_trueIsActive_shouldRetainValue() {
        sampleForm.setIsActive(true);
        when(formRepository.save(any(ExclusiveForm.class))).thenReturn(sampleForm);

        ExclusiveForm saved = exclusiveFormService.saveForm(sampleForm);

        assertNotNull(saved);
        assertTrue(saved.getIsActive());
        verify(formRepository, times(1)).save(sampleForm);
    }

    @Test
    @DisplayName("updateForm - successfully update all fields when DTO fields are non-null")
    void updateForm_allFieldsProvided_shouldUpdateAll() {
        ExclusiveFormDto dto = new ExclusiveFormDto();
        dto.setTitle("Updated Title");
        dto.setDescription("Updated Description");
        dto.setImageUrl("https://example.com/new.png");
        dto.setSheetId("newSheetId");
        dto.setIsActive(false);

        when(formRepository.findById(1L)).thenReturn(Optional.of(sampleForm));
        when(formRepository.save(any(ExclusiveForm.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExclusiveForm result = exclusiveFormService.updateForm(1L, dto);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("https://example.com/new.png", result.getImageUrl());
        assertEquals("newSheetId", result.getSheetId());
        assertFalse(result.getIsActive());
        verify(formRepository, times(1)).findById(1L);
        verify(formRepository, times(1)).save(sampleForm);
    }

    @Test
    @DisplayName("updateForm - retain existing optional fields when DTO fields are null")
    void updateForm_nullOptionalFields_shouldRetainExistingValues() {
        ExclusiveFormDto dto = new ExclusiveFormDto();
        dto.setTitle("Updated Title");
        dto.setDescription("Updated Description");
        dto.setImageUrl(null);
        dto.setSheetId(null);
        dto.setIsActive(null);

        when(formRepository.findById(1L)).thenReturn(Optional.of(sampleForm));
        when(formRepository.save(any(ExclusiveForm.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ExclusiveForm result = exclusiveFormService.updateForm(1L, dto);

        assertNotNull(result);
        assertEquals("Updated Title", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals("https://example.com/banner.png", result.getImageUrl());
        assertEquals("sheet123", result.getSheetId());
        assertTrue(result.getIsActive());
        verify(formRepository, times(1)).save(sampleForm);
    }

    @Test
    @DisplayName("updateForm - should throw EntityNotFoundException when form not found")
    void updateForm_notFound_shouldThrow() {
        ExclusiveFormDto dto = new ExclusiveFormDto();
        dto.setTitle("Title");
        when(formRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> exclusiveFormService.updateForm(99L, dto));
        assertEquals("Form not found", ex.getMessage());
        verify(formRepository, never()).save(any());
    }

    @Test
    @DisplayName("deleteForm - successfully delete existing form")
    void deleteForm_existing_shouldDelete() {
        when(formRepository.findById(1L)).thenReturn(Optional.of(sampleForm));

        exclusiveFormService.deleteForm(1L);

        verify(formRepository, times(1)).findById(1L);
        verify(formRepository, times(1)).delete(sampleForm);
    }

    @Test
    @DisplayName("deleteForm - should throw EntityNotFoundException when form not found")
    void deleteForm_notFound_shouldThrow() {
        when(formRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> exclusiveFormService.deleteForm(99L));
        assertEquals("Form not found", ex.getMessage());
        verify(formRepository, never()).delete(any());
    }

    @Test
    @DisplayName("getFormById - successfully return form when found")
    void getFormById_found_shouldReturnForm() {
        when(formRepository.findById(1L)).thenReturn(Optional.of(sampleForm));

        ExclusiveForm result = exclusiveFormService.getFormById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Workshop Registration", result.getTitle());
        verify(formRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("getFormById - should throw EntityNotFoundException when form not found")
    void getFormById_notFound_shouldThrow() {
        when(formRepository.findById(99L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> exclusiveFormService.getFormById(99L));
        assertEquals("Form not found", ex.getMessage());
        verify(formRepository, times(1)).findById(99L);
    }

    @Test
    @DisplayName("getAllForms - should return list ordered by creation date descending")
    void getAllForms_shouldReturnAllFormsOrdered() {
        when(formRepository.findAllByOrderByCreatedAtDesc()).thenReturn(List.of(sampleForm));

        List<ExclusiveForm> result = exclusiveFormService.getAllForms();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleForm.getTitle(), result.get(0).getTitle());
        verify(formRepository, times(1)).findAllByOrderByCreatedAtDesc();
    }

    @Test
    @DisplayName("getActiveForms - should return active forms ordered by creation date descending")
    void getActiveForms_shouldReturnActiveFormsOrdered() {
        when(formRepository.findByIsActiveTrueOrderByCreatedAtDesc()).thenReturn(List.of(sampleForm));

        List<ExclusiveForm> result = exclusiveFormService.getActiveForms();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getIsActive());
        verify(formRepository, times(1)).findByIsActiveTrueOrderByCreatedAtDesc();
    }
}
