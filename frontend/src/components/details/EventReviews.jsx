import { useCallback, useEffect, useState } from "react";
import { FiStar, FiLock, FiCheckCircle } from "react-icons/fi";
import {
  addReview,
  getRatingSummary,
  getPublicComments,
  hasUserReviewed,
} from "../../services/EventReviewService";

const Stars = ({ value, size = "w-5 h-5" }) => (
  <div className="flex items-center gap-0.5" aria-label={`${value} out of 5 stars`}>
    {[1, 2, 3, 4, 5].map((n) => (
      <FiStar
        key={n}
        className={`${size} ${
          n <= Math.round(value)
            ? "fill-current text-amber-400"
            : "text-slate-300 dark:text-slate-600"
        }`}
      />
    ))}
  </div>
);

/**
 * Props
 *  - eventId
 *  - isPast           reviews open only after the event ends
 *  - isLoggedIn
 *  - isRegistered     only registered attendees can submit
 *  - onLoginRequired
 */
const EventReviews = ({
  eventId,
  isPast,
  isLoggedIn,
  isRegistered,
  onLoginRequired,
}) => {
  const [summary, setSummary] = useState(null);
  const [publicComments, setPublicComments] = useState([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState(null);

  const [rating, setRating] = useState(0);
  const [hover, setHover] = useState(0);
  const [comment, setComment] = useState("");
  const [submitting, setSubmitting] = useState(false);
  const [submitted, setSubmitted] = useState(false);
  const [formError, setFormError] = useState(null);
  const [checkingMine, setCheckingMine] = useState(false);

  const loadPublic = useCallback(async () => {
    try {
      const [s, c] = await Promise.all([
        getRatingSummary(eventId),
        getPublicComments(eventId),
      ]);
      setSummary(s);
      setPublicComments(Array.isArray(c) ? c : []);
      setLoadError(null);
    } catch (err) {
      setLoadError(err.message);
    } finally {
      setLoading(false);
    }
  }, [eventId]);

  useEffect(() => {
    if (!eventId) return;
    setLoading(true);
    setSubmitted(false);
    setRating(0);
    setComment("");
    setFormError(null);
    loadPublic();
  }, [eventId, loadPublic]);

  useEffect(() => {
    if (!eventId || !isLoggedIn || !isPast) {
      setSubmitted(false);
      return;
    }

    let cancelled = false;
    const check = async () => {
      setCheckingMine(true);
      try {
        const reviewed = await hasUserReviewed(eventId);
        if (!cancelled) setSubmitted(reviewed);
      } catch {
        // Non-blocking — form still works; 409 handles duplicates
      } finally {
        if (!cancelled) setCheckingMine(false);
      }
    };
    check();
    return () => {
      cancelled = true;
    };
  }, [eventId, isLoggedIn, isPast]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (rating < 1) {
      setFormError("Please choose a star rating.");
      return;
    }
    setSubmitting(true);
    setFormError(null);
    try {
      await addReview({ eventId, rating, comment });
      setSubmitted(true);
      loadPublic();
    } catch (err) {
      setFormError(err.message);
      if (err.status === 409) setSubmitted(true);
    } finally {
      setSubmitting(false);
    }
  };

  const average = summary?.averageRating ?? 0;
  const count = summary?.reviewCount ?? 0;

  return (
    <section className="space-y-8">
      <div className="space-y-4">
        <h4 className="text-sm font-black text-slate-900 dark:text-slate-100 uppercase tracking-[0.15em] flex items-center gap-3">
          Event feedback
          <div className="h-px flex-1 bg-slate-100 dark:bg-slate-700" />
        </h4>

        <div className="flex items-center gap-4">
          {loading ? (
            <div className="h-8 w-40 rounded-full bg-slate-100 dark:bg-slate-800 animate-pulse" />
          ) : loadError ? (
            <p className="text-sm text-red-500 dark:text-red-300">{loadError}</p>
          ) : count === 0 ? (
            <p className="text-sm text-slate-400 dark:text-slate-300">No ratings yet.</p>
          ) : (
            <>
              <span className="text-4xl font-extrabold text-slate-900 dark:text-slate-100">
                {Number(average).toFixed(1)}
              </span>
              <div>
                <Stars value={average} />
                <p className="text-xs text-slate-500 dark:text-slate-400 mt-1">
                  {count} {count === 1 ? "review" : "reviews"}
                </p>
              </div>
            </>
          )}
        </div>
      </div>

      <div className="rounded-3xl border border-slate-100 dark:border-slate-700 bg-slate-50 dark:bg-slate-900 p-6">
        {!isPast ? (
          <p className="text-sm text-slate-500 dark:text-slate-300">
            Reviews open after the event ends.
          </p>
        ) : !isLoggedIn ? (
          <button
            type="button"
            onClick={onLoginRequired}
            className="px-5 py-2.5 rounded-2xl bg-[#4B98C8] text-white font-bold text-sm hover:bg-[#205E85] transition-colors"
          >
            Log in to review
          </button>
        ) : checkingMine ? (
          <div className="h-6 w-48 rounded-full bg-slate-100 dark:bg-slate-800 animate-pulse" />
        ) : submitted ? (
          <div className="flex items-center gap-3 text-sm text-emerald-600 dark:text-emerald-400 font-semibold">
            <FiCheckCircle className="w-5 h-5 shrink-0" />
            Thanks for your feedback!
          </div>
        ) : !isRegistered ? (
          <p className="text-sm text-slate-500 dark:text-slate-300">
            Only attendees who registered for this event can leave feedback.
          </p>
        ) : (
          <form onSubmit={handleSubmit} className="space-y-4">
            <h3 className="font-bold text-slate-900 dark:text-slate-100">Rate this event</h3>

            <div className="flex items-center gap-1" onMouseLeave={() => setHover(0)}>
              {[1, 2, 3, 4, 5].map((n) => (
                <button
                  key={n}
                  type="button"
                  aria-label={`${n} star${n > 1 ? "s" : ""}`}
                  onMouseEnter={() => setHover(n)}
                  onClick={() => setRating(n)}
                  className="p-1"
                >
                  <FiStar
                    className={`w-7 h-7 transition-colors ${
                      n <= (hover || rating)
                        ? "fill-current text-amber-400"
                        : "text-slate-300 dark:text-slate-600"
                    }`}
                  />
                </button>
              ))}
            </div>

            <div>
              <textarea
                value={comment}
                onChange={(e) => setComment(e.target.value)}
                maxLength={255}
                rows={3}
                placeholder="Leave a comment (optional)"
                className="w-full rounded-2xl border border-slate-200 dark:border-slate-600 bg-white dark:bg-slate-800 text-slate-800 dark:text-slate-100 p-3 text-sm focus:outline-none focus:border-[#4B98C8]"
              />
              <div className="flex items-center justify-between mt-1 text-xs text-slate-400 gap-3">
                <span className="flex items-center gap-1">
                  <FiLock className="w-3 h-3 shrink-0" />
                  Anonymous. Comments stay private unless an admin makes them public.
                </span>
                <span className="shrink-0">{comment.length}/255</span>
              </div>
            </div>

            {formError && <p className="text-sm text-red-500 dark:text-red-300">{formError}</p>}

            <button
              type="submit"
              disabled={submitting}
              className="px-5 py-2.5 rounded-2xl bg-[#4B98C8] text-white font-bold text-sm hover:bg-[#205E85] transition-colors disabled:opacity-60"
            >
              {submitting ? "Submitting..." : "Submit review"}
            </button>
          </form>
        )}
      </div>

      <div className="space-y-3">
        <h3 className="font-bold text-slate-900 dark:text-slate-100">Comments</h3>
        {publicComments.length === 0 ? (
          <p className="text-sm text-slate-400 dark:text-slate-300">No public comments yet.</p>
        ) : (
          publicComments.map((c, i) => (
            <p
              key={i}
              className="rounded-2xl bg-white dark:bg-slate-900 border border-slate-100 dark:border-slate-700 p-4 text-sm text-slate-700 dark:text-slate-200"
            >
              {c.comment}
            </p>
          ))
        )}
      </div>
    </section>
  );
};

export default EventReviews;
