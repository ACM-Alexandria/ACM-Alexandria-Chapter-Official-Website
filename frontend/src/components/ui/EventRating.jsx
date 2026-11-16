import { useEffect, useState } from "react";
import { FiStar } from "react-icons/fi";
import { getRatingSummary } from "../../services/EventReviewService";
import { muted } from "./uiStyles";

// A past event's average rating on its card ("★ 4.6 (12)"). Shows nothing for upcoming events or before anyone
// has rated it.
const EventRating = ({ eventId, past, className = "" }) => {
  const [summary, setSummary] = useState(null);

  useEffect(() => {
    if (!past || !eventId) return undefined;
    let cancelled = false;
    getRatingSummary(eventId)
      .then((s) => { if (!cancelled) setSummary(s); })
      .catch(() => { if (!cancelled) setSummary(null); });
    return () => { cancelled = true; };
  }, [past, eventId]);

  const count = summary?.reviewCount ?? 0;
  if (!past || count === 0) return null;
  return (
    <span className={`inline-flex shrink-0 items-center gap-1 text-sm font-semibold text-slate-700 dark:text-ocean-222 ${className}`}>
      <FiStar className="h-3.5 w-3.5 fill-current text-amber-400" aria-hidden="true" />
      {Number(summary.averageRating ?? 0).toFixed(1)}
      <span className={`font-medium ${muted}`}>({count}<span className="sr-only"> {count === 1 ? "review" : "reviews"}</span>)</span>
    </span>
  );
};

export default EventRating;
