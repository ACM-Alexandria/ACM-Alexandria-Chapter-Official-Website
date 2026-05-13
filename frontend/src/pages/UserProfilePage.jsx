import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { fetchUserProfile, updateUserProfile } from "../services/userService";
import Navbar from "../components/HomePage/Navbar";
import { 
  HiOutlineUser, 
  HiOutlinePhone, 
  HiOutlineAcademicCap, 
  HiOutlineBriefcase, 
  HiOutlineCheckCircle,
  HiOutlineExclamationCircle,
  HiOutlinePencilAlt,
  HiOutlineX,
  HiOutlineCheck,
  HiOutlineLibrary
} from "react-icons/hi";

const UserProfilePage = () => {
  const { user } = useAuth();
  const navigate = useNavigate();

  const [profile, setProfile] = useState({
    name: "",
    phoneNumber: "",
    isAlexEngStudent: null,
    department: "",
    batch: ""
  });

  const [backupProfile, setBackupProfile] = useState(null);
  const [isEditing, setIsEditing] = useState(false);

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  // Fetch profile data from backend
  useEffect(() => {
    if (!user?.id) return;

    const loadProfile = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await fetchUserProfile(user.id);
        // Carefully map from database JSON keys defined by @JsonProperty
        const profileData = {
          name: data.name || "",
          phoneNumber: data.phone_number || "",
          isAlexEngStudent: data.is_alex_eng_student ?? null,
          department: data.department || "",
          batch: data.batch || ""
        };
        setProfile(profileData);
        setBackupProfile(profileData);
      } catch (err) {
        console.error("Profile fetch failure:", err);
        setError("Failed to load your profile. Please try again later.");
      } finally {
        setLoading(false);
      }
    };

    loadProfile();
  }, [user?.id]);

  // Auto-hide success banners
  useEffect(() => {
    if (success) {
      const timer = setTimeout(() => setSuccess(false), 4000);
      return () => clearTimeout(timer);
    }
  }, [success]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    if (name === "phoneNumber") {
      // Filter out non-digit inputs and limit strictly to 11 chars
      const cleanVal = value.replace(/\D/g, "").substring(0, 11);
      setProfile((prev) => ({ ...prev, [name]: cleanVal }));
      return;
    }
    setProfile((prev) => ({ ...prev, [name]: value }));
  };

  const handleToggleAlexEng = (val) => {
    setProfile((prev) => ({
      ...prev,
      isAlexEngStudent: val,
      // Clear university specific values if turned off
      department: val ? prev.department : "",
      batch: val ? prev.batch : ""
    }));
  };

  const handleCancelEdit = () => {
    if (backupProfile) {
      setProfile(backupProfile); // Restore original state
    }
    setError(null);
    setIsEditing(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!user?.id) return;

    // Strictly enforce: Exactly 11 digits, starting with 01
    const phoneRegex = /^01[0-9]{9}$/;
    if (!phoneRegex.test(profile.phoneNumber)) {
      setError("Invalid Phone Number: Must be exactly 11 digits starting with '01'.");
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }

    // Enforce Batch and Department if student flag is checked
    if (profile.isAlexEngStudent === true) {
      if (!profile.batch) {
        setError("Validation Error: Please select your Academic Batch.");
        window.scrollTo({ top: 0, behavior: 'smooth' });
        return;
      }
      if (!profile.department) {
        setError("Validation Error: Please select your Department.");
        window.scrollTo({ top: 0, behavior: 'smooth' });
        return;
      }
    }

    setSubmitting(true);
    setError(null);
    setSuccess(false);

    // Compose payload strictly matching @JsonProperty backend specifications
    const payload = {
      name: profile.name,
      phone_number: profile.phoneNumber,
      is_alex_eng_student: profile.isAlexEngStudent,
      department: profile.isAlexEngStudent ? (profile.department || null) : null,
      batch: profile.isAlexEngStudent ? (profile.batch || null) : null
    };

    try {
      await updateUserProfile(user.id, payload);
      setBackupProfile(profile); // Commit local updates to persistent backup
      setSuccess(true);
      setIsEditing(false);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (err) {
      console.error("Profile update failure:", err);
      setError(err.message || "Unable to save your changes. Please check your inputs.");
    } finally {
      setSubmitting(false);
    }
  };



  return (
    <div className="min-h-screen bg-slate-50/50 selection:bg-blue-100">
      <Navbar />

      <main className="pt-[120px] pb-20 px-4 sm:px-6 md:px-12 max-w-5xl mx-auto">
        
        {/* Page Header Section */}
        <div className="mb-10" style={{ animation: "fadeInDown 0.6s ease-out both" }}>
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
            <div className="flex items-start sm:items-center gap-4">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-[#4B98C8] to-[#205E85] shadow-lg shadow-blue-200/50 flex items-center justify-center text-white text-2xl font-black tracking-wider shrink-0">
                {user?.email?.[0].toUpperCase() || "U"}
              </div>
              <div>
                <h1 className="text-3xl font-black text-slate-900 tracking-tight">Account Profile</h1>
                <p className="text-slate-500 font-medium text-sm mt-1">
                  Manage your official registration details and contact preferences.
                </p>
              </div>
            </div>

            {!loading && !isEditing && (
              <button
                onClick={() => setIsEditing(true)}
                className="px-6 py-3 flex items-center gap-2 rounded-2xl border border-slate-200 bg-white shadow-sm text-slate-700 font-bold text-sm transition-all hover:bg-slate-50 hover:border-slate-300 hover:shadow-md hover:-translate-y-0.5 active:translate-y-0"
              >
                <HiOutlinePencilAlt className="w-5 h-5 text-[#4B98C8]" />
                Edit Information
              </button>
            )}
          </div>
        </div>

        {/* Notification Alerts */}
        {success && (
          <div className="mb-6 flex items-center gap-3 bg-emerald-50 border border-emerald-200 text-emerald-800 rounded-2xl px-5 py-4 animate-[fadeIn_0.4s_ease] shadow-sm">
            <HiOutlineCheckCircle className="w-6 h-6 text-emerald-500 shrink-0" />
            <p className="font-bold text-sm">Profile saved successfully! Your information is now fully updated.</p>
          </div>
        )}

        {error && (
          <div className="mb-6 flex items-center gap-3 bg-rose-50 border border-rose-200 text-rose-800 rounded-2xl px-5 py-4 animate-[fadeIn_0.4s_ease] shadow-sm">
            <HiOutlineExclamationCircle className="w-6 h-6 text-rose-500 shrink-0" />
            <p className="font-bold text-sm">{error}</p>
          </div>
        )}

        {/* Master Profile Display Card */}
        <div 
          className="bg-white rounded-[2.5rem] border border-slate-100 shadow-xl shadow-slate-200/30 overflow-hidden transition-all duration-500"
          style={{ animation: "fadeInUp 0.8s cubic-bezier(0.22, 1, 0.36, 1) 0.1s both" }}
        >
          
          {loading ? (
            /* Skeleton Skeleton Placeholders */
            <div className="p-8 md:p-12 space-y-8 animate-pulse">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                {[1, 2, 3, 4].map((i) => (
                  <div key={i} className="space-y-2">
                    <div className="h-4 bg-slate-100 rounded w-24" />
                    <div className="h-14 bg-slate-50 rounded-2xl" />
                  </div>
                ))}
              </div>
              <div className="h-14 bg-slate-100 rounded-2xl w-full md:w-40 ml-auto mt-8" />
            </div>
          ) : !isEditing ? (
            /* ================= VIEW-ONLY PRESENTATION ================= */
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

                {/* Alexandria University Faculty of Engineering Status Panel */}
                <div className="bg-slate-50/50 border border-slate-100/60 rounded-[1.5rem] p-6 flex items-center gap-5 col-span-1 md:col-span-2">
                  <div className={`w-14 h-14 bg-white border border-slate-100 rounded-2xl shadow-sm flex items-center justify-center shrink-0 
                    ${profile.isAlexEngStudent === true ? "text-[#4B98C8]" : "text-slate-400"}`}>
                    <HiOutlineLibrary className="w-7 h-7" />
                  </div>
                  <div className="overflow-hidden">
                    <span className="text-xs font-bold text-slate-400 block mb-0.5">Student in Faculty of Engineering, Alexandria University?</span>
                    <span className={`font-extrabold truncate block text-base md:text-lg leading-tight
                      ${profile.isAlexEngStudent === true ? "text-emerald-600" : "text-slate-500"}`}>
                      {profile.isAlexEngStudent === true ? "Yes" : 
                       profile.isAlexEngStudent === false ? "No" : 
                       <em className="text-slate-300 font-semibold not-italic">Not specified</em>}
                    </span>
                  </div>
                </div>

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
                           <em className="text-slate-300 font-semibold not-italic">Not provided</em>}
                        </span>
                      </div>
                    </div>
                  </>
                )}

              </div>



            </div>
          ) : (
            /* ================= ACTIVE EDITING WORKSPACE ================= */
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
                      ${profile.isAlexEngStudent === true 
                        ? "bg-gradient-to-r from-[#4B98C8] to-[#205E85] text-white shadow-md" 
                        : "text-slate-500 hover:text-slate-800 hover:bg-slate-50"
                      }`}
                  >
                    {profile.isAlexEngStudent === true && <HiOutlineCheck className="w-3.5 h-3.5" />}
                    Yes
                  </button>
                  <button
                    type="button"
                    onClick={() => handleToggleAlexEng(false)}
                    className={`px-5 py-2.5 rounded-xl text-xs font-bold transition-all flex items-center gap-1.5
                      ${profile.isAlexEngStudent === false || profile.isAlexEngStudent === null
                        ? "bg-slate-800 text-white shadow-md" 
                        : "text-slate-500 hover:text-slate-800 hover:bg-slate-50"
                      }`}
                  >
                    {(profile.isAlexEngStudent === false || profile.isAlexEngStudent === null) && <HiOutlineCheck className="w-3.5 h-3.5" />}
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
          )}
        </div>
      </main>
    </div>
  );
};

export default UserProfilePage;
