import api from "./api";
import tokenService from "./tokenService";

/**
 * Centralized Authentication Service
 * All auth API calls go through the single axios instance (services/api.js).
 * Tokens are managed via tokenService (in-memory + encrypted IndexedDB).
 */

/**
 * Login user with email and password.
 * Stores tokens via tokenService on success.
 * Backend returns camelCase: { id, email, accessToken, refreshToken }
 */
export const login = async (email, password) => {
  try {
    const response = await api.post("/api/v1/auth/login", { email, password });
    const { accessToken, refreshToken } = response.data;

    // Store tokens securely
    await tokenService.setTokens(accessToken, refreshToken);

    return response.data;
  } catch (error) {
    if (error.response?.data?.error) {
      throw new Error(error.response.data.error);
    }
    throw new Error("An error occurred during login. Please try again.");
  }
};

/**
 * Register a new user.
 * Backend expects: { email, password, password_confirmation }
 * Backend returns: { id, email }
 */
export const register = async (userData) => {
  try {
    const response = await api.post("/api/v1/auth/register", userData);
    return response.data;
  } catch (error) {
    if (error.response?.data?.error) {
      throw new Error(error.response.data.error);
    }
    throw new Error("An error occurred during registration. Please try again.");
  }
};

/**
 * Logout user — calls backend, then clears all local tokens.
 * Backend expects: { refresh_token } (snake_case)
 */
export const logout = async () => {
  try {
    const refreshToken = await tokenService.loadRefreshToken();
    if (refreshToken) {
      await api.post("/api/v1/auth/logout", {
        refresh_token: refreshToken,
      });
    }
  } catch (error) {
    console.warn("Logout API call failed:", error);
  } finally {
    await tokenService.clearAllTokens();
  }
};

/**
 * Initiate password reset — sends email with reset link.
 */
export const forgotPassword = async (email) => {
  try {
    const response = await api.post("/api/v1/auth/forgot-password", { email });
    return response.data;
  } catch (error) {
    if (error.response?.data?.error) {
      throw new Error(error.response.data.error);
    }
    throw new Error(
      "An error occurred while sending reset link. Please try again.",
    );
  }
};

/**
 * Reset password with token from email link.
 * Backend expects: { token, new_password, new_password_confirm }
 */
export const resetPassword = async (data) => {
  try {
    const response = await api.post("/api/v1/auth/reset-password", {
      token: data.token,
      new_password: data.new_password,
      new_password_confirm: data.new_password_confirm,
    });
    return response.data;
  } catch (error) {
    if (error.response?.data?.error) {
      throw new Error(error.response.data.error);
    }
    throw new Error(
      "An error occurred while resetting password. Please try again.",
    );
  }
};

/**
 * Refresh access token using stored refresh token.
 * Backend expects: { refresh_token } (snake_case)
 * Backend returns: { access_token, refresh_token } (snake_case)
 */
export const refreshAccessToken = async () => {
  const refreshToken = await tokenService.loadRefreshToken();

  if (!refreshToken) {
    throw new Error("No refresh token available");
  }

  try {
    const response = await api.post("/api/v1/auth/refresh", {
      refresh_token: refreshToken,
    });

    const { access_token, refresh_token } = response.data;

    // Store rotated tokens
    tokenService.setAccessToken(access_token);
    if (refresh_token) {
      await tokenService.setRefreshToken(refresh_token);
    }

    return access_token;
  } catch (error) {
    await tokenService.clearAllTokens();
    throw new Error("Token refresh failed =", error);
  }
};
