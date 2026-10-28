import { useEffect, useState } from "react";
import { FiStar } from "react-icons/fi";
import { getRatingSummary } from "../../../services/EventReviewService";

const isEventPast = (event) => {
  const end = event?.endTime || event?.eventTime;
  return end ? new Date(end) < new Date() : false;
};

const EventCard = ({ event, index, onShowDetails }) => {
  const [imageError, setImageError] = useState(false);
  const [ratingSummary, setRatingSummary] = useState(null);
  const past = isEventPast(event);

  useEffect(() => {
    if (!past || !event?.id) {
      setRatingSummary(null);
      return;
    }

    let cancelled = false;
    getRatingSummary(event.id)
      .then((summary) => {
        if (!cancelled) setRatingSummary(summary);
      })
      .catch(() => {
        if (!cancelled) setRatingSummary(null);
      });

    return () => {
      cancelled = true;
    };
  }, [past, event?.id]);

  const handleOpenDetails = () => {
    if (!event?.id) return;
    onShowDetails?.(event.id);
  };

  const reviewCount = ratingSummary?.reviewCount ?? 0;
  const average = ratingSummary?.averageRating ?? 0;

  return (
    <div
      className="group bg-white dark:bg-slate-800 rounded-[2.5rem] shadow-xl shadow-slate-200/50 dark:shadow-slate-950/40 overflow-hidden border border-slate-100 dark:border-slate-700 transition-all duration-500 hover:-translate-y-2 cursor-pointer flex flex-col h-full"
      data-aos="fade-up"
      data-aos-delay={index * 100}
      onClick={handleOpenDetails}
    >
      <div className="relative h-64 overflow-hidden bg-slate-100 dark:bg-slate-700">
        <div className="absolute inset-0 bg-gradient-to-t from-slate-900/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 z-10 flex items-end justify-center p-8">
          <span className="text-white font-bold uppercase tracking-[0.2em] text-xs translate-y-4 group-hover:translate-y-0 transition-transform duration-500">
            View Details
          </span>
        </div>

        {event.imageUrl && !imageError ? (
          <img
            src={event.imageUrl}
            alt={event.name}
            className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
            onError={() => setImageError(true)}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#4B98C8]/20 to-[#205E85]/20">
            <span className="text-slate-300 dark:text-slate-500 text-7xl font-black opacity-40">
              {event.name?.charAt(0) || "E"}
            </span>
          </div>
        )}
      </div>

      <div className="p-8 flex flex-col flex-1 items-center text-center">
        <h3 className="text-2xl font-extrabold text-slate-900 dark:text-slate-100 tracking-tight mb-4 group-hover:text-[#4B98C8] transition-colors leading-tight">
          {event.name}
        </h3>

        <p className="text-slate-500 dark:text-slate-300 text-base leading-relaxed mb-6 line-clamp-3 font-medium flex-1">
          {event.description || "Join us for our upcoming events and be part of our community."}
        </p>

        {past && reviewCount > 0 && (
          <div className="mb-4 inline-flex items-center gap-2 text-sm font-bold text-slate-600 dark:text-slate-300">
            <FiStar className="w-4 h-4 fill-current text-amber-400" />
            <span>{Number(average).toFixed(1)}</span>
            <span className="text-slate-400 dark:text-slate-500 font-medium">
              ({reviewCount} {reviewCount === 1 ? "review" : "reviews"})
            </span>
          </div>
        )}

        <div className="mt-auto pt-6 w-full flex items-center justify-center gap-2 group-hover:gap-4 transition-all duration-300">
          <div className="h-px flex-1 bg-slate-100 dark:bg-slate-700" />
          <div className="w-10 h-10 rounded-full bg-slate-50 dark:bg-slate-700 flex items-center justify-center text-slate-400 dark:text-slate-300 group-hover:bg-[#4B98C8] group-hover:text-white transition-all duration-300">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              className="h-5 w-5"
              viewBox="0 0 20 20"
              fill="currentColor"
            >
              <path
                fillRule="evenodd"
                d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z"
                clipRule="evenodd"
              />
            </svg>
          </div>
          <div className="h-px flex-1 bg-slate-100 dark:bg-slate-700" />
        </div>
      </div>
    </div>
  );
};

export default EventCard;
