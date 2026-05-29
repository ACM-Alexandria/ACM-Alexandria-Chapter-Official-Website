package com.acm.acmwebsite.feature.exception;

public class GoogleSheetsCredentialsException extends GoogleSheetsException {
    public GoogleSheetsCredentialsException(String message) {
        super(message);
    }
    public GoogleSheetsCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}
