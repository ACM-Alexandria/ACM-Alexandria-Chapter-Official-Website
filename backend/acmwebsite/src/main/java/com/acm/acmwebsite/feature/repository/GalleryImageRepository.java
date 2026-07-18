package com.acm.acmwebsite.feature.repository;

import com.acm.acmwebsite.feature.entity.GalleryImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GalleryImageRepository extends JpaRepository<GalleryImage, Long> {
}
