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

export const sendCustomEmail = async (payload) => {
  try {
    const response = await api.post("/api/admin/emails/send-custom", payload, { timeout: 60000 });
    return response.data;
  } catch (error) {
    console.error("Error sending custom email:", error);
    throw error.response?.data || new Error("Failed to send custom email.");
  }
};

export const previewCustomEmail = async (payload) => {
  try {
    const response = await api.post("/api/admin/emails/preview", payload);
    return response.data;
  } catch (error) {
    console.error("Error previewing custom email recipients:", error);
    throw error.response?.data || new Error("Failed to preview email recipients.");
  }
};

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   0. USER MANAGEMENT
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
export const searchUsers = async (query = "", role = "", page = 0, size = 10, committeeId = "", clubId = "") => {
  try {
    const params = new URLSearchParams({ page, size });
    if (query) params.append("query", query);
    if (role) params.append("role", role);
    if (committeeId) params.append("committeeId", committeeId);
    if (clubId) params.append("clubId", clubId);
    const response = await api.get(`/api/v1/admin/users?${params.toString()}`);
    return response.data;
  } catch (error) {
    console.error("Error searching users:", error);
    throw error.response?.data || new Error("Failed to search users.");
  }
};

export const previewMembersExport = async (filters) => {
  try {
    const response = await api.post("/api/v1/admin/users/export/preview", filters);
    return response.data;
  } catch (error) {
    console.error("Error previewing members export:", error);
    throw error.response?.data || new Error("Failed to preview members export.");
  }
};

export const exportMembersSheet = async (payload) => {
  try {
    // Creating and filling a new sheet can take longer than the default 10s client timeout
    const response = await api.post("/api/v1/admin/users/export/sheet", payload, { timeout: 60000 });
    return response.data;
  } catch (error) {
    console.error("Error exporting members sheet:", error);
    throw error.response?.data || new Error("Failed to export members sheet.");
  }
};

export const updateUserRole = async (userId, role) => {
  try {
    const response = await api.put(`/api/v1/admin/users/${userId}/role`, { role });
    return response.data;
  } catch (error) {
    console.error("Error updating user role:", error);
    throw error.response?.data || new Error("Failed to update user role.");
  }
};

export const assignUserAssociations = async (userId, committeeId, clubId) => {
  try {
    const response = await api.put(`/api/v1/admin/users/${userId}/associations`, { committeeId, clubId });
    return response.data;
  } catch (error) {
    console.error("Error assigning user associations:", error);
    throw error.response?.data || new Error("Failed to assign associations.");
  }
};

