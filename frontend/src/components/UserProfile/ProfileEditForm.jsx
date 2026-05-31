import React from "react";
import { 
  HiOutlineUser, 
  HiOutlinePhone, 
  HiOutlineAcademicCap, 
  HiOutlineBriefcase, 
  HiOutlineX,
  HiOutlineCheck
} from "react-icons/hi";

const ProfileEditForm = ({ 
  user, 
  profile, 
  handleInputChange, 
  handleToggleAlexEng, 
  handleCancelEdit, 
  handleSubmit, 
  submitting 
}) => {
  return (
    <form onSubmit={handleSubmit} className="p-8 md:p-12 flex flex-col gap-10 animate-[fadeIn_0.5s_ease]">
      
      <div className="bg-slate-50/70 border border-slate-100 rounded-2xl px-6 py-4">
        <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Registered Email</span>
        <span className="font-bold text-slate-700 break-all text-sm md:text-base">{user?.email}</span>
      </div>

      {/* Core Basic Fields */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
        
        {/* Full Name Field */}
        <div className="space-y-2 group">
          <label className="text-[13px] font-bold text-slate-800 flex items-center gap-2 ml-1">
            <HiOutlineUser className="w-4 h-4 text-slate-400 group-focus-within:text-[#4B98C8] transition-colors" />
            Full Name
          </label>
          <input
            type="text"
            name="name"
            value={profile.name}
            onChange={handleInputChange}
            placeholder="e.g. John Doe"
            className="w-full h-14 px-5 bg-slate-50 border border-slate-100 rounded-2xl outline-none text-slate-800 font-semibold text-[15px] transition-all placeholder:text-slate-400 focus:border-[#4B98C8]/30 focus:bg-white focus:ring-4 focus:ring-blue-50"
            required
          />
        </div>

        {/* Phone Number Field */}
        <div className="space-y-2 group">
          <label className="text-[13px] font-bold text-slate-800 flex items-center gap-2 ml-1">
            <HiOutlinePhone className="w-4 h-4 text-slate-400 group-focus-within:text-[#4B98C8] transition-colors" />
            Phone Number
          </label>
          <input
            type="tel"
            name="phoneNumber"
            value={profile.phoneNumber}
            onChange={handleInputChange}
            placeholder="e.g. 01XXXXXXXXXX"
            pattern="01[0-9]{9}"
            maxLength="11"
            title="Phone number must be exactly 11 digits starting with 01"
            className="w-full h-14 px-5 bg-slate-50 border border-slate-100 rounded-2xl outline-none text-slate-800 font-semibold text-[15px] transition-all placeholder:text-slate-400 focus:border-[#4B98C8]/30 focus:bg-white focus:ring-4 focus:ring-blue-50"
            required
          />
        </div>

      </div>

      {/* Alexandria Engineering Student Checker Card */}
      <div className="bg-slate-50/80 border border-slate-100 rounded-3xl p-6 flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
        <div>
          <h4 className="font-black text-slate-800 text-[15px]">Alexandria Engineering Student?</h4>
          <p className="text-xs font-medium text-slate-500 mt-0.5">
            Check this box to unlock academic department and graduation batch fields.
          </p>
        </div>
        
        <div className="flex items-center bg-white border border-slate-200 rounded-2xl p-1 shadow-sm">
          <button
            type="button"
            onClick={() => handleToggleAlexEng(true)}
            className={`px-5 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5
              ${profile.isAlexEngStudent === true || profile.isAlexEngStudent === null
                ? "bg-gradient-to-r from-[#4B98C8] to-[#205E85] text-white shadow-md" 
                : "text-slate-500 hover:text-slate-800 hover:bg-slate-50"
              }`}
          >
            {(profile.isAlexEngStudent === true || profile.isAlexEngStudent === null) && <HiOutlineCheck className="w-3.5 h-3.5" />}
            Yes
          </button>
          <button
            type="button"
            onClick={() => handleToggleAlexEng(false)}
            className={`px-5 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5
              ${profile.isAlexEngStudent === false
                ? "bg-slate-800 text-white shadow-md" 
                : "text-slate-500 hover:text-slate-800 hover:bg-slate-50"
              }`}
          >
            {profile.isAlexEngStudent === false && <HiOutlineCheck className="w-3.5 h-3.5" />}
            No
          </button>
        </div>
      </div>

      {/* Conditional Fields: Displayed Only If Checkbox is TRUE */}
      {profile.isAlexEngStudent === true && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6 border-t border-slate-100 pt-8 animate-[fadeIn_0.4s_ease]">
          
          {/* Academic Batch Field */}
          <div className="space-y-2 group col-span-1">
            <label className="text-[13px] font-bold text-slate-800 flex items-center gap-2 ml-1">
              <HiOutlineAcademicCap className="w-4 h-4 text-slate-400 group-focus-within:text-[#4B98C8] transition-colors" />
              Batch
            </label>
            <div className="relative">
              <select
                name="batch"
                value={profile.batch}
                onChange={handleInputChange}
                required={profile.isAlexEngStudent === true}
                className="w-full h-14 px-5 bg-slate-50 border border-slate-100 rounded-2xl outline-none text-slate-800 font-semibold text-[15px] transition-all focus:border-[#4B98C8]/30 focus:bg-white focus:ring-4 focus:ring-blue-50 appearance-none cursor-pointer"
              >
                <option value="">-- Select Batch --</option>
                <option value="2026">2026</option>
                <option value="2027">2027</option>
                <option value="2028">2028</option>
                <option value="2028++">2028++</option>
                <option value="2029">2029</option>
                <option value="2030">2030</option>
              </select>
              <div className="absolute inset-y-0 right-5 flex items-center pointer-events-none text-slate-400">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </div>
            </div>
          </div>

          {/* Department Select Field */}
          <div className="space-y-2 group col-span-1">
            <label className="text-[13px] font-bold text-slate-800 flex items-center gap-2 ml-1">
              <HiOutlineBriefcase className="w-4 h-4 text-slate-400 group-focus-within:text-[#4B98C8] transition-colors" />
              Department
            </label>
            <div className="relative">
              <select
                name="department"
                value={profile.department}
                onChange={handleInputChange}
                required={profile.isAlexEngStudent === true}
                className="w-full h-14 px-5 bg-slate-50 border border-slate-100 rounded-2xl outline-none text-slate-800 font-semibold text-[15px] transition-all focus:border-[#4B98C8]/30 focus:bg-white focus:ring-4 focus:ring-blue-50 appearance-none cursor-pointer"
              >
                <option value="">-- Select Department --</option>
                <option value="CSED">Computer & Systems (CSED)</option>
                <option value="CCE">Computer & Communications (CCE)</option>
                <option value="OTHER">Other</option>
              </select>
              <div className="absolute inset-y-0 right-5 flex items-center pointer-events-none text-slate-400">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                  <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                </svg>
              </div>
            </div>
          </div>

        </div>
      )}

      <div className="h-px bg-slate-100/80 w-full" />

      <div className="flex justify-end items-center gap-4 pt-2">
        <button
          type="button"
          onClick={handleCancelEdit}
          disabled={submitting}
          className="h-14 px-8 flex items-center gap-2 rounded-2xl font-bold text-sm text-slate-600 transition-all hover:bg-slate-50 disabled:opacity-50 active:scale-95"
        >
          <HiOutlineX className="w-5 h-5 text-slate-400" />
          Discard Changes
        </button>

        <button
          type="submit"
          disabled={submitting}
          className={`
            h-14 px-12 rounded-2xl font-extrabold text-sm uppercase tracking-widest text-white transition-all duration-300 flex items-center justify-center gap-3 shadow-xl
            ${submitting 
              ? "bg-slate-300 cursor-not-allowed shadow-none" 
              : "bg-gradient-to-r from-[#4B98C8] to-[#205E85] hover:-translate-y-1 hover:shadow-[#4B98C8]/25 hover:shadow-2xl active:scale-95"
            }
          `}
        >
          {submitting ? (
            <>
              <span className="w-5 h-5 rounded-full border-2 border-white/30 border-t-white animate-spin" />
              Saving...
            </>
          ) : (
            <>
              Save Changes
            </>
          )}
        </button>
      </div>
    </form>
  );
};

export default ProfileEditForm;
