// Keep live and archived payload differences out of the shared member cards.
export const normalizeBoards = (source) => {
  const members = (items) => (items ?? []).map((member) => ({
    id: member.id,
    userName: member.userName || member.user?.name || "Member",
    profileImageUrl: member.profileImageUrl || member.user?.profileImageUrl || null,
    linkedinUrl: member.linkedinUrl || member.user?.linkedinUrl || null,
    role: member.role,
    order: member.order ?? null,
  })).sort((a, b) => (a.order ?? Infinity) - (b.order ?? Infinity) || a.id - b.id);
  const groups = (items) => (items ?? []).map((group) => ({
    id: group.id,
    name: group.name,
    description: group.description,
    isExternal: group.isExternal,
    logoUrl: group.logoUrl || group.imageUrl || null,
    boardRoles: members(group.boardRoles),
  }));
  return {
    highBoard: members(source.highBoard),
    committees: groups(source.committees),
    clubs: groups(source.clubs),
  };
};
