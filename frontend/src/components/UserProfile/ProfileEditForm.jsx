import React from "react";
import { 
  HiOutlineUser, 
  HiOutlinePhone, 
  HiOutlineAcademicCap, 
  HiOutlineBriefcase, 
  HiOutlineX,
  HiOutlineCheck,
  HiOutlineLink,
  HiOutlinePhotograph
} from "react-icons/hi";
import { useState } from "react";

const ProfileEditForm = ({ 
  user, 
  profile, 
  handleInputChange, 
  handleToggleAlexEng, 
  handleCancelEdit, 
  handleSubmit, 
  submitting,
  onImageUpload
}) => {
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState("");

  const handleImageChange = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;

    try {
      setUploading(true);
      setUploadError("");
      await onImageUpload(file);
    } catch (err) {
      setUploadError("Failed to upload image. Please try again.");
    } finally {
      setUploading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="p-6 md:p-12 flex flex-col gap-6 md:gap-8 bg-white dark:bg-slate-900">
      
      {/* Header and Image Upload */}
      <div className="flex flex-col md:flex-row justify-between items-start md:items-center gap-6 border-b border-slate-200 dark:border-slate-800 pb-6 md:pb-8">
        <div className="flex flex-col sm:flex-row items-start sm:items-center gap-5 sm:gap-6 w-full md:w-auto">
          {profile.profileImageUrl && profile.role !== "USER" && profile.role !== "ACM_MEMBER" ? (
            <img src={profile.profileImageUrl} alt="Profile" className="w-20 h-20 md:w-24 md:h-24 rounded-full object-cover border border-slate-200 dark:border-slate-700" />
          ) : (
            <div className="w-20 h-20 md:w-24 md:h-24 rounded-full bg-slate-100 dark:bg-slate-800 flex items-center justify-center text-[#4B98C8] text-3xl font-semibold border border-slate-200 dark:border-slate-700">
              {user?.email?.[0].toUpperCase() || "U"}
            </div>
          )}
          <div className="flex flex-col gap-2 w-full sm:w-auto">
            <span className="text-sm text-slate-500 dark:text-slate-400 break-all">{user?.email}</span>
            {profile.role !== "USER" && profile.role !== "ACM_MEMBER" && (
              <>
                <label className="cursor-pointer bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-600 px-4 py-2 rounded-md text-sm font-medium shadow-sm hover:bg-slate-50 dark:hover:bg-slate-700 transition-colors w-fit text-center">
                  {uploading ? "Uploading..." : "Change Image"}
                  <input type="file" className="hidden" accept="image/*" onChange={handleImageChange} disabled={uploading || submitting} />
                </label>
                {uploadError && <span className="text-xs text-red-500">{uploadError}</span>}
              </>
            )}
          </div>
        </div>
      </div>

      {/* Core Basic Fields */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
        
        {/* Full Name Field */}
        <div className="space-y-1.5">
          <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 flex items-center gap-2">
            Full Name
          </label>
          <input
            type="text"
            name="name"
            value={profile.name}
            onChange={handleInputChange}
            placeholder="e.g. John Doe"
            className="w-full h-11 px-4 bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-md outline-none text-slate-900 dark:text-slate-100 text-sm transition-all focus:border-[#4B98C8] focus:ring-1 focus:ring-[#4B98C8]"
            required
          />
        </div>

        {/* Phone Number Field */}
        <div className="space-y-1.5">
          <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 flex items-center gap-2">
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
            className="w-full h-11 px-4 bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-md outline-none text-slate-900 dark:text-slate-100 text-sm transition-all focus:border-[#4B98C8] focus:ring-1 focus:ring-[#4B98C8]"
            required
          />
        </div>

        {/* LinkedIn URL Field */}
        <div className="space-y-1.5 col-span-1 md:col-span-2">
          <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 flex items-center gap-2">
            LinkedIn URL <span className="text-slate-400 font-normal">(Optional)</span>
          </label>
          <input
            type="url"
            name="linkedinUrl"
            value={profile.linkedinUrl}
            onChange={handleInputChange}
            placeholder="https://linkedin.com/in/username"
            className="w-full h-11 px-4 bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-md outline-none text-slate-900 dark:text-slate-100 text-sm transition-all focus:border-[#4B98C8] focus:ring-1 focus:ring-[#4B98C8]"
          />
        </div>

      </div>

      <hr className="border-slate-200 dark:border-slate-800 my-2" />

      {/* Alexandria Engineering Student Checker Card */}
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4 sm:gap-6">
        <div>
          <h4 className="font-semibold text-slate-900 dark:text-slate-100 text-sm">Alexandria Engineering Student?</h4>
          <p className="text-xs sm:text-sm text-slate-500 dark:text-slate-400 mt-1">
            Check this to provide your academic department and graduation batch.
          </p>
        </div>
        
        <div className="flex items-center gap-2 self-start sm:self-auto">
          <button
            type="button"
            onClick={() => handleToggleAlexEng(true)}
            className={`px-4 py-2 rounded-md text-sm font-medium transition-colors flex items-center gap-1.5 border
              ${profile.isAlexEngStudent === true || profile.isAlexEngStudent === null
                ? "bg-[#4B98C8] text-white border-[#4B98C8]" 
                : "bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 border-slate-300 dark:border-slate-600 hover:bg-slate-50 dark:hover:bg-slate-700"
              }`}
          >
            {(profile.isAlexEngStudent === true || profile.isAlexEngStudent === null) && <HiOutlineCheck className="w-4 h-4" />}
            Yes
          </button>
          <button
            type="button"
            onClick={() => handleToggleAlexEng(false)}
            className={`px-4 py-2 rounded-md text-sm font-medium transition-colors flex items-center gap-1.5 border
              ${profile.isAlexEngStudent === false
                ? "bg-slate-800 text-white border-slate-800" 
                : "bg-white dark:bg-slate-800 text-slate-700 dark:text-slate-300 border-slate-300 dark:border-slate-600 hover:bg-slate-50 dark:hover:bg-slate-700"
              }`}
          >
            {profile.isAlexEngStudent === false && <HiOutlineCheck className="w-4 h-4" />}
            No
          </button>
        </div>
      </div>

      {/* Conditional Fields: Displayed Only If Checkbox is TRUE */}
      {profile.isAlexEngStudent === true && (
        <div className="grid grid-cols-1 md:grid-cols-2 gap-x-8 gap-y-6">
          
          {/* Academic Batch Field */}
          <div className="space-y-1.5 col-span-1">
            <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 flex items-center gap-2">
              Batch
            </label>
            <select
              name="batch"
              value={profile.batch}
              onChange={handleInputChange}
              required={profile.isAlexEngStudent === true}
              className="w-full h-11 px-4 bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-md outline-none text-slate-900 dark:text-slate-100 text-sm transition-all focus:border-[#4B98C8] focus:ring-1 focus:ring-[#4B98C8] bg-none"
            >
              <option value="">-- Select Batch --</option>
              <option value="2026">2026</option>
              <option value="2027">2027</option>
              <option value="2028">2028</option>
              <option value="2028++">2028++</option>
              <option value="2029">2029</option>
              <option value="2030">2030</option>
            </select>
          </div>

          {/* Department Select Field */}
          <div className="space-y-1.5 col-span-1">
            <label className="text-sm font-semibold text-slate-700 dark:text-slate-300 flex items-center gap-2">
              Department
            </label>
            <select
              name="department"
              value={profile.department}
              onChange={handleInputChange}
              required={profile.isAlexEngStudent === true}
              className="w-full h-11 px-4 bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-md outline-none text-slate-900 dark:text-slate-100 text-sm transition-all focus:border-[#4B98C8] focus:ring-1 focus:ring-[#4B98C8] bg-none"
            >
              <option value="">-- Select Department --</option>
              <option value="CSED">Computer & Systems (CSED)</option>
              <option value="CCE">Computer & Communications (CCE)</option>
              <option value="OTHER">Other</option>
            </select>
          </div>

        </div>
      )}

      <hr className="border-slate-200 dark:border-slate-800 mt-2 mb-2" />

      {/* Form Actions */}
      <div className="flex flex-col sm:flex-row justify-end items-stretch sm:items-center gap-3">
        <button
          type="button"
          onClick={handleCancelEdit}
          disabled={submitting}
          className="h-10 px-5 rounded-md font-medium text-sm text-slate-600 dark:text-slate-300 bg-white dark:bg-slate-800 border border-slate-300 dark:border-slate-600 transition-colors hover:bg-slate-50 dark:hover:bg-slate-700 disabled:opacity-50 w-full sm:w-auto"
        >
          Cancel
        </button>

        <button
          type="submit"
          disabled={submitting}
          className={`
            h-10 px-6 rounded-md font-medium text-sm text-white transition-colors flex items-center justify-center gap-2 w-full sm:w-auto
            ${submitting 
              ? "bg-[#4B98C8]/70 cursor-not-allowed" 
              : "bg-[#4B98C8] hover:bg-[#3d7da6]"
            }
          `}
        >
          {submitting ? (
            <>
              <span className="w-4 h-4 rounded-full border-2 border-white/30 border-t-white animate-spin" />
              Saving...
            </>
          ) : (
            "Save Changes"
          )}
        </button>
      </div>
    </form>
  );
};

export default ProfileEditForm;
