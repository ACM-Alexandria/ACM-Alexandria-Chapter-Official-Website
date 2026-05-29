package com.acm.acmwebsite.feature.exception;

public class GoogleSheetsAcessDenialException extends GoogleSheetsException {
    public GoogleSheetsAcessDenialException(String message) {
        super(message);
    }

    public GoogleSheetsAcessDenialException(String message, Throwable cause) {
        super(message, cause);
    }
}
