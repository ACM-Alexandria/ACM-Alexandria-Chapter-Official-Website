package com.acm.acmwebsite.feature.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record FAQReorderDto(
        @NotNull(message = "FAQ id is required") 
        Integer id,
        @Min(value = 0, message = "Order must be zero or greater") 
        int order
) {
}
