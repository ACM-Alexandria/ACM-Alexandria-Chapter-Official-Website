export const pastBoardResources = [
  { path: "high-board", label: "High board", member: true },
  { path: "committees", label: "Committees" },
  { path: "committee-board", label: "Committee board", member: true, parent: "committees", relation: "pastCommitteeId" },
  { path: "clubs", label: "Clubs" },
  { path: "club-board", label: "Club board", member: true, parent: "clubs", relation: "pastClubId" },
];
