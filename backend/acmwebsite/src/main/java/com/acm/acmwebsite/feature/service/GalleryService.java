package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.core.constants.CacheNames;
import com.acm.acmwebsite.feature.entity.GalleryImage;
import com.acm.acmwebsite.feature.repository.GalleryImageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GalleryService {

    private final GalleryImageRepository repository;
    private static final Logger logger = LoggerFactory.getLogger(GalleryService.class);
    public GalleryService(GalleryImageRepository repository) {
        this.repository = repository;
    }

    @Cacheable(value = CacheNames.GALLERY,key = "'AllGallery'")
    public List<GalleryImage> getAll() {
        logger.info("fetching all gallery images from database...");
        return repository.findAll();
    }

    @CacheEvict(value = CacheNames.GALLERY, allEntries = true)
    public GalleryImage add(GalleryImage image) {
        if (image.getImageUrl() == null || image.getImageUrl().isBlank()) {
            throw new IllegalArgumentException("imageUrl is required");
        }
        return repository.save(image);
    }

    @CacheEvict(value = CacheNames.GALLERY, allEntries = true)
    public GalleryImage update(Long id, GalleryImage updated) {
        GalleryImage existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gallery image not found: " + id));
        if (updated.getImageUrl() != null && !updated.getImageUrl().isBlank()) {
            existing.setImageUrl(updated.getImageUrl());
        }
        existing.setCaption(updated.getCaption());
        return repository.save(existing);
    }
    @CacheEvict(value = CacheNames.GALLERY, allEntries = true)
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
