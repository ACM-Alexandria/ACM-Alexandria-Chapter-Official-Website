package com.acm.acmwebsite.feature.badges;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BadgeAwardRepository extends JpaRepository<BadgeAward, Long> {

    String WITH_DETAILS = "SELECT a FROM BadgeAward a JOIN FETCH a.badgeType JOIN FETCH a.member "
            + "JOIN FETCH a.committee JOIN FETCH a.awardedBy LEFT JOIN FETCH a.period ";

    @Query(WITH_DETAILS + "WHERE a.member.id = :memberId AND a.revokedAt IS NULL ORDER BY a.awardedAt DESC")
    List<BadgeAward> findActiveByMember(@Param("memberId") UUID memberId);

    @Query(WITH_DETAILS + "WHERE a.publicId = :publicId AND a.revokedAt IS NULL")
    Optional<BadgeAward> findActiveByPublicId(@Param("publicId") UUID publicId);

    @Query(WITH_DETAILS + "WHERE a.period.id = :periodId AND a.revokedAt IS NULL "
            + "ORDER BY a.badgeType.sortOrder, a.committee.name, a.rank, a.awardedAt")
    List<BadgeAward> findActiveByPeriod(@Param("periodId") Long periodId);

    @Query(WITH_DETAILS + "WHERE a.badgeType.id = :typeId AND a.period.id = :periodId AND a.committee.id = :committeeId "
            + "AND a.revokedAt IS NULL ORDER BY a.rank, a.awardedAt")
    List<BadgeAward> findActiveForCommittee(@Param("typeId") Long typeId,
                                            @Param("periodId") Long periodId,
                                            @Param("committeeId") Long committeeId);

    // Any award, active or revoked, of this type in this period for these members.
    @Query("SELECT a FROM BadgeAward a WHERE a.badgeType.id = :typeId AND a.period.id = :periodId AND a.member.id IN :memberIds")
    List<BadgeAward> findForMembers(@Param("typeId") Long typeId,
                                    @Param("periodId") Long periodId,
                                    @Param("memberIds") Collection<UUID> memberIds);

    boolean existsByPeriod_Id(Long periodId);

    @Modifying
    @Query("UPDATE BadgeAward a SET a.seenByMember = true WHERE a.member.id = :memberId AND a.seenByMember = false")
    int markSeen(@Param("memberId") UUID memberId);
}
