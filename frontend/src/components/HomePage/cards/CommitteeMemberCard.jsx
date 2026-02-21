const CommitteeMemberCard = ({ member }) => {
  return (
    <div className="bg-gray-50 hover:bg-gray-100 transition-colors duration-200 rounded-2xl border border-gray-200 w-64 p-5 text-center">
      <div className="w-24 h-24 mx-auto rounded-full overflow-hidden bg-white border border-gray-200 flex items-center justify-center">
        {member.imageUrl ? (
          <img
            src={member.imageUrl}
            alt={member.name}
            className="w-full h-full object-cover"
          />
        ) : (
          <span className="text-gray-500 text-2xl font-semibold">
            {member.name?.charAt(0) || "M"}
          </span>
        )}
      </div>

      <p className="mt-4 text-gray-800 font-semibold text-lg leading-tight">
        {member.name}
      </p>
      <p className="mt-2 text-gray-600 text-base leading-tight">
        {member.role}
      </p>
    </div>
  );
};

export default CommitteeMemberCard;
