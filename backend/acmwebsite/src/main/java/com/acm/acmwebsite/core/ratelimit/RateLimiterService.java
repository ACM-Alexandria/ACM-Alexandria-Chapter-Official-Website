package com.acm.acmwebsite.core.ratelimit;

import io.github.bucket4j.Bucket;

public interface RateLimiterService {
    /**
     * Retrieves or creates a rate-limit bucket for a specific key (IP address).
     * @param apiKey The unique identifier (IP, User ID, etc.)
     * @return The Bucket associated with that key.
     */
    Bucket resolveBucket(String apiKey);
}