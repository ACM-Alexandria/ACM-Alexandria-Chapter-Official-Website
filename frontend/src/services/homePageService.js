import api from "./api";

// Fetch clubs
export const fetchClubs = async () => {
  try {
    const response = await api.get("/api/clubs");
    return response.data;
  } catch (error) {
    console.error("Error fetching clubs:", error);
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

// Fetch programs
export const fetchPrograms = async () => {
  try {
    const response = await api.get("/api/program");
    return response.data;
  } catch (error) {
    console.error("Error fetching programs:", error);
    throw error;
  }
};

// Fetch all home page data on load (independent fetch - one failure won't affect others)
export const fetchHomePageData = async () => {
  const results = await Promise.allSettled([
    fetchClubs().catch(() => []),
    fetchCommittee().catch(() => []),
    fetchHighBoard().catch(() => []),
    fetchEvents(0).catch(() => ({ content: [] })),
    fetchPrograms().catch(() => []),
  ]);

  return {
    clubs: results[0].status === "fulfilled" ? results[0].value : [],
    committee: results[1].status === "fulfilled" ? results[1].value : [],
    highBoard: results[2].status === "fulfilled" ? results[2].value : [],
    events: results[3].status === "fulfilled" && Array.isArray(results[3].value?.content) ? results[3].value.content : [],
    programs: results[4].status === "fulfilled" ? results[4].value : [],
  };
};
