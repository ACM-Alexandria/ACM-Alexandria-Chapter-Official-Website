import { useEffect, useState } from "react";
import { fetchEventById } from "../../services/homePageService";

const formatDateTime = (eventTime) => {
  if (!eventTime) return "TBA";

  try {
    const date = new Date(eventTime);
    return date.toLocaleString("en-US", {
      month: "long",
      day: "numeric",
      year: "numeric",
      hour: "numeric",
      minute: "2-digit",
      hour12: true,
    });
  } catch {
    return eventTime;
  }
};

const EventDetailsSidebar = ({ eventId, isOpen, onClose }) => {
  const [event, setEvent] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [imageError, setImageError] = useState(false);
  const [isVisible, setIsVisible] = useState(false);

  useEffect(() => {
    if (!isOpen || !eventId) return;

    const loadEvent = async () => {
      setLoading(true);
      setError(null);
      setImageError(false);

      try {
        const data = await fetchEventById(eventId);
        setEvent(data);
      } catch (err) {
        console.error("Failed to load event details:", err);
        setError("Unable to load event details right now.");
      } finally {
        setLoading(false);
      }
    };

    loadEvent();
  }, [eventId, isOpen]);

  useEffect(() => {
    if (!isOpen) {
      setIsVisible(false);
      return;
    }

    const frame = requestAnimationFrame(() => {
      setIsVisible(true);
    });

    const originalOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    return () => {
      document.body.style.overflow = originalOverflow;
      cancelAnimationFrame(frame);
    };
  }, [isOpen]);

  if (!eventId) return null;

  return (
    <div
      className={`fixed inset-0 z-50 transition-opacity duration-300 ${isVisible ? "pointer-events-auto opacity-100" : "pointer-events-none opacity-0"}`}
      aria-hidden={!isVisible}
    >
      <div
        className={`absolute inset-0 bg-slate-950/60 backdrop-blur-[2px] transition-opacity duration-300 ${isVisible ? "opacity-100" : "opacity-0"}`}
        onClick={onClose}
      />

      <aside
        className={`absolute top-0 right-0 h-full w-full md:w-1/2 max-w-3xl overflow-hidden bg-gradient-to-b from-white via-slate-50 to-white shadow-[0_30px_80px_rgba(15,23,42,0.35)] transition-transform duration-300 ease-out ${isVisible ? "translate-x-0" : "translate-x-full"}`}
      >
        <div className="h-full overflow-y-auto">
          {loading ? (
            <div className="p-6 space-y-5 animate-pulse">
              <div className="h-72 rounded-3xl bg-gradient-to-br from-slate-200 to-slate-100" />
              <div className="space-y-3 rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
                <div className="h-8 w-2/3 rounded bg-slate-200" />
                <div className="h-4 w-1/2 rounded bg-slate-200" />
                <div className="h-4 w-full rounded bg-slate-200" />
                <div className="h-4 w-5/6 rounded bg-slate-200" />
              </div>
            </div>
          ) : error ? (
            <div className="p-6">
              <div className="rounded-3xl border border-red-200 bg-red-50 p-6">
                <p className="text-base font-semibold text-red-600">{error}</p>
              </div>
            </div>
          ) : !event ? (
            <div className="p-6">
              <div className="rounded-3xl border border-slate-200 bg-white p-6 shadow-sm">
                <p className="text-gray-600 text-lg">Event not found.</p>
              </div>
            </div>
          ) : (
            <div className="pb-6">
              <div className="relative h-80 overflow-hidden bg-gradient-to-br from-[#4B98C8] via-[#377fab] to-[#205E85]">
                <button
                  type="button"
                  onClick={onClose}
                  className="absolute right-4 top-4 z-20 grid h-11 w-11 place-items-center rounded-full border border-white/20 bg-black/20 text-white backdrop-blur-md shadow-lg transition-all duration-300 hover:bg-white/20 hover:scale-105"
                  aria-label="Close event details"
                >
                  ×
                </button>

                {event.imageUrl && !imageError ? (
                  <img
                    src={event.imageUrl}
                    alt={event.name}
                    className="w-full h-full object-cover"
                    onError={() => setImageError(true)}
                  />
                ) : (
                  <div className="flex h-full items-center justify-center">
                    <span className="text-white text-8xl font-bold opacity-20">
                      {event.name?.charAt(0) || "E"}
                    </span>
                  </div>
                )}

                <div className="absolute inset-0 bg-gradient-to-t from-slate-950/70 via-slate-950/20 to-transparent" />

                <div className="absolute left-6 right-6 bottom-6">
                  <div className="inline-flex rounded-full bg-white/15 px-4 py-1 text-xs font-semibold uppercase tracking-[0.22em] text-white/90 backdrop-blur-md">
                    Event Spotlight
                  </div>
                  <h3 className="mt-4 max-w-xl text-3xl font-bold leading-tight text-white md:text-4xl">
                    {event.name}
                  </h3>
                </div>
              </div>

              <div className="space-y-5 px-6 pt-6">
                <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
                  <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
                    <p className="text-xs font-semibold uppercase tracking-[0.22em] text-slate-400">
                      Date & Time
                    </p>
                    <p className="mt-2 text-base font-semibold text-slate-900">
                      {formatDateTime(event.eventTime)}
                    </p>
                  </div>

                  <div className="rounded-2xl border border-slate-200 bg-white p-4 shadow-sm">
                    <p className="text-xs font-semibold uppercase tracking-[0.22em] text-slate-400">
                      Location
                    </p>
                    <p className="mt-2 text-base font-semibold text-slate-900">
                      {event.location || "TBA"}
                    </p>
                  </div>
                </div>

                <div className="rounded-3xl border border-slate-200 bg-white p-5 shadow-sm">
                  <p className="text-xs font-semibold uppercase tracking-[0.22em] text-slate-400">
                    About this event
                  </p>
                  <p className="mt-3 text-gray-700 leading-8 whitespace-pre-line">
                    {event.description || "No event description available yet."}
                  </p>
                </div>

                {event.googleFormUrl && (
                  <div className="pb-2 pt-1">
                    <a
                      href={event.googleFormUrl}
                      target="_blank"
                      rel="noreferrer"
                      className="inline-flex w-full items-center justify-center gap-2 rounded-2xl bg-gradient-to-r from-[#4B98C8] to-[#205E85] px-6 py-4 text-base font-semibold text-white shadow-lg shadow-[#205E85]/20 transition-all duration-300 hover:-translate-y-0.5 hover:shadow-xl hover:shadow-[#205E85]/25"
                    >
                      Register for this event
                      <span aria-hidden="true">→</span>
                    </a>
                  </div>
                )}
              </div>
            </div>
          )}
        </div>
      </aside>
    </div>
  );
};

export default EventDetailsSidebar;
