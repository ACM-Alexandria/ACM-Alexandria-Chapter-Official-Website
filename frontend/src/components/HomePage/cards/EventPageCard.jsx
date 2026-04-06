import { useState } from "react";

const EventPageCard = ({ event }) => {
    const [imageError, setImageError] = useState(false);

    return (
        <div
            className="bg-white rounded-xl shadow-md overflow-hidden flex flex-col transition-transform duration-300 hover:-translate-y-1 hover:shadow-xl"
            data-aos="fade-up"
        >
            {/* Image */}
            <div className="bg-gradient-to-r from-[#4B98C8] to-[#205E85] h-52 flex items-center justify-center overflow-hidden">
                {event.imageUrl && !imageError ? (
                    <img
                        src={event.imageUrl}
                        alt={event.name}
                        className="w-full h-full object-cover"
                        onError={() => setImageError(true)}
                    />
                ) : (
                    <span className="text-white text-7xl font-bold opacity-20 select-none">
                        {event.name?.charAt(0)?.toUpperCase() || "E"}
                    </span>
                )}
            </div>

            {/* Content */}
            <div className="p-5 flex flex-col flex-1">
                <h3 className="text-lg font-bold text-gray-800 mb-4 line-clamp-2">
                    {event.name}
                </h3>

                {/* View Details button — not wired up yet */}
                <div className="mt-auto">
                    <button
                        disabled
                        className="w-full bg-gradient-to-r from-[#4B98C8] to-[#205E85] text-white font-semibold py-2.5 px-6 rounded-md flex items-center justify-center gap-2 opacity-80 cursor-not-allowed"
                    >
                        View Details
                        <svg
                            className="w-4 h-4"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                        >
                            <path
                                strokeLinecap="round"
                                strokeLinejoin="round"
                                strokeWidth={2}
                                d="M13 7l5 5m0 0l-5 5m5-5H6"
                            />
                        </svg>
                    </button>
                </div>
            </div>
        </div>
    );
};

export default EventPageCard;
