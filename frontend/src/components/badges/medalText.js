// The two lines printed on a badge medal, taken from its period label: "Nov 2026" → NOV / 2026,
// "Q2 · 2027" → Q2 / 2027, "Season 2027" → 2027 / SEASON. Empty when there is no label.
export const medalText = (periodType, periodLabel = "") => {
  const label = String(periodLabel || "").trim();
  if (!label) return { top: "", bottom: "" };
  if (periodType === "SEASON") {
    return { top: label.replace(/^season\s*/i, ""), bottom: "SEASON" };
  }
  const [first = "", second = ""] = label.split(/[\s·]+/).filter(Boolean);
  return { top: first.toUpperCase(), bottom: second };
};
