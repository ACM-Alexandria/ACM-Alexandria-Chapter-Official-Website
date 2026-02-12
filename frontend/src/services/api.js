import axios from "axios";

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

// Request interceptor
api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("token");
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error),
);

// Response interceptor
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      // Get the request URL
      const requestUrl = error.config?.url || "";
      
      // Only redirect if NOT on auth endpoints (login, register, etc.)
      const isAuthEndpoint = 
        requestUrl.includes("/auth/login") || 
        requestUrl.includes("/auth/register") ||
        requestUrl.includes("/auth/forgot-password") ||
        requestUrl.includes("/auth/reset-password");
      
      // Only redirect to login if:
      // 1. NOT an auth endpoint (login/register)
      // 2. NOT already on the login page
      if (!isAuthEndpoint && window.location.pathname !== "/login") {
        localStorage.removeItem("token");
        window.location.href = "/login";
      }
    }
    return Promise.reject(error);
  },
);

export default api;