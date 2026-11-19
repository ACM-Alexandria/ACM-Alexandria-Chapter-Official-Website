import api from "./api";

// Season settings: the chapter's seasons and quarters. Every admin role can read them;
// only the super admin can change them. Archiving the season lives in pastBoardsAdminService.
const base = "/api/admin/seasons";

// { season: 2026, label: "Season 2026/27", period: { id, startDate, endDate, ... } | null }
export const fetchCurrentSeason = async () => (await api.get(`${base}/current`)).data;
// Seasons and quarters, newest first. Months aren't listed; use resolveMonth.
export const fetchPeriods = async () => (await api.get(`${base}/periods`)).data;
// payload: { type: "SEASON" | "QUARTER", name?, startDate, endDate, season }
export const createPeriod = async (payload) => (await api.post(`${base}/periods`, payload)).data;
export const updatePeriod = async (id, payload) => (await api.put(`${base}/periods/${id}`, payload)).data;
export const deletePeriod = async (id) => api.delete(`${base}/periods/${id}`);
// "2026-11" -> the period for that calendar month, created on first use.
export const resolveMonth = async (month) => (await api.post(`${base}/months`, { month })).data;
