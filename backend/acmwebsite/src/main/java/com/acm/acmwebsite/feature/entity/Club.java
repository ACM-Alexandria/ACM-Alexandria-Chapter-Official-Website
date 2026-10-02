package com.acm.acmwebsite.feature.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.time.LocalDateTime;
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
    @Column(name = "google_sheet_url")
    private String googleSheetUrl;
    @Column(name = "sheet_last_updated_at")
    private LocalDateTime sheetLastUpdatedAt;
    @Column(name = "is_external", nullable = false)
    private boolean isExternal = false;
    @Column(name = "registration_open", nullable = false)
    private boolean registrationOpen = false;

    // When the subscriber announcement last went out; null = never announced (guards against accidental resends)
    @Column(name = "announcement_sent_at")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private LocalDateTime announcementSentAt;

    // Request-only flag: when false, creating the club skips the subscriber announcement
    @Transient
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private Boolean sendAnnouncement;

    public Club() {
    }

    public Club(Long id, String name, String description, String imageUrl, List<String> socialMediaLinks) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.socialMediaLinks = socialMediaLinks;
    }

    public Club(Long id, String name, String description, String imageUrl, List<String> socialMediaLinks, boolean isExternal) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        this.socialMediaLinks = socialMediaLinks;
        this.isExternal = isExternal;
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

    public String getGoogleSheetUrl() {
        return googleSheetUrl;
    }

    public void setGoogleSheetUrl(String googleSheetUrl) {
        this.googleSheetUrl = googleSheetUrl;
    }

    public LocalDateTime getSheetLastUpdatedAt() {
        return sheetLastUpdatedAt;
    }

    public void setSheetLastUpdatedAt(LocalDateTime sheetLastUpdatedAt) {
        this.sheetLastUpdatedAt = sheetLastUpdatedAt;
    }

    public boolean getIsExternal() {
        return isExternal;
    }

    public boolean isExternal() {
        return isExternal;
    }

    public void setIsExternal(boolean isExternal) {
        this.isExternal = isExternal;
    }

    public boolean isRegistrationOpen() {
        return registrationOpen;
    }

    public boolean getRegistrationOpen() {
        return registrationOpen;
    }

    public void setRegistrationOpen(boolean registrationOpen) {
        this.registrationOpen = registrationOpen;
    }

    public Boolean getSendAnnouncement() {
        return sendAnnouncement;
    }

    public void setSendAnnouncement(Boolean sendAnnouncement) {
        this.sendAnnouncement = sendAnnouncement;
    }

    public LocalDateTime getAnnouncementSentAt() {
        return announcementSentAt;
    }

    public void setAnnouncementSentAt(LocalDateTime announcementSentAt) {
        this.announcementSentAt = announcementSentAt;
    }
}
