package com.acm.acmwebsite.User_Authentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordDTO {
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;
}
