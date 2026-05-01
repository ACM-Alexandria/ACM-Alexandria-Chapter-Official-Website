const CommitteeMemberCard = ({ member }) => {
  return (
    <div className="bg-white hover:shadow-lg hover:shadow-slate-200/50 transition-all duration-300 rounded-3xl border border-slate-100 w-64 p-6 text-center group mx-auto">
      <div className="w-20 h-20 mx-auto rounded-full overflow-hidden bg-slate-50 border border-slate-100 flex items-center justify-center transition-transform duration-500 group-hover:scale-110">
        {member.imageUrl ? (
          <img
            src={member.imageUrl}
            alt={member.name}
            className="w-full h-full object-cover"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#4B98C8]/10 to-[#205E85]/10">
            <span className="text-slate-400 text-2xl font-black">
              {member.name?.charAt(0) || "M"}
            </span>
          </div>
        )}
      </div>

      <p className="mt-5 text-slate-900 font-extrabold text-base tracking-tight leading-tight group-hover:text-[#4B98C8] transition-colors">
        {member.name}
      </p>
      <p className="mt-1 text-slate-500 font-bold text-[10px] uppercase tracking-[0.15em] opacity-70">
        {member.role}
      </p>

      {member.linkedinUrl && (
        <div className="mt-4 flex justify-center opacity-0 translate-y-2 group-hover:opacity-100 group-hover:translate-y-0 transition-all duration-300">
          <a 
            href={member.linkedinUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="w-8 h-8 rounded-full bg-slate-100 flex items-center justify-center text-slate-600 hover:bg-[#0077b5] hover:text-white transition-colors cursor-pointer"
            title="LinkedIn Profile"
          >
            <span className="text-[10px] font-bold">IN</span>
          </a>
        </div>
      )}
    </div>
  );
};

export default CommitteeMemberCard;
