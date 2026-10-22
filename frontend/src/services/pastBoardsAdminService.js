import api from "./api";

const base = "/api/admin/boards";

export const fetchPastSeasons = async () => (await api.get(`${base}/past/seasons`)).data;
export const fetchCurrentBoardSeason = async () => (await api.get(`${base}/current-season`)).data.currentSeason;
export const fetchPastResource = async (resource, season) =>
  (await api.get(`${base}/past/${resource}`, { params: { season } })).data;
export const createPastResource = async (resource, data) =>
  (await api.post(`${base}/past/${resource}`, data)).data;
export const updatePastResource = async (resource, id, data) =>
  (await api.put(`${base}/past/${resource}/${id}`, data)).data;
export const deletePastResource = async (resource, id) =>
  api.delete(`${base}/past/${resource}/${id}`);

// Copying a full board can take longer than the default API timeout.
export const archiveCurrentSeason = async (season) =>
  (await api.post(`${base}/archive`, { season }, { timeout: 60000 })).data;
