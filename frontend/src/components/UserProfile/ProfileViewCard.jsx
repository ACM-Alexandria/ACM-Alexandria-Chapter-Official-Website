import React from "react";
import { 
  HiOutlineUser, 
  HiOutlinePhone, 
  HiOutlineAcademicCap, 
  HiOutlineBriefcase, 
  HiOutlineLibrary,
  HiOutlineLink
} from "react-icons/hi";

const getRoleDisplay = (role) => {
  switch (role) {
    case "SUPER_ADMIN":
      return { label: "System Administrator" };
    case "ACM_HIGH_BOARD":
      return { label: "High Board" };
    case "ACM_COMMITTEE_BOARD":
      return { label: "Committee Board" };
    case "ACM_CLUB_BOARD":
      return { label: "Club Board" };
    case "ACM_MEMBER":
      return { label: "ACM Member" };
    default:
      return { label: "User" };
  }
};

const ProfileViewCard = ({ user, profile }) => {
  const roleDisplay = getRoleDisplay(profile.role);

  return (
    <div className="p-6 md:p-12 flex flex-col gap-8 md:gap-10 bg-white dark:bg-slate-900">
      
      {/* Header Banner - Clean and Minimal */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6 border-b border-slate-200 dark:border-slate-800 pb-6 md:pb-8">
        
        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-5 sm:gap-6 w-full md:w-auto">
          {profile.profileImageUrl && profile.role !== "USER" && profile.role !== "ACM_MEMBER" ? (
            <img src={profile.profileImageUrl} alt="Profile" className="w-20 h-20 md:w-24 md:h-24 rounded-full object-cover border border-slate-200 dark:border-slate-700" />
          ) : (
            <div className="w-20 h-20 md:w-24 md:h-24 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center text-[#4B98C8] text-3xl font-semibold border border-slate-200 dark:border-slate-700">
              {user?.email?.[0].toUpperCase() || "U"}
            </div>
          )}
          
          <div className="flex flex-col gap-1 w-full sm:w-auto">
            <h2 className="text-xl sm:text-2xl md:text-3xl font-bold text-slate-900 dark:text-slate-100 tracking-tight break-words">
              {profile.name || "ACM Member"}
            </h2>
            <span className="text-sm text-slate-500 dark:text-slate-400 break-all">{user?.email}</span>
          </div>
        </div>
        
        {/* Role Badge - Simple Pill */}
        <div className="flex items-center px-4 py-2 rounded-full bg-blue-50 dark:bg-blue-900/30 border border-blue-100 dark:border-blue-800 self-start md:self-auto mt-2 md:mt-0">
          <span className="font-semibold text-sm text-[#4B98C8] dark:text-blue-400">
            {roleDisplay.label}
          </span>
        </div>
      </div>

      {/* Information Grid - Clean Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        
        {/* Phone Panel */}
        <div className="bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 rounded-xl p-5 md:p-6 flex items-start gap-3 md:gap-4">
          <div className="text-slate-400 dark:text-slate-500 mt-1 shrink-0">
            <HiOutlinePhone className="w-5 h-5 md:w-6 md:h-6" />
          </div>
          <div className="overflow-hidden w-full">
            <span className="text-[10px] md:text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider block mb-1">Phone Number</span>
            <span className="font-medium text-slate-900 dark:text-slate-200 text-sm md:text-base break-all">
              {profile.phoneNumber || <span className="text-slate-400 italic">Not provided</span>}
            </span>
          </div>
        </div>

        {/* LinkedIn Panel */}
        <div className="bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 rounded-xl p-5 md:p-6 flex items-start gap-3 md:gap-4">
          <div className="text-slate-400 dark:text-slate-500 mt-1 shrink-0">
            <HiOutlineLink className="w-5 h-5 md:w-6 md:h-6" />
          </div>
          <div className="overflow-hidden w-full">
            <span className="text-[10px] md:text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider block mb-1">Professional Network</span>
            {profile.linkedinUrl ? (
              <a href={profile.linkedinUrl} target="_blank" rel="noreferrer" className="font-medium text-[#4B98C8] hover:underline truncate block text-sm md:text-base">
                {profile.linkedinUrl}
              </a>
            ) : (
              <span className="font-medium text-slate-900 dark:text-slate-200 text-base block">
                <span className="text-slate-400 italic">Not connected</span>
              </span>
            )}
          </div>
        </div>

        {/* Alexandria University Status */}
        {profile.isAlexEngStudent !== null && (
          <div className="bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 rounded-xl p-5 md:p-6 flex items-start gap-3 md:gap-4 col-span-1 md:col-span-2">
            <div className="text-slate-400 dark:text-slate-500 mt-1 shrink-0">
              <HiOutlineLibrary className="w-5 h-5 md:w-6 md:h-6" />
            </div>
            <div className="overflow-hidden w-full">
              <span className="text-[10px] md:text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider block mb-1">Alexandria University, Faculty of Engineering</span>
              <span className="font-medium text-slate-900 dark:text-slate-200 text-sm md:text-base block">
                {profile.isAlexEngStudent ? "Registered Student" : "Not a student"}
              </span>
            </div>
          </div>
        )}

        {/* Dynamic Academic Details */}
        {profile.isAlexEngStudent === true && (
          <>
            <div className="bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 rounded-xl p-5 md:p-6 flex items-start gap-3 md:gap-4">
              <div className="text-slate-400 dark:text-slate-500 mt-1 shrink-0">
                <HiOutlineAcademicCap className="w-5 h-5 md:w-6 md:h-6" />
              </div>
              <div className="overflow-hidden w-full">
                <span className="text-[10px] md:text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider block mb-1">Academic Batch</span>
                <span className="font-medium text-slate-900 dark:text-slate-200 text-sm md:text-base block break-words">
                  {profile.batch || <span className="text-slate-400 italic">Not provided</span>}
                </span>
              </div>
            </div>

            <div className="bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 rounded-xl p-5 md:p-6 flex items-start gap-3 md:gap-4">
              <div className="text-slate-400 dark:text-slate-500 mt-1 shrink-0">
                <HiOutlineBriefcase className="w-5 h-5 md:w-6 md:h-6" />
              </div>
              <div className="overflow-hidden w-full">
                <span className="text-[10px] md:text-xs font-semibold text-slate-500 dark:text-slate-400 uppercase tracking-wider block mb-1">Department</span>
                <span className="font-medium text-slate-900 dark:text-slate-200 text-sm md:text-base block break-words">
                  {profile.department === "CSED" ? "Computer & Systems" : 
                   profile.department === "CCE" ? "Computer & Comms" : 
                   profile.department === "OTHER" ? "Other" : 
                   <span className="text-slate-400 italic">Not provided</span>}
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
