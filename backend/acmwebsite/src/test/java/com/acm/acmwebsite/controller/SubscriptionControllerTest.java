package com.acm.acmwebsite.controller;

import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acm.acmwebsite.EmailServer.exception.GlobalExceptionHandler;
import com.acm.acmwebsite.core.ratelimit.RateLimitInterceptor;
import com.acm.acmwebsite.core.util.JwtUtil;
import com.acm.acmwebsite.feature.controller.SubscriptionController;
import com.acm.acmwebsite.feature.service.SubscriptionService;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(controllers = SubscriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@DisplayName("SubscriptionController WebMvc Tests")
class SubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionService subscriptionService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RateLimitInterceptor rateLimitInterceptor;

    @BeforeEach
    void setUp() throws Exception {
        when(rateLimitInterceptor.preHandle(any(), any(), any())).thenReturn(true);
    }

    @Nested
    @DisplayName("POST /api/subscriptions/news/subscribe")
    class SubscribeToNewsTests {

        @Test
        @DisplayName("should return 401 Unauthorized when authentication is null")
        void shouldReturn401WhenAuthIsNull() throws Exception {
            mockMvc.perform(post("/api/subscriptions/news/subscribe"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", equalTo("Unauthorized")));
        }

        @Test
        @DisplayName("should return 401 Unauthorized when authentication is not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);

            mockMvc.perform(post("/api/subscriptions/news/subscribe").principal(auth))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", equalTo("Unauthorized")));
        }

        @Test
        @DisplayName("should return 200 OK on successful subscription")
        void shouldSubscribeSuccessfully() throws Exception {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("user@test.com", null, Collections.emptyList());

            doNothing().when(subscriptionService).subscribeToNews("user@test.com");

            mockMvc.perform(post("/api/subscriptions/news/subscribe").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", equalTo("Successfully subscribed to news")));

            verify(subscriptionService).subscribeToNews("user@test.com");
        }

        @Test
        @DisplayName("should return 400 Bad Request when subscription throws exception")
        void shouldReturn400WhenSubscriptionFails() throws Exception {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("user@test.com", null, Collections.emptyList());

            doThrow(new RuntimeException("Already subscribed")).when(subscriptionService).subscribeToNews("user@test.com");

            mockMvc.perform(post("/api/subscriptions/news/subscribe").principal(auth))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Already subscribed")));

            verify(subscriptionService).subscribeToNews("user@test.com");
        }
    }

    @Nested
    @DisplayName("POST /api/subscriptions/news/unsubscribe")
    class UnsubscribeFromNewsTests {

        @Test
        @DisplayName("should return 401 Unauthorized when authentication is null")
        void shouldReturn401WhenAuthIsNull() throws Exception {
            mockMvc.perform(post("/api/subscriptions/news/unsubscribe"))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", equalTo("Unauthorized")));
        }

        @Test
        @DisplayName("should return 401 Unauthorized when authentication is not authenticated")
        void shouldReturn401WhenNotAuthenticated() throws Exception {
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);

            mockMvc.perform(post("/api/subscriptions/news/unsubscribe").principal(auth))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message", equalTo("Unauthorized")));
        }

        @Test
        @DisplayName("should return 200 OK on successful unsubscription")
        void shouldUnsubscribeSuccessfully() throws Exception {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("user@test.com", null, Collections.emptyList());

            doNothing().when(subscriptionService).unsubscribeFromNews("user@test.com");

            mockMvc.perform(post("/api/subscriptions/news/unsubscribe").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.message", equalTo("Successfully unsubscribed from news")));

            verify(subscriptionService).unsubscribeFromNews("user@test.com");
        }

        @Test
        @DisplayName("should return 400 Bad Request when unsubscription throws exception")
        void shouldReturn400WhenUnsubscriptionFails() throws Exception {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("user@test.com", null, Collections.emptyList());

            doThrow(new RuntimeException("Not subscribed")).when(subscriptionService).unsubscribeFromNews("user@test.com");

            mockMvc.perform(post("/api/subscriptions/news/unsubscribe").principal(auth))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.message", equalTo("Not subscribed")));

            verify(subscriptionService).unsubscribeFromNews("user@test.com");
        }
    }

    @Nested
    @DisplayName("GET /api/subscriptions/news/subscription-status")
    class GetNewsSubscriptionStatusTests {

        @Test
        @DisplayName("should return subscribed false when authentication is null")
        void shouldReturnFalseWhenAuthIsNull() throws Exception {
            mockMvc.perform(get("/api/subscriptions/news/subscription-status"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subscribed", equalTo(false)));
        }

        @Test
        @DisplayName("should return subscribed false when authentication is not authenticated")
        void shouldReturnFalseWhenNotAuthenticated() throws Exception {
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(false);

            mockMvc.perform(get("/api/subscriptions/news/subscription-status").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subscribed", equalTo(false)));
        }

        @Test
        @DisplayName("should return subscribed false when authentication getName is null")
        void shouldReturnFalseWhenNameIsNull() throws Exception {
            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn(null);

            mockMvc.perform(get("/api/subscriptions/news/subscription-status").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subscribed", equalTo(false)));
        }

        @Test
        @DisplayName("should return subscribed true when user is subscribed")
        void shouldReturnTrueWhenSubscribed() throws Exception {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("user@test.com", null, Collections.emptyList());

            when(subscriptionService.isSubscribedToNews("user@test.com")).thenReturn(true);

            mockMvc.perform(get("/api/subscriptions/news/subscription-status").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subscribed", equalTo(true)));

            verify(subscriptionService).isSubscribedToNews("user@test.com");
        }

        @Test
        @DisplayName("should return subscribed false when user is not subscribed")
        void shouldReturnFalseWhenNotSubscribed() throws Exception {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("user@test.com", null, Collections.emptyList());

            when(subscriptionService.isSubscribedToNews("user@test.com")).thenReturn(false);

            mockMvc.perform(get("/api/subscriptions/news/subscription-status").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subscribed", equalTo(false)));

            verify(subscriptionService).isSubscribedToNews("user@test.com");
        }

        @Test
        @DisplayName("should return subscribed false when status check throws exception")
        void shouldReturnFalseWhenServiceThrowsException() throws Exception {
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken("user@test.com", null, Collections.emptyList());

            when(subscriptionService.isSubscribedToNews("user@test.com")).thenThrow(new RuntimeException("Database error"));

            mockMvc.perform(get("/api/subscriptions/news/subscription-status").principal(auth))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.subscribed", equalTo(false)));

            verify(subscriptionService).isSubscribedToNews("user@test.com");
        }
    }
}
