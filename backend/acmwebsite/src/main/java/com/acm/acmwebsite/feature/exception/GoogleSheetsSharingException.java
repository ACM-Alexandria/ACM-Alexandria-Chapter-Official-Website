package com.acm.acmwebsite.feature.exception;

public class GoogleSheetsSharingException extends GoogleSheetsException {
    public GoogleSheetsSharingException(String message) {
        super(message);
    }
    public GoogleSheetsSharingException(String message, Throwable cause) {
        super(message, cause);
    }
}
