package com.acm.acmwebsite.feature.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "gallery_image")
public class GalleryImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String imageUrl;

    private String caption;

    public GalleryImage() {}

    public GalleryImage(Long id, String imageUrl, String caption) {
        this.id = id;
        this.imageUrl = imageUrl;
        this.caption = caption;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getCaption() { return caption; }
    public void setCaption(String caption) { this.caption = caption; }
}
