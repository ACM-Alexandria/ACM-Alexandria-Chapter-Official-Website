import api from "./api";


// Fetches dynamic form questions configured for a specific event.
export const fetchEventQuestions = async (eventId) => {
  try {
    const response = await api.get(`/api/events/${eventId}/questions`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching questions for event ${eventId}:`, error);
    throw error.response?.data || new Error("Failed to fetch event questions.");
  }
};

// Fetches dynamic form questions configured for a specific club.
export const fetchClubQuestions = async (clubId) => {
  try {
    const response = await api.get(`/api/clubs/${clubId}/questions`);
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
    const response = await api.post(`/api/events/${eventId}/register`, payload);
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
    const response = await api.post(`/api/clubs/${clubId}/register`, payload);
    return response.data;
  } catch (error) {
    console.error(`Error registering for club ${clubId}:`, error);
    throw error.response?.data || new Error("Club joining failed.");
  }
};

// Checks if user is registered for event (User Request)
export const checkEventRegistrationStatus = async (eventId, userId) => {
  try {
    const response = await api.get(`/api/events/${eventId}/is-registered`, {
      params: { userId }
    });
    return response.data.registered;
  } catch (error) {
    console.error(`Error checking event registration status:`, error);
    return false;
  }
};

// Checks if user is registered for club (User Request)
export const checkClubRegistrationStatus = async (clubId, userId) => {
  try {
    const response = await api.get(`/api/clubs/${clubId}/is-registered`, {
      params: { userId }
    });
    return response.data.registered;
  } catch (error) {
    console.error(`Error checking club registration status:`, error);
    return false;
  }
};

export default {
  fetchEventQuestions,
  fetchClubQuestions,
  registerForEvent,
  registerForClub,
  checkEventRegistrationStatus,
  checkClubRegistrationStatus,
};
