import { FiUsers, FiLayers, FiCalendar, FiAward, FiBookOpen, FiFileText } from "react-icons/fi";

// Every resource the admin and HR dashboards manage, under one name. The tab id is the name
// the sidebar and the list loader use; the type is the name the registration, question and
// email calls use. Both dashboards pass these values, so they reach the same endpoints.
export const RESOURCE_TYPES = {
  committee: { tab: "committees", label: "Committee", apiPath: "/api/committee", emails: false },
  event: { tab: "events", label: "Event", apiPath: "/api/events", emails: true },
  club: { tab: "clubs", label: "Club", apiPath: "/api/clubs", emails: true },
  program: { tab: "programs", label: "Program", apiPath: "/api/program", emails: true },
  "exclusive-form": { tab: "exclusiveForms", label: "Exclusive form", apiPath: "/api/exclusive-forms", emails: false },
};

// Resource type behind a sidebar tab, or null for tabs that are not resources (users, radio...)
export const resourceTypeForTab = (tab) =>
  Object.keys(RESOURCE_TYPES).find((type) => RESOURCE_TYPES[type].tab === tab) ?? null;

// Sidebar tabs both dashboards show in Resource Management
export const SHARED_RESOURCE_TABS = [
  { id: "users", label: "Users", icon: FiUsers },
  { id: "committees", label: "Committees", icon: FiLayers },
  { id: "events", label: "Events", icon: FiCalendar },
  { id: "clubs", label: "Clubs", icon: FiAward },
  { id: "programs", label: "Programs", icon: FiBookOpen },
  { id: "exclusiveForms", label: "Exclusive Forms", icon: FiFileText },
];
