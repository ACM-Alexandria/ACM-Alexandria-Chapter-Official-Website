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

// Fetch all home page data on load
export const fetchHomePageData = async () => {
  try {
    const [clubsData, committeeData, eventsData] = await Promise.all([
      fetchClubs(),
      fetchCommittee(),
      fetchEvents(),
    ]);

    return {
      clubs: clubsData,
      committee: committeeData,
      events: eventsData,
    };
  } catch (error) {
    console.error("Error fetching home page data:", error);
    throw error;
  }
};
