package com.acm.acmwebsite.feature.entity;

import com.acm.acmwebsite.feature.enums.FAQCategory;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "faq")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FAQ {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(nullable = false)
    private String question;

    @Column(nullable = false)
    private String answer;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FAQCategory category;

    @Column(name = "display_order", nullable = false)
    private int order;

}
