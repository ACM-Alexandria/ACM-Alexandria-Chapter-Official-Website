package com.acm.acmwebsite.feature.dto.past;

import lombok.Data;
import java.util.List;

@Data
public class PastCommitteeResponse {
    private Long id;
    private Integer season;
    private String name;
    private String description;
    private String logoUrl;
    private List<PastCommitteeBoardResponse> boardRoles = List.of();
}
