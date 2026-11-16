package com.acm.acmwebsite.feature.badges;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.Candidate;
import com.acm.acmwebsite.feature.repository.PointsLogRepository.MemberPointsTotal;

@DisplayName("Suggestion ranking: competition ranks cut at the number of winners")
class BadgeSuggestionRankTest {

    private record Total(UUID memberId, Long committeeId, Long total) implements MemberPointsTotal {
        public UUID getMemberId() { return memberId; }
        public Long getCommitteeId() { return committeeId; }
        public Long getTotal() { return total; }
    }

    private static List<MemberPointsTotal> totals(long... points) {
        List<MemberPointsTotal> list = new ArrayList<>();
        for (long p : points) {
            list.add(new Total(UUID.randomUUID(), 1L, p));
        }
        return list;
    }

    private static List<Candidate> rank(List<MemberPointsTotal> sorted, int winners, Set<UUID> awarded) {
        return BadgeSuggestionService.rank(sorted, winners, Map.of(), awarded);
    }

    @Test
    @DisplayName("Takes the top N when there is no tie at the cut")
    void topN() {
        List<Candidate> result = rank(totals(128, 90, 70), 2, Set.of());
        assertEquals(2, result.size());
        assertEquals(List.of(1, 2), result.stream().map(Candidate::rank).toList());
        assertTrue(result.stream().noneMatch(Candidate::tied));
    }

    @Test
    @DisplayName("Includes everyone tied for the last place and flags them")
    void tieAtCut() {
        List<Candidate> result = rank(totals(128, 90, 90, 60), 2, Set.of());
        assertEquals(3, result.size());
        assertEquals(List.of(1, 2, 2), result.stream().map(Candidate::rank).toList());
        assertFalse(result.get(0).tied());
        assertTrue(result.get(1).tied());
        assertTrue(result.get(2).tied());
    }

    @Test
    @DisplayName("A tie that fits inside the places is not flagged")
    void tieThatFits() {
        List<Candidate> result = rank(totals(100, 100, 50), 2, Set.of());
        assertEquals(2, result.size());
        assertTrue(result.stream().noneMatch(Candidate::tied));
    }

    @Test
    @DisplayName("A tie for first that exceeds the places is all included")
    void tieForFirst() {
        List<Candidate> result = rank(totals(80, 80, 80, 10), 1, Set.of());
        assertEquals(3, result.size());
        assertTrue(result.stream().allMatch(c -> c.rank() == 1 && c.tied()));
    }

    @Test
    @DisplayName("Zero winners or no members gives no candidates")
    void empty() {
        assertTrue(rank(totals(50, 40), 0, Set.of()).isEmpty());
        assertTrue(rank(List.of(), 3, Set.of()).isEmpty());
    }

    @Test
    @DisplayName("Marks members who already hold the badge")
    void alreadyAwarded() {
        List<MemberPointsTotal> sorted = totals(50, 40);
        UUID first = sorted.get(0).getMemberId();
        List<Candidate> result = rank(sorted, 2, Set.of(first));
        assertTrue(result.get(0).alreadyAwarded());
        assertFalse(result.get(1).alreadyAwarded());
    }
}
