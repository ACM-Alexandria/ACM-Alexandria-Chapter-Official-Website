package com.acm.acmwebsite.feature.service;

import com.acm.acmwebsite.feature.entity.GalleryImage;
import com.acm.acmwebsite.feature.repository.GalleryImageRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GalleryService {

    private final GalleryImageRepository repository;

    public GalleryService(GalleryImageRepository repository) {
        this.repository = repository;
    }

    public List<GalleryImage> getAll() {
        return repository.findAll();
    }

    public GalleryImage add(GalleryImage image) {
        if (image.getImageUrl() == null || image.getImageUrl().isBlank()) {
            throw new IllegalArgumentException("imageUrl is required");
        }
        return repository.save(image);
    }

    public GalleryImage update(Long id, GalleryImage updated) {
        GalleryImage existing = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Gallery image not found: " + id));
        if (updated.getImageUrl() != null && !updated.getImageUrl().isBlank()) {
            existing.setImageUrl(updated.getImageUrl());
        }
        existing.setCaption(updated.getCaption());
        return repository.save(existing);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }
}
