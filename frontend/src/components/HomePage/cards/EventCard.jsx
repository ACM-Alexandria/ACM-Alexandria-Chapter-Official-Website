import { useState } from "react";
import ChevronRightIcon from "../../icons/ChevronRightIcon";

const EventCard = ({ event, onShowDetails }) => {
  const [imageError, setImageError] = useState(false);

  const handleOpenDetails = () => {
    if (!event?.id) return;
    onShowDetails?.(event.id);
  };

  return (
    <div
      className="group bg-white rounded-lg shadow-md overflow-hidden transition-all duration-300 hover:-translate-y-1 hover:shadow-xl cursor-pointer"
      data-aos="fade-up"
      onClick={handleOpenDetails}
    >
      {/* Event Image */}
      <div className="bg-gradient-to-r from-[#4B98C8] to-[#205E85] h-48 flex items-center justify-center">
        {event.imageUrl && !imageError ? (
          <img
            src={event.imageUrl}
            alt={event.name}
            className="w-full h-full object-cover"
            onError={() => setImageError(true)}
          />
        ) : (
          <span className="text-white text-6xl font-bold opacity-20">
            {event.name?.charAt(0) || "E"}
          </span>
        )}
      </div>

      <div className="p-6">
        <h3 className="text-2xl font-bold text-gray-800 text-center mb-4">
          {event.name}
        </h3>

        <div className="flex items-center justify-center gap-1 text-[#205E85] font-bold tracking-wider uppercase text-sm">
          <span>Show Details</span>
          <ChevronRightIcon className="w-4 h-4 transition-transform duration-300 group-hover:translate-x-1" />
        </div>
      </div>
    </div>
  );
};

export default EventCard;
