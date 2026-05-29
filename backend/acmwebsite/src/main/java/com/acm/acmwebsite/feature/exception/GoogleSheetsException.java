package com.acm.acmwebsite.feature.exception;

public class GoogleSheetsException extends RuntimeException {
    public GoogleSheetsException(String message) {
        super(message);
    }
    public GoogleSheetsException(String message, Throwable cause) {
        super(message, cause);
    }
}
