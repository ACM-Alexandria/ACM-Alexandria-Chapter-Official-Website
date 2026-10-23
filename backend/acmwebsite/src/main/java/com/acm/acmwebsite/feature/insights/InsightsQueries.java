package com.acm.acmwebsite.feature.insights;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.enums.SubscriptionStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/** Read-only aggregate queries behind the Insights tab. Nothing here loads whole tables. */
@Repository
public class InsightsQueries {

    private final EntityManager em;

    public InsightsQueries(EntityManager em) {
        this.em = em;
    }

    private long count(String jpql, Map<String, Object> params) {
        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        params.forEach(query::setParameter);
        return query.getSingleResult();
    }

    private <T> List<T> list(String jpql, Class<T> type, Map<String, Object> params, int limit) {
        TypedQuery<T> query = em.createQuery(jpql, type);
        params.forEach(query::setParameter);
        if (limit > 0) query.setMaxResults(limit);
        return query.getResultList();
    }

    private List<LocalDateTime> times(String jpql, LocalDateTime since) {
        return list(jpql, LocalDateTime.class, Map.of("since", since), 0);
    }

    private List<Object[]> rows(String jpql, Map<String, Object> params, int limit) {
        return list(jpql, Object[].class, params, limit);
    }

    // ---- totals ---------------------------------------------------------------------------

    public long total(String entity) {
        return count("SELECT COUNT(e) FROM " + entity + " e", Map.of());
    }

    // ---- members --------------------------------------------------------------------------

    public List<LocalDateTime> signupTimes(LocalDateTime since) {
        return times("SELECT u.createdAt FROM User u WHERE u.createdAt >= :since", since);
    }

    /** Members who signed up before a time: the starting point of the growth line. */
    public long usersBefore(LocalDateTime time) {
        return count("SELECT COUNT(u) FROM User u WHERE u.createdAt < :time", Map.of("time", time));
    }

    public long confirmedUsers() {
        return count("SELECT COUNT(u) FROM User u WHERE u.emailConfirmed = true", Map.of());
    }

    public List<User> topMembersByPoints(int limit) {
        return list("SELECT u FROM User u WHERE u.seasonPoints > 0 ORDER BY u.seasonPoints DESC", User.class, Map.of(), limit);
    }

    // ---- registrations over time ------------------------------------------------------------

    public List<LocalDateTime> eventRegistrationTimes(LocalDateTime since) {
        return times("SELECT r.registeredAt FROM EventRegistration r WHERE r.registeredAt >= :since", since);
    }

    public List<LocalDateTime> clubRegistrationTimes(LocalDateTime since) {
        return times("SELECT r.registeredAt FROM ClubRegistration r WHERE r.registeredAt >= :since", since);
    }

    public List<LocalDateTime> programRegistrationTimes(LocalDateTime since) {
        return times("SELECT r.registeredAt FROM ProgramRegistration r WHERE r.registeredAt >= :since", since);
    }

    public List<LocalDateTime> committeeRegistrationTimes(LocalDateTime since) {
        return times("SELECT r.registeredAt FROM CommitteeRegistration r WHERE r.registeredAt >= :since", since);
    }

    // ---- open right now ---------------------------------------------------------------------

    /** Active committee calls: {committee name, opened at, registrations}. */
    public List<Object[]> activeCallsWithRegistrations() {
        return rows("SELECT c.committee.name, c.openedAt, (SELECT COUNT(r) FROM CommitteeRegistration r WHERE r.committeeCall = c) "
                + "FROM CommitteeCall c WHERE c.closedAt IS NULL ORDER BY c.openedAt DESC", Map.of(), 0);
    }

    /** Clubs open for registration: {name, registrations}. */
    public List<Object[]> openClubsWithRegistrations() {
        return rows("SELECT c.name, (SELECT COUNT(r) FROM ClubRegistration r WHERE r.club = c) "
                + "FROM Club c WHERE c.registrationOpen = true ORDER BY c.name", Map.of(), 0);
    }

    /** Programs open for registration: {name, start date, registrations}. */
    public List<Object[]> openProgramsWithRegistrations() {
        return rows("SELECT p.name, p.startDate, (SELECT COUNT(r) FROM ProgramRegistration r WHERE r.program = p) "
                + "FROM Program p WHERE p.registrationOpen = true ORDER BY p.startDate", Map.of(), 0);
    }

    /** Events open for registration or still ahead: {name, event time, registration open, registrations}. */
    public List<Object[]> openOrUpcomingEvents(LocalDateTime now) {
        return rows("SELECT e.name, e.eventTime, e.registrationOpen, (SELECT COUNT(r) FROM EventRegistration r WHERE r.event = e) "
                + "FROM Event e WHERE e.registrationOpen = true OR e.eventTime > :now ORDER BY e.eventTime", Map.of("now", now), 0);
    }

    // ---- committees -------------------------------------------------------------------------

    public long openCommittees() {
        return count("SELECT COUNT(c) FROM Committee c WHERE c.isOpen = true", Map.of());
    }

    public long activeCalls() {
        return count("SELECT COUNT(c) FROM CommitteeCall c WHERE c.closedAt IS NULL", Map.of());
    }

