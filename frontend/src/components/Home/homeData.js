import {
  fetchAllClubs,
  fetchCommittee,
  fetchEvents,
  fetchGalleryImages,
  fetchHighBoard,
  fetchPrograms,
  fetchSeasons,
  fetchSocialLinks,
  fetchStats,
} from "../../services/homePageService";
import { fetchBoardSeasons } from "../../services/pastBoardsService";

// Turns the API responses into the shapes the home page components use, and loads them all at once.
// One failing call only empties its own part of the page.
const items = (data) => (Array.isArray(data) ? data : data?.content ?? []);
const byOrder = (a, b) => (a.order ?? 99) - (b.order ?? 99);

export const toMember = (record) => ({
  name: record.userName || record.user?.name || "",
  role: record.role,
  order: record.order,
  image: record.profileImageUrl || record.user?.profileImageUrl || "",
  linkedin: record.linkedinUrl || record.user?.linkedinUrl || "",
});

export const emptyHomeData = {
  committees: [],
  clubs: [],
  highBoard: [],
  events: [],
  programs: [],
  radio: [],
  gallery: [],
  social: [],
  currentSeason: null,
  stats: null,
  counts: { events: 0, programs: 0, radio: 0 },
};

// The season's newest episode (highest number), for the radio section's player.
const latestEpisode = (episodes = []) => {
  const [episode] = [...episodes].sort((a, b) => (b.episodeNumber ?? 0) - (a.episodeNumber ?? 0));
  return episode ? { number: episode.episodeNumber, title: episode.title, guest: episode.guest, url: episode.url } : null;
};

export const toHomeData = ({ committees, clubs, highBoard, events, programs, seasons, gallery, social, boardSeasons, stats }) => ({
  committees: items(committees).map((c) => ({
    id: c.id,
    name: (c.name || "").trim(),
    description: c.description,
    logoUrl: c.logoUrl,
    open: Boolean(c.open ?? c.isOpen),
    board: (c.boardRoles || []).map(toMember).sort(byOrder),
  })),
  clubs: items(clubs).map((c) => ({
    id: c.id,
    name: (c.name || "").trim(),
    description: c.description,
    imageUrl: c.imageUrl,
    open: Boolean(c.registrationOpen),
    board: (c.boardRoles || []).map(toMember).sort(byOrder),
  })),
  highBoard: items(highBoard).map(toMember).sort(byOrder),
  events: items(events)
    .map((e) => ({ id: e.id, name: (e.name || "").trim(), description: e.description, imageUrl: e.imageUrl, time: e.eventTime, location: e.location, open: Boolean(e.registrationOpen) }))
    .sort((a, b) => new Date(b.time) - new Date(a.time)),
  programs: items(programs).map((p) => ({
    id: p.id,
    name: p.name,
    description: p.description,
    imageUrl: p.imageUrl,
    startDate: p.startDate,
    endDate: p.endDate,
    time: p.time,
    open: Boolean(p.registrationOpen),
  })),
  radio: items(seasons).map((r) => ({ id: r.id, season: r.seasonNumber, imageUrl: r.imageUrl, episodes: (r.episodes || []).length, latest: latestEpisode(r.episodes) })),
  gallery: items(gallery).map((g) => g.imageUrl).filter(Boolean),
  social: items(social).map((s) => ({ platform: s.platform, url: s.url })),
  currentSeason: boardSeasons?.currentSeason ?? null,
  stats: stats && typeof stats.users === "number" ? stats : null,
  counts: {
    events: events?.totalElements ?? items(events).length,
    programs: programs?.totalElements ?? items(programs).length,
    radio: seasons?.totalElements ?? items(seasons).length,
  },
});

export const fetchHomeData = async () => {
  const calls = {
    committees: fetchCommittee,
    clubs: fetchAllClubs,
    highBoard: fetchHighBoard,
    events: () => fetchEvents(0),
    programs: () => fetchPrograms(0),
    seasons: () => fetchSeasons(0),
    gallery: fetchGalleryImages,
    social: fetchSocialLinks,
    boardSeasons: fetchBoardSeasons,
    stats: fetchStats,
  };
  const keys = Object.keys(calls);
  const results = await Promise.allSettled(keys.map((key) => calls[key]()));
  const raw = Object.fromEntries(keys.map((key, i) => [key, results[i].status === "fulfilled" ? results[i].value : null]));
  return toHomeData(raw);
};
