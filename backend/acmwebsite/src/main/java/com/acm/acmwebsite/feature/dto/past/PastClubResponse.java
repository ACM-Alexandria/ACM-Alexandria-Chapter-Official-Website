package com.acm.acmwebsite.feature.dto.past;

import lombok.Data;

@Data
public class PastClubResponse {
    private Long id;
    private Integer season;
    private String name;
    private String description;
    private String logoUrl;
}
