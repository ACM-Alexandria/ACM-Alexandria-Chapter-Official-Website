package com.acm.acmwebsite.service;

import com.acm.acmwebsite.feature.entity.GalleryImage;
import com.acm.acmwebsite.feature.repository.GalleryImageRepository;
import com.acm.acmwebsite.feature.service.GalleryService;
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
class GalleryServiceTest {

    @Mock
    private GalleryImageRepository galleryImageRepository;

    @InjectMocks
    private GalleryService galleryService;

    private GalleryImage sampleImage;

    @BeforeEach
    void setUp() {
        sampleImage = new GalleryImage(1L, "https://example.com/img1.jpg", "Opening Ceremony");
    }

    @Test
    @DisplayName("getAll - should return all gallery images from repository")
    void getAll_shouldReturnAllImages() {
        when(galleryImageRepository.findAll()).thenReturn(List.of(sampleImage));

        List<GalleryImage> result = galleryService.getAll();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(sampleImage.getId(), result.get(0).getId());
        assertEquals("https://example.com/img1.jpg", result.get(0).getImageUrl());
        assertEquals("Opening Ceremony", result.get(0).getCaption());
        verify(galleryImageRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("add - successfully save gallery image when imageUrl is valid")
    void add_validImageUrl_shouldSave() {
        when(galleryImageRepository.save(any(GalleryImage.class))).thenReturn(sampleImage);

        GalleryImage result = galleryService.add(sampleImage);

        assertNotNull(result);
        assertEquals(sampleImage.getId(), result.getId());
        verify(galleryImageRepository, times(1)).save(sampleImage);
    }

    @Test
    @DisplayName("add - should throw IllegalArgumentException when imageUrl is null")
    void add_nullImageUrl_shouldThrow() {
        sampleImage.setImageUrl(null);

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> galleryService.add(sampleImage));
        assertEquals("imageUrl is required", ex.getMessage());
        verify(galleryImageRepository, never()).save(any());
    }

    @Test
    @DisplayName("add - should throw IllegalArgumentException when imageUrl is blank")
    void add_blankImageUrl_shouldThrow() {
        sampleImage.setImageUrl("   ");

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> galleryService.add(sampleImage));
        assertEquals("imageUrl is required", ex.getMessage());
        verify(galleryImageRepository, never()).save(any());
    }

    @Test
    @DisplayName("update - successfully update both imageUrl and caption when new imageUrl is provided")
    void update_withNewImageUrl_shouldUpdateAllFields() {
        GalleryImage existing = new GalleryImage(1L, "https://example.com/old.jpg", "Old Caption");
        GalleryImage updated = new GalleryImage(null, "https://example.com/new.jpg", "New Caption");

        when(galleryImageRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(galleryImageRepository.save(existing)).thenReturn(existing);

        GalleryImage result = galleryService.update(1L, updated);

        assertNotNull(result);
        assertEquals("https://example.com/new.jpg", result.getImageUrl());
        assertEquals("New Caption", result.getCaption());
        verify(galleryImageRepository, times(1)).findById(1L);
        verify(galleryImageRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("update - retain existing imageUrl when updated imageUrl is null")
    void update_nullImageUrl_shouldKeepExistingImageUrl() {
        GalleryImage existing = new GalleryImage(1L, "https://example.com/old.jpg", "Old Caption");
        GalleryImage updated = new GalleryImage(null, null, "Updated Caption Only");

        when(galleryImageRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(galleryImageRepository.save(existing)).thenReturn(existing);

        GalleryImage result = galleryService.update(1L, updated);

        assertNotNull(result);
        assertEquals("https://example.com/old.jpg", result.getImageUrl());
        assertEquals("Updated Caption Only", result.getCaption());
        verify(galleryImageRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("update - retain existing imageUrl when updated imageUrl is blank")
    void update_blankImageUrl_shouldKeepExistingImageUrl() {
        GalleryImage existing = new GalleryImage(1L, "https://example.com/old.jpg", "Old Caption");
        GalleryImage updated = new GalleryImage(null, "   ", "Updated Caption Only");

        when(galleryImageRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(galleryImageRepository.save(existing)).thenReturn(existing);

        GalleryImage result = galleryService.update(1L, updated);

        assertNotNull(result);
        assertEquals("https://example.com/old.jpg", result.getImageUrl());
        assertEquals("Updated Caption Only", result.getCaption());
        verify(galleryImageRepository, times(1)).save(existing);
    }

    @Test
    @DisplayName("update - should throw RuntimeException when gallery image id not found")
    void update_notFound_shouldThrow() {
        when(galleryImageRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> galleryService.update(99L, sampleImage));
        assertEquals("Gallery image not found: 99", ex.getMessage());
        verify(galleryImageRepository, times(1)).findById(99L);
        verify(galleryImageRepository, never()).save(any());
    }

    @Test
    @DisplayName("delete - should call repository deleteById")
    void delete_shouldCallRepository() {
        galleryService.delete(1L);

        verify(galleryImageRepository, times(1)).deleteById(1L);
    }
}
