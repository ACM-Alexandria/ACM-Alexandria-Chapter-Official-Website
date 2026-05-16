package com.acm.acmwebsite.feature.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FormQuestionResponseDto {
    private Long id;

    @JsonProperty("question_text")
    private String questionText;

    @JsonProperty("question_type")
    private String questionType;

    @JsonProperty("is_required")
    private Boolean isRequired;

    private List<String> options;
}
