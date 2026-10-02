import api from "./api";

/**
 * Submit HR Feedback (Suggestion or Complaint)
 * @param {Object} feedbackData - { type, content, isAnonymous }
 */
export const submitHRFeedback = async (feedbackData) => {
  try {
    const response = await api.post("/api/v1/hr-feedback", feedbackData);
    return response.data;
  } catch (error) {
    console.error("Error submitting HR feedback:", error);
    throw error.response?.data || new Error("Failed to submit HR feedback.");
  }
};

/**
 * Fetch all HR Feedback (HR Board only)
 */
export const getAllHRFeedback = async () => {
  try {
    const response = await api.get("/api/v1/hr-feedback");
    return response.data;
  } catch (error) {
    console.error("Error fetching HR feedback:", error);
    throw error.response?.data || new Error("Failed to fetch HR feedback.");
  }
};

const hrFeedbackService = {
  submitHRFeedback,
  getAllHRFeedback,
};

export default hrFeedbackService;
