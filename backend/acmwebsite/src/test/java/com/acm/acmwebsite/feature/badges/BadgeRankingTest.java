package com.acm.acmwebsite.feature.badges;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.acm.acmwebsite.User_Authentication.entity.User;
import com.acm.acmwebsite.feature.badges.BadgeRankingService.Scored;
import com.acm.acmwebsite.feature.badges.dto.BadgeDtos.RankedMember;

@DisplayName("Committee ranking: competition ranks, the open places suggested")
class BadgeRankingTest {

    // Members already sorted by points, highest first.
    private static List<Scored> members(long... points) {
        List<Scored> list = new ArrayList<>();
        for (long p : points) {
            User u = new User();
            u.setId(UUID.randomUUID());
            u.setName("Member " + list.size());
            list.add(new Scored(u, p));
        }
        return list;
    }

    private static List<Boolean> suggested(List<RankedMember> ranked) {
        return ranked.stream().map(RankedMember::suggested).toList();
    }

    @Test
    @DisplayName("Everyone is listed; the top open places are suggested")
    void topPlacesSuggested() {
        List<RankedMember> ranked = BadgeRankingService.rankMembers(members(128, 90, 70, 10), 2, false);
        assertEquals(4, ranked.size());
        assertEquals(List.of(1, 2, 3, 4), ranked.stream().map(RankedMember::rank).toList());
        assertEquals(List.of(true, true, false, false), suggested(ranked));
        assertTrue(ranked.stream().noneMatch(RankedMember::tied));
    }

    @Test
    @DisplayName("A tie for the last open place is flagged, not suggested")
    void tieAtTheCut() {
        List<RankedMember> ranked = BadgeRankingService.rankMembers(members(128, 90, 90, 60), 2, false);
        assertEquals(List.of(1, 2, 2, 4), ranked.stream().map(RankedMember::rank).toList());
        assertEquals(List.of(true, false, false, false), suggested(ranked));
        assertEquals(List.of(false, true, true, false), ranked.stream().map(RankedMember::tied).toList());
    }

    @Test
    @DisplayName("A tie that fits inside the open places is suggested")
    void tieThatFits() {
        List<RankedMember> ranked = BadgeRankingService.rankMembers(members(100, 100, 50), 2, false);
        assertEquals(List.of(true, true, false), suggested(ranked));
        assertTrue(ranked.stream().noneMatch(RankedMember::tied));
    }

    @Test
    @DisplayName("Members with no points are listed but never suggested")
    void noPointsNeverSuggested() {
        List<RankedMember> ranked = BadgeRankingService.rankMembers(members(40, 0, 0), 3, false);
        assertEquals(List.of(true, false, false), suggested(ranked));
        assertTrue(ranked.stream().noneMatch(RankedMember::tied));
        assertEquals(0, ranked.get(2).points());
    }

    @Test
    @DisplayName("No open places means nothing is suggested")
    void noOpenPlaces() {
        List<RankedMember> ranked = BadgeRankingService.rankMembers(members(50, 40), 0, false);
        assertEquals(2, ranked.size());
        assertTrue(ranked.stream().noneMatch(m -> m.suggested() || m.tied()));
        assertFalse(BadgeRankingService.rankMembers(List.of(), 3, false).iterator().hasNext());
    }
}
