// Readable addresses for event and program pages: /events/12-icpc-training-camp. The number finds the item; the
// words are there for people and search engines, so an old or mistyped name still opens the page and is then
// corrected.

const MAX_SLUG = 60;

// "ICPC Training Camp 2026!" -> "icpc-training-camp-2026". Words in other scripts (Arabic) are dropped, so a name
// with no Latin letters or digits gives "", and the address is just the number.
export const slugify = (name = "") =>
  name
    .normalize("NFKD")
    .replace(/[̀-ͯ]/g, "")
    .toLowerCase()
    .replace(/[^a-z0-9]+/g, "-")
    .replace(/^-+|-+$/g, "")
    .slice(0, MAX_SLUG)
    .replace(/-+$/g, "");

const withSlug = (id, name) => {
  const slug = slugify(name);
  return slug ? `${id}-${slug}` : `${id}`;
};

export const eventPath = (event) => `/events/${withSlug(event.id, event.name)}`;
export const programPath = (program) => `/programs/${withSlug(program.id, program.name)}`;

// The id at the start of an address part ("12-icpc-training-camp" -> 12), or null when there is none.
export const idFromSlug = (value = "") => {
  const match = /^(\d+)(?:-|$)/.exec(value);
  return match ? Number(match[1]) : null;
};
