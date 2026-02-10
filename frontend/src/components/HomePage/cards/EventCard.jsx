const EventCard = ({ event }) => {
  // Format time from ISO string (e.g., "2026-02-22T16:00:00" -> "4:00 PM")
  const formatTime = (timeString) => {
    if (!timeString) return "";
    try {
      const date = new Date(timeString);
      return date.toLocaleTimeString("en-US", {
        hour: "numeric",
        minute: "2-digit",
        hour12: true,
      });
    } catch {
      return timeString;
    }
  };

  // Format date (e.g., "2026-02-22T16:00:00" -> "Feb 22, 2026")
  const formatDate = (dateString) => {
    if (!dateString) return "";
    try {
      const date = new Date(dateString);
      return date.toLocaleDateString("en-US", {
        month: "short",
        day: "numeric",
        year: "numeric",
      });
    } catch {
      return dateString;
    }
  };

  return (
    <div
      className="bg-white rounded-lg shadow-lg overflow-hidden"
      data-aos="fade-up"
    >
      {/* Event Image Placeholder */}
      <div className="bg-gradient-to-r from-[#4B98C8] to-[#205E85] h-48 flex items-center justify-center">
        <span className="text-white text-6xl font-bold opacity-20">
          {event.name?.charAt(0) || "E"}
        </span>
      </div>

      {/* Event Info */}
      <div className="p-6">
        <div className="flex items-center justify-between mb-2">
          <h3 className="text-2xl font-bold text-gray-800">{event.name}</h3>
          {event.status && (
            <span className="bg-blue-100 text-blue-800 text-xs font-semibold px-3 py-1 rounded-full">
              {event.status}
            </span>
          )}
        </div>

        <p className="text-gray-600 text-sm mb-4 line-clamp-3">
          {event.description || "Join us for an amazing event"}
        </p>

        {/* Event Meta Info - Date, Time and Location */}
        <div className="space-y-3 mb-4 text-gray-600 text-sm border-t pt-4">
          {event.eventTime && (
            <div className="flex items-center gap-2">
              <span className="font-semibold text-gray-800">Date:</span>
              <span>{formatDate(event.eventTime)}</span>
            </div>
          )}

          {event.eventTime && (
            <div className="flex items-center gap-2">
              <span className="font-semibold text-gray-800">Time:</span>
              <span>{formatTime(event.eventTime)}</span>
            </div>
          )}

          {event.location && (
            <div className="flex items-center gap-2">
              <span className="font-semibold text-gray-800">Location:</span>
              <span>{event.location}</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default EventCard;
