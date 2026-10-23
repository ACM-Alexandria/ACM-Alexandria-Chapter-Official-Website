package com.acm.acmwebsite.feature.dto.past;

import lombok.Builder;
import lombok.Data;
import java.util.List;

@Data
@Builder
public class PastBoardResponse {
    private Integer season;
    private List<PastHighBoardResponse> highBoard;
    private List<PastCommitteeResponse> committees;
    private List<PastClubResponse> clubs;
}
