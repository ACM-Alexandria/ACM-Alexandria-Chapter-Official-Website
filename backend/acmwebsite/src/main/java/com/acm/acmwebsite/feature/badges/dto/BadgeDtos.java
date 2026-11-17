package com.acm.acmwebsite.feature.badges.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import com.acm.acmwebsite.feature.badges.BadgeIcon;
import com.acm.acmwebsite.feature.seasons.PeriodType;
import com.acm.acmwebsite.feature.seasons.dto.SeasonDtos.PeriodResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class BadgeDtos {

    private BadgeDtos() {
    }

    // ---- Badge types ----

    public record BadgeTypeResponse(
            Long id,
            String name,
            String description,
            PeriodType periodType,
            BadgeIcon icon,
            int defaultWinners,
            boolean active,
            int sortOrder) {
    }

    // ---- Suggestions ----

    // The Award screen for one committee. `winners` is how many winners each committee gets in all
    // (already awarded ones included); left out, the badge type's default.
    public record CommitteeRankingRequest(
            @NotNull Long badgeTypeId,
            @NotNull Long periodId,
            @NotNull Long committeeId,
            @Min(1) @Max(50) Integer winners) {
    }

    // `awarded`: this badge already given in the committee for the period. `members`: everyone else in the
    // committee, highest points first; the top places still open are marked `suggested`.
    public record CommitteeRankingResponse(
            BadgeTypeResponse badgeType,
            PeriodResponse period,
            Long committeeId,
            String committeeName,
            int winners,
            List<AwardView> awarded,
            List<RankedMember> members) {
    }

    // `rank` is shared by tied members (1, 2, 2, 4). `tied` marks members sharing the last open place when
    // there are more of them than places, so the admin chooses; they aren't `suggested`.
    // Members with no points are never suggested.
    public record RankedMember(
            UUID memberId,
            String name,
            String profileImageUrl,
            long points,
            int rank,
            boolean suggested,
            boolean tied,
            // A committee board member rather than an ACM member.
            boolean board) {
    }

    // ---- Awards ----

    // `winners` caps how many members of each committee can hold the badge for the period; it defaults to the
    // badge's default (3), the same number the Award screen ranks with.
    public record AwardRequest(
            @NotNull Long badgeTypeId,
            @NotNull Long periodId,
            @Size(max = 300) String note,
            @NotEmpty @Size(max = 200) List<@Valid Recipient> recipients,
            @Min(1) @Max(50) Integer winners) {
    }

    // The badge goes to the member's current committee; it can't be chosen.
    // `note` is this member's own description of the badge (why they earned it); it falls back to the request's.
    public record Recipient(
            @NotNull UUID memberId,
            @Min(1) Integer rank,
            @Size(max = 300) String note) {
    }

    // Sets or clears (blank) an awarded badge's description.
    public record AwardNoteRequest(@Size(max = 300) String note) {
    }

    public record MemberSummary(
            UUID id,
            String name,
            String profileImageUrl,
            String committeeName) {
    }

    // One badge as members, the public and admins see it. `awardedByName` is filled for admins only,
    // `isNew` for the member's own list only.
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record AwardView(
            Long id,
            UUID publicId,
            Long badgeTypeId,
            String name,
            String description,
            BadgeIcon icon,
            PeriodType periodType,
            Long periodId,
            String periodLabel,
            LocalDate periodStart,
            LocalDate periodEnd,
            Long committeeId,
            String committeeName,
            Integer rank,
            String note,
            LocalDateTime awardedAt,
            MemberSummary member,
            String awardedByName,
            Boolean isNew) {
    }

    public record MemberBadgesResponse(MemberSummary member, List<AwardView> badges) {
    }
}
