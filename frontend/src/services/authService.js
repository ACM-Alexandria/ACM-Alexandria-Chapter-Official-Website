import axios from "axios";

// Base API configuration
const API_BASE_URL = "http://localhost:8000";

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

/**
 * Reset user password using the token from email
 * @param {Object} data - Reset password data
 * @param {string} data.token - Password reset token from URL
 * @param {string} data.new_password - New password
 * @param {string} data.new_password_confirm - Password confirmation
 * @returns {Promise} API response
 */
export const resetPassword = async (data) => {
  try {
    const response = await apiClient.post("/api/v1/auth/reset-password", {
      token: data.token,
      new_password: data.new_password,
      new_password_confirm: data.new_password_confirm,
    });
    return response.data;
  } catch (error) {
    // Re-throw the error to be handled by the component
    throw error;
  }
};

export default {
  resetPassword,
};
import api from "./api";

/**
 * Login user with email and password
 */
export const login = async (credentials) => {
  try {
    const response = await api.post("/v1/auth/login", credentials);
    return response.data;
  } catch (error) {
    // Extract error message from backend response
    if (error.response?.data?.error) {
      throw new Error(error.response.data.error);
    }
    throw new Error("An error occurred during login. Please try again.");
  }
};

/**
 * Register a new user
 */
export const register = async (userData) => {
  try {
    const response = await api.post("/v1/auth/register", userData);
    return response.data;
  } catch (error) {
    if (error.response?.data?.error) {
      throw new Error(error.response.data.error);
    }
    throw new Error("An error occurred during registration. Please try again.");
  }
};

/**
 * Logout user
 */
export const logout = async (refreshToken) => {
  try {
    const response = await api.post("/v1/user/logout", {
      refreshToken,
    });
    return response.data;
  } catch (error) {
    if (error.response?.data?.error) {
      throw new Error(error.response.data.error);
    }
    throw new Error("An error occurred during logout. Please try again.");
  }
};

/**
 * Initiate password reset
 */
export const forgotPassword = async (email) => {
  try {
    const response = await api.post("/v1/auth/forgot-password", { email });
    return response.data;
  } catch (error) {
    if (error.response?.data?.error) {
      throw new Error(error.response.data.error);
    }
    throw new Error(
      "An error occurred while sending reset link. Please try again."
    );
  }
};

/**
 * Reset password with token
 */
export const resetPassword = async (resetData) => {
  try {
    const response = await api.post("/v1/auth/reset-password", resetData);
    return response.data;
  } catch (error) {
    if (error.response?.data?.error) {
      throw new Error(error.response.data.error);
    }
    throw new Error(
      "An error occurred while resetting password. Please try again."
    );
  }
};