const CommitteeCard = ({ committee }) => {
  return (
    <div
      className="bg-white rounded-lg shadow-lg overflow-hidden hover:shadow-xl transition-shadow duration-300"
      data-aos="fade-up"
    >
      {/* Committee Logo/Icon */}
      <div className="bg-gradient-to-r from-[#4B98C8] to-[#205E85] h-56 flex items-center justify-center">
        {committee.logoUrl ? (
          <img
            src={committee.logoUrl}
            alt={committee.name}
            className="h-32 w-32 object-contain"
          />
        ) : (
          <span className="text-white text-6xl font-bold opacity-20">
            {committee.name?.charAt(0) || "C"}
          </span>
        )}
      </div>

      {/* Committee Info */}
      <div className="p-8">
        <h3 className="text-3xl font-bold text-gray-800 mb-3">
          {committee.name}
        </h3>
        <p className="text-gray-600 text-base mb-4 line-clamp-4">
          {committee.description || "Learn more about this committee"}
        </p>

        {/* Open Status Badge */}
        {committee.isOpen && (
          <span className="inline-block bg-green-100 text-green-800 text-sm font-semibold px-4 py-2 rounded-full">
            Applications Open
          </span>
        )}
      </div>
    </div>
  );
};

export default CommitteeCard;
