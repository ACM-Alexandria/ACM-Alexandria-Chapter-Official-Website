package com.acm.acmwebsite.feature.entity.past;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "past_committee_board", indexes = @Index(name = "idx_past_committee_board_season", columnList = "season"))
@Getter
@Setter
@NoArgsConstructor
public class PastCommitteeBoard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer season;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "image_url", length = 2048)
    private String imageUrl;

    @Column(name = "linkedin_url", length = 2048)
    private String linkedinUrl;

    @Column(nullable = false, length = 255)
    private String role;

    @Column(name = "display_order")
    private Integer displayOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "past_committee_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private PastCommittee pastCommittee;
}
