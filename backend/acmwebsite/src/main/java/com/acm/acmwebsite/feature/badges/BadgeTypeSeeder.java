package com.acm.acmwebsite.feature.badges;

import com.acm.acmwebsite.feature.seasons.PeriodType;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

// The badge types are fixed and defined here. On every startup this creates any that are missing and
// brings existing ones in line with these definitions, so changing a badge means changing this list.
@Component
@RequiredArgsConstructor
public class BadgeTypeSeeder implements CommandLineRunner {

    // Suggested winners per committee unless the admin picks another number on the Award screen.
    static final int DEFAULT_WINNERS = 3;

    static final List<BadgeType> BADGES = List.of(
            new BadgeType("Monthly MVP", "Top member of their committee for the month.",
                    PeriodType.MONTH, BadgeIcon.TROPHY, DEFAULT_WINNERS, 10),
            new BadgeType("Quarter MVP", "Top member of their committee for the quarter.",
                    PeriodType.QUARTER, BadgeIcon.TROPHY, DEFAULT_WINNERS, 20),
            new BadgeType("Season Champion", "Top member of their committee for the whole season.",
                    PeriodType.SEASON, BadgeIcon.CROWN, DEFAULT_WINNERS, 30));

    private final BadgeTypeRepository typeRepository;

    @Override
    @Transactional
    public void run(String... args) {
        for (BadgeType def : BADGES) {
            BadgeType type = typeRepository.findByName(def.getName()).orElseGet(BadgeType::new);
            type.setName(def.getName());
            type.setDescription(def.getDescription());
            type.setPeriodType(def.getPeriodType());
            type.setIcon(def.getIcon());
            type.setDefaultWinners(def.getDefaultWinners());
            type.setSortOrder(def.getSortOrder());
            type.setActive(true);
            typeRepository.save(type);
        }
    }
}
