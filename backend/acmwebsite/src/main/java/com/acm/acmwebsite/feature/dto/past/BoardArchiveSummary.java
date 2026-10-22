package com.acm.acmwebsite.feature.dto.past;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BoardArchiveSummary {
    private Integer archivedSeason;
    private int highBoardMembers;
    private int committees;
    private int committeeBoardMembers;
    private int clubs;
    private int clubBoardMembers;
    private int usersDowngraded;
    private Integer newCurrentSeason;
}
