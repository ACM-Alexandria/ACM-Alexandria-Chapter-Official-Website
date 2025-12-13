package com.acm.acmwebsite.User_Authentication.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "Email can't be empty")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password can't be empty")
    private String password;
}
