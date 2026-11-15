package com.acm.acmwebsite.feature.dto;

import com.acm.acmwebsite.feature.enums.FAQCategory;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)   // null fields are left out of the JSON
public class FAQDto {
    private Integer id;

    @NotBlank(message = "Question is required")
    private String question;

    @NotBlank(message = "Answer is required")
    private String answer;

    @NotNull(message = "Category is required")
    private FAQCategory category;

    @Min(value = 0, message = "Order must be zero or greater")
    private int order;
}
