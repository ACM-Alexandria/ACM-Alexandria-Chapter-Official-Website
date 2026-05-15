import { useState } from "react";

const HighBoardCard = ({ member, index }) => {
  const [imageError, setImageError] = useState(false);

  return (
    <div
      className="group bg-white rounded-[2rem] shadow-xl shadow-slate-200/50 overflow-hidden border border-slate-100 hover:-translate-y-2 transition-all duration-500"
      data-aos="fade-up"
      data-aos-delay={index * 100}
    >
      <div className="relative h-64 overflow-hidden bg-slate-100">
        <div className="absolute inset-0 bg-gradient-to-t from-slate-900/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 z-10" />
        
        {member.imageUrl && !imageError ? (
          <img
            src={member.imageUrl}
            alt={member.name}
            className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
            onError={() => setImageError(true)}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#4B98C8]/20 to-[#205E85]/20">
            <span className="text-slate-300 text-6xl font-black select-none">
              {member.name?.charAt(0) || "H"}
            </span>
          </div>
        )}

        <div className="absolute bottom-4 left-0 right-0 px-6 z-20 translate-y-4 opacity-0 group-hover:translate-y-0 group-hover:opacity-100 transition-all duration-500">
          <div className="flex justify-center gap-3">
            {member.linkedinUrl && (
              <a 
                href={member.linkedinUrl}
                target="_blank"
                rel="noopener noreferrer"
                className="w-8 h-8 rounded-full bg-white/20 backdrop-blur-md flex items-center justify-center text-white hover:bg-[#0077b5] transition-colors cursor-pointer"
                title="LinkedIn Profile"
              >
                <span className="text-[10px] font-bold">IN</span>
              </a>
            )}
          </div>
        </div>
      </div>

      <div className="p-6 text-center">
        <h3 className="text-xl font-extrabold text-slate-900 tracking-tight mb-1 group-hover:text-[#4B98C8] transition-colors">
          {member.name}
        </h3>
        <p className="text-sm font-bold text-[#4B98C8] uppercase tracking-widest opacity-80">
          {member.role}
        </p>
      </div>
    </div>
  );
};

export default HighBoardCard;
