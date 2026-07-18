import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { fetchUserProfile, updateUserProfile } from "../services/userService";
import Navbar from "../components/HomePage/Navbar";
import ProfileViewCard from "../components/UserProfile/ProfileViewCard";
import ProfileEditForm from "../components/UserProfile/ProfileEditForm";
import { 
  HiOutlineCheckCircle,
  HiOutlineExclamationCircle,
  HiOutlinePencilAlt
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

  const handleStartEdit = () => {
    // If state is null when user clicks edit, automatically promote it to true to pre-select 'Yes' in form
    setProfile(prev => ({
      ...prev,
      isAlexEngStudent: prev.isAlexEngStudent ?? true
    }));
    setIsEditing(true);
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
                onClick={handleStartEdit}
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
            /* Skeleton Placeholders */
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
            <ProfileViewCard user={user} profile={profile} />
          ) : (
            <ProfileEditForm 
              user={user} 
              profile={profile} 
              handleInputChange={handleInputChange} 
              handleToggleAlexEng={handleToggleAlexEng} 
              handleCancelEdit={handleCancelEdit} 
              handleSubmit={handleSubmit} 
              submitting={submitting} 
            />
          )}
        </div>
      </main>
    </div>
  );
};

export default UserProfilePage;
