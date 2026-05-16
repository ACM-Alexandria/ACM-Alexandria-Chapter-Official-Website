package com.acm.acmwebsite.feature.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class ProfileIncompleteException extends RuntimeException {
    public ProfileIncompleteException(String message) {
        super(message);
    }
}
