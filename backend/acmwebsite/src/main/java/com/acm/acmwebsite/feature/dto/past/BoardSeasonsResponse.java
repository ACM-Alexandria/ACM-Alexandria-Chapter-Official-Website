package com.acm.acmwebsite.feature.dto.past;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class BoardSeasonsResponse {
    private Integer currentSeason;
    private List<Integer> pastSeasons;
}
