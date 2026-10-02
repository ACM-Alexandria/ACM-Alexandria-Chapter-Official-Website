package com.acm.acmwebsite.feature.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class AnnouncementNotAllowedException extends RuntimeException {
    public AnnouncementNotAllowedException(String message) {
        super(message);
    }
}
