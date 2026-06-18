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

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   1. HIGH BOARD MEMBERS CRUD
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
export const addHighBoardMember = async (memberData) => {
  try {
    const response = await api.post("/api/highboard/members", memberData);
    return response.data;
  } catch (error) {
    console.error("Error adding high board member:", error);
    throw error.response?.data || new Error("Failed to add high board member.");
  }
};

export const updateHighBoardMember = async (id, memberData) => {
  try {
    const response = await api.put(`/api/highboard/members/${id}`, memberData);
    return response.data;
  } catch (error) {
    console.error("Error updating high board member:", error);
    throw error.response?.data || new Error("Failed to update high board member.");
  }
};

export const deleteHighBoardMember = async (id) => {
  try {
    const response = await api.delete(`/api/highboard/members/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error deleting high board member:", error);
    throw error.response?.data || new Error("Failed to delete high board member.");
  }
};

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   2. COMMITTEE BOARD MEMBERS CRUD
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
export const addCommitteeBoardMember = async (committeeId, memberData) => {
  try {
    const response = await api.post(`/api/committee/${committeeId}/board-members`, memberData);
    return response.data;
  } catch (error) {
    console.error("Error adding committee board member:", error);
    throw error.response?.data || new Error("Failed to add committee board member.");
  }
};

export const updateCommitteeBoardMember = async (id, memberData) => {
  try {
    const response = await api.put(`/api/committee/board-members/${id}`, memberData);
    return response.data;
  } catch (error) {
    console.error("Error updating committee board member:", error);
    throw error.response?.data || new Error("Failed to update committee board member.");
  }
};

export const deleteCommitteeBoardMember = async (id) => {
  try {
    const response = await api.delete(`/api/committee/board-members/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error deleting committee board member:", error);
    throw error.response?.data || new Error("Failed to delete committee board member.");
  }
};

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   3. COMMITTEES CRUD & CALL MANAGEMENT
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
export const createCommittee = async (committeeData) => {
  try {
    const response = await api.post("/api/committee", committeeData);
    return response.data;
  } catch (error) {
    console.error("Error creating committee:", error);
    throw error.response?.data || new Error("Failed to create committee.");
  }
};

export const updateCommittee = async (id, committeeData) => {
  try {
    const response = await api.put(`/api/committee/${id}`, committeeData);
    return response.data;
  } catch (error) {
    console.error("Error updating committee:", error);
    throw error.response?.data || new Error("Failed to update committee.");
  }
};

export const deleteCommittee = async (id) => {
  try {
    const response = await api.delete(`/api/committee/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error deleting committee:", error);
    throw error.response?.data || new Error("Failed to delete committee.");
  }
};

export const openCommitteeCall = async (id) => {
  try {
    const response = await api.post(`/api/committee/${id}/open-call`);
    return response.data;
  } catch (error) {
    console.error("Error opening committee call:", error);
    throw error.response?.data || new Error("Failed to open committee call.");
  }
};

export const closeCommitteeCall = async (id) => {
  try {
    const response = await api.post(`/api/committee/${id}/close-call`);
    return response.data;
  } catch (error) {
    console.error("Error closing committee call:", error);
    throw error.response?.data || new Error("Failed to close committee call.");
  }
};

export const changeCallMessage = async (id, messageData) => {
  try {
    const response = await api.post(`/api/committee/${id}/change-message`, messageData);
    return response.data;
  } catch (error) {
    console.error("Error changing committee call message:", error);
    throw error.response?.data || new Error("Failed to change call message.");
  }
};

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   4. EVENTS CRUD
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
export const createEvent = async (eventData) => {
  try {
    const response = await api.post("/api/events", eventData);
    return response.data;
  } catch (error) {
    console.error("Error creating event:", error);
    throw error.response?.data || new Error("Failed to create event.");
  }
};

export const updateEvent = async (id, eventData) => {
  try {
    const response = await api.put(`/api/events/${id}`, eventData);
    return response.data;
  } catch (error) {
    console.error("Error updating event:", error);
    throw error.response?.data || new Error("Failed to update event.");
  }
};

export const deleteEvent = async (id) => {
  try {
    const response = await api.delete(`/api/events/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error deleting event:", error);
    throw error.response?.data || new Error("Failed to delete event.");
  }
};

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   5. CLUBS CRUD
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
export const createClub = async (clubData) => {
  try {
    const response = await api.post("/api/clubs", clubData);
    return response.data;
  } catch (error) {
    console.error("Error creating club:", error);
    throw error.response?.data || new Error("Failed to create club.");
  }
};

export const updateClub = async (id, clubData) => {
  try {
    const response = await api.put(`/api/clubs/${id}`, clubData);
    return response.data;
  } catch (error) {
    console.error("Error updating club:", error);
    throw error.response?.data || new Error("Failed to update club.");
  }
};

export const deleteClub = async (id) => {
  try {
    const response = await api.delete(`/api/clubs/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error deleting club:", error);
    throw error.response?.data || new Error("Failed to delete club.");
  }
};

export const fetchClubSocialLinks = async (clubId) => {
  try {
    const response = await api.get(`/api/clubs/${clubId}/social-links`);
    return response.data;
  } catch (error) {
    console.error("Error fetching club social links:", error);
    throw error.response?.data || new Error("Failed to fetch club social links.");
  }
};

export const updateClubSocialLinks = async (clubId, socialLinks) => {
  try {
    const response = await api.put(`/api/clubs/${clubId}/social-links`, socialLinks);
    return response.data;
  } catch (error) {
    console.error("Error updating club social links:", error);
    throw error.response?.data || new Error("Failed to update club social links.");
  }
};

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   6. PROGRAMS CRUD
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
export const createProgram = async (programData) => {
  try {
    const response = await api.post("/api/program", programData);
    return response.data;
  } catch (error) {
    console.error("Error creating program:", error);
    throw error.response?.data || new Error("Failed to create program.");
  }
};

export const updateProgram = async (id, programData) => {
  try {
    const response = await api.put(`/api/program/${id}`, programData);
    return response.data;
  } catch (error) {
    console.error("Error updating program:", error);
    throw error.response?.data || new Error("Failed to update program.");
  }
};

export const deleteProgram = async (id) => {
  try {
    const response = await api.delete(`/api/program/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error deleting program:", error);
    throw error.response?.data || new Error("Failed to delete program.");
  }
};

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   7. REGISTRATION ANALYSIS & GOOGLE SHEETS SYNC
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
export const fetchRegistrationAnalysis = async (resourceType, id) => {
  try {
    const resourcePath = resourceType === "event" ? "events" : resourceType === "club" ? "clubs" : "committee/calls";
    const response = await api.get(`/api/${resourcePath}/${id}/registrations/analysis`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching ${resourceType} registration analysis:`, error);
    throw error.response?.data || new Error(`Failed to fetch ${resourceType} registration analysis.`);
  }
};

export const syncRegistrationSheet = async (resourceType, id) => {
  try {
    const resourcePath = resourceType === "event" ? "events" : resourceType === "club" ? "clubs" : "committee/calls";
    const response = await api.post(`/api/${resourcePath}/${id}/registrations/sheet`);
    return response.data;
  } catch (error) {
    console.error(`Error syncing ${resourceType} registration sheet:`, error);
    throw error.response?.data || new Error(`Failed to sync ${resourceType} registration sheet.`);
  }
};

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   8. FORM QUESTIONS CRUD
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
export const fetchQuestions = async (resourceType, resourceId) => {
  try {
    const resourcePath = resourceType === "event" ? "events" : resourceType === "club" ? "clubs" : "committee";
    const response = await api.get(`/api/${resourcePath}/${resourceId}/questions`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching ${resourceType} questions:`, error);
    throw error.response?.data || new Error(`Failed to fetch ${resourceType} questions.`);
  }
};

export const createQuestion = async (resourceType, resourceId, questionData) => {
  try {
    const resourcePath = resourceType === "event" ? "events" : resourceType === "club" ? "clubs" : "committee";
    const response = await api.post(`/api/${resourcePath}/${resourceId}/questions`, questionData);
    return response.data;
  } catch (error) {
    console.error(`Error creating ${resourceType} question:`, error);
    throw error.response?.data || new Error(`Failed to create ${resourceType} question.`);
  }
};

export const updateQuestion = async (resourceType, resourceId, questionId, questionData) => {
  try {
    const resourcePath = resourceType === "event" ? "events" : resourceType === "club" ? "clubs" : "committee";
    const response = await api.put(`/api/${resourcePath}/${resourceId}/questions/${questionId}`, questionData);
    return response.data;
  } catch (error) {
    console.error(`Error updating ${resourceType} question:`, error);
    throw error.response?.data || new Error(`Failed to update ${resourceType} question.`);
  }
};

export const deleteQuestion = async (resourceType, resourceId, questionId) => {
  try {
    const resourcePath = resourceType === "event" ? "events" : resourceType === "club" ? "clubs" : "committee";
    const response = await api.delete(`/api/${resourcePath}/${resourceId}/questions/${questionId}`);
    return response.data;
  } catch (error) {
    console.error(`Error deleting ${resourceType} question:`, error);
    throw error.response?.data || new Error(`Failed to delete ${resourceType} question.`);
  }
};

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   8.5. COMMITTEE CALLS HISTORY
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
export const fetchCommitteeCalls = async (committeeId) => {
  try {
    const response = await api.get(`/api/committee/${committeeId}/calls`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching calls for committee ${committeeId}:`, error);
    throw error.response?.data || new Error("Failed to fetch committee calls.");
  }
};


/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   9. GLOBAL ACM SOCIAL LINKS CRUD
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
export const fetchSocialLinks = async () => {
  try {
    const response = await api.get("/api/socialLinks");
    return response.data;
  } catch (error) {
    console.error("Error fetching social links:", error);
    throw error.response?.data || new Error("Failed to fetch social links.");
  }
};

export const createSocialLink = async (socialLinkData) => {
  try {
    const response = await api.post("/api/socialLinks", socialLinkData);
    return response.data;
  } catch (error) {
    console.error("Error creating social link:", error);
    throw error.response?.data || new Error("Failed to create social link.");
  }
};

export const updateSocialLink = async (id, socialLinkData) => {
  try {
    const response = await api.put(`/api/socialLinks/${id}`, socialLinkData);
    return response.data;
  } catch (error) {
    console.error("Error updating social link:", error);
    throw error.response?.data || new Error("Failed to update social link.");
  }
};

export const deleteSocialLink = async (id) => {
  try {
    const response = await api.delete(`/api/socialLinks/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error deleting social link:", error);
    throw error.response?.data || new Error("Failed to delete social link.");
  }
};

export const uploadImage = async (file) => {
  try {
    const formData = new FormData();
    formData.append("file", file);
    const response = await api.post("/api/images/upload", formData, {
      headers: {
        "Content-Type": "multipart/form-data",
      },
    });
    return response.data;
  } catch (error) {
    console.error("Error uploading image:", error);
    throw error.response?.data || new Error("Failed to upload image.");
  }
};

export default {
  fetchInsights,
  addHighBoardMember,
  updateHighBoardMember,
  deleteHighBoardMember,
  addCommitteeBoardMember,
  updateCommitteeBoardMember,
  deleteCommitteeBoardMember,
  createCommittee,
  updateCommittee,
  deleteCommittee,
  openCommitteeCall,
  closeCommitteeCall,
  changeCallMessage,
  createEvent,
  updateEvent,
  deleteEvent,
  createClub,
  updateClub,
  deleteClub,
  fetchClubSocialLinks,
  updateClubSocialLinks,
  createProgram,
  updateProgram,
  deleteProgram,
  fetchRegistrationAnalysis,
  syncRegistrationSheet,
  fetchQuestions,
  createQuestion,
  updateQuestion,
  deleteQuestion,
  fetchSocialLinks,
  createSocialLink,
  updateSocialLink,
  deleteSocialLink,
  fetchCommitteeCalls,
  uploadImage,
};
