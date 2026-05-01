import { useState } from "react";

const EventCard = ({ event, index, onShowDetails }) => {
  const [imageError, setImageError] = useState(false);

  const handleOpenDetails = () => {
    if (!event?.id) return;
    onShowDetails?.(event.id);
  };
  return (
    <div
      className="group bg-white rounded-[2.5rem] shadow-xl shadow-slate-200/50 overflow-hidden border border-slate-100 transition-all duration-500 hover:-translate-y-2 cursor-pointer flex flex-col h-full"
      data-aos="fade-up"
      data-aos-delay={index * 100}
      onClick={handleOpenDetails}
    >
      {/* Event Image */}
      <div className="relative h-64 overflow-hidden bg-slate-100">
        <div className="absolute inset-0 bg-gradient-to-t from-slate-900/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 z-10 flex items-end justify-center p-8">
          <span className="text-white font-bold uppercase tracking-[0.2em] text-xs translate-y-4 group-hover:translate-y-0 transition-transform duration-500">
            View Details
          </span>
        </div>

        {event.imageUrl && !imageError ? (
          <img
            src={event.imageUrl}
            alt={event.name}
            className={`w-full h-full object-cover transition-transform duration-700 group-hover:scale-110`}
            onError={() => setImageError(true)}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#4B98C8]/20 to-[#205E85]/20">
            <span className="text-slate-300 text-7xl font-black opacity-40">
              {event.name?.charAt(0) || "E"}
            </span>
          </div>
        )}
      </div>

      <div className="p-8 flex flex-col flex-1 items-center text-center">
        <h3
          className={`text-2xl font-extrabold tracking-tight mb-4 group-hover:text-[#4B98C8] transition-colors leading-tight`}
        >
          {event.name}
        </h3>

        <p className="text-slate-500 text-base leading-relaxed mb-6 line-clamp-3 font-medium flex-1">
          {event.description || "Join us for our upcoming events and be part of our community."}
        </p>

        <div className="mt-auto pt-6 w-full flex items-center justify-center gap-2 group-hover:gap-4 transition-all duration-300">
          <div className="h-px flex-1 bg-slate-100" />
          <div
            className={`w-10 h-10 rounded-full flex items-center justify-center transition-all duration-300 `}
          >
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
          <div className="h-px flex-1 bg-slate-100" />
        </div>
      </div>
    </div>
  );
};

export default EventCard;
