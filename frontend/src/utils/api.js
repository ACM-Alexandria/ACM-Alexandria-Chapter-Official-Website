import axios from 'axios';

// Create axios instance with base configuration
const api = axios.create({
  baseURL: '/api/v1', // Base URL for all API requests
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 second timeout
});

/**
 * Sends a forgot password request to the backend
 * @param {string} email - User's email address
 * @returns {Promise} - Axios promise with response data
 * @throws {Error} - On network error or server error
 */
export const forgotPassword = async (email) => {
  try {
    const response = await api.post('/auth/forgot-password', { email });
    return response.data;
  } catch (error) {
    // Re-throw the error for component-level handling
    throw error;
  }
};

// Export the axios instance for future use with other endpoints
export default api;
