package com.acm.acmwebsite.feature.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class EmailsLockedException extends RuntimeException {
    public EmailsLockedException() {
        super("Emails are locked by the super admin. Unlock them to send emails.");
    }
}
