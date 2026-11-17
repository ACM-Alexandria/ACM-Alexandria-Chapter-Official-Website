import api from "./api";

// Badges as members and visitors see them. A badge is
// { id, publicId, name, description, icon, periodType, periodLabel, periodStart, periodEnd,
//   committeeName, rank, note, awardedAt, member: { id, name, profileImageUrl, committeeName }, isNew? }.

// The signed-in member's badges, newest first; `isNew` until they open their badges.
export const fetchMyBadges = async () => (await api.get("/api/badges/me")).data;
// Clears the "new" marker on all of the signed-in member's badges.
export const markMyBadgesSeen = async () => api.post("/api/badges/me/seen");

// A member's badges { member, badges } for the admin and HR member view.
export const fetchMemberBadges = async (userId) => (await api.get(`/api/badges/members/${userId}`)).data;

// Public, no sign-in: one badge, for the page shared links open.
export const fetchPublicBadge = async (publicId) => (await api.get(`/api/public/badges/${publicId}`)).data;
