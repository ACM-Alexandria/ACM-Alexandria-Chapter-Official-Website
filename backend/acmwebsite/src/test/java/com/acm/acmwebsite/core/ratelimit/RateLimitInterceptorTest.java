package com.acm.acmwebsite.core.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.io.PrintWriter;
import java.io.StringWriter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RateLimitInterceptorTest {

    @Mock
    private RateLimiterService rateLimiterService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private Bucket bucket;

    @Mock
    private ConsumptionProbe probe;

    private RateLimitInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new RateLimitInterceptor(rateLimiterService);
    }

    @Test
    @DisplayName("Should allow request and set remaining header when tokens are available")
    void shouldAllowRequestWhenTokensAvailable() throws Exception {
        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(rateLimiterService.resolveBucket("192.168.1.100")).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
        when(probe.isConsumed()).thenReturn(true);
        when(probe.getRemainingTokens()).thenReturn(9L);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isTrue();
        verify(response).addHeader("X-Rate-Limit-Remaining", "9");
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    @DisplayName("Should block request with 429 and set retry header when tokens are exhausted")
    void shouldBlockRequestWhenTokensExhausted() throws Exception {
        StringWriter stringWriter = new StringWriter();
        PrintWriter printWriter = new PrintWriter(stringWriter);

        when(request.getRemoteAddr()).thenReturn("192.168.1.100");
        when(rateLimiterService.resolveBucket("192.168.1.100")).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
        when(probe.isConsumed()).thenReturn(false);
        when(probe.getNanosToWaitForRefill()).thenReturn(15_000_000_000L); // 15 seconds
        when(response.getWriter()).thenReturn(printWriter);

        boolean result = interceptor.preHandle(request, response, new Object());

        assertThat(result).isFalse();
        verify(response).setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        verify(response).addHeader("X-Rate-Limit-Retry-After-Seconds", "15");
        printWriter.flush();
        assertThat(stringWriter.toString()).contains("Too many requests. Please try again in 15 seconds.");
    }
}
