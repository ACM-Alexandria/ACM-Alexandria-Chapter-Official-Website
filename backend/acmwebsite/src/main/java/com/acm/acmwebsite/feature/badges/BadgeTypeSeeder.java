package com.acm.acmwebsite.feature.badges;

import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

// Creates the starter badge types the first time the app runs with none. Admins can edit or add more later.
@Component
@RequiredArgsConstructor
public class BadgeTypeSeeder implements CommandLineRunner {

    private final BadgeTypeRepository typeRepository;

    @Override
    public void run(String... args) {
        if (typeRepository.count() > 0) {
            return;
        }
        typeRepository.saveAll(List.of(
                new BadgeType("Monthly MVP", "Top member of their committee for the month.",
                        PeriodType.MONTH, BadgeIcon.TROPHY, 1, 10),
                new BadgeType("Quarter MVP", "Top member of their committee for the quarter.",
                        PeriodType.QUARTER, BadgeIcon.TROPHY, 1, 20),
                new BadgeType("Season Champion", "Top member of their committee for the whole season.",
                        PeriodType.SEASON, BadgeIcon.CROWN, 1, 30)));
    }
}
