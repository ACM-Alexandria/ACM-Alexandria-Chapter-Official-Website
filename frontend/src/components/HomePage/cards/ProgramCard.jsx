import { useState } from "react";

const ProgramCard = ({ program, index, onShowDetails }) => {
  const [imageError, setImageError] = useState(false);

  const handleOpenDetails = () => {
    if (!program?.id) return;
    onShowDetails?.(program.id);
  };

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
      className="group bg-white rounded-[2.5rem] shadow-xl shadow-slate-200/50 overflow-hidden border border-slate-100 transition-all duration-500 hover:-translate-y-2 cursor-pointer flex flex-col md:flex-row h-full"
      data-aos="fade-up"
      data-aos-delay={index * 100}
      onClick={handleOpenDetails}
    >
      {/* Program Image Container */}
      <div className="relative w-full md:w-2/5 h-64 md:h-auto overflow-hidden bg-slate-100 shrink-0">
        <div className="absolute inset-0 bg-gradient-to-tr from-[#4B98C8]/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 z-10" />
        
        {program.imageUrl && !imageError ? (
          <img
            src={program.imageUrl}
            alt={program.name}
            className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
            onError={() => setImageError(true)}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#4B98C8]/20 to-[#205E85]/20">
            <span className="text-slate-300 text-7xl font-black select-none">
              {program.name?.charAt(0) || "P"}
            </span>
          </div>
        )}

        <div className="absolute top-6 left-6 z-20">
          <div className="px-4 py-1.5 bg-white/90 backdrop-blur-md rounded-full shadow-sm text-[#4B98C8] text-[10px] font-bold uppercase tracking-widest border border-white">
            Program
          </div>
        </div>
      </div>

      {/* Program Info */}
      <div className="p-8 md:p-10 flex flex-col flex-1">
        <h3 className="text-2xl font-extrabold text-slate-900 tracking-tight mb-4 group-hover:text-[#4B98C8] transition-colors leading-tight">
          {program.name}
        </h3>
        <p className="text-slate-500 text-base leading-relaxed mb-8 line-clamp-4 font-medium">
          {program.description || "Advancing computing knowledge through specialized academic and professional programs."}
        </p>

        {/* Program Meta Info */}
        {(program.startDate || program.time) && (
          <div className="mt-auto grid grid-cols-2 gap-4 pt-6 border-t border-slate-50">
            <div className="space-y-1">
              <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Starts</p>
              <p className="text-sm font-extrabold text-slate-700 tracking-tight">
                {program.startDate && program.endDate ? (
                  <>
                    {formatDate(program.startDate)}
                    <span className="block text-slate-400 text-xs font-semibold my-0.5">Till</span>
                    {formatDate(program.endDate)}
                  </>
                ) : program.startDate ? (
                  formatDate(program.startDate)
                ) : (
                  "TBD"
                )}
              </p>
            </div>
            <div className="space-y-1">
              <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Schedule</p>
              <p className="text-sm font-extrabold text-slate-700 tracking-tight line-clamp-2">{program.time || "TBD"}</p>
            </div>
          </div>
        )}

        {/* View Details CTA */}
        <div className="mt-auto pt-6 w-full flex items-center justify-center gap-2 group-hover:gap-4 transition-all duration-300">
          <div className="h-px flex-1 bg-slate-100" />
          <div className="w-10 h-10 rounded-full bg-slate-50 flex items-center justify-center text-slate-400 group-hover:bg-[#4B98C8] group-hover:text-white transition-all duration-300">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="h-px flex-1 bg-slate-100" />
        </div>
      </div>
    </div>
  );
};

export default ProgramCard;
