package com.acm.acmwebsite.feature.entity;

import jakarta.persistence.*;

// i need to complete this
@Entity
public class Committee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(columnDefinition = "TEXT")
    private String description;
}
