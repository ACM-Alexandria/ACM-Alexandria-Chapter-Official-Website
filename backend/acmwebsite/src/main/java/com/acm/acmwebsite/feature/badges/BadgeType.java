package com.acm.acmwebsite.feature.badges;

import com.acm.acmwebsite.feature.seasons.PeriodType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// A kind of badge, such as "Monthly MVP". Every badge is awarded within a committee for a period.
// The types are fixed in code (BadgeTypeSeeder); admins can't add or edit them.
@Entity
@Table(name = "badge_types")
@Getter
@Setter
@NoArgsConstructor
public class BadgeType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String name;

    @Column(length = 500)
    private String description;

    // The period this badge is awarded for.
    @Enumerated(EnumType.STRING)
    @Column(name = "period_type", nullable = false, length = 16)
    private PeriodType periodType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private BadgeIcon icon = BadgeIcon.TROPHY;

    // How many winners each committee gets by default when suggesting; admins can change it per committee.
    @Column(name = "default_winners", nullable = false)
    private int defaultWinners = 1;

    // Always true for the fixed types; kept so an awarded type could be retired later without losing its awards.
    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;

    public BadgeType(String name, String description, PeriodType periodType, BadgeIcon icon, int defaultWinners, int sortOrder) {
        this.name = name;
        this.description = description;
        this.periodType = periodType;
        this.icon = icon;
        this.defaultWinners = defaultWinners;
        this.sortOrder = sortOrder;
    }
}
