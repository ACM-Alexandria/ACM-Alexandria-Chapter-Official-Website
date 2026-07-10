import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { FiMic, FiPlay, FiTv } from "react-icons/fi";

const SeasonCard = ({ season, index }) => {
  const [imageError, setImageError] = useState(false);
  const navigate = useNavigate();

  const handleCardClick = () => {
    if (!season?.id) return;
    navigate(`/radio/seasons/${season.id}`);
  };

  const episodesCount = season.episodes ? season.episodes.length : 0;

  return (
    <div
      className="group bg-white rounded-[2.5rem] shadow-xl shadow-slate-200/50 overflow-hidden border border-slate-100 transition-all duration-500 hover:-translate-y-2 cursor-pointer flex flex-col h-full"
      data-aos="fade-up"
      data-aos-delay={index * 100}
      onClick={handleCardClick}
    >
      {/* Cover Image Container */}
      <div className="relative h-64 overflow-hidden bg-slate-100 shrink-0">
        <div className="absolute inset-0 bg-gradient-to-t from-slate-900/70 via-slate-900/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 z-10 flex flex-col items-center justify-center p-8">
          <div className="w-14 h-14 rounded-full bg-white/20 backdrop-blur-md text-white flex items-center justify-center border border-white/30 scale-75 group-hover:scale-100 transition-all duration-500 shadow-lg">
            <FiPlay className="w-6 h-6 fill-white ml-1" />
          </div>
          <span className="text-white text-xs font-extrabold uppercase tracking-[0.2em] mt-4 translate-y-3 group-hover:translate-y-0 transition-transform duration-500">
            Listen to Season
          </span>
        </div>

        {season.imageUrl && !imageError ? (
          <img
            src={season.imageUrl}
            alt={`Season ${season.seasonNumber}`}
            className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
            onError={() => setImageError(true)}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#4B98C8]/20 to-[#205E85]/20">
            <FiMic className="text-slate-300 w-16 h-16 opacity-40 group-hover:rotate-6 transition-transform duration-500" />
          </div>
        )}

        {/* Floating Season Tag */}
        <div className="absolute top-6 left-6 z-20">
          <div className="px-4 py-1.5 bg-[#4B98C8] text-white rounded-full shadow-md text-[10px] font-extrabold uppercase tracking-widest border border-white/25">
            Season {season.seasonNumber}
          </div>
        </div>

        {/* Floating Episode Count Tag */}
        <div className="absolute bottom-6 right-6 z-20 group-hover:opacity-0 transition-opacity duration-300">
          <div className="px-3.5 py-1.5 bg-slate-900/70 backdrop-blur-md text-white rounded-xl text-[10px] font-bold tracking-wider border border-white/10">
            {episodesCount} {episodesCount === 1 ? "Episode" : "Episodes"}
          </div>
        </div>
      </div>

      {/* Description Content */}
      <div className="p-8 flex flex-col flex-1 items-center text-center">
        <h3 className="text-2xl font-extrabold text-slate-900 tracking-tight mb-3 group-hover:text-[#4B98C8] transition-colors leading-tight">
          Season {season.seasonNumber}
        </h3>
        
        <p className="text-slate-500 text-sm leading-relaxed mb-6 font-medium flex-1">
          Tune in to listen to conversations with brilliant minds and expert guests in computing, technology, and programming.
        </p>

        {/* Premium Bottom Action Line */}
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

export default SeasonCard;
