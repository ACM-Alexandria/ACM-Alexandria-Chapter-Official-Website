// Date helpers for seasons and quarters. Dates are ISO strings ("2026-09-01"), read as local dates.
// An ongoing season or quarter has no end date (endDate is null).

export const todayIso = () => {
  const now = new Date();
  return `${now.getFullYear()}-${String(now.getMonth() + 1).padStart(2, "0")}-${String(now.getDate()).padStart(2, "0")}`;
};

// "2026-09-01" -> "1 Sep 2026"
export const formatDate = (iso) => {
  if (!iso) return "";
  const [y, m, d] = iso.split("-").map(Number);
  return new Date(y, m - 1, d).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" });
};

// "1 Sep 2026 – 15 Nov 2026", or "1 Sep 2026 – ongoing"
export const formatRange = (start, end) => `${formatDate(start)} – ${end ? formatDate(end) : "ongoing"}`;

// Whole weeks between two inclusive ISO dates; for an ongoing period, the weeks so far.
export const weeksBetween = (start, end) => {
  const last = end || todayIso();
  const days = (Date.parse(last) - Date.parse(start)) / 86400000 + 1;
  const weeks = Math.max(0, Math.round(days / 7));
  const text = weeks === 1 ? "1 week" : `${weeks} weeks`;
  return end ? text : `${text} so far`;
};

// Seasons are named by their year, as on the Past boards page.
export const seasonLabel = (season) => `${season}`;

export const isCurrentPeriod = (p, today = todayIso()) => p.startDate <= today && (!p.endDate || today <= p.endDate);
