package com.acm.acmwebsite.feature.dto.past;

import org.hibernate.validator.constraints.URL;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PastCommitteeRequest {
    @NotNull
    @Min(2000)
    private Integer season;

    @NotBlank
    @Size(max = 255)
    private String name;

    private String description;

    @URL(regexp = "(?i)(https?://[^/\\s?#@]+([/?#][^\\s]*)?)?",
            message = "URL must be an absolute HTTP or HTTPS URL")
    @Size(max = 2048)
    private String logoUrl;
}
