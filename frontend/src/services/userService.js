import api from "./api";


// Loads detailed user profile based on user UUID.
export const fetchUserProfile = async (userId) => {
  try {
    const response = await api.get(`/api/v1/users/${userId}/profile`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching user profile:`, error);
    throw error.response?.data || new Error("Unable to load user profile.");
  }
};

// Updates complete user profile dataset.
export const updateUserProfile = async (userId, profileData) => {
  try {
    const response = await api.put(`/api/v1/users/${userId}/profile`, profileData);
    return response.data;
  } catch (error) {
    console.error(`Error updating user profile:`, error);
    throw error.response?.data || new Error("Failed to update profile.");
  }
};

export default {
  fetchUserProfile,
  updateUserProfile,
};
