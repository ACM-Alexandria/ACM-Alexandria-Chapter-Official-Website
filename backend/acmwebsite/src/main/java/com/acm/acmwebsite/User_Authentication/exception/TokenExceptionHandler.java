package com.acm.acmwebsite.User_Authentication.exception;

import com.acm.acmwebsite.User_Authentication.dto.ErrorMessageResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class TokenExceptionHandler {
    @ExceptionHandler({
            ExpiredRefreshTokenException.class,
            InvalidRefreshTokenException.class
    })
    public ResponseEntity<ErrorMessageResponse> TokenExceptions(RuntimeException ex) {
        ErrorMessageResponse errorMessageResponse = new ErrorMessageResponse(ex.getMessage());
        return ResponseEntity.status(401).body(errorMessageResponse);

    }
}
