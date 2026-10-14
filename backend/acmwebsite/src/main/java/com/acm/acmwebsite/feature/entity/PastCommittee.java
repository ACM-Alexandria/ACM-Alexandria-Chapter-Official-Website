package com.acm.acmwebsite.feature.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "past_committees", indexes = @Index(name = "idx_past_committees_season", columnList = "season"))
@Getter
@Setter
@NoArgsConstructor
public class PastCommittee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer season;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "logo_url", length = 2048)
    private String logoUrl;
}
