// Single source of truth for user roles in the admin UI.
// `scope` marks which association filter narrows the role ("committee", "club" or null),
// `group` is used to cluster roles in selection UIs. Adding a new backend role (e.g. EX_MEMBER)
// only needs a new entry here.
export const ROLE_OPTIONS = [
  { value: "SUPER_ADMIN", label: "Super Admin", scope: null, group: "other" },
  { value: "ACM_HIGH_BOARD", label: "High Board", scope: null, group: "leadership" },
  { value: "ACM_COMMITTEE_BOARD", label: "Committee Board", scope: "committee", group: "leadership" },
  { value: "ACM_CLUB_BOARD", label: "Club Board", scope: "club", group: "leadership" },
  { value: "ACM_MEMBER", label: "ACM Member", scope: "committee", group: "members" },
  { value: "USER", label: "Standard User", scope: null, group: "other" },
];

export const ROLE_GROUPS = [
  { key: "leadership", label: "Leadership" },
  { key: "members", label: "Members" },
  { key: "other", label: "Other Accounts" },
];

export const ROLE_LABELS = Object.fromEntries(ROLE_OPTIONS.map((r) => [r.value, r.label]));

export const EXPORT_PRESETS = [
  { key: "all", label: "All Members + Boards", roles: ["ACM_HIGH_BOARD", "ACM_COMMITTEE_BOARD", "ACM_CLUB_BOARD", "ACM_MEMBER"] },
  { key: "committees", label: "Members + Committee Heads", roles: ["ACM_COMMITTEE_BOARD", "ACM_MEMBER"] },
  { key: "members", label: "Members Only", roles: ["ACM_MEMBER"] },
  { key: "boards", label: "All Boards", roles: ["ACM_HIGH_BOARD", "ACM_COMMITTEE_BOARD", "ACM_CLUB_BOARD"] },
  { key: "highBoard", label: "High Board Only", roles: ["ACM_HIGH_BOARD"] },
];
