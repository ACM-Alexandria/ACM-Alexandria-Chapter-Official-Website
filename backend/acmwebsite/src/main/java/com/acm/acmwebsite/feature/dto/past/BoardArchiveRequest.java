package com.acm.acmwebsite.feature.dto.past;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BoardArchiveRequest {
    @NotNull
    @Min(2000)
    private Integer season;
}
