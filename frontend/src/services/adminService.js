import api from "./api";

// Fetch admin dashboard analytics insights
export const fetchInsights = async () => {
  try {
    const response = await api.get("/api/admin/insights");
    return response.data;
  } catch (error) {
    console.error("Error fetching admin insights:", error);
    throw error.response?.data || new Error("Unable to load admin insights.");
  }
};

export default {
  fetchInsights,
};