    /** Committee calls by registrations in the period: {committee name, opened at, registrations}. */
    public List<Object[]> topCalls(LocalDateTime since, int limit) {
        return rows("SELECT r.committeeCall.committee.name, r.committeeCall.openedAt, COUNT(r) FROM CommitteeRegistration r "
                + "WHERE r.registeredAt >= :since GROUP BY r.committeeCall.id, r.committeeCall.committee.name, r.committeeCall.openedAt "
                + "ORDER BY COUNT(r) DESC", Map.of("since", since), limit);
    }

    // ---- events -----------------------------------------------------------------------------

    public long upcomingEvents(LocalDateTime now) {
        return count("SELECT COUNT(e) FROM Event e WHERE e.eventTime > :now", Map.of("now", now));
    }

    public long eventsOpen() {
        return count("SELECT COUNT(e) FROM Event e WHERE e.registrationOpen = true", Map.of());
    }

    /** Events by registrations in the period: {name, event time, registrations}. */
    public List<Object[]> topEvents(LocalDateTime since, int limit) {
        return rows("SELECT r.event.name, r.event.eventTime, COUNT(r) FROM EventRegistration r WHERE r.registeredAt >= :since "
                + "GROUP BY r.event.id, r.event.name, r.event.eventTime ORDER BY COUNT(r) DESC", Map.of("since", since), limit);
    }

    // ---- clubs ------------------------------------------------------------------------------

    public long externalClubs() {
        return count("SELECT COUNT(c) FROM Club c WHERE c.isExternal = true", Map.of());
    }

    public long clubsOpen() {
        return count("SELECT COUNT(c) FROM Club c WHERE c.registrationOpen = true", Map.of());
    }

    /** Clubs by registrations in the period: {name, registrations}. */
    public List<Object[]> topClubs(LocalDateTime since, int limit) {
        return rows("SELECT r.club.name, COUNT(r) FROM ClubRegistration r WHERE r.registeredAt >= :since "
                + "GROUP BY r.club.id, r.club.name ORDER BY COUNT(r) DESC", Map.of("since", since), limit);
    }

    // ---- programs ---------------------------------------------------------------------------

    public long runningPrograms(LocalDateTime now) {
        return count("SELECT COUNT(p) FROM Program p WHERE p.startDate <= :now AND p.endDate >= :now", Map.of("now", now));
    }

    public long programsOpen() {
        return count("SELECT COUNT(p) FROM Program p WHERE p.registrationOpen = true", Map.of());
    }

    /** Programs by registrations in the period: {name, start date, registrations}. */
    public List<Object[]> topPrograms(LocalDateTime since, int limit) {
        return rows("SELECT r.program.name, r.program.startDate, COUNT(r) FROM ProgramRegistration r WHERE r.registeredAt >= :since "
                + "GROUP BY r.program.id, r.program.name, r.program.startDate ORDER BY COUNT(r) DESC", Map.of("since", since), limit);
    }

    // ---- points -----------------------------------------------------------------------------

    public long pointsAwarded(LocalDateTime since) {
        return count("SELECT COALESCE(SUM(p.points), 0) FROM PointsLog p WHERE p.points > 0 AND p.createdAt >= :since", Map.of("since", since));
    }

    /** Points taken away, as a positive number. */
    public long pointsDeducted(LocalDateTime since) {
        return -count("SELECT COALESCE(SUM(p.points), 0) FROM PointsLog p WHERE p.points < 0 AND p.createdAt >= :since", Map.of("since", since));
    }

    /** Point changes in the period: {created at, points}. */
    public List<Object[]> pointChanges(LocalDateTime since) {
        return rows("SELECT p.createdAt, p.points FROM PointsLog p WHERE p.createdAt >= :since", Map.of("since", since), 0);
    }

    // ---- radio ------------------------------------------------------------------------------

    /** {season number, episodes}. */
    public List<Object[]> episodesPerSeason() {
        return rows("SELECT s.seasonNumber, COUNT(e) FROM RadioSeason s LEFT JOIN RadioEpisode e ON e.radioSeason = s "
                + "GROUP BY s.id, s.seasonNumber ORDER BY s.seasonNumber", Map.of(), 0);
    }

    // ---- communication ----------------------------------------------------------------------

    public long activeSubscribers() {
        return count("SELECT COUNT(DISTINCT s.user.id) FROM Subscription s WHERE s.status = :status",
                Map.of("status", SubscriptionStatus.ACTIVE));
    }

    /** {subscribe to, status, count}. */
    public List<Object[]> subscriptionsByTypeAndStatus() {
        return rows("SELECT s.subscribeTo, s.status, COUNT(s) FROM Subscription s GROUP BY s.subscribeTo, s.status", Map.of(), 0);
    }

    /** Announcements sent since a time, for one announceable entity (Event, Club, Program, CommitteeCall). */
    public long announcementsSince(String entity, LocalDateTime since) {
        return count("SELECT COUNT(e) FROM " + entity + " e WHERE e.announcementSentAt >= :since", Map.of("since", since));
    }

    public long confirmationEmailsSince(LocalDateTime since) {
        return count("SELECT COUNT(r) FROM EmailConfirmationRequest r WHERE r.requestedAt >= :since", Map.of("since", since));
    }
}
