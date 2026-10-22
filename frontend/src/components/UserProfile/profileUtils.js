const ROLE_LABELS = {
  SUPER_ADMIN: "System Administrator",
  ACM_HIGH_BOARD: "High Board",
  ACM_COMMITTEE_BOARD: "Committee Board",
  ACM_CLUB_BOARD: "Club Board",
  ACM_MEMBER: "ACM Member",
};

export const DEPARTMENT_LABELS = {
  CSED: "Computer & Systems (CSED)",
  CCE: "Computer & Communications (CCE)",
  OTHER: "Other",
};

export const getRoleLabel = (role) => ROLE_LABELS[role] || "User";

// Only board members and admins may show a profile photo.
export const canShowPhoto = (role) => role !== "USER" && role !== "ACM_MEMBER";
