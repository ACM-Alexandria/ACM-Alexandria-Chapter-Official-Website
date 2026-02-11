import { useState } from "react";

const ProgramCard = ({ program }) => {
  const [imageError, setImageError] = useState(false);

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
      {/* Program Image */}
      <div className="bg-gradient-to-r from-[#4B98C8] to-[#205E85] h-48 flex items-center justify-center">
        {program.imageUrl && !imageError ? (
          <img
            src={program.imageUrl}
            alt={program.name}
            className="w-full h-full object-cover"
            onError={() => setImageError(true)}
          />
        ) : (
          <span className="text-white text-6xl font-bold opacity-20">
            {program.name?.charAt(0) || "P"}
          </span>
        )}
      </div>

      {/* Program Info */}
      <div className="p-6">
        <h3 className="text-2xl font-bold text-gray-800 mb-2">
          {program.name}
        </h3>

        <p className="text-gray-600 text-sm mb-4 line-clamp-3">
          {program.description || "Learn more about this program"}
        </p>

        {/* Program Meta Info - Date and Time */}
        {program.eventTime && (
          <div className="space-y-3 mb-4 text-gray-600 text-sm border-t pt-4">
            <div className="flex items-center gap-2">
              <span className="font-semibold text-gray-800">Date:</span>
              <span>{formatDate(program.eventTime)}</span>
            </div>

            <div className="flex items-center gap-2">
              <span className="font-semibold text-gray-800">Time:</span>
              <span>{formatTime(program.eventTime)}</span>
            </div>
          </div>
        )}

        {/* Registration Button */}
        {program.googleFormUrl && (
          <a
            href={program.googleFormUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="inline-block w-full text-center bg-gradient-to-r from-[#4B98C8] to-[#205E85] text-white font-semibold py-2 px-4 rounded-lg hover:shadow-lg transition-shadow duration-300"
          >
            Register Now
          </a>
        )}
      </div>
    </div>
  );
};

export default ProgramCard;
