const ClubCard = ({ club }) => {
  return (
    <div
      className="bg-white rounded-lg shadow-lg overflow-hidden"
      data-aos="fade-up"
    >
      {/* Club Image Placeholder */}
      <div className="bg-gradient-to-r from-[#4B98C8] to-[#205E85] h-40 flex items-center justify-center">
        <span className="text-white text-5xl font-bold opacity-20">
          {club.name?.charAt(0) || "C"}
        </span>
      </div>

      {/* Club Info */}
      <div className="p-6">
        <h3 className="text-2xl font-bold text-gray-800 mb-2">{club.name}</h3>
        <p className="text-gray-600 text-sm mb-4 line-clamp-3">
          {club.description || "Join our community and grow together"}
        </p>

        {/* Club Meta Info */}
        {club.members && (
          <p className="text-gray-500 text-xs">{club.members} members</p>
        )}
      </div>
    </div>
  );
};

export default ClubCard;
