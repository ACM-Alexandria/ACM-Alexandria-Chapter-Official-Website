import { useState } from "react";

const EventCard = ({ event }) => {
  const [imageError, setImageError] = useState(false);

  return (
    <div
      className="bg-white rounded-lg shadow-lg overflow-hidden"
      data-aos="fade-up"
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
        <h3 className="text-2xl font-bold text-gray-800">{event.name}</h3>
      </div>
    </div>
  );
};

export default EventCard;
