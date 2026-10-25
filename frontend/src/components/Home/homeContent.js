// Wording and figures shown on the home page.
export const tagline = "Discover the world of technology, innovation, and community";
export const contactEmail = "alexandria.acm.chapter@gmail.com";

// The closing call to action of the lighthouse scene: join for visitors, the profile for signed-in members.
export const joinCta = { to: "/register", label: "Join ACM Alexandria", note: "Create an account, then apply when a committee call opens." };
export const profileCta = { to: "/profile", label: "Go to your profile", note: "See your committees, points, and registrations." };

// The hero's three totals from /api/stats, each rounded down to the nearest ten and shown with a "+"
// (exact below ten): every user account, ACM members (members and the three boards), and event plus
// program registrations.
export const heroStats = (stats) => {
  if (!stats) return [];
  const round = (n) => (n >= 10 ? { value: Math.floor(n / 10) * 10, suffix: "+" } : { value: n, suffix: "" });
  return [
    { key: "users", label: "Users", ...round(stats.users) },
    { key: "members", label: "ACM members", ...round(stats.members) },
    { key: "registrations", label: "Registrations", ...round(stats.eventRegistrations + stats.programRegistrations) },
  ];
};
