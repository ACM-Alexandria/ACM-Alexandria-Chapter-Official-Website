package com.acm.acmwebsite.feature.badges;

// A badge change that clashes with existing data, such as overlapping periods or a duplicate award. Answered with 409.
public class BadgeConflictException extends RuntimeException {
    public BadgeConflictException(String message) {
        super(message);
    }
}
