package com.acm.acmwebsite.User_Authentication.exception;

import com.acm.acmwebsite.User_Authentication.dto.ErrorMessageResponse;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SignatureException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class UserExceptionHandler {

  @ExceptionHandler(DuplicateEmailException.class)
  public ResponseEntity<ErrorMessageResponse> duplicateEmail(DuplicateEmailException ex) {
    ErrorMessageResponse errorMessageResponse = new ErrorMessageResponse(ex.getMessage());
    return ResponseEntity.status(409).body(errorMessageResponse);
  }

  @ExceptionHandler(PasswordAndConfirmationMisMatch.class)
  public ResponseEntity<ErrorMessageResponse> passwordAndConfirmMismatch(
      PasswordAndConfirmationMisMatch ex) {
    ErrorMessageResponse errorMessageResponse =
        new ErrorMessageResponse("Password and confirmation don't match");
    return ResponseEntity.status(400).body(errorMessageResponse);
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorMessageResponse> inputValidation(MethodArgumentNotValidException ex) {
    ErrorMessageResponse errorMessageResponse =
        new ErrorMessageResponse(
            ex.getBindingResult().getAllErrors().getFirst().getDefaultMessage());
    return ResponseEntity.status(400).body(errorMessageResponse);
  }

  @ExceptionHandler({
    ExpiredJwtException.class,
    SignatureException.class,
    MalformedJwtException.class,
    UnsupportedJwtException.class
  })
  public ResponseEntity<ErrorMessageResponse> expiredOrInvalidJwt(JwtException ex) {
    ErrorMessageResponse errorMessageResponse = new ErrorMessageResponse(ex.getMessage());
    return ResponseEntity.status(401).body(errorMessageResponse);
  }
}
