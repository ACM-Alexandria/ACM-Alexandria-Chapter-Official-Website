import api from "./api";

// Fetch clubs with pagination
export const fetchClubs = async (page = 0) => {
  try {
    const response = await api.get(`/api/clubs?page=${page}`);
    return response.data; // Page: { content, totalPages, ... }
  } catch (error) {
    console.error("Error fetching clubs:", error);
    throw error;
  }
};

// Fetch single club by id
export const fetchClubById = async (id) => {
  try {
    const response = await api.get(`/api/clubs/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching club details:", error);
    throw error;
  }
};

// Fetch committee members
export const fetchCommittee = async () => {
  try {
    const response = await api.get("/api/committee");
    return response.data;
  } catch (error) {
    console.error("Error fetching committee:", error);
    throw error;
  }
};

// Fetch high board members
export const fetchHighBoard = async () => {
  try {
    const response = await api.get("/api/highboard");
    return response.data;
  } catch (error) {
    console.error("Error fetching high board:", error);
    throw error;
  }
};

// Fetch events
export const fetchEvents = async (page = 0) => {
  try {
    const response = await api.get(`/api/events?page=${page}`);
    return response.data; // Spring Page: { content, totalPages, number, totalElements, ... }
  } catch (error) {
    console.error("Error fetching events by page:", error);
    throw error;
  }
};

// Fetch single event by id (full event payload)
export const fetchEventById = async (id) => {
  try {
    const response = await api.get(`/api/events/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching event details:", error);
    throw error;
  }
};

// Fetch programs with pagination
export const fetchPrograms = async (page = 0) => {
  try {
    const response = await api.get(`/api/program?page=${page}`);
    return response.data; // Spring Page: { content, totalPages, number, ... }
  } catch (error) {
    console.error("Error fetching programs:", error);
    throw error;
  }
};

// Fetch single program by id
export const fetchProgramById = async (id) => {
  try {
    const response = await api.get(`/api/program/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching program details:", error);
    throw error;
  }
};

// Fetch all home page data on load
export const fetchHomePageData = async () => {
  const results = await Promise.allSettled([
    fetchClubs(0).catch(() => ({ content: [] })),
    fetchCommittee().catch(() => []),
    fetchHighBoard().catch(() => []),
    fetchEvents(0).catch(() => ({ content: [] })),
    fetchPrograms(0).catch(() => ({ content: [] })),
    fetchSeasons().catch(() => []),
  ]);

  const extractContent = (result) => {
    if (result.status !== "fulfilled") return [];
    const val = result.value;
    if (Array.isArray(val)) return val;
    if (val && Array.isArray(val.content)) return val.content;
    return [];
  };

  return {
    clubs: extractContent(results[0]),
    committee: results[1].status === "fulfilled" ? results[1].value : [],
    highBoard: results[2].status === "fulfilled" ? results[2].value : [],
    events: extractContent(results[3]),
    programs: extractContent(results[4]),
    seasons: extractContent(results[5]),
  };
};

// Fetch social links
export const fetchSocialLinks = async () => {
  try {
    const response = await api.get("/api/socialLinks");
    return response.data;
  } catch (error) {
    console.error("Error fetching social links:", error);
    throw error;
  }
};

// Subscribe to committee
export const subscribeToCommittee = async (id) => {
  const response = await api.post(`/api/committee/${id}/subscribe`);
  return response.data;
};

// Unsubscribe from committee
export const unsubscribeFromCommittee = async (id) => {
  const response = await api.post(`/api/committee/${id}/unsubscribe`);
  return response.data;
};

// Get committee subscription status
export const fetchCommitteeSubscriptionStatus = async (id) => {
  const response = await api.get(`/api/committee/${id}/subscription-status`);
  return response.data; // { subscribed: boolean }
};

// Subscribe to news
export const subscribeToNews = async () => {
  const response = await api.post("/api/subscriptions/news/subscribe");
  return response.data;
};

// Unsubscribe from news
export const unsubscribeFromNews = async () => {
  const response = await api.post("/api/subscriptions/news/unsubscribe");
  return response.data;
};

// Get news subscription status
export const fetchNewsSubscriptionStatus = async () => {
  const response = await api.get("/api/subscriptions/news/subscription-status");
  return response.data; // { subscribed: boolean }
};

// Fetch public gallery images
export const fetchGalleryImages = async () => {
  try {
    const response = await api.get("/api/gallery");
    return response.data;
  } catch (error) {
    console.error("Error fetching gallery images:", error);
    throw error;
  }
};

// Fetch seasons with pagination
export const fetchSeasons = async (page = 0) => {
  try {
    const response = await api.get(`/api/radio/seasons?page=${page}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching seasons:", error);
    throw error;
  }
};

// Fetch single season details
export const fetchSeasonById = async (id) => {
  try {
    const response = await api.get(`/api/radio/seasons/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching season details:", error);
    throw error;
  }
};

// Fetch active exclusive forms
export const fetchActiveExclusiveForms = async () => {
  try {
    const response = await api.get('/api/exclusive-forms/active');
    return response.data;
  } catch (error) {
    console.error("Error fetching active exclusive forms:", error);
    throw error;
  }
};

// Fetch exclusive form by id
export const fetchExclusiveFormById = async (id) => {
  try {
    const response = await api.get(`/api/exclusive-forms/${id}`);
    return response.data;
  } catch (error) {
    console.error("Error fetching exclusive form details:", error);
    throw error;
  }
};
