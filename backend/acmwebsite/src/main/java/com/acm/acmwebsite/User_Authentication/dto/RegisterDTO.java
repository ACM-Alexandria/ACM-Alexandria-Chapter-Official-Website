package com.acm.acmwebsite.User_Authentication.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/** RegisterDTO */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RegisterDTO {
  @NotBlank(message = "Email can't be empty")
  @Email(
      message = "Invalid Email Format",
      regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
  private String email;

  @NotBlank(message = "Password can't be empty")
  @Pattern(
      regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{8,}$",
      message =
          "Password must be at least 8 characters, contain a digit, uppercase, lowercase, and a"
              + " special character")
  private String password;

  @NotBlank(message = "Confirm password can't be empty")
  @JsonProperty("password_confirmation")
  private String passwordConfirmation;
}
