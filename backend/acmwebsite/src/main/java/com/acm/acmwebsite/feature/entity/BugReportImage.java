package com.acm.acmwebsite.feature.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "bug_report_images")
public class BugReportImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bug_report_id", nullable = false)
    @JsonIgnore
    private BugReport bugReport;

    @Column(nullable = false, name = "image_url")
    private String imageUrl;

    public BugReportImage() {
    }

    public BugReportImage(BugReport bugReport, String imageUrl) {
        this.bugReport = bugReport;
        this.imageUrl = imageUrl;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BugReport getBugReport() {
        return bugReport;
    }

    public void setBugReport(BugReport bugReport) {
        this.bugReport = bugReport;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
