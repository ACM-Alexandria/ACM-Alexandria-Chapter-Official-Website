import { useState } from "react";

const ClubCard = ({ club, index, onShowDetails }) => {
  const [imageError, setImageError] = useState(false);

  const handleOpenDetails = () => {
    if (!club?.id) return;
    onShowDetails?.(club.id);
  };

  return (
    <div
      className="group bg-white rounded-[2.5rem] shadow-xl shadow-slate-200/50 overflow-hidden border border-slate-100 hover:-translate-y-2 transition-all duration-500 flex flex-col h-full cursor-pointer"
      data-aos="fade-up"
      data-aos-delay={index * 100}
      onClick={handleOpenDetails}
    >
      {/* Club Image Container */}
      <div className="relative h-72 overflow-hidden bg-slate-100">
        <div className="absolute inset-0 bg-gradient-to-t from-slate-900/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 z-10 flex items-end justify-center p-8">
           <span className="text-white font-bold uppercase tracking-[0.2em] text-xs translate-y-4 group-hover:translate-y-0 transition-transform duration-500">
             View Details
           </span>
        </div>
        
        {club.imageUrl && !imageError ? (
          <img
            src={club.imageUrl}
            alt={club.name}
            className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
            onError={() => setImageError(true)}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#4B98C8]/20 to-[#205E85]/20">
            <span className="text-slate-300 text-7xl font-black select-none">
              {club.name?.charAt(0) || "C"}
            </span>
          </div>
        )}

        <div className="absolute top-4 right-4 z-20">
          <div className="px-3 py-1 bg-white/20 backdrop-blur-md rounded-full border border-white/20 text-white text-[10px] font-bold uppercase tracking-wider">
            Club
          </div>
        </div>
      </div>

      {/* Club Info */}
      <div className="p-8 flex flex-col flex-1">
        <h3 className="text-2xl font-extrabold text-slate-900 tracking-tight mb-3 group-hover:text-[#4B98C8] transition-colors">
          {club.name}
        </h3>
        <p className="text-slate-500 text-base leading-relaxed mb-6 line-clamp-3 font-medium flex-1">
          {club.description || "Join our community and explore new horizons together."}
        </p>

        <div className="flex items-center justify-between pt-6 border-t border-slate-50">
          {club.members ? (
            <div className="flex items-center gap-2">
              <div className="flex -space-x-2">
                {[1, 2, 3].map((i) => (
                  <div key={i} className="w-6 h-6 rounded-full border-2 border-white bg-slate-200" />
                ))}
              </div>
              <span className="text-xs font-bold text-slate-400 uppercase tracking-widest">
                {club.members} Members
              </span>
            </div>
          ) : (
            <span className="text-xs font-bold text-[#4B98C8] uppercase tracking-widest">
              Join Now
            </span>
          )}
          
          <div className="w-10 h-10 rounded-full bg-slate-50 flex items-center justify-center text-slate-400 group-hover:bg-[#4B98C8] group-hover:text-white transition-all duration-300">
            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clipRule="evenodd" />
            </svg>
          </div>
        </div>
      </div>
    </div>
  );
};

export default ClubCard;
