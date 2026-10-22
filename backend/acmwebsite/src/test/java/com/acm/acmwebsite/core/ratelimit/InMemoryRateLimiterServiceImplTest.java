package com.acm.acmwebsite.core.ratelimit;

import com.acm.acmwebsite.core.ratelimit.impl.InMemoryRateLimiterServiceImpl;
import io.github.bucket4j.Bucket;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryRateLimiterServiceImplTest {

    private InMemoryRateLimiterServiceImpl rateLimiterService;

    @BeforeEach
    void setUp() {
        rateLimiterService = new InMemoryRateLimiterServiceImpl();
    }

    @Test
    @DisplayName("Should create non-null Bucket for a new API key")
    void shouldCreateNonNullBucketForNewKey() {
        Bucket bucket = rateLimiterService.resolveBucket("192.168.1.1");
        assertThat(bucket).isNotNull();
    }

    @Test
    @DisplayName("Should return the exact same Bucket instance for repeated calls with same IP")
    void shouldReturnSameBucketForSameKey() {
        Bucket bucket1 = rateLimiterService.resolveBucket("192.168.1.1");
        Bucket bucket2 = rateLimiterService.resolveBucket("192.168.1.1");

        assertThat(bucket1).isSameAs(bucket2);
    }

    @Test
    @DisplayName("Should return different Bucket instances for different IP addresses")
    void shouldReturnDifferentBucketsForDifferentKeys() {
        Bucket bucket1 = rateLimiterService.resolveBucket("192.168.1.1");
        Bucket bucket2 = rateLimiterService.resolveBucket("192.168.1.2");

        assertThat(bucket1).isNotSameAs(bucket2);
    }

    @Test
    @DisplayName("Bucket should have 10 initial tokens and reject after 10 consumptions")
    void shouldEnforceInitialCapacityOf10Tokens() {
        Bucket bucket = rateLimiterService.resolveBucket("10.0.0.1");

        // Consume 10 tokens successfully
        for (int i = 0; i < 10; i++) {
            assertThat(bucket.tryConsume(1)).isTrue();
        }

        // 11th consumption should fail
        assertThat(bucket.tryConsume(1)).isFalse();
    }
}
