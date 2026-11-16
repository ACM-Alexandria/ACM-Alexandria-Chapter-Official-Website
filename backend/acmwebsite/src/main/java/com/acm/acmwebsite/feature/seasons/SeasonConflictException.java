package com.acm.acmwebsite.feature.seasons;

// A season change that clashes with existing data, such as overlapping dates or a period still in use. Answered with 409.
public class SeasonConflictException extends RuntimeException {
    public SeasonConflictException(String message) {
        super(message);
    }
}
