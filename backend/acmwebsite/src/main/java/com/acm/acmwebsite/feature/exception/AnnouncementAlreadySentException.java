package com.acm.acmwebsite.feature.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;

@ResponseStatus(HttpStatus.CONFLICT)
public class AnnouncementAlreadySentException extends RuntimeException {
    private final LocalDateTime sentAt;

    public AnnouncementAlreadySentException(LocalDateTime sentAt) {
        super("This announcement was already sent. Confirm to send it again.");
        this.sentAt = sentAt;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
