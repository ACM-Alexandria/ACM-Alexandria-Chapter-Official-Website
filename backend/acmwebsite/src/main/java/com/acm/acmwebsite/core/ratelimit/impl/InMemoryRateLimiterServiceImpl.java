package com.acm.acmwebsite.core.ratelimit.impl;

import com.acm.acmwebsite.core.ratelimit.RateLimiterService;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class InMemoryRateLimiterServiceImpl implements RateLimiterService {

    // Thread-safe map to store buckets in RAM
    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public Bucket resolveBucket(String apiKey) {
        return cache.computeIfAbsent(apiKey, this::newBucket);
    }

    private Bucket newBucket(String apiKey) {
        // Configuration: Allow 10 requests per 1 minute.
        // Refill.greedy means tokens are added smoothly over time, not all at once at
        // the end of the minute.
        Bandwidth limit = Bandwidth.classic(10, Refill.greedy(10, Duration.ofMinutes(1)));

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}