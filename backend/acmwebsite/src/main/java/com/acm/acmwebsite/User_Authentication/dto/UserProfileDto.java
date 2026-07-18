package com.acm.acmwebsite.User_Authentication.dto;

import com.acm.acmwebsite.User_Authentication.enums.Department;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDto {

    private UUID id;

    private String email;

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Phone number is required")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("is_alex_eng_student")
    private Boolean isAlexEngStudent;

    private Department department;

    private String batch;
}
