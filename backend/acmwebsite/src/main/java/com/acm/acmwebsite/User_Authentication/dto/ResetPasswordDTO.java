package com.acm.acmwebsite.User_Authentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordDTO {

    @NotBlank(message = "Token is required.")
    private String token;

    @NotBlank(message = "New password is required.")
    private String new_password;

    @NotBlank(message = "Password confirmation is required.")
    private String new_password_confirm;
}
