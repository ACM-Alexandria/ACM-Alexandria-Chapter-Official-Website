import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";
import { fetchEventById } from "../../services/homePageService";
import { HiOutlineCalendar, HiOutlineLocationMarker, HiOutlineX } from "react-icons/hi";

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
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
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
      className={`fixed inset-0 z-50 transition-opacity duration-500 ${isVisible ? "pointer-events-auto opacity-100" : "pointer-events-none opacity-0"}`}
      aria-hidden={!isVisible}
    >
      {/* Backdrop */}
      <div
        className={`absolute inset-0 bg-slate-900/40 backdrop-blur-md transition-opacity duration-500 ${isVisible ? "opacity-100" : "opacity-0"}`}
        onClick={onClose}
      />

      <aside
        className={`absolute top-0 right-0 h-full w-full md:w-[600px] lg:w-[700px] overflow-hidden bg-white shadow-2xl transition-transform duration-500 ease-[cubic-bezier(0.22,1,0.36,1)] ${isVisible ? "translate-x-0" : "translate-x-full"}`}
      >
        <div className="h-full flex flex-col">
          {/* Header Image Section */}
          <div className="relative h-80 shrink-0 overflow-hidden bg-slate-100">
            {/* Close Button */}
            <button
              type="button"
              onClick={onClose}
              className="absolute right-6 top-6 z-30 w-12 h-12 flex items-center justify-center rounded-2xl bg-white/10 backdrop-blur-xl text-white border border-white/20 shadow-xl transition-all duration-300 hover:bg-white hover:text-slate-900 hover:scale-105 active:scale-95"
              aria-label="Close"
            >
              <HiOutlineX className="w-6 h-6" />
            </button>

            {loading ? (
              <div className="w-full h-full animate-pulse bg-slate-200" />
            ) : event?.imageUrl && !imageError ? (
              <img
                src={event.imageUrl}
                alt={event.name}
                className="w-full h-full object-cover transition-transform duration-1000 scale-105 group-hover:scale-100"
                onError={() => setImageError(true)}
              />
            ) : (
              <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#4B98C8] to-[#205E85]">
                <span className="text-white text-9xl font-black opacity-10 select-none">
                  {event?.name?.charAt(0) || "E"}
                </span>
              </div>
            )}

            {/* Gradient Overlay */}
            <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/20 to-transparent opacity-80" />

            <div className="absolute bottom-10 left-10 right-10 z-20">
              {!loading && event && (() => {
                const isPast = event.eventTime ? new Date(event.eventTime) < new Date() : false;
                return (
                  <div style={{ animation: "slideLeft 0.8s cubic-bezier(0.22,1,0.36,1) both" }}>
                    <div className={`inline-flex rounded-full px-4 py-1.5 text-[10px] font-black uppercase tracking-[0.2em] text-white mb-4 ${isPast ? 'bg-slate-500/50 backdrop-blur-md' : 'bg-[#4B98C8]'}`}>
                      {isPast ? 'Past Event' : 'Upcoming Event'}
                    </div>
                    <h2 className="text-3xl md:text-4xl font-extrabold text-white tracking-tight leading-tight">
                      {event.name}
                    </h2>
                  </div>
                );
              })()}
            </div>
          </div>

          {/* Content Area */}
          <div className="flex-1 overflow-y-auto px-10 py-12 scrollbar-hide">
            {loading ? (
              <div className="space-y-8 animate-pulse">
                <div className="grid grid-cols-2 gap-6">
                  <div className="h-24 bg-slate-50 rounded-3xl" />
                  <div className="h-24 bg-slate-50 rounded-3xl" />
                </div>
                <div className="h-64 bg-slate-50 rounded-[2.5rem]" />
              </div>
            ) : error ? (
              <div className="flex flex-col items-center justify-center h-full text-center space-y-4">
                <div className="w-20 h-20 bg-red-50 rounded-full flex items-center justify-center text-red-500 mb-2">
                   <HiOutlineX className="w-10 h-10" />
                </div>
                <p className="text-xl font-extrabold text-slate-900">{error}</p>
                <button onClick={onClose} className="px-6 py-2 bg-slate-900 text-white rounded-xl font-bold">Close</button>
              </div>
            ) : event && (
              <div className="space-y-10" style={{ animation: "fadeIn 1s ease 0.3s both" }}>
                {/* Meta Grid */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-6">
                  <div className="bg-slate-50 p-6 rounded-[2rem] border border-slate-100 flex items-center gap-4 group transition-all duration-300 hover:bg-white hover:shadow-xl hover:shadow-slate-100">
                    <div className="w-12 h-12 rounded-2xl bg-white flex items-center justify-center text-[#4B98C8] shadow-sm">
                      <HiOutlineCalendar className="w-6 h-6" />
                    </div>
                    <div>
                      <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Date & Time</p>
                      <p className="text-sm font-extrabold text-slate-900 leading-tight">
                        {formatDateTime(event.eventTime)}
                      </p>
                    </div>
                  </div>

                  <div className="bg-slate-50 p-6 rounded-[2rem] border border-slate-100 flex items-center gap-4 group transition-all duration-300 hover:bg-white hover:shadow-xl hover:shadow-slate-100">
                    <div className="w-12 h-12 rounded-2xl bg-white flex items-center justify-center text-[#4B98C8] shadow-sm">
                      <HiOutlineLocationMarker className="w-6 h-6" />
                    </div>
                    <div>
                      <p className="text-[10px] font-black text-slate-400 uppercase tracking-widest mb-1">Location</p>
                      <p className="text-sm font-extrabold text-slate-900 leading-tight">
                        {event.location || "To Be Announced"}
                      </p>
                    </div>
                  </div>
                </div>

                {/* About Section */}
                <div className="space-y-4">
                  <h4 className="text-sm font-black text-slate-900 uppercase tracking-[0.15em] flex items-center gap-3">
                    About this event
                    <div className="h-px flex-1 bg-slate-100" />
                  </h4>
                  <p className="text-slate-600 text-lg leading-relaxed font-medium whitespace-pre-line">
                    {event.description || "Join us for an inspiring session dedicated to computer science and technological innovation. Stay tuned for more updates."}
                  </p>
                </div>

                {/* Action Section */}
                {(() => {
                  const isPast = event.eventTime ? new Date(event.eventTime) < new Date() : false;
                  return (
                    <div className="pt-6">
                      {isPast ? (
                        <button
                          disabled
                          className="group relative flex w-full items-center justify-center gap-3 overflow-hidden rounded-[2rem] bg-slate-200 px-10 py-6 text-lg font-black text-slate-400 cursor-not-allowed border border-slate-200"
                        >
                          <span className="relative z-10 uppercase tracking-widest text-sm">Event Concluded</span>
                        </button>
                      ) : (
                        <button
                          onClick={() => {
                            if (!isAuthenticated) {
                              navigate("/login");
                            } else {
                              console.log("Initiating registration flow for event:", eventId);
                              // Phase 5 custom form hook will wire here
                            }
                          }}
                          className="group relative flex w-full items-center justify-center gap-3 overflow-hidden rounded-[2rem] bg-gradient-to-r from-[#4B98C8] to-[#205E85] px-10 py-6 text-lg font-black text-white shadow-2xl shadow-blue-200 transition-all duration-300 hover:-translate-y-1 hover:shadow-blue-300 active:scale-[0.98]"
                        >
                          <span className="relative z-10 uppercase tracking-widest text-sm">Register for this event</span>
                          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 relative z-10 group-hover:translate-x-1 transition-transform" viewBox="0 0 20 20" fill="currentColor">
                            <path fillRule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clipRule="evenodd" />
                          </svg>
                          <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent -translate-x-full group-hover:animate-[shimmer_2s_infinite]" />
                        </button>
                      )}
                    </div>
                  );
                })()}
              </div>
            )}
          </div>
        </div>
      </aside>
    </div>
  );
};

export default EventDetailsSidebar;
