package com.acm.acmwebsite.User_Authentication.exception;

import lombok.NoArgsConstructor;

/** PasswordAndConfirmationMisMatch */
@NoArgsConstructor
public class PasswordAndConfirmationMisMatch extends RuntimeException {
  public PasswordAndConfirmationMisMatch(String message) {
    super(message);
  }
}
