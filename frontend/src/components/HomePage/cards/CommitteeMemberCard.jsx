const CommitteeMemberCard = ({ member }) => {
  return (
    <div className="bg-white hover:shadow-lg hover:shadow-slate-200/50 transition-all duration-300 border border-slate-100 text-center group mx-auto w-[105px] min-[375px]:w-[120px] min-[425px]:w-[135px] sm:w-40 md:w-48 lg:w-64 p-2.5 min-[375px]:p-3 min-[425px]:p-3.5 sm:p-4 md:p-5 lg:p-6 rounded-2xl sm:rounded-3xl">
      <div className="relative mx-auto rounded-full overflow-hidden bg-slate-50 border border-slate-100 flex items-center justify-center w-12 h-12 min-[375px]:w-14 min-[375px]:h-14 min-[425px]:w-16 min-[425px]:h-16 sm:w-16 sm:h-16 lg:w-20 lg:h-20">
        {member.imageUrl ? (
          <img
            src={member.imageUrl}
            alt={member.name}
            className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#4B98C8]/10 to-[#205E85]/10 transition-transform duration-700 group-hover:scale-110">
            <span className="text-slate-400 font-black text-xs min-[375px]:text-sm min-[425px]:text-base sm:text-lg lg:text-2xl">
              {member.name?.charAt(0) || "M"}
            </span>
          </div>
        )}

        {member.linkedinUrl && (
          <div className="hidden lg:block absolute inset-0 bg-gradient-to-t from-slate-900/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-300 z-10" />
        )}

        {member.linkedinUrl && (
          <div className="hidden lg:flex absolute bottom-2 left-0 right-0 z-20 justify-center translate-y-3 opacity-0 group-hover:translate-y-0 group-hover:opacity-100 transition-all duration-300">
            <a 
              href={member.linkedinUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="rounded-full bg-white/20 backdrop-blur-md flex items-center justify-center text-white hover:bg-[#0077b5] transition-colors cursor-pointer w-7.5 h-7.5"
              title="LinkedIn Profile"
            >
              <span className="text-[9px] font-bold">IN</span>
            </a>
          </div>
        )}
      </div>

      <p className="text-slate-900 font-extrabold tracking-tight leading-tight group-hover:text-[#4B98C8] transition-colors mt-2.5 min-[375px]:mt-3 min-[425px]:mt-3.5 sm:mt-4 lg:mt-5 text-[10px] min-[375px]:text-[11px] min-[425px]:text-xs sm:text-xs md:text-sm lg:text-base">
        {member.name}
      </p>
      <p className="text-slate-500 font-bold opacity-70 uppercase tracking-[0.15em] mt-0.5 sm:mt-1 text-[8px] min-[375px]:text-[8.5px] min-[425px]:text-[9px] sm:text-[9px] md:text-[9.5px] lg:text-[10px] truncate">
        {member.role}
      </p>

      {member.linkedinUrl && (
        <div className="flex lg:hidden justify-center mt-2.5">
          <a 
            href={member.linkedinUrl}
            target="_blank"
            rel="noopener noreferrer"
            className="rounded-full bg-slate-100 flex items-center justify-center text-slate-600 hover:bg-[#0077b5] hover:text-white transition-colors cursor-pointer w-5.5 h-5.5 min-[375px]:w-6 min-[375px]:h-6 min-[425px]:w-7 min-[425px]:h-7 sm:w-7 sm:h-7"
            title="LinkedIn Profile"
          >
            <span className="text-[7px] min-[375px]:text-[8px] min-[425px]:text-[9px] sm:text-[8px] font-bold">IN</span>
          </a>
        </div>
      )}
    </div>
  );
};

export default CommitteeMemberCard;
