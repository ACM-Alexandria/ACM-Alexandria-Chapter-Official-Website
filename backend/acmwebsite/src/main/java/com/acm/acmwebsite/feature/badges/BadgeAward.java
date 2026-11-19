package com.acm.acmwebsite.feature.badges;

import com.acm.acmwebsite.feature.seasons.SeasonPeriod;
import java.time.LocalDateTime;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.entity.Committee;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// One badge given to one member. Revoking keeps the row (so it can be restored) but hides it everywhere.
@Entity
@Table(name = "badge_awards", uniqueConstraints = {
        @UniqueConstraint(name = "uk_badge_awards_public_id", columnNames = "public_id"),
        @UniqueConstraint(name = "uk_badge_awards_type_period_member", columnNames = {"badge_type_id", "period_id", "member_id"})
})
@Getter
@Setter
@NoArgsConstructor
public class BadgeAward {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Used in public share links, so award ids can't be guessed.
    @Column(name = "public_id", nullable = false, updatable = false)
    private UUID publicId = UUID.randomUUID();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "badge_type_id", nullable = false)
    private BadgeType badgeType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "period_id", nullable = false)
    private SeasonPeriod period;

    // The committee the badge was won in, kept even if the member moves committee later.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "committee_id", nullable = false)
    private Committee committee;

    // Place within the committee for that period (1 = top), when the award came from a ranking.
    @Column(name = "award_rank")
    private Integer rank;

    @Column(length = 300)
    private String note;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "awarded_by_id", nullable = false)
    private User awardedBy;

    @CreationTimestamp
    @Column(name = "awarded_at", nullable = false)
    private LocalDateTime awardedAt;

    @Column(name = "revoked_at")
    private LocalDateTime revokedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "revoked_by_id")
    private User revokedBy;

    // False until the member opens their badges, so the profile can mark new ones.
    @Column(name = "seen_by_member", nullable = false)
    private boolean seenByMember = false;

    public boolean isActive() {
        return revokedAt == null;
    }
}
