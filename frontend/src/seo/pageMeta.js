// Search and share metadata for each page: the tab title, the description search results show, and whether search
// engines may index the page. index.html carries the home page's values for crawlers that don't run JavaScript;
// usePageMeta swaps them as the visitor moves between pages.

export const siteName = "ACM Alexandria Student Chapter";
export const siteUrl = "https://alex.hosting.acm.org";
// The share preview image (1280×720) for pages without one of their own.
export const defaultImage = `${siteUrl}/video/welcome-poster.jpg`;
export const defaultDescription =
  "The ACM student chapter at Alexandria University, where students learn, build and grow together through committees, clubs, workshops, competitions and training programs.";

// The chapter in an event's or a program's structured data (index.html describes it in full under this @id).
export const organizationRef = { "@type": "Organization", "@id": `${siteUrl}/#organization`, name: siteName, url: `${siteUrl}/` };

// The place in structured data when an event or program names none. The address is only the city, since events
// also happen at outside venues.
export const venue = {
  name: "Faculty of Engineering, Alexandria University",
  address: { "@type": "PostalAddress", addressLocality: "Alexandria", addressCountry: "EG" },
};

// Pages for members, admins and one-off links (sign in, password reset, email confirmation) are kept out of
// search results with `noindex`.
export const pageMeta = {
  home: {
    path: "/",
    title: `${siteName} | Alexandria University`,
    description: defaultDescription,
  },
  events: {
    path: "/events",
    title: "Events: workshops, talks and competitions",
    description: "Workshops, tech talks, hackathons and competitions run by the ACM Alexandria Student Chapter, open to every student. See what's coming up and register.",
  },
  programs: {
    path: "/programs",
    title: "Training programs",
    description: "Weekly training tracks from the ACM Alexandria Student Chapter that build real skills in software, problem solving and more. Browse the programs and apply.",
  },
  radio: {
    path: "/radio",
    title: "ACM Radio podcast",
    description: "ACM Radio, the chapter's podcast: conversations with members, guests and alumni about tech, careers and student life at Alexandria University.",
  },
  season: {
    title: "ACM Radio season",
    description: "Every episode of this ACM Radio season, with its hosts and guests.",
  },
  boards: {
    path: "/boards",
    title: "Boards: the people who lead the chapter",
    description: "The high board, committee boards and club boards of the ACM Alexandria Student Chapter, this season and in past seasons.",
  },
  rules: {
    path: "/rules",
    title: "Chapter rules",
    description: "How members of the ACM Alexandria Student Chapter work together: the rules for events, committees, points and conduct.",
  },
  faq: {
    path: "/faq",
    title: "Frequently asked questions",
    description: "Answers to common questions about joining the ACM Alexandria Student Chapter, registering for events and programs, committees and points.",
  },
  // A member's badge page is meant for the links they share (LinkedIn reads the backend's share page), not for
  // search results that list students by name.
  badge: {
    title: "Chapter badge",
    description: "A badge awarded by the ACM Alexandria Student Chapter.",
    noindex: true,
  },
  login: { path: "/login", title: "Sign in", noindex: true },
  register: {
    path: "/register",
    title: "Join the chapter",
    description: "Create your ACM Alexandria Student Chapter account to register for events, programs and committees.",
  },
  forgotPassword: { title: "Forgot password", noindex: true },
  resetPassword: { title: "Reset password", noindex: true },
  emailConfirmation: { title: "Confirm your email", noindex: true },
  profile: { title: "Your profile", noindex: true },
  admin: { title: "Admin", noindex: true },
  adminUserProfile: { title: "Member profile", noindex: true },
  hrDashboard: { title: "HR dashboard", noindex: true },
};
