package com.acm.acmwebsite.feature.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OpenCallRequestDto {
    // When false, opening the call skips emailing the committee's subscribers
    private Boolean sendAnnouncement;
}
