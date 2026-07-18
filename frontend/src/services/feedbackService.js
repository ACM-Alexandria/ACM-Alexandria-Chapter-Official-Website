import api from "./api";

/**
 * Submit a feature suggestion
 * @param {Object} featureData - { name, description }
 */
export const submitFeature = async (featureData) => {
  try {
    const response = await api.post("/api/feedback/features", featureData);
    return response.data;
  } catch (error) {
    console.error("Error submitting feature suggestion:", error);
    throw error.response?.data || new Error("Failed to submit feature suggestion.");
  }
};

/**
 * Submit a bug report
 * @param {Object} bugData - { name, description, imageUrls }
 */
export const submitBug = async (bugData) => {
  try {
    const response = await api.post("/api/feedback/bugs", bugData);
    return response.data;
  } catch (error) {
    console.error("Error submitting bug report:", error);
    throw error.response?.data || new Error("Failed to submit bug report.");
  }
};

/**
 * Upload a single screenshot file to Cloudinary
 * @param {File} file
 */
export const uploadScreenshot = async (file) => {
  try {
    const formData = new FormData();
    formData.append("file", file);
    const response = await api.post("/api/feedback/upload-screenshot", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error uploading screenshot:", error);
    throw error.response?.data || new Error("Failed to upload screenshot.");
  }
};

/**
 * Fetch all feature suggestions (Admin-only)
 */
export const getAllFeatures = async () => {
  try {
    const response = await api.get("/api/feedback/features");
    return response.data;
  } catch (error) {
    console.error("Error fetching feature suggestions:", error);
    throw error.response?.data || new Error("Failed to fetch feature suggestions.");
  }
};

/**
 * Fetch all bug reports (Admin-only)
 */
export const getAllBugs = async () => {
  try {
    const response = await api.get("/api/feedback/bugs");
    return response.data;
  } catch (error) {
    console.error("Error fetching bug reports:", error);
    throw error.response?.data || new Error("Failed to fetch bug reports.");
  }
};

/**
 * Toggle a feature suggestion status (Admin-only)
 */
export const toggleFeatureStatus = async (id) => {
  try {
    const response = await api.put(`/api/feedback/features/${id}/toggle-status`);
    return response.data;
  } catch (error) {
    console.error(`Error toggling feature suggestion ${id} status:`, error);
    throw error.response?.data || new Error("Failed to update feature suggestion status.");
  }
};

/**
 * Toggle a bug report status (Admin-only)
 */
export const toggleBugStatus = async (id) => {
  try {
    const response = await api.put(`/api/feedback/bugs/${id}/toggle-status`);
    return response.data;
  } catch (error) {
    console.error(`Error toggling bug report ${id} status:`, error);
    throw error.response?.data || new Error("Failed to update bug report status.");
  }
};

const feedbackService = {
  submitFeature,
  submitBug,
  uploadScreenshot,
  getAllFeatures,
  getAllBugs,
  toggleFeatureStatus,
  toggleBugStatus,
};

export default feedbackService;
