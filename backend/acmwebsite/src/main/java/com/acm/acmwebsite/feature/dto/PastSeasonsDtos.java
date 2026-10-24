package com.acm.acmwebsite.feature.dto;

import jakarta.validation.constraints.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

public final class PastSeasonsDtos {

    private PastSeasonsDtos() {
    }

    @Data
    public static class BoardArchiveRequest {
        @NotNull
        @Min(2000)
        private Integer season;
    }

    @Data
    @Builder
    public static class BoardArchiveSummary {
        private Integer archivedSeason;
        private int highBoardMembers;
        private int committees;
        private int committeeBoardMembers;
        private int clubs;
        private int clubBoardMembers;
        private int usersDowngraded;
        private Integer newCurrentSeason;
    }

    @Data
    @Builder
    public static class BoardSeasonsResponse {
        private Integer currentSeason;
        private List<Integer> pastSeasons;
    }

    @Data
    @Builder
    public static class PastBoardResponse {
        private Integer season;
        private List<PastHighBoardResponse> highBoard;
        private List<PastCommitteeResponse> committees;
        private List<PastClubResponse> clubs;
    }

    @Data
    public static class PastClubBoardRequest {
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
        private Long pastClubId;
    }

    @Data
    public static class PastClubBoardResponse {
        private Long id;
        private Integer season;
        private String userName;
        private String profileImageUrl;
        private String linkedinUrl;
        private String role;
        private Integer order;
        private Long pastClubId;
    }

    @Data
    public static class PastClubRequest {
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

    @Data
    public static class PastClubResponse {
        private Long id;
        private Integer season;
        private String name;
        private String description;
        private String logoUrl;
        private List<PastClubBoardResponse> boardRoles = List.of();
    }

    @Data
    public static class PastCommitteeBoardRequest {
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

    @Data
    public static class PastCommitteeBoardResponse {
        private Long id;
        private Integer season;
        private String userName;
        private String profileImageUrl;
        private String linkedinUrl;
        private String role;
        private Integer order;
        private Long pastCommitteeId;
    }

    @Data
    public static class PastCommitteeRequest {
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

    @Data
    public static class PastCommitteeResponse {
        private Long id;
        private Integer season;
        private String name;
        private String description;
        private String logoUrl;
        private List<PastCommitteeBoardResponse> boardRoles = List.of();
    }

    @Data
    public static class PastHighBoardRequest {
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
    }

    @Data
    public static class PastHighBoardResponse {
        private Long id;
        private Integer season;
        private String userName;
        private String profileImageUrl;
        private String linkedinUrl;
        private String role;
        private Integer order;
    }
}
