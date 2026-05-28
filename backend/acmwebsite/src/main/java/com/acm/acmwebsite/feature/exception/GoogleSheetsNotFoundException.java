package com.acm.acmwebsite.feature.exception;

public class GoogleSheetsNotFoundException extends GoogleSheetsException {
    public GoogleSheetsNotFoundException(String message) {
        super(message);
    }
    public GoogleSheetsNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}
