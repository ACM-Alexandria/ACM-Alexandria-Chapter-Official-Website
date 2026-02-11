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