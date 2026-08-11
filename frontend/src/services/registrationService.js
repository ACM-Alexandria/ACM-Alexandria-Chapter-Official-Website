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
    const response = await api.get(`/api/events/${eventId}/is-registered/${userId}`);
    return response.data.registered;
  } catch (error) {
    console.error(`Error checking event registration status:`, error);
    return false;
  }
};

// Checks if user is registered for club (User Request)
export const checkClubRegistrationStatus = async (clubId, userId) => {
  try {
    const response = await api.get(`/api/clubs/${clubId}/is-registered/${userId}`);
    return response.data.registered;
  } catch (error) {
    console.error(`Error checking club registration status:`, error);
    return false;
  }
};

// Fetches dynamic form questions configured for a specific committee.
export const fetchCommitteeQuestions = async (committeeId) => {
  try {
    const response = await api.get(`/api/committee/${committeeId}/questions`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching questions for committee ${committeeId}:`, error);
    throw error.response?.data || new Error("Failed to fetch committee questions.");
  }
};

// Submits registration payload (answers map) for a committee.
export const registerForCommittee = async (committeeId, userId, answers = {}) => {
  try {
    const payload = { userId, answers };
    const response = await api.post(`/api/committee/${committeeId}/register`, payload);
    return response.data;
  } catch (error) {
    console.error(`Error registering for committee ${committeeId}:`, error);
    throw error.response?.data || new Error("Committee application failed.");
  }
};

// Checks if user has already applied for the currently open call of a committee.
export const checkCommitteeRegistrationStatus = async (committeeId, userId) => {
  try {
    const response = await api.get(`/api/committee/${committeeId}/is-registered/${userId}`);
    return response.data.registered;
  } catch (error) {
    console.error(`Error checking committee registration status:`, error);
    return false;
  }
};

// Fetches dynamic form questions configured for a specific program.
export const fetchProgramQuestions = async (programId) => {
  try {
    const response = await api.get(`/api/program/${programId}/questions`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching questions for program ${programId}:`, error);
    throw error.response?.data || new Error("Failed to fetch program questions.");
  }
};

// Submits registration payload (answers map) for a program.
export const registerForProgram = async (programId, userId, answers = {}) => {
  try {
    const payload = { userId, answers };
    const response = await api.post(`/api/program/${programId}/register`, payload);
    return response.data;
  } catch (error) {
    console.error(`Error registering for program ${programId}:`, error);
    throw error.response?.data || new Error("Program registration failed.");
  }
};

// Checks if user is registered for a program.
export const checkProgramRegistrationStatus = async (programId, userId) => {
  try {
    const response = await api.get(`/api/program/${programId}/is-registered/${userId}`);
    return response.data.registered;
  } catch (error) {
    console.error(`Error checking program registration status:`, error);
    return false;
  }
};

// Fetches dynamic form questions configured for a specific exclusive form.
export const fetchExclusiveFormQuestions = async (formId) => {
  try {
    const response = await api.get(`/api/exclusive-forms/${formId}/questions`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching questions for exclusive form ${formId}:`, error);
    throw error.response?.data || new Error("Failed to fetch exclusive form questions.");
  }
};

// Submits registration payload (answers map) for an exclusive form.
export const registerForExclusiveForm = async (formId, userId, answers = {}) => {
  try {
    const payload = { userId, answers };
    const response = await api.post(`/api/exclusive-forms/${formId}/register`, payload);
    return response.data;
  } catch (error) {
    console.error(`Error registering for exclusive form ${formId}:`, error);
    throw error.response?.data || new Error("Exclusive form submission failed.");
  }
};

// Checks if user is registered for an exclusive form.
export const checkExclusiveFormRegistrationStatus = async (formId, userId) => {
  try {
    const response = await api.get(`/api/exclusive-forms/${formId}/is-registered/${userId}`);
    return response.data.registered;
  } catch (error) {
    console.error(`Error checking exclusive form registration status:`, error);
    return false;
  }
};

export default {
  fetchEventQuestions,
  fetchClubQuestions,
  fetchCommitteeQuestions,
  fetchProgramQuestions,
  fetchExclusiveFormQuestions,
  registerForEvent,
  registerForClub,
  registerForCommittee,
  registerForProgram,
  registerForExclusiveForm,
  checkEventRegistrationStatus,
  checkClubRegistrationStatus,
  checkCommitteeRegistrationStatus,
  checkProgramRegistrationStatus,
  checkExclusiveFormRegistrationStatus,
};
