import api from "./api";

export const fetchBoardSeasons = async () => (await api.get("/api/boards/seasons")).data;
export const fetchPastBoards = async (season) => (await api.get(`/api/boards/past/${season}`)).data;
