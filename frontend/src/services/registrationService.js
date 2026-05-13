import api from "./api";


// Fetches dynamic form questions configured for a specific event.
export const fetchEventQuestions = async (eventId) => {
  try {
    const response = await api.get(`/api/v1/registrations/events/${eventId}/questions`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching questions for event ${eventId}:`, error);
    throw error.response?.data || new Error("Failed to fetch event questions.");
  }
};

// Fetches dynamic form questions configured for a specific club.
export const fetchClubQuestions = async (clubId) => {
  try {
    const response = await api.get(`/api/v1/registrations/clubs/${clubId}/questions`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching questions for club ${clubId}:`, error);
    throw error.response?.data || new Error("Failed to fetch club questions.");
  }
};

// Submits registration payload (answers map) for an event.
export const registerForEvent = async (eventId, userId, answers = {}) => {
  try {
    const payload = { userId, answers };
    const response = await api.post(`/api/v1/registrations/events/${eventId}`, payload);
    return response.data;
  } catch (error) {
    console.error(`Error registering for event ${eventId}:`, error);
    throw error.response?.data || new Error("Registration failed.");
  }
};


// Submits registration payload (answers map) for a club.
export const registerForClub = async (clubId, userId, answers = {}) => {
  try {
    const payload = { userId, answers };
    const response = await api.post(`/api/v1/registrations/clubs/${clubId}`, payload);
    return response.data;
  } catch (error) {
    console.error(`Error registering for club ${clubId}:`, error);
    throw error.response?.data || new Error("Club joining failed.");
  }
};

export default {
  fetchEventQuestions,
  fetchClubQuestions,
  registerForEvent,
  registerForClub,
};
