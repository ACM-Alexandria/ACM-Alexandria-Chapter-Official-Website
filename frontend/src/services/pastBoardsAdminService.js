import api from "./api";

const base = "/api/admin/boards";

export const fetchPastSeasons = async () => (await api.get(`${base}/past/seasons`)).data;
export const fetchCurrentBoardSeason = async () => (await api.get(`${base}/current-season`)).data.currentSeason;
// The backend rejects a season that already has past records (409) and returns the reason as a message.
export const addPastSeason = async (season) =>
  (await api.post(`${base}/past/seasons`, { season })).data;
export const fetchPastResource = async (resource, season) =>
  (await api.get(`${base}/past/${resource}`, { params: { season } })).data;
export const createPastResource = async (resource, data) =>
  (await api.post(`${base}/past/${resource}`, data)).data;
export const updatePastResource = async (resource, id, data) =>
  (await api.put(`${base}/past/${resource}/${id}`, data)).data;
// Moves every record of a season to a new year; the backend rejects a year that already exists.
export const updatePastSeason = async (season, newSeason) => (await api.put(`${base}/past/seasons/${season}`, { season: newSeason })).data;
// Removes the season and every board record and member that belongs to it.
export const deletePastSeason = async (season) => api.delete(`${base}/past/seasons/${season}`);
export const deletePastResource = async (resource, id) =>
  api.delete(`${base}/past/${resource}/${id}`);

// Copying a full board can take longer than the default API timeout.
export const archiveCurrentSeason = async (season) =>
  (await api.post(`${base}/archive`, { season }, { timeout: 60000 })).data;
