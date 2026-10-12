import api from "./api";

// GET: /api/pointsLog/{id}/logs
// Using size=5 to match your Spring Boot @PageableDefault
export const fetchUserPointsLogs = async (userId, page = 0, size = 5) => {
  try {
    const response = await api.get(`/api/pointsLog/${userId}/logs`, {
      params: { page, size }
    });
    // Returns a Spring Boot Page object (data.content has the array of logs)
    return response.data;
  } catch (error) {
    console.error(`Error fetching points logs:`, error);
    throw error.response?.data || new Error("Unable to load points history.");
  }
};

// POST: /api/pointsLog/add
// logData must include: { member: UUID, assigner: UUID, reason: String, points: int }
export const addUserPointsLog = async (logData) => {
  try {
    const response = await api.post(`/api/pointsLog/add`, logData);
    return response.data;
  } catch (error) {
    console.error(`Error adding points log:`, error);
    throw error.response?.data || new Error("Failed to add points.");
  }
};

// PUT: /api/pointsLog/update/{id}
// logId is the UUID of the specific log, not the user!
// logData must include: { points: int, reason: String }
export const updateUserPointsLog = async (logId, logData) => {
  try {
    const response = await api.put(`/api/pointsLog/update/${logId}`, logData);
    return response.data;
  } catch (error) {
    console.error(`Error updating points log:`, error);
    throw error.response?.data || new Error("Failed to update points log.");
  }
};

// DELETE: /api/pointsLog/delete/{id}
export const deleteUserPointsLog = async (logId) => {
  try {
    const response = await api.delete(`/api/pointsLog/delete/${logId}`);
    return response.data;
  } catch (error) {
    console.error(`Error deleting points log:`, error);
    throw error.response?.data || new Error("Failed to delete points log.");
  }
};

export default {
  fetchUserPointsLogs,
  addUserPointsLog,
  updateUserPointsLog,
  deleteUserPointsLog,
};