package com.acm.acmwebsite.feature.badges;

import org.springframework.stereotype.Component;

import com.acm.acmwebsite.feature.seasons.PeriodUsage;

import lombok.RequiredArgsConstructor;

// A period with badges awarded for it (active or revoked) can't be deleted from Season settings.
@Component
@RequiredArgsConstructor
public class BadgePeriodUsage implements PeriodUsage {

    private final BadgeAwardRepository awardRepository;

    @Override
    public boolean isPeriodInUse(Long periodId) {
        return awardRepository.existsByPeriod_Id(periodId);
    }

    @Override
    public String usageDescription() {
        return "badges were awarded for it";
    }
}
