import { useState } from "react";

const ProgramCard = ({ program, index }) => {
  const [imageError, setImageError] = useState(false);

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
      className="group bg-white rounded-[2.5rem] shadow-xl shadow-slate-200/50 overflow-hidden border border-slate-100 transition-all duration-500 hover:-translate-y-2 flex flex-col md:flex-row h-full"
      data-aos="fade-up"
      data-aos-delay={index * 100}
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
        {program.eventTime && (
          <div className="mt-auto grid grid-cols-2 gap-4 pt-6 border-t border-slate-50">
            <div className="space-y-1">
              <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Date</p>
              <p className="text-sm font-extrabold text-slate-700 tracking-tight">{formatDate(program.eventTime)}</p>
            </div>
            <div className="space-y-1">
              <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest">Time</p>
              <p className="text-sm font-extrabold text-slate-700 tracking-tight">{formatTime(program.eventTime)}</p>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default ProgramCard;
