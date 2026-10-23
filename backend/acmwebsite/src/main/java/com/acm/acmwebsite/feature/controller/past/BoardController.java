package com.acm.acmwebsite.feature.controller.past;

import com.acm.acmwebsite.feature.dto.past.BoardSeasonsResponse;
import com.acm.acmwebsite.feature.dto.past.PastBoardResponse;
import com.acm.acmwebsite.feature.service.SystemSettingsService;
import com.acm.acmwebsite.feature.service.past.PastBoardQueryService;
import com.acm.acmwebsite.feature.service.past.PastSeasonService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {
    private final SystemSettingsService settings;
    private final PastSeasonService seasons;
    private final PastBoardQueryService boards;

    @GetMapping("/seasons")
    public BoardSeasonsResponse seasons() {
        return BoardSeasonsResponse.builder().currentSeason(settings.getCurrentSeason())
                .pastSeasons(seasons.getSeasons()).build();
    }

    @GetMapping("/past/{season}")
    public PastBoardResponse past(@PathVariable Integer season) {
        return boards.getBySeason(season);
    }
}
