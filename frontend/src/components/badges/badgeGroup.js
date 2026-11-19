// Where a badge was won, as shown on cards, pages and share text: a committee's members ("Technical"), or the
// board of a committee or a club ("Technical Board", "Chess Club Board"). Older awards only have committeeName.
export const badgeGroupLabel = (badge) => {
  const name = badge?.groupName || badge?.committeeName || badge?.clubName || "";
  return badge?.track === "BOARD" && name ? `${name} Board` : name;
};
