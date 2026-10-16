package com.acm.acmwebsite.feature.dto.past;

import org.hibernate.validator.constraints.URL;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PastCommitteeBoardRequest {
    @NotNull
    @Min(2000)
    private Integer season;

    @NotBlank
    @Size(max = 255)
    private String name;

    @NotBlank
    @Size(max = 255)
    private String role;

    @URL(regexp = "(?i)(https?://[^/\\s?#@]+([/?#][^\\s]*)?)?",
            message = "URL must be an absolute HTTP or HTTPS URL")
    @Size(max = 2048)
    private String imageUrl;

    @URL(regexp = "(?i)(https?://[^/\\s?#@]+([/?#][^\\s]*)?)?",
            message = "URL must be an absolute HTTP or HTTPS URL")
    @Size(max = 2048)
    private String linkedinUrl;

    private Integer displayOrder;

    @NotNull
    private Long pastCommitteeId;
}
