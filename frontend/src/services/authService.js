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
