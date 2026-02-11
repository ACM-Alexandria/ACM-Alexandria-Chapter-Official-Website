import api from "./api";

// Fetch clubs
export const fetchClubs = async () => {
  try {
    const response = await api.get("/clubs");
    return response.data;
  } catch (error) {
    console.error("Error fetching clubs:", error);
    throw error;
  }
};

// Fetch committee members
export const fetchCommittee = async () => {
  try {
    const response = await api.get("/committee");
    return response.data;
  } catch (error) {
    console.error("Error fetching committee:", error);
    throw error;
  }
};

// Fetch events
export const fetchEvents = async () => {
  try {
    const response = await api.get("/events");
    return response.data;
  } catch (error) {
    console.error("Error fetching events:", error);
    throw error;
  }
};

// Fetch programs
export const fetchPrograms = async () => {
  try {
    const response = await api.get("/program");
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
    fetchEvents().catch(() => []),
    fetchPrograms().catch(() => []),
  ]);

  return {
    clubs: results[0].status === "fulfilled" ? results[0].value : [],
    committee: results[1].status === "fulfilled" ? results[1].value : [],
    events: results[2].status === "fulfilled" ? results[2].value : [],
    programs: results[3].status === "fulfilled" ? results[3].value : [],
  };
};
