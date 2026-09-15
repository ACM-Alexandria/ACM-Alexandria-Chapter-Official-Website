package com.acm.acmwebsite.User_Authentication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {
  private UUID id;

  private String email;

  private String name;

  @JsonProperty("profile_image_url")
  private String profileImageUrl;

  @JsonProperty("created_at")
  private LocalDateTime createdAt;

  @JsonProperty("updated_at")
  private LocalDateTime updatedAt;

  private String role;
  
  @JsonProperty("association_name")
  private String associationName;

  @JsonProperty("board_role")
  private String boardRole;

  @JsonProperty("board_order")
  private Integer boardOrder;

  @JsonProperty("committee_id")
  private Long committeeId;

  @JsonProperty("club_id")
  private Long clubId;
  // no reset password fields here for security reasons
}
