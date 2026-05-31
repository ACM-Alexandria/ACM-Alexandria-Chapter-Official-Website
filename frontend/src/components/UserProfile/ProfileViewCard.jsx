import React from "react";
import { 
  HiOutlineUser, 
  HiOutlinePhone, 
  HiOutlineAcademicCap, 
  HiOutlineBriefcase, 
  HiOutlineLibrary 
} from "react-icons/hi";

const ProfileViewCard = ({ user, profile }) => {
  return (
    <div className="p-8 md:p-12 flex flex-col gap-10">
      
      <div className="bg-slate-50/70 border border-slate-100 rounded-2xl px-6 py-4">
        <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Registered Email</span>
        <span className="font-bold text-slate-700 break-all text-sm md:text-base">{user?.email}</span>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 animate-[fadeIn_0.5s_ease]">
        
        {/* Name Panel */}
        <div className="bg-slate-50/50 border border-slate-100/60 rounded-[1.5rem] p-6 flex items-center gap-5">
          <div className="w-14 h-14 bg-white border border-slate-100 rounded-2xl shadow-sm flex items-center justify-center text-slate-400 shrink-0">
            <HiOutlineUser className="w-7 h-7" />
          </div>
          <div className="overflow-hidden">
            <span className="text-xs font-bold text-slate-400 block mb-0.5">Full Name</span>
            <span className="font-extrabold text-slate-800 truncate block text-lg leading-tight">
              {profile.name || <em className="text-slate-300 font-semibold not-italic">Not provided</em>}
            </span>
          </div>
        </div>

        {/* Phone Panel */}
        <div className="bg-slate-50/50 border border-slate-100/60 rounded-[1.5rem] p-6 flex items-center gap-5">
          <div className="w-14 h-14 bg-white border border-slate-100 rounded-2xl shadow-sm flex items-center justify-center text-slate-400 shrink-0">
            <HiOutlinePhone className="w-7 h-7" />
          </div>
          <div className="overflow-hidden">
            <span className="text-xs font-bold text-slate-400 block mb-0.5">Phone Number</span>
            <span className="font-extrabold text-slate-800 truncate block text-lg leading-tight">
              {profile.phoneNumber || <em className="text-slate-300 font-semibold not-italic">Not provided</em>}
            </span>
          </div>
        </div>

        {/* Alexandria University Faculty of Engineering Status Panel - Hidden if not filled */}
        {profile.isAlexEngStudent !== null && (
          <div className="bg-slate-50/50 border border-slate-100/60 rounded-[1.5rem] p-6 flex items-center gap-5 col-span-1 md:col-span-2 animate-[fadeIn_0.3s_ease]">
            <div className={`w-14 h-14 bg-white border border-slate-100 rounded-2xl shadow-sm flex items-center justify-center shrink-0 
              ${profile.isAlexEngStudent === true ? "text-[#4B98C8]" : "text-slate-400"}`}>
              <HiOutlineLibrary className="w-7 h-7" />
            </div>
            <div className="overflow-hidden">
              <span className="text-xs font-bold text-slate-400 block mb-0.5">Student in Faculty of Engineering, Alexandria University?</span>
              <span className={`font-extrabold truncate block text-base md:text-lg leading-tight
                ${profile.isAlexEngStudent === true ? "text-emerald-600" : "text-slate-500"}`}>
                {profile.isAlexEngStudent === true ? "Yes" : "No"}
              </span>
            </div>
          </div>
        )}

        {/* Dynamically Show University Information only if flag is true */}
        {profile.isAlexEngStudent === true && (
          <>
            {/* Batch Panel */}
            <div className="bg-slate-50/50 border border-slate-100/60 rounded-[1.5rem] p-6 flex items-center gap-5 col-span-1 animate-[fadeIn_0.3s_ease]">
              <div className="w-14 h-14 bg-white border border-slate-100 rounded-2xl shadow-sm flex items-center justify-center text-slate-400 shrink-0">
                <HiOutlineAcademicCap className="w-7 h-7" />
              </div>
              <div className="overflow-hidden">
                <span className="text-xs font-bold text-slate-400 block mb-0.5">Academic Batch</span>
                <span className="font-extrabold text-slate-800 truncate block text-lg leading-tight">
                  {profile.batch || <em className="text-slate-300 font-semibold not-italic">Not provided</em>}
                </span>
              </div>
            </div>

            {/* Department Panel */}
            <div className="bg-slate-50/50 border border-slate-100/60 rounded-[1.5rem] p-6 flex items-center gap-5 col-span-1 animate-[fadeIn_0.3s_ease]">
              <div className="w-14 h-14 bg-white border border-slate-100 rounded-2xl shadow-sm flex items-center justify-center text-slate-400 shrink-0">
                <HiOutlineBriefcase className="w-7 h-7" />
              </div>
              <div className="overflow-hidden">
                <span className="text-xs font-bold text-slate-400 block mb-0.5">Department</span>
                <span className="font-extrabold text-slate-800 truncate block text-lg leading-tight">
                  {profile.department === "CSED" ? "Computer & Systems (CSED)" : 
                   profile.department === "CCE" ? "Computer & Communications (CCE)" : 
                   profile.department === "OTHER" ? "Other" : 
                   <em className="text-slate-300 font-semibold not-italic">Not provided</em>}
                </span>
              </div>
            </div>
          </>
        )}

      </div>
    </div>
  );
};

export default ProfileViewCard;
