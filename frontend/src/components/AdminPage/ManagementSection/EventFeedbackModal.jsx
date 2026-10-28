import { useEffect, useState } from "react";
import { FiEye, FiEyeOff, FiStar } from "react-icons/fi";
import {
  getAdminReviews,
  toggleReviewVisibility,
} from "../../../services/EventReviewService";
import ModalShell from "../SharedComponents/ModalShell";
import { secondaryButtonClass } from "../SharedComponents/adminStyles";

const Stars = ({ value }) => (
  <div className="flex items-center gap-0.5" aria-label={`${value} out of 5 stars`}>
    {[1, 2, 3, 4, 5].map((star) => (
      <FiStar
        key={star}
        className={`h-4 w-4 ${
          star <= Math.round(value)
            ? "fill-current text-amber-400"
            : "text-slate-300 dark:text-slate-600"
        }`}
      />
    ))}
  </div>
);

const EventFeedbackModal = ({ open, onClose, event }) => {
  const [reviews, setReviews] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [togglingId, setTogglingId] = useState(null);

  useEffect(() => {
    if (!open || !event?.id) return;

    let cancelled = false;
    const load = async () => {
      setLoading(true);
      setError("");
      try {
        const data = await getAdminReviews(event.id);
        if (!cancelled) setReviews(Array.isArray(data) ? data : []);
      } catch (err) {
        if (!cancelled) setError(err.message || "Could not load event feedback.");
      } finally {
        if (!cancelled) setLoading(false);
      }
    };

    load();
    return () => {
      cancelled = true;
    };
  }, [open, event?.id]);

  const handleToggleVisibility = async (reviewId) => {
    setTogglingId(reviewId);
    setError("");
    try {
      const result = await toggleReviewVisibility(reviewId);
      setReviews((current) =>
        current.map((review) =>
          review.reviewId === reviewId ? { ...review, status: result.status } : review,
        ),
      );
    } catch (err) {
      setError(err.message || "Could not change comment visibility.");
    } finally {
      setTogglingId(null);
    }
  };

  return (
    <ModalShell
      open={open}
      onClose={onClose}
      title={`${event?.name || "Event"} feedback`}
      size="lg"
      footer={
        <button type="button" onClick={onClose} className={secondaryButtonClass}>
          Close
        </button>
      }
    >
      <div className="space-y-4">
        <p className="text-sm text-slate-600 dark:text-slate-400">
          Review comments are anonymous. Use the visibility control to publish or hide a comment.
        </p>

        {error && (
          <p role="alert" className="rounded-lg border border-rose-200 bg-rose-50 p-3 text-sm text-rose-700 dark:border-rose-900 dark:bg-rose-950/30 dark:text-rose-300">
            {error}
          </p>
        )}

        {loading ? (
          <div className="space-y-3 animate-pulse">
            <div className="h-20 rounded-xl bg-slate-100 dark:bg-slate-800" />
            <div className="h-20 rounded-xl bg-slate-100 dark:bg-slate-800" />
          </div>
        ) : reviews.length === 0 ? (
          <p className="py-8 text-center text-sm text-slate-500 dark:text-slate-400">
            No feedback has been submitted for this event.
          </p>
        ) : (
          <ul className="space-y-3">
            {reviews.map((review) => {
              const isPublic = review.status === "PUBLIC";
              return (
                <li
                  key={review.reviewId}
                  className="flex items-start justify-between gap-4 rounded-xl border border-slate-200 bg-slate-50 p-4 dark:border-slate-700 dark:bg-slate-800/60"
                >
                  <div className="min-w-0 space-y-2">
                    <Stars value={review.rating} />
                    <p className={`break-words text-sm ${review.comment ? "text-slate-800 dark:text-slate-100" : "italic text-slate-500 dark:text-slate-400"}`}>
                      {review.comment || "No comment"}
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => handleToggleVisibility(review.reviewId)}
                    disabled={togglingId === review.reviewId}
                    className={`shrink-0 inline-flex items-center gap-1.5 rounded-lg border px-3 py-2 text-xs font-semibold transition-colors disabled:cursor-not-allowed disabled:opacity-60 ${
                      isPublic
                        ? "border-emerald-200 bg-emerald-50 text-emerald-700 dark:border-emerald-800 dark:bg-emerald-950/40 dark:text-emerald-300"
                        : "border-slate-200 bg-white text-slate-700 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-200"
                    }`}
                  >
                    {isPublic ? <FiEye className="h-4 w-4" /> : <FiEyeOff className="h-4 w-4" />}
                    {isPublic ? "Public" : "Private"}
                  </button>
                </li>
              );
            })}
          </ul>
        )}
      </div>
    </ModalShell>
  );
};

export default EventFeedbackModal;
