import api from "./api";

// Badge management (every admin-page role: super admin, high board, committee and club boards). Errors come back with `response.data.message`.
const base = "/api/admin/badges";

// Periods (months, quarters, seasons) come from seasonsAdminService.

// The fixed badge types (Monthly MVP, Quarter MVP, Season Champion); they can't be edited.
export const fetchBadgeTypes = async () => (await api.get(`${base}/types`)).data;

// One committee for the Award screen: { winners, awarded: [award], members: [{ memberId, name, points, rank,
// suggested, tied }] } — members ranked by points in the period. winners: per committee (null = the default).
// group: { track: "MEMBER" | "BOARD", committeeId } or, for a club's board, { track: "BOARD", clubId }.
export const fetchCommitteeRanking = async (badgeTypeId, periodId, group, winners) =>
  (await api.post(`${base}/committee-ranking`, { badgeTypeId, periodId, ...group, winners })).data;

// Every badge awarded for a period.
export const fetchAwards = async (periodId) => (await api.get(`${base}/awards`, { params: { periodId } })).data;
// recipients: [{ memberId, rank? }], all from `group` (as for the ranking). `winners` is the most people of the
// group that can hold the badge for the period; the backend refuses awards past it.
export const awardBadges = async (badgeTypeId, periodId, recipients, winners, group) =>
  (await api.post(`${base}/awards`, { badgeTypeId, periodId, recipients, winners, ...group })).data;
export const revokeAward = async (id) => api.delete(`${base}/awards/${id}`);
// The member's description on an awarded badge; blank removes it.
export const updateAwardNote = async (id, note) => (await api.patch(`${base}/awards/${id}`, { note })).data;

export { messageOf } from "../utils/apiError";
