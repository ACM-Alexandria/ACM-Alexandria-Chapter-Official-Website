// Text and date formatting used across the site.
export const shortName = (name = "") =>
  name.replace(/\s*Committee\s*$/i, "").replace("Training & Development", "T&D").trim();

export const initials = (name = "") =>
  name.trim().split(/\s+/).slice(0, 2).map((word) => word[0]).join("").toUpperCase();

export const eventDate = (iso) => {
  const date = new Date(iso);
  return {
    day: date.toLocaleDateString("en-GB", { day: "2-digit" }),
    month: date.toLocaleDateString("en-GB", { month: "short" }),
    full: date.toLocaleDateString("en-GB", { weekday: "short", day: "numeric", month: "short", year: "numeric" }),
  };
};

export const summary = (text = "", length = 150) => {
  const flat = text.replace(/\s+/g, " ").trim();
  return flat.length > length ? `${flat.slice(0, length).replace(/\s\S*$/, "")}…` : flat;
};
