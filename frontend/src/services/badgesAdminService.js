import api from "./api";

// Badge management (every admin-page role: super admin, high board, committee and club boards). Errors come back with `response.data.message`.
const base = "/api/admin/badges";

// Periods (months, quarters, seasons) come from seasonsAdminService.

// The fixed badge types (Monthly MVP, Quarter MVP, Season Champion); they can't be edited.
export const fetchBadgeTypes = async () => (await api.get(`${base}/types`)).data;

// One committee for the Award screen: { winners, awarded: [award], members: [{ memberId, name, points, rank,
// suggested, tied }] } — members ranked by points in the period. winners: per committee (null = the default).
export const fetchCommitteeRanking = async (badgeTypeId, periodId, committeeId, winners) =>
  (await api.post(`${base}/committee-ranking`, { badgeTypeId, periodId, committeeId, winners })).data;

// Every badge awarded for a period.
export const fetchAwards = async (periodId) => (await api.get(`${base}/awards`, { params: { periodId } })).data;
// recipients: [{ memberId, rank? }]; each badge goes to the member's own committee.
export const awardBadges = async (badgeTypeId, periodId, recipients, note) =>
  (await api.post(`${base}/awards`, { badgeTypeId, periodId, recipients, note })).data;
export const revokeAward = async (id) => api.delete(`${base}/awards/${id}`);

export { messageOf } from "../utils/apiError";
