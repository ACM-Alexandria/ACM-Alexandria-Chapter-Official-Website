package com.acm.acmwebsite.feature.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// A past season exists as soon as it is created, even before any of its board records are added.
@Entity
@Table(name = "past_seasons", uniqueConstraints = @UniqueConstraint(name = "uk_past_seasons_season", columnNames = "season"))
@Getter
@Setter
@NoArgsConstructor
public class PastSeason {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer season;

    public PastSeason(Integer season) {
        this.season = season;
    }
}
