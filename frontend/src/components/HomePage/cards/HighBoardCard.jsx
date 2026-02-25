import { useState } from "react";

const HighBoardCard = ({ member }) => {
  const [imageError, setImageError] = useState(false);

  return (
    <div
      className="bg-white rounded-lg shadow-lg overflow-hidden hover:shadow-xl transition-shadow duration-300"
      data-aos="fade-up"
    >
      <div className="bg-linear-to-r from-[#4B98C8] to-[#205E85] h-52 flex items-center justify-center overflow-hidden">
        {member.imageUrl && !imageError ? (
          <img
            src={member.imageUrl}
            alt={member.name}
            className="w-full h-full object-cover"
            onError={() => setImageError(true)}
          />
        ) : (
          <span className="text-white text-6xl font-bold opacity-20">
            {member.name?.charAt(0) || "H"}
          </span>
        )}
      </div>

      <div className="p-6 text-center">
        <h3 className="text-2xl font-bold text-gray-800 mb-2">{member.name}</h3>
        <p className="text-gray-600 text-base">{member.role}</p>
      </div>
    </div>
  );
};

export default HighBoardCard;
