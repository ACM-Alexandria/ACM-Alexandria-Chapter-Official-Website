package com.acm.acmwebsite.feature.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "committee_call")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommitteeCall {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "committee_id", nullable = false)
    @ToString.Exclude
    private Committee committee;

    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt;

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    @Column(name = "google_sheet_url")
    private String googleSheetUrl;

    @Column(name = "sheet_last_updated_at")
    private LocalDateTime sheetLastUpdatedAt;
}
