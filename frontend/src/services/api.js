import axios from "axios";
import tokenService from "./tokenService";
import { getEnv } from "../utils/env";

const rawBaseUrl =
  getEnv("VITE_API_BASE_URL") || "http://localhost:8080";
const API_BASE_URL = rawBaseUrl.endsWith("/") ? rawBaseUrl.slice(0, -1) : rawBaseUrl;

const api = axios.create({
  baseURL: API_BASE_URL,
  timeout: 10000,
  headers: {
    "Content-Type": "application/json",
  },
});

// Request interceptor — attach access token from tokenService (in-memory)
api.interceptors.request.use(
  (config) => {
    const requestUrl = config.url || "";
    // Do not attach token for public auth or refresh endpoints
    const isAuthEndpoint =
      requestUrl.includes("/auth/login") ||
      requestUrl.includes("/auth/register") ||
      requestUrl.includes("/auth/forgot-password") ||
      requestUrl.includes("/auth/reset-password") ||
      requestUrl.includes("/auth/refresh");

    if (!isAuthEndpoint) {
      const token = tokenService.getAccessToken();
      if (token) {
        config.headers.Authorization = `Bearer ${token}`;
      }
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// Flag to track token refresh state
let isRefreshing = false;
let failedQueue = [];

const processQueue = (error, token = null) => {
  failedQueue.forEach((prom) => {
    if (error) {
      prom.reject(error);
    } else {
      prom.resolve(token);
    }
  });
  failedQueue = [];
};

// Response interceptor — handle 401 with silent token refresh
api.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const requestUrl = originalRequest?.url || "";

    // Check if the endpoint is a public auth/refresh endpoint
    const isAuthEndpoint =
      requestUrl.includes("/auth/login") ||
      requestUrl.includes("/auth/register") ||
      requestUrl.includes("/auth/forgot-password") ||
      requestUrl.includes("/auth/reset-password") ||
      requestUrl.includes("/auth/refresh");

    // If it's a 401 error and it's NOT a public auth endpoint, try to refresh
    if (error.response?.status === 401 && !isAuthEndpoint && !originalRequest._retry) {
      if (isRefreshing) {
        return new Promise((resolve, reject) => {
          failedQueue.push({ resolve, reject });
        })
          .then((token) => {
            originalRequest.headers.Authorization = `Bearer ${token}`;
            return api(originalRequest);
          })
          .catch((err) => Promise.reject(err));
      }

      originalRequest._retry = true;
      isRefreshing = true;

      try {
        const refreshToken = await tokenService.loadRefreshToken();
        if (!refreshToken) {
          throw new Error("No refresh token available");
        }

        // Call the refresh endpoint using a clean axios instance to avoid interceptor issues
        const response = await axios.post(
          `${API_BASE_URL}/api/v1/auth/refresh`,
          { refresh_token: refreshToken },
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );

        const { access_token, refresh_token } = response.data;

        tokenService.setAccessToken(access_token);
        if (refresh_token) {
          await tokenService.setRefreshToken(refresh_token);
        }

        processQueue(null, access_token);

        // Retry the original request
        originalRequest.headers.Authorization = `Bearer ${access_token}`;
        return api(originalRequest);
      } catch (refreshError) {
        processQueue(refreshError, null);

        // Clear tokens and dispatch forced logout on refresh failure
        await tokenService.clearAllTokens();
        if (window.location.pathname !== "/login") {
          window.dispatchEvent(new Event("auth:logout"));
        }
        return Promise.reject(refreshError);
      } finally {
        isRefreshing = false;
      }
    }

    // Handle standard 401 cleanup if the refresh failed or wasn't possible
    if (error.response?.status === 401 && !isAuthEndpoint && window.location.pathname !== "/login") {
      await tokenService.clearAllTokens();
      window.dispatchEvent(new Event("auth:logout"));
    }

    return Promise.reject(error);
  },
);

export default api;
