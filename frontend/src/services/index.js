/**
 * Services Index
 *
 * Re-exports all services for convenient importing
 *
 * Usage:
 * import { tokenService, authApi, apiClient } from './services';
 * // or
 * import { getAccessToken, login, get } from './services';
 */

// Token management
export { default as tokenService } from './tokenService';
export {
  getAccessToken,
  setAccessToken,
  removeAccessToken,
  getRefreshToken,
  setRefreshToken,
  removeRefreshToken,
  setTokens,
  clearAllTokens,
  hasAccessToken,
  hasRefreshToken,
} from './tokenService';

// Auth API
export { default as authApi } from './authApi';
export {
  refreshAccessToken,
  login,
  register,
  logout,
  isAuthenticated,
  canRestoreSession,
} from './authApi';

// API Client
export { default as apiClient } from './apiClient';
export {
  apiRequest,
  get,
  post,
  put,
  del,
} from './apiClient';

