package com.acm.acmwebsite.feature.dto;

import com.acm.acmwebsite.feature.enums.ReviewStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)   // null fields are left out of the JSON
public class EventReviewDto {

    private Long eventId;
    private Long reviewId;

    @Min(1) @Max(5)
    private Integer rating;

    @Size(max = 255)
    private String comment;

    private ReviewStatus status;
    private Double averageRating;
    private Long reviewCount;
}