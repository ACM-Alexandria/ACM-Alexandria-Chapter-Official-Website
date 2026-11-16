package com.acm.acmwebsite.feature.seasons;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

// Makes sure a current season exists once the app starts, so requests never race to create the first one.
@Component
@RequiredArgsConstructor
public class CurrentSeasonBootstrap implements CommandLineRunner {

    private final CurrentSeasonService currentSeasonService;

    @Override
    public void run(String... args) {
        currentSeasonService.getCurrentSeason();
    }
}
