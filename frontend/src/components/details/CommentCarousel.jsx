import { useRef, useState } from "react";
import { FiChevronLeft, FiChevronRight, FiMessageCircle } from "react-icons/fi";
import { card, muted } from "../ui/uiStyles";

// Up to this many comments get a dot each; more get a "3 / 12" counter.
const MAX_DOTS = 7;
// How far a swipe must travel to turn the page, in px.
const SWIPE = 40;

const arrow = "flex h-10 w-10 items-center justify-center rounded-full text-ocean-676 ring-1 ring-ocean-106 transition-colors hover:bg-ocean-31 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ocean-461 dark:text-ocean-222 dark:ring-white/15 dark:hover:bg-white/5";

// Attendee comments one at a time: a large quote with arrows, dots (or a counter when there are many), arrow
// keys, and swipe on touch screens. The slides share one grid cell, so the card is as tall as the longest comment
// and nothing below it jumps when you turn the page.
const CommentCarousel = ({ comments }) => {
  const [index, setIndex] = useState(0);
  const start = useRef(null);
  const total = comments.length;
  const go = (to) => setIndex((to + total) % total);

  const onKeyDown = (e) => {
    if (e.key === "ArrowLeft") { e.preventDefault(); go(index - 1); }
    if (e.key === "ArrowRight") { e.preventDefault(); go(index + 1); }
  };
  const onTouchStart = (e) => { start.current = e.touches[0].clientX; };
  const onTouchEnd = (e) => {
    if (start.current === null) return;
    const dx = e.changedTouches[0].clientX - start.current;
    start.current = null;
    if (Math.abs(dx) >= SWIPE) go(index + (dx < 0 ? 1 : -1));
  };

  return (
    <section aria-roledescription="carousel" aria-label="What attendees said">
      <p className="flex items-center gap-2 text-sm font-semibold text-slate-900 dark:text-white">
        <FiMessageCircle className="h-4 w-4 text-ocean-573 dark:text-ocean-316" aria-hidden="true" />
        What attendees said
        <span className="rounded-full bg-ocean-67 px-2 py-0.5 text-xs font-semibold text-ocean-676 dark:bg-ocean-808 dark:text-ocean-222">{total}</span>
      </p>

      <div
        tabIndex={0}
        onKeyDown={onKeyDown}
        onTouchStart={onTouchStart}
        onTouchEnd={onTouchEnd}
        aria-live="polite"
        className={`mt-3 grid rounded-2xl px-6 pb-7 pt-4 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ocean-461 sm:px-8 ${card}`}
      >
        <span aria-hidden="true" className="col-start-1 row-start-1 select-none font-serif text-6xl leading-none text-ocean-145 dark:text-ocean-678">&ldquo;</span>
        {comments.map((c, i) => (
          <p
            key={i}
            role="group"
            aria-roledescription="slide"
            aria-label={`${i + 1} of ${total}`}
            aria-hidden={i !== index}
            className={`col-start-1 row-start-1 mt-12 whitespace-pre-line break-words font-serif text-lg leading-relaxed text-slate-800 transition-all duration-300 sm:text-xl dark:text-ocean-102 ${i === index ? "translate-y-0 opacity-100" : "pointer-events-none translate-y-1 opacity-0"}`}
          >
            {c.comment}
          </p>
        ))}
      </div>

      {total > 1 && (
        <div className="mt-3 flex items-center justify-center gap-4">
          <button type="button" onClick={() => go(index - 1)} aria-label="Previous comment" className={arrow}><FiChevronLeft className="h-5 w-5" aria-hidden="true" /></button>
          {total <= MAX_DOTS ? (
            <div className="flex items-center gap-1">
              {comments.map((_, i) => (
                <button key={i} type="button" onClick={() => go(i)} aria-label={`Comment ${i + 1}`} aria-current={i === index} className="group flex h-6 w-4 items-center justify-center focus-visible:outline-none">
                  <span className={`block h-2 rounded-full transition-all duration-300 group-focus-visible:ring-2 group-focus-visible:ring-ocean-461 ${i === index ? "w-5 bg-ocean-676 dark:bg-ocean-461" : "w-2 bg-ocean-106 group-hover:bg-ocean-255 dark:bg-white/20 dark:group-hover:bg-white/40"}`} />
                </button>
              ))}
            </div>
          ) : (
            <span className={`min-w-14 text-center text-sm font-semibold tabular-nums ${muted}`}>{index + 1} / {total}</span>
          )}
          <button type="button" onClick={() => go(index + 1)} aria-label="Next comment" className={arrow}><FiChevronRight className="h-5 w-5" aria-hidden="true" /></button>
        </div>
      )}
    </section>
  );
};

export default CommentCarousel;
