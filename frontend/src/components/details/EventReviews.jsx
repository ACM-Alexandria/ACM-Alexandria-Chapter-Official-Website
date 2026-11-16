import { useCallback, useEffect, useState } from "react";
import { FiCheckCircle, FiLock, FiStar } from "react-icons/fi";
import { addReview, getPublicComments, getRatingSummary, hasUserReviewed } from "../../services/EventReviewService";
import { PanelSection } from "../ui/SidePanel";
import CommentCarousel from "./CommentCarousel";
import { errorBox, field, muted, primaryButton, secondaryButton, skeleton } from "../ui/uiStyles";

const MAX_COMMENT = 255;
const box = "rounded-2xl bg-white p-5 ring-1 ring-ocean-106 dark:bg-ocean-871 dark:ring-white/10";

const Stars = ({ value, className = "h-4 w-4" }) => (
  <span className="flex items-center gap-0.5" aria-label={`${Number(value).toFixed(1)} out of 5 stars`}>
    {[1, 2, 3, 4, 5].map((n) => (
      <FiStar key={n} aria-hidden="true" className={`${className} ${n <= Math.round(value) ? "fill-current text-amber-400" : "text-slate-300 dark:text-ocean-678"}`} />
    ))}
  </span>
);

// Feedback on an event: its average rating and public comments for everyone, and once the event has ended, a
// form for attendees who registered (one review each, anonymous).
const EventReviews = ({ eventId, isPast, isLoggedIn, isRegistered, onLoginRequired }) => {
  const [summary, setSummary] = useState(null);
  const [comments, setComments] = useState([]);
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
      const [s, c] = await Promise.all([getRatingSummary(eventId), getPublicComments(eventId)]);
      setSummary(s);
      setComments(Array.isArray(c) ? c : []);
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

  // Whether this person already reviewed the event, so the form isn't offered twice.
  useEffect(() => {
    if (!eventId || !isLoggedIn || !isPast) {
      setSubmitted(false);
      return undefined;
    }
    let cancelled = false;
    setCheckingMine(true);
    hasUserReviewed(eventId)
      .then((reviewed) => { if (!cancelled) setSubmitted(reviewed); })
      .catch(() => {}) // The form still works; the server answers 409 for a second review.
      .finally(() => { if (!cancelled) setCheckingMine(false); });
    return () => { cancelled = true; };
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
    <PanelSection title="Event feedback">
      <div className="space-y-4">
        <div className="flex min-h-12 items-center gap-4">
          {loading ? (
            <div className={`h-10 w-44 rounded-xl ${skeleton}`} />
          ) : loadError ? (
            <p className="text-sm text-red-600 dark:text-red-300">{loadError}</p>
          ) : count === 0 ? (
            <p className={`text-sm ${muted}`}>No ratings yet.</p>
          ) : (
            <>
              <span className="text-4xl font-semibold tracking-tight text-slate-900 dark:text-white">{Number(average).toFixed(1)}</span>
              <div>
                <Stars value={average} className="h-5 w-5" />
                <p className={`mt-1 text-xs ${muted}`}>{count} {count === 1 ? "review" : "reviews"}</p>
              </div>
            </>
          )}
        </div>

        <div className={box}>
          {!isPast ? (
            <p className={`text-sm ${muted}`}>Reviews open after the event ends.</p>
          ) : !isLoggedIn ? (
            <div className="flex flex-wrap items-center justify-between gap-3">
              <p className={`text-sm ${muted}`}>Went to this event? Sign in to rate it.</p>
              <button type="button" onClick={onLoginRequired} className={secondaryButton}>Sign in to review</button>
            </div>
          ) : checkingMine ? (
            <div className={`h-6 w-48 rounded-full ${skeleton}`} />
          ) : submitted ? (
            <p className="flex items-center gap-2 text-sm font-semibold text-emerald-700 dark:text-emerald-300">
              <FiCheckCircle className="h-5 w-5 shrink-0" aria-hidden="true" />Thanks for your feedback!
            </p>
          ) : !isRegistered ? (
            <p className={`text-sm ${muted}`}>Only people who registered for this event can leave feedback.</p>
          ) : (
            <form onSubmit={handleSubmit} className="space-y-4">
              <div>
                <p className="text-sm font-semibold text-slate-900 dark:text-white">Rate this event</p>
                <div className="mt-2 flex items-center gap-1" onMouseLeave={() => setHover(0)}>
                  {[1, 2, 3, 4, 5].map((n) => (
                    <button
                      key={n}
                      type="button"
                      aria-label={`${n} star${n > 1 ? "s" : ""}`}
                      aria-pressed={rating === n}
                      onMouseEnter={() => setHover(n)}
                      onFocus={() => setHover(n)}
                      onBlur={() => setHover(0)}
                      onClick={() => setRating(n)}
                      className="rounded-lg p-1 transition-transform hover:scale-110 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ocean-461"
                    >
                      <FiStar aria-hidden="true" className={`h-7 w-7 transition-colors ${n <= (hover || rating) ? "fill-current text-amber-400" : "text-slate-300 dark:text-ocean-678"}`} />
                    </button>
                  ))}
                </div>
              </div>

              <div>
                <textarea
                  value={comment}
                  onChange={(e) => setComment(e.target.value)}
                  maxLength={MAX_COMMENT}
                  rows={3}
                  placeholder="Leave a comment (optional)"
                  aria-label="Comment"
                  className={`${field} resize-y py-3`}
                />
                <div className={`mt-1.5 flex items-start justify-between gap-3 text-xs ${muted}`}>
                  <span className="flex items-start gap-1.5"><FiLock className="mt-0.5 h-3 w-3 shrink-0" aria-hidden="true" />Anonymous. Comments stay private unless an admin makes them public.</span>
                  <span className="shrink-0 tabular-nums">{comment.length}/{MAX_COMMENT}</span>
                </div>
              </div>

              {formError && <p className={errorBox}>{formError}</p>}

              <button type="submit" disabled={submitting} className={primaryButton}>{submitting ? "Submitting…" : "Submit review"}</button>
            </form>
          )}
        </div>

        {comments.length > 0 && <CommentCarousel key={eventId} comments={comments} />}
      </div>
    </PanelSection>
  );
};

export default EventReviews;
