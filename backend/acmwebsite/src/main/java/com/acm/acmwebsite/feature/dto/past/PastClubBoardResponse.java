package com.acm.acmwebsite.feature.dto.past;

import lombok.Data;

@Data
public class PastClubBoardResponse {
    private Long id;
    private Integer season;
    private String userName;
    private String profileImageUrl;
    private String linkedinUrl;
    private String role;
    private Integer order;
    private Long pastClubId;
}
