package com.acm.acmwebsite.feature.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
public class Club {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
    private String imageUrl;
    @ElementCollection
    @CollectionTable(name = "club_social_links", joinColumns = @JoinColumn(name = "club_id"))
    @Column(name = "link")
    private List<String> socialMediaLinks;

    public Club() {
    }

    public Club(Long id, String name, String description, String imageUrl, List<String> socialMediaLinks) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.socialMediaLinks = socialMediaLinks;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getSocialMediaLinks() {
        return socialMediaLinks;
    }

    public void setSocialMediaLinks(List<String> socialMediaLinks) {
        this.socialMediaLinks = socialMediaLinks;
    }
}
