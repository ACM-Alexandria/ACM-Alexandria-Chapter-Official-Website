import api from "./api";

const errorMessage = (error, fallback) =>
  error.response?.data?.message || error.response?.data?.error || fallback;

export const fetchFAQs = async ({ page = 0, size = 20, query = "" } = {}) => {
  try {
    const response = await api.get("/api/faq", { params: { page, size, ...(query.trim() && { query: query.trim() }) } });
    return response.data;
  } catch (error) {
    throw new Error(errorMessage(error, "Unable to load frequently asked questions."));
  }
};

export const createFAQ = async (faq) => {
  try {
    return (await api.post("/api/faq", faq)).data;
  } catch (error) {
    throw new Error(errorMessage(error, "Unable to create the FAQ entry."));
  }
};

export const updateFAQ = async (id, faq) => {
  try {
    return (await api.put(`/api/faq/${id}`, faq)).data;
  } catch (error) {
    throw new Error(errorMessage(error, "Unable to update the FAQ entry."));
  }
};

export const deleteFAQ = async (id) => {
  try {
    await api.delete(`/api/faq/${id}`);
  } catch (error) {
    throw new Error(errorMessage(error, "Unable to delete the FAQ entry."));
  }
};

export const reorderFAQs = async (items) => {
  try {
    return (await api.put("/api/faq/reorder", items)).data;
  } catch (error) {
    throw new Error(errorMessage(error, "Unable to reorder FAQ entries."));
  }
};
