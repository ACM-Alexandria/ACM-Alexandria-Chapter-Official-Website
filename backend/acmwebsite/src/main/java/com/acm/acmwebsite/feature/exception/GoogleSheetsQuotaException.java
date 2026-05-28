package com.acm.acmwebsite.feature.exception;

public class GoogleSheetsQuotaException extends GoogleSheetsException {
    public GoogleSheetsQuotaException(String message) {
        super(message);
    }
    public GoogleSheetsQuotaException(String message, Throwable cause) {
        super(message, cause);
    }
}
