package com.acm.acmwebsite.feature.badges.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.acm.acmwebsite.feature.badges.BadgeIcon;
import com.acm.acmwebsite.feature.badges.PeriodType;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public final class BadgeDtos {

    private BadgeDtos() {
    }

    // ---- Periods ----

    // A season (`name` ignored) or a quarter inside season `season`.
    public record PeriodRequest(
            @NotNull PeriodType type,
            @Size(max = 60) String name,
            @NotNull LocalDate startDate,
            @NotNull LocalDate endDate,
            @NotNull @Min(2000) Integer season) {
    }

    public record MonthRequest(@NotNull YearMonth month) {
    }

    public record PeriodResponse(
            Long id,
            PeriodType type,
            String name,
            String label,
            LocalDate startDate,
            LocalDate endDate,
            Integer season) {
    }

    // ---- Badge types ----

    public record BadgeTypeRequest(
            @NotBlank @Size(max = 80) String name,
            @Size(max = 500) String description,
            PeriodType periodType,
            @NotNull BadgeIcon icon,
            @Min(0) @Max(50) int defaultWinners,
            boolean active,
            int sortOrder) {
    }

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

    // `winnersPerCommittee` maps committee id to how many winners it gets; missing committees use the type's default.
    public record SuggestionRequest(
            @NotNull Long badgeTypeId,
            @NotNull Long periodId,
            Map<Long, Integer> winnersPerCommittee) {
    }

    public record SuggestionResponse(
            BadgeTypeResponse badgeType,
            PeriodResponse period,
            List<CommitteeSuggestion> committees) {
    }

    public record CommitteeSuggestion(
            Long committeeId,
            String committeeName,
            int winners,
            List<Candidate> candidates) {
    }

    // `rank` is shared by tied members (1, 2, 2, 4). `tied` marks members sharing the last place that fits,
    // when there are more of them than places left, so an admin has to choose.
    public record Candidate(
            UUID memberId,
            String name,
            String profileImageUrl,
            long points,
            int rank,
            boolean tied,
            boolean alreadyAwarded) {
    }

    // ---- Awards ----

    public record AwardRequest(
            @NotNull Long badgeTypeId,
            Long periodId,
            @Size(max = 300) String note,
            @NotEmpty @Size(max = 200) List<@Valid Recipient> recipients) {
    }

    // `committeeId` defaults to the member's current committee.
    public record Recipient(
            @NotNull UUID memberId,
            Long committeeId,
            @Min(1) Integer rank) {
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
