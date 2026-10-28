import { EXPORT_PRESETS, ROLE_OPTIONS } from "../../../constants/roles";

// Which roles are scoped to a committee or a club, the default role selection, and the filter payload sent
// for member exports and targeted emails.
export const COMMITTEE_SCOPED_ROLES = ROLE_OPTIONS
    .filter((role) => role.scope === "committee")
    .map((role) => role.value);
export const CLUB_SCOPED_ROLES = ROLE_OPTIONS
    .filter((role) => role.scope === "club")
    .map((role) => role.value);
export const DEFAULT_MEMBERS_ROLES = [...EXPORT_PRESETS[0].roles];

export const buildMembersFilters = (roles, committeeIds, clubIds) => ({
    roles,
    committeeIds: roles.some((role) => COMMITTEE_SCOPED_ROLES.includes(role)) ? committeeIds : [],
    clubIds: roles.some((role) => CLUB_SCOPED_ROLES.includes(role)) ? clubIds : [],
});
