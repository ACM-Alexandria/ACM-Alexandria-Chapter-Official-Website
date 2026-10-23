package com.acm.acmwebsite.feature.insights;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.enums.SubscripeTo;
import com.acm.acmwebsite.feature.enums.SubscriptionStatus;
import com.acm.acmwebsite.feature.insights.dto.InsightsDto.Bucket;
import com.acm.acmwebsite.feature.insights.dto.InsightsDto.Chart;
import com.acm.acmwebsite.feature.insights.dto.InsightsDto.Kpi;
import com.acm.acmwebsite.feature.insights.dto.InsightsDto.OpenItem;
import com.acm.acmwebsite.feature.insights.dto.InsightsDto.Overview;
import com.acm.acmwebsite.feature.insights.dto.InsightsDto.RankItem;
import com.acm.acmwebsite.feature.insights.dto.InsightsDto.Ranking;
import com.acm.acmwebsite.feature.insights.dto.InsightsDto.Section;
import com.acm.acmwebsite.feature.insights.dto.InsightsDto.Series;
import com.acm.acmwebsite.feature.insights.dto.InsightsDto.Status;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/** Builds the Insights tab: the global overview, then one section per tab under it. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InsightsService {

    /** Section tabs under the overview, in display order. */
    public static final List<String> SECTIONS = List.of(
            "committees", "events", "clubs", "programs", "points", "radio", "communication");

    private static final int TOP = 10;

    private final InsightsQueries q;
    private final SystemSettingsService systemSettings;

    // ---- overview -----------------------------------------------------------------------------

    public Overview overview(InsightsRange range) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = range.since(now);

        List<LocalDateTime> signups = q.signupTimes(since);
        List<LocalDateTime> events = q.eventRegistrationTimes(since);
        List<LocalDateTime> clubs = q.clubRegistrationTimes(since);
        List<LocalDateTime> programs = q.programRegistrationTimes(since);
        List<LocalDateTime> calls = q.committeeRegistrationTimes(since);
        InsightsAxis axis = InsightsAxis.of(range, now, concat(signups, events, clubs, programs, calls));

        List<Bucket> signupBuckets = axis.count(signups);

        List<Kpi> kpis = List.of(
                new Kpi("members", "Members", q.total("User"), (long) signups.size(), values(signupBuckets)),
                new Kpi("committees", "Committees", q.total("Committee"), null, null),
                new Kpi("events", "Events", q.total("Event"), null, null),
                new Kpi("clubs", "Clubs", q.total("Club"), null, null),
                new Kpi("programs", "Programs", q.total("Program"), null, null));

        List<Chart> charts = List.of(
                new Chart("newMembers", "New members", "area", List.of(new Series("members", "New members", signupBuckets))),
                new Chart("userGrowth", "User growth", "area", List.of(
                        new Series("members", "Members", cumulative(q.usersBefore(since), signupBuckets)))),
                new Chart("registrations", "Registrations", "stacked", List.of(
                        new Series("events", "Events", axis.count(events)),
                        new Series("clubs", "Clubs", axis.count(clubs)),
                        new Series("programs", "Programs", axis.count(programs)),
                        new Series("committeeCalls", "Committee calls", axis.count(calls)))));

        return new Overview(range.name(), kpis, charts, openNow(now));
    }

    private List<OpenItem> openNow(LocalDateTime now) {
        List<OpenItem> items = new ArrayList<>();
        for (Object[] r : q.activeCallsWithRegistrations()) {
            items.add(new OpenItem("committeeCall", (String) r[0] + " call", "Opened " + date(r[1]), toLong(r[2])));
        }
        for (Object[] r : q.openClubsWithRegistrations()) {
            items.add(new OpenItem("club", (String) r[0], "Registration open", toLong(r[1])));
        }
        for (Object[] r : q.openProgramsWithRegistrations()) {
            items.add(new OpenItem("program", (String) r[0], "Starts " + date(r[1]), toLong(r[2])));
        }
        for (Object[] r : q.openOrUpcomingEvents(now)) {
            boolean open = Boolean.TRUE.equals(r[2]);
            items.add(new OpenItem("event", (String) r[0], (open ? "Registration open · " : "Upcoming · ") + date(r[1]), toLong(r[3])));
        }
        return items;
    }

    // ---- sections -----------------------------------------------------------------------------

    public Section section(String id, InsightsRange range) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime since = range.since(now);
        return switch (id) {
            case "committees" -> committees(range, since);
            case "events" -> events(range, now, since);
            case "clubs" -> clubs(range, now, since);
            case "programs" -> programs(range, now, since);
            case "points" -> points(range, now, since);
            case "radio" -> radio(range);
            case "communication" -> communication(range, now, since);
            default -> throw new IllegalArgumentException("Unknown insights section: " + id);
        };
    }

    private Section committees(InsightsRange range, LocalDateTime since) {
        List<Kpi> kpis = List.of(
                kpi("committees", "Committees", q.total("Committee")),
                kpi("open", "Open committees", q.openCommittees()),
                kpi("activeCalls", "Active calls", q.activeCalls()));
        List<RankItem> members = q.membersPerCommittee(TOP).stream()
                .map(r -> new RankItem((String) r[0], null, toLong(r[1])))
                .toList();
        return new Section("committees", range.name(), kpis, List.of(),
                List.of(new Ranking("membersPerCommittee", "Members per committee", members)), List.of());
    }

    private Section events(InsightsRange range, LocalDateTime now, LocalDateTime since) {
        long total = q.total("Event");
        long upcoming = q.upcomingEvents(now);
        List<Kpi> kpis = List.of(
                kpi("events", "Events", total),
                kpi("upcoming", "Upcoming", upcoming),
                kpi("past", "Past", total - upcoming),
                kpi("open", "Open for registration", q.eventsOpen()));
        List<RankItem> top = q.topEvents(since, TOP).stream()
                .map(r -> new RankItem((String) r[0], date(r[1]), toLong(r[2])))
                .toList();
        return new Section("events", range.name(), kpis, List.of(),
                List.of(new Ranking("topEvents", "Top events by registrations", top)), List.of());
    }

    private Section clubs(InsightsRange range, LocalDateTime now, LocalDateTime since) {
        long total = q.total("Club");
        long external = q.externalClubs();
        List<Kpi> kpis = List.of(
                kpi("clubs", "Clubs", total),
                kpi("internal", "Internal", total - external),
                kpi("external", "External", external),
                kpi("open", "Open for registration", q.clubsOpen()));
        List<RankItem> top = q.topClubs(since, TOP).stream()
                .map(r -> new RankItem((String) r[0], null, toLong(r[1])))
                .toList();
        return new Section("clubs", range.name(), kpis, List.of(),
                List.of(new Ranking("topClubs", "Top clubs by registrations", top)), List.of());
    }

    private Section programs(InsightsRange range, LocalDateTime now, LocalDateTime since) {
        List<Kpi> kpis = List.of(
                kpi("programs", "Programs", q.total("Program")),
                kpi("running", "Running now", q.runningPrograms(now)),
                kpi("open", "Open for registration", q.programsOpen()));
        List<RankItem> top = q.topPrograms(since, TOP).stream()
                .map(r -> new RankItem((String) r[0], "Starts " + date(r[1]), toLong(r[2])))
                .toList();
        return new Section("programs", range.name(), kpis, List.of(),
                List.of(new Ranking("topPrograms", "Top programs by registrations", top)), List.of());
    }

    private Section points(InsightsRange range, LocalDateTime now, LocalDateTime since) {
        LocalDateTime allTime = InsightsRange.ALL.since(now);
        List<Kpi> kpis = List.of(
                new Kpi("awarded", "Points awarded", q.pointsAwarded(allTime), q.pointsAwarded(since), null),
                new Kpi("deducted", "Points deducted", q.pointsDeducted(allTime), q.pointsDeducted(since), null));

        List<Object[]> changes = q.pointChanges(since);
        List<Object[]> awarded = changes.stream().filter(r -> ((Number) r[1]).longValue() > 0).toList();
        List<Object[]> deducted = changes.stream()
                .filter(r -> ((Number) r[1]).longValue() < 0)
                .map(r -> new Object[]{r[0], -((Number) r[1]).longValue()})
                .toList();
        InsightsAxis axis = InsightsAxis.of(range, now, changes.stream().map(r -> (LocalDateTime) r[0]).toList());

        List<RankItem> top = q.topMembersByPoints(TOP).stream()
                .map((User u) -> new RankItem(u.getName() != null ? u.getName() : u.getEmail(), null, u.getSeasonPoints()))
                .toList();

        return new Section("points", range.name(), kpis,
                List.of(new Chart("points", "Points over time", "stacked", List.of(
                        new Series("awarded", "Awarded", axis.sum(awarded)),
                        new Series("deducted", "Deducted", axis.sum(deducted))))),
                List.of(new Ranking("topMembers", "Top members by season points", top)), List.of());
    }

    private Section radio(InsightsRange range) {
        List<Kpi> kpis = List.of(
                kpi("seasons", "Seasons", q.total("RadioSeason")),
                kpi("episodes", "Episodes", q.total("RadioEpisode")));
        List<RankItem> perSeason = q.episodesPerSeason().stream()
                .map(r -> new RankItem("Season " + r[0], null, toLong(r[1])))
                .toList();
        return new Section("radio", range.name(), kpis, List.of(),
                List.of(new Ranking("episodesPerSeason", "Episodes per season", perSeason)), List.of());
    }

    private Section communication(InsightsRange range, LocalDateTime now, LocalDateTime since) {
        LocalDateTime allTime = InsightsRange.ALL.since(now);
        List<String> announceable = List.of("Event", "Club", "Program", "CommitteeCall");
        long announcementsAll = announceable.stream().mapToLong(e -> q.announcementsSince(e, allTime)).sum();
        long announcementsInPeriod = announceable.stream().mapToLong(e -> q.announcementsSince(e, since)).sum();
        long users = q.total("User");

        List<Kpi> kpis = List.of(
                kpi("subscribers", "Active subscribers", q.activeSubscribers()),
                new Kpi("announcements", "Announcements sent", announcementsAll, announcementsInPeriod, null),
                new Kpi("confirmationEmails", "Confirmation emails", q.confirmationEmailsSince(allTime), q.confirmationEmailsSince(since), null),
                kpi("unconfirmed", "Unconfirmed users", users - q.confirmedUsers()));

        List<RankItem> subscriptions = q.subscriptionsByTypeAndStatus().stream()
                .map(r -> new RankItem(topicLabel(r[0]), statusLabel(r[1]), toLong(r[2])))
                .sorted((a, b) -> Long.compare(b.value(), a.value()))
                .toList();
        List<RankItem> announcements = List.of(
                new RankItem("Events", null, q.announcementsSince("Event", since)),
                new RankItem("Clubs", null, q.announcementsSince("Club", since)),
                new RankItem("Programs", null, q.announcementsSince("Program", since)),
                new RankItem("Committee calls", null, q.announcementsSince("CommitteeCall", since)));

        boolean emailsOn = systemSettings.isEmailsEnabled();
        return new Section("communication", range.name(), kpis, List.of(),
                List.of(
                        new Ranking("subscriptions", "Subscriptions by type and status", subscriptions),
                        new Ranking("announcements", "Announcements sent in the period", announcements)),
                List.of(new Status("Site emails", emailsOn ? "On" : "Locked", emailsOn ? "success" : "danger")));
    }

    // ---- helpers ------------------------------------------------------------------------------

    private static Kpi kpi(String key, String label, long value) {
        return new Kpi(key, label, value, null, null);
    }

    /** Running total: members before the period, then each bucket's signups added on. */
    private static List<Bucket> cumulative(long start, List<Bucket> buckets) {
        List<Bucket> running = new ArrayList<>(buckets.size());
        long total = start;
        for (Bucket bucket : buckets) {
            total += bucket.value();
            running.add(new Bucket(bucket.start(), total));
        }
        return running;
    }

    private static long toLong(Object value) {
        return value instanceof Number n ? n.longValue() : 0L;
    }

    private static List<Long> values(List<Bucket> buckets) {
        return buckets.stream().map(Bucket::value).toList();
    }

    @SafeVarargs
    private static List<LocalDateTime> concat(List<LocalDateTime>... lists) {
        return Stream.of(lists).flatMap(List::stream).toList();
    }

    private static String date(Object value) {
        return value instanceof LocalDateTime t ? t.toLocalDate().toString() : "—";
    }

    private static String topicLabel(Object topic) {
        return topic == SubscripeTo.NEWS ? "News" : "Committee";
    }

    private static String statusLabel(Object status) {
        if (status instanceof SubscriptionStatus s) {
            return switch (s) {
                case ACTIVE -> "Active";
                case PENDING -> "Pending";
                case UNSUBSCRIBE -> "Unsubscribed";
            };
        }
        return String.valueOf(status);
    }
}