export const assignUser = async (userId, payload) => {
  try {
    const response = await api.post(`/api/v1/admin/users/${userId}/assign`, payload);
    return response.data;
  } catch (error) {
    console.error("Error assigning user role/boards:", error);
    throw error.response?.data || new Error("Failed to assign user details.");
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

export const openCommitteeCall = async (id, sendAnnouncement = true) => {
  try {
    const response = await api.post(`/api/committee/${id}/open-call`, { sendAnnouncement });
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

export const announceCommitteeCall = async (id, force = false) => {
  try {
    const response = await api.post(`/api/committee/${id}/announce-call`, null, { params: { force } });
    return response.data;
  } catch (error) {
    console.error("Error announcing committee call:", error);
    throw error.response?.data || new Error("Failed to send the call email.");
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

export const openClubCall = async (id) => {
  try {
    const response = await api.post(`/api/clubs/${id}/open-call`);
    return response.data;
  } catch (error) {
    console.error("Error opening club call:", error);
    throw error.response?.data || new Error("Failed to open club call.");
  }
};

export const closeClubCall = async (id) => {
  try {
    const response = await api.post(`/api/clubs/${id}/close-call`);
    return response.data;
  } catch (error) {
    console.error("Error closing club call:", error);
    throw error.response?.data || new Error("Failed to close club call.");
  }
};

export const openEventCall = async (id) => {
  try {
    const response = await api.post(`/api/events/${id}/open-call`);
    return response.data;
  } catch (error) {
    console.error("Error opening event call:", error);
    throw error.response?.data || new Error("Failed to open event call.");
  }
};

export const closeEventCall = async (id) => {
  try {
    const response = await api.post(`/api/events/${id}/close-call`);
    return response.data;
  } catch (error) {
    console.error("Error closing event call:", error);
    throw error.response?.data || new Error("Failed to close event call.");
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

export const toggleProgramRegistration = async (programId, open) => {
  try {
    const response = await api.post(`/api/program/${programId}/toggle-registration?open=${open}`);
    return response.data;
  } catch (error) {
    console.error("Error toggling program registration:", error);
    throw error.response?.data || new Error("Failed to toggle program registration.");
  }
};

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   6.5. EXCLUSIVE FORMS CRUD
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
export const fetchExclusiveForms = async () => {
  try {
    const response = await api.get("/api/exclusive-forms");
    return response.data;
  } catch (error) {
    console.error("Error fetching exclusive forms:", error);
    throw error.response?.data || new Error("Failed to fetch exclusive forms.");
  }
};

export const createExclusiveForm = async (formData) => {
  try {
    const response = await api.post("/api/exclusive-forms", formData);
    return response.data;
  } catch (error) {
    console.error("Error creating exclusive form:", error);
    throw error.response?.data || new Error("Failed to create exclusive form.");
  }
};

export const updateExclusiveForm = async (id, formData) => {
  try {
    const response = await api.put(`/api/exclusive-forms/${id}`, formData);
    return response.data;
  } catch (error) {
    console.error("Error updating exclusive form:", error);
    throw error.response?.data || new Error("Failed to update exclusive form.");
  }
};

export const deleteExclusiveForm = async (id) => {
  try {
    const response = await api.delete(`/api/exclusive-forms/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error deleting exclusive form:", error);
    throw error.response?.data || new Error("Failed to delete exclusive form.");
  }
};

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   6.6. EMAIL NOTIFICATIONS (manual announcements & site-wide lock)
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
const RESOURCE_PATHS = {
  events: "/api/events",
  clubs: "/api/clubs",
  programs: "/api/program",
};

const resourcePath = (resourceType) => {
  const basePath = RESOURCE_PATHS[resourceType];
  if (!basePath) throw new Error(`Email actions are not supported for ${resourceType}.`);
  return basePath;
};

// Pass force = true to resend an announcement that was already sent
export const sendAnnouncement = async (resourceType, id, force = false) => {
  const basePath = resourcePath(resourceType);
  try {
    const response = await api.post(`${basePath}/${id}/announce`, null, { params: { force } });
    return response.data;
  } catch (error) {
    console.error("Error sending announcement:", error);
    throw error.response?.data || new Error("Failed to send announcement.");
  }
};

// Runs send(false); if the backend says it was already sent, asks the admin and resends with force.
// confirm is the themed dialog's confirm (useThemedDialog). Returns false when the admin declined to resend.
export const sendWithResendPrompt = async (send, confirm) => {
  try {
    await send(false);
    return true;
  } catch (err) {
    if (err?.code !== "ANNOUNCEMENT_ALREADY_SENT") throw err;
    const when = err.sentAt ? new Date(err.sentAt).toLocaleString() : "earlier";
    const resend = await confirm({
      tone: "warning",
      title: "Already Sent",
      message: `This was already sent on ${when}. Send it to all subscribers again?`,
      confirmLabel: "Yes, Resend",
    });
    if (!resend) return false;
    await send(true);
    return true;
  }
};

// Announcement status (events and clubs)
export const fetchEmailSettings = async (resourceType, id) => {
  const basePath = resourcePath(resourceType);
  try {
    const response = await api.get(`${basePath}/${id}/email-settings`);
    return response.data;
  } catch (error) {
    console.error("Error fetching email settings:", error);
    throw error.response?.data || new Error("Failed to fetch email settings.");
  }
};

// Site-wide email lock (any admin can read it; only SUPER_ADMIN can change it)
export const fetchEmailLock = async () => {
  try {
    const response = await api.get("/api/admin/settings/email-lock");
    return response.data;
  } catch (error) {
    console.error("Error fetching email lock:", error);
    throw error.response?.data || new Error("Failed to fetch email lock status.");
  }
};

export const updateEmailLock = async (emailsEnabled) => {
  try {
    const response = await api.put("/api/admin/settings/email-lock", { emailsEnabled });
    return response.data;
  } catch (error) {
    console.error("Error updating email lock:", error);
    throw error.response?.data || new Error("Failed to update email lock.");
  }
};

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   7. REGISTRATION ANALYSIS & GOOGLE SHEETS SYNC
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
export const fetchRegistrationAnalysis = async (resourceType, id) => {
  try {
    const resourcePath =
      resourceType === "event" ? "events" :
        resourceType === "club" ? "clubs" :
          resourceType === "program" ? "program" :
            resourceType === "exclusive-form" ? "exclusive-forms" :
              "committee/calls";
    const response = await api.get(`/api/${resourcePath}/${id}/registrations/analysis`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching ${resourceType} registration analysis:`, error);
    throw error.response?.data || new Error(`Failed to fetch ${resourceType} registration analysis.`);
  }
};

export const syncRegistrationSheet = async (resourceType, id) => {
  try {
    const resourcePath =
      resourceType === "event" ? "events" :
        resourceType === "club" ? "clubs" :
          resourceType === "program" ? "program" :
            resourceType === "exclusive-form" ? "exclusive-forms" :
              "committee/calls";
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
    const resourcePath =
      resourceType === "event" ? "events" :
        resourceType === "club" ? "clubs" :
          resourceType === "program" ? "program" :
            resourceType === "exclusive-form" ? "exclusive-forms" :
              "committee";
    const response = await api.get(`/api/${resourcePath}/${resourceId}/questions`);
    return response.data;
  } catch (error) {
    console.error(`Error fetching ${resourceType} questions:`, error);
    throw error.response?.data || new Error(`Failed to fetch ${resourceType} questions.`);
  }
};

export const createQuestion = async (resourceType, resourceId, questionData) => {
  try {
    const resourcePath =
      resourceType === "event" ? "events" :
        resourceType === "club" ? "clubs" :
          resourceType === "program" ? "program" :
            resourceType === "exclusive-form" ? "exclusive-forms" :
              "committee";
    const response = await api.post(`/api/${resourcePath}/${resourceId}/questions`, questionData);
    return response.data;
  } catch (error) {
    console.error(`Error creating ${resourceType} question:`, error);
    throw error.response?.data || new Error(`Failed to create ${resourceType} question.`);
  }
};

export const updateQuestion = async (resourceType, resourceId, questionId, questionData) => {
  try {
    const resourcePath =
      resourceType === "event" ? "events" :
        resourceType === "club" ? "clubs" :
          resourceType === "program" ? "program" :
            resourceType === "exclusive-form" ? "exclusive-forms" :
              "committee";
    const response = await api.put(`/api/${resourcePath}/${resourceId}/questions/${questionId}`, questionData);
    return response.data;
  } catch (error) {
    console.error(`Error updating ${resourceType} question:`, error);
    throw error.response?.data || new Error(`Failed to update ${resourceType} question.`);
  }
};

export const deleteQuestion = async (resourceType, resourceId, questionId) => {
  try {
    const resourcePath =
      resourceType === "event" ? "events" :
        resourceType === "club" ? "clubs" :
          resourceType === "program" ? "program" :
            resourceType === "exclusive-form" ? "exclusive-forms" :
              "committee";
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

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   10. ACM GALLERY
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
export const fetchGalleryImages = async () => {
  try {
    const response = await api.get("/api/gallery");
    return response.data;
  } catch (error) {
    console.error("Error fetching gallery images:", error);
    throw error.response?.data || new Error("Failed to fetch gallery images.");
  }
};

export const addGalleryImage = async (imageData) => {
  try {
    const response = await api.post("/api/gallery", imageData);
    return response.data;
  } catch (error) {
    console.error("Error adding gallery image:", error);
    throw error.response?.data || new Error("Failed to add gallery image.");
  }
};

export const updateGalleryImage = async (id, imageData) => {
  try {
    const response = await api.put(`/api/gallery/${id}`, imageData);
    return response.data;
  } catch (error) {
    console.error("Error updating gallery image:", error);
    throw error.response?.data || new Error("Failed to update gallery image.");
  }
};

export const deleteGalleryImage = async (id) => {
  try {
    await api.delete(`/api/gallery/${id}`);
  } catch (error) {
    console.error("Error deleting gallery image:", error);
    throw error.response?.data || new Error("Failed to delete gallery image.");
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

export const createSeason = async (seasonData) => {
  try {
    const response = await api.post("/api/radio/seasons", seasonData);
    return response.data;
  } catch (error) {
    console.error("Error creating season:", error);
    throw error.response?.data || new Error("Failed to create season.");
  }
};

export const updateSeason = async (id, seasonData) => {
  try {
    const response = await api.put(`/api/radio/seasons/${id}`, seasonData);
    return response.data;
  } catch (error) {
    console.error("Error updating season:", error);
    throw error.response?.data || new Error("Failed to update season.");
  }
};

export const deleteSeason = async (id) => {
  try {
    const response = await api.delete(`/api/radio/seasons/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error deleting season:", error);
    throw error.response?.data || new Error("Failed to delete season.");
  }
};

export const createEpisode = async (episodeData) => {
  try {
    const response = await api.post("/api/radio/episodes", episodeData);
    return response.data;
  } catch (error) {
    console.error("Error creating episode:", error);
    throw error.response?.data || new Error("Failed to create episode.");
  }
};

export const updateEpisode = async (id, episodeData) => {
  try {
    const response = await api.put(`/api/radio/episodes/${id}`, episodeData);
    return response.data;
  } catch (error) {
    console.error("Error updating episode:", error);
    throw error.response?.data || new Error("Failed to update episode.");
  }
};

export const deleteEpisode = async (id) => {
  try {
    const response = await api.delete(`/api/radio/episodes/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error deleting episode:", error);
    throw error.response?.data || new Error("Failed to delete episode.");
  }
};

export const fetchEpisodesBySeason = async (seasonId, page = 0) => {
  try {
    const response = await api.get(`/api/radio/seasons/${seasonId}/episodes/page/${page}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching paginated episodes:", error);
    throw error.response?.data || new Error("Failed to fetch episodes.");
  }
};

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   11. PARTNERS CRUD
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
export const fetchPartners = async () => {
  try {
    const response = await api.get("/api/partners");
    return response.data;
  } catch (error) {
    console.error("Error fetching partners:", error);
    throw error.response?.data || new Error("Failed to fetch partners.");
  }
};

export const createPartner = async (partnerData) => {
  try {
    const response = await api.post("/api/partners", partnerData);
    return response.data;
  } catch (error) {
    console.error("Error creating partner:", error);
    throw error.response?.data || new Error("Failed to create partner.");
  }
};

export const updatePartner = async (id, partnerData) => {
  try {
    const response = await api.put(`/api/partners/${id}`, partnerData);
    return response.data;
  } catch (error) {
    console.error("Error updating partner:", error);
    throw error.response?.data || new Error("Failed to update partner.");
  }
};

export const deletePartner = async (id) => {
  try {
    await api.delete(`/api/partners/${id}`);
  } catch (error) {
    console.error("Error deleting partner:", error);
    throw error.response?.data || new Error("Failed to delete partner.");
  }
};

export default {
  fetchInsights,
  searchUsers,
  previewMembersExport,
  exportMembersSheet,
  updateUserRole,
  assignUserAssociations,
  assignUser,
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
  announceCommitteeCall,
  changeCallMessage,
  createEvent,
  updateEvent,
  deleteEvent,
  openEventCall,
  closeEventCall,
  createClub,
  updateClub,
  deleteClub,
  openClubCall,
  closeClubCall,
  fetchClubSocialLinks,
  updateClubSocialLinks,
  createProgram,
  updateProgram,
  deleteProgram,
  toggleProgramRegistration,
  fetchExclusiveForms,
  createExclusiveForm,
  updateExclusiveForm,
  deleteExclusiveForm,
  sendAnnouncement,
  sendWithResendPrompt,
  fetchEmailSettings,
  fetchEmailLock,
  updateEmailLock,
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
  fetchGalleryImages,
  addGalleryImage,
  updateGalleryImage,
  deleteGalleryImage,
  uploadImage,
  createSeason,
  updateSeason,
  deleteSeason,
  createEpisode,
  updateEpisode,
  deleteEpisode,
  fetchEpisodesBySeason,
  fetchPartners,
  createPartner,
  updatePartner,
  deletePartner,
};
