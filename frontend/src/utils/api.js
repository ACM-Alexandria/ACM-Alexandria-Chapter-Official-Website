import axios from 'axios';
import tokenService from './tokenService';
import { refreshAccessToken } from './authApi';

const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api";

const axiosConfig = {
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
};

const api = axios.create(axiosConfig);

// Flag to prevent multiple simultaneous refresh attempts
let isRefreshing = false;
let refreshSubscribers = [];

/**
 * Subscribe to token refresh completion
 */
const subscribeTokenRefresh = (callback) => {
  refreshSubscribers.push(callback);
};

/**
 * Notify all subscribers that refresh is complete
 */
const onRefreshComplete = (token) => {
  refreshSubscribers.forEach((callback) => callback(token));
  refreshSubscribers = [];
};

/**
 * Notify all subscribers that refresh failed
 */
const onRefreshFailed = () => {
  refreshSubscribers.forEach((callback) => callback(null));
  refreshSubscribers = [];
};

/**
 * Request Interceptor
 * Automatically attaches access token to all requests
 */
api.interceptors.request.use(
  (config) => {
    // Get access token from memory (tokenService)
    const token = tokenService.getAccessToken();
    
    // Attach token if available
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

/**
 * Response Interceptor
 * Handles 401 Unauthorized by attempting token refresh
 */
api.interceptors.response.use(
  (response) => {
    // Pass through successful responses
    return response;
  },
  async (error) => {
    const originalRequest = error.config;

    // Check if error is 401 Unauthorized
    if (error.response?.status === 401 && !originalRequest._retry) {
      // Check if this is an auth endpoint (login, register, etc.)
      const isAuthEndpoint = 
        originalRequest.url?.includes('/auth/login') ||
        originalRequest.url?.includes('/auth/register') ||
        originalRequest.url?.includes('/auth/forgot-password') ||
        originalRequest.url?.includes('/auth/reset-password') ||
        originalRequest.url?.includes('/auth/refresh');

      // Don't retry auth endpoints
      if (isAuthEndpoint) {
        return Promise.reject(error);
      }

      // Mark request as retried to prevent infinite loops
      originalRequest._retry = true;

      // If already refreshing, queue this request
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          subscribeTokenRefresh((token) => {
            if (token) {
              // Update authorization header with new token
              originalRequest.headers.Authorization = `Bearer ${token}`;
              resolve(api(originalRequest));
            } else {
              reject(error);
            }
          });
        });
      }

      // Start refresh process
      isRefreshing = true;

      try {
        // Attempt to refresh the token
        const newAccessToken = await refreshAccessToken();

        // Update token in original request
        originalRequest.headers.Authorization = `Bearer ${newAccessToken}`;

        // Notify all queued requests
        onRefreshComplete(newAccessToken);

        // Reset refresh flag
        isRefreshing = false;

        // Retry the original request with new token
        return api(originalRequest);

      } catch (refreshError) {
        // Refresh failed - force logout
        isRefreshing = false;
        onRefreshFailed();

        // Clear all tokens
        await tokenService.clearAllTokens();

        // Dispatch logout event for app-wide handling (no page reload)
        window.dispatchEvent(new CustomEvent('auth:logout', {
          detail: { reason: 'Token refresh failed' }
        }));

        return Promise.reject(refreshError);
      }
    }

    // For non-401 errors or already retried requests, reject
    return Promise.reject(error);
  }
);

export default api;