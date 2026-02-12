/**
 * Authentication API Service
 *
 * Handles all authentication-related API calls including:
 * - Login
 * - Logout
 * - Token refresh
 *
 * Uses snake_case for API request/response to match backend expectations
 */

import tokenService from './tokenService';

// API base URL - can be configured via environment variable
const API_BASE = '/api/v1/auth';

/**
 * Refresh the access token using stored refresh token
 * @returns {Promise<string>} New access token
 * @throws {Error} If refresh fails or no refresh token available
 */
export const refreshAccessToken = async () => {
  const refreshToken = await tokenService.loadRefreshToken();

  if (!refreshToken) {
    throw new Error('No refresh token available');
  }

  const response = await fetch(`${API_BASE}/refresh`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ refresh_token: refreshToken }),
  });

  if (!response.ok) {
    // Clear tokens on refresh failure
    await tokenService.clearAllTokens();
    throw new Error('Token refresh failed');
  }

  const data = await response.json();

  // Store new tokens (using snake_case from backend response)
  tokenService.setAccessToken(data.access_token);

  // Backend may return a new refresh token (rotation) or keep the same one
  if (data.refresh_token) {
    await tokenService.setRefreshToken(data.refresh_token);
  }

  return data.access_token;
};

/**
 * Login user and store tokens securely
 * @param {string} email - User email
 * @param {string} password - User password
 * @returns {Promise<Object>} Login response data
 * @throws {Error} If login fails
 */
export const login = async (email, password) => {
  const response = await fetch(`${API_BASE}/login`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify({ email, password }),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Login failed');
  }

  const data = await response.json();

  // Store tokens securely (expects snake_case from backend)
  await tokenService.setTokens(data.access_token, data.refresh_token);

  return data;
};

/**
 * Register a new user
 * @param {Object} userData - User registration data
 * @returns {Promise<Object>} Registration response data
 * @throws {Error} If registration fails
 */
export const register = async (userData) => {
  const response = await fetch(`${API_BASE}/register`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(userData),
  });

  if (!response.ok) {
    const errorData = await response.json().catch(() => ({}));
    throw new Error(errorData.message || 'Registration failed');
  }

  return await response.json();
};

/**
 * Logout user and clear all tokens
 * Attempts to call backend logout endpoint, then clears local tokens
 * @returns {Promise<void>}
 */
export const logout = async () => {
  try {
    const accessToken = tokenService.getAccessToken();
    const refreshToken = await tokenService.loadRefreshToken();

    if (accessToken || refreshToken) {
      await fetch(`${API_BASE}/logout`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(accessToken && { 'Authorization': `Bearer ${accessToken}` }),
        },
        body: JSON.stringify({
          refresh_token: refreshToken,
        }),
      });
    }
  } catch (error) {
    // Log error but continue with local cleanup
    console.warn('Logout API call failed:', error);
  } finally {
    // Always clear local tokens regardless of API success
    await tokenService.clearAllTokens();
  }
};

/**
 * Check if user is currently authenticated
 * @returns {boolean} True if user has access token
 */
export const isAuthenticated = () => {
  return tokenService.hasAccessToken();
};

/**
 * Check if user can potentially restore session
 * @returns {boolean} True if refresh token exists
 */
export const canRestoreSession = () => {
  return tokenService.hasRefreshToken();
};

export default {
  refreshAccessToken,
  login,
  register,
  logout,
  isAuthenticated,
  canRestoreSession,
};
