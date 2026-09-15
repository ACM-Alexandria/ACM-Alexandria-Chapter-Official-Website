import { useState } from "react";
import { useUserProfile } from "../../../hooks/useUserProfile";

const HighBoardCard = ({ member, index }) => {
  const [imageError, setImageError] = useState(false);
  const userId = member.user?.id || member.userId || member.user;
  const { profile, loading } = useUserProfile(userId);
  
  const name = profile?.name || member.user?.name || (loading ? "Loading..." : "Unknown Member");
  const imageUrl = profile?.profile_image_url || member.user?.profile_image_url || null;
  const linkedinUrl = profile?.linkedin_url || member.user?.linkedin_url || null;

  return (
    <div
      className="group bg-white dark:bg-slate-800 rounded-[2rem] shadow-xl shadow-slate-200/50 dark:shadow-slate-950/40 overflow-hidden border border-slate-100 dark:border-slate-700 hover:-translate-y-2 transition-all duration-500"
      data-aos="fade-up"
      data-aos-delay={index * 100}
    >
      <div className="relative h-64 overflow-hidden bg-slate-100 dark:bg-slate-700">
        <div className="hidden lg:block absolute inset-0 bg-gradient-to-t from-slate-900/60 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 z-10" />
        
        {imageUrl && !imageError ? (
          <img
            src={imageUrl}
            alt={name}
            className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
            onError={() => setImageError(true)}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#4B98C8]/20 to-[#205E85]/20">
            <span className="text-slate-300 dark:text-slate-500 text-6xl font-black select-none">
              {loading ? "..." : (name?.charAt(0) || "H")}
            </span>
          </div>
        )}

        <div className="hidden lg:flex absolute bottom-4 left-0 right-0 px-6 z-20 translate-y-4 opacity-0 group-hover:translate-y-0 group-hover:opacity-100 transition-all duration-500 justify-center">
          <div className="flex justify-center gap-3">
            {linkedinUrl && (
              <a 
                href={linkedinUrl}
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
        <h3 className="text-xl font-extrabold text-slate-900 dark:text-slate-100 tracking-tight mb-1 group-hover:text-[#4B98C8] transition-colors">
          {name}
        </h3>
        <p className="text-sm font-bold text-[#4B98C8] uppercase tracking-widest opacity-80">
          {member.role}
        </p>
        {linkedinUrl && (
          <div className="flex lg:hidden justify-center mt-3">
            <a 
              href={linkedinUrl}
              target="_blank"
              rel="noopener noreferrer"
              className="w-8 h-8 rounded-full bg-slate-100 dark:bg-slate-700 flex items-center justify-center text-slate-600 dark:text-slate-200 hover:bg-[#0077b5] hover:text-white transition-colors cursor-pointer"
              title="LinkedIn Profile"
            >
              <span className="text-[10px] font-bold">IN</span>
            </a>
          </div>
        )}
      </div>
    </div>
  );
};

export default HighBoardCard;
