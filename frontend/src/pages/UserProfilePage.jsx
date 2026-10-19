import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { fetchUserProfile, updateUserProfile, uploadProfileImage } from "../services/userService";
import { fetchUserPointsLogs } from "../services/pointsLogService";
import Navbar from "../components/HomePage/Navbar";
import ProfileViewCard from "../components/UserProfile/ProfileViewCard";
import ProfileEditForm from "../components/UserProfile/ProfileEditForm";
import PointsLogSection from "../components/UserProfile/PointsLogSection";

import {
  HiOutlineCheckCircle,
  HiOutlineExclamationCircle,
  HiOutlineArrowLeft,
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
    batch: "",
    linkedinUrl: "",
    profileImageUrl: "",
    committeeName: "",
    role: "USER",
    seasonPoints: 0,
  });

  const [backupProfile, setBackupProfile] = useState(null);
  const [isEditing, setIsEditing] = useState(false);

  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  // --- PROFILE ALERTS STATE ---
  const [profileError, setProfileError] = useState(null);
  const [profileSuccess, setProfileSuccess] = useState(false);

  // --- POINTS LOG ALERTS STATE ---
  const [pointsError, setPointsError] = useState(null);
  const [pointsSuccess, setPointsSuccess] = useState(false);

  // --- POINTS LOG STATE ---
  const [pointsLogs, setPointsLogs] = useState([]);
  const [pointsPage, setPointsPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  // Fetch profile data from backend
  useEffect(() => {
    if (!user?.id) return;

    const loadProfile = async () => {
      setLoading(true);
      setProfileError(null);
      try {
        const data = await fetchUserProfile(user.id);
        const profileData = {
          name: data.name || "",
          phoneNumber: data.phone_number || "",
          isAlexEngStudent: data.is_alex_eng_student ?? null,
          department: data.department || "",
          batch: data.batch || "",
          linkedinUrl: data.linkedin_url || "",
          profileImageUrl: data.profile_image_url || "",
          committeeName: data.committee_name || "",
          role: data.role || user?.role || "USER",
          seasonPoints: data.season_points ?? 0,
        };
        setProfile(profileData);
        setBackupProfile(profileData);
      } catch (err) {
        console.error("Profile fetch failure:", err);
        setProfileError("Failed to load your profile. Please try again later.");
      } finally {
        setLoading(false);
      }
    };

    loadProfile();
  }, [user?.id]);

  // Auto-hide Profile success banner
  useEffect(() => {
    if (profileSuccess) {
      const timer = setTimeout(() => setProfileSuccess(false), 4000);
      return () => clearTimeout(timer);
    }
  }, [profileSuccess]);

  // Auto-hide Points success banner
  useEffect(() => {
    if (pointsSuccess) {
      const timer = setTimeout(() => setPointsSuccess(false), 4000);
      return () => clearTimeout(timer);
    }
  }, [pointsSuccess]);

  // Fetch Points Logs
  useEffect(() => {
    if (!user?.id) return;

    const loadPointsLogs = async () => {
      try {
        const data = await fetchUserPointsLogs(user.id, pointsPage);
        setPointsLogs(data.content || []);
        setTotalPages(data.totalPages || 0);
      } catch (err) {
        console.error("Failed to load points logs:", err);
      }
    };

    loadPointsLogs();
  }, [user?.id, pointsPage, profile.role]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    if (name === "phoneNumber") {
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
      department: val ? prev.department : "",
      batch: val ? prev.batch : ""
    }));
  };

  const handleStartEdit = () => {
    setProfile(prev => ({
      ...prev,
      isAlexEngStudent: prev.isAlexEngStudent ?? true
    }));
    setIsEditing(true);
  };

  const handleCancelEdit = () => {
    if (backupProfile) {
      setProfile(backupProfile);
    }
    setProfileError(null);
    setIsEditing(false);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!user?.id) return;

    const phoneRegex = /^01[0-9]{9}$/;
    if (!phoneRegex.test(profile.phoneNumber)) {
      setProfileError("Invalid Phone Number: Must be exactly 11 digits starting with '01'.");
      window.scrollTo({ top: 0, behavior: 'smooth' });
      return;
    }

    if (profile.isAlexEngStudent === true) {
      if (!profile.batch) {
        setProfileError("Validation Error: Please select your Academic Batch.");
        window.scrollTo({ top: 0, behavior: 'smooth' });
        return;
      }
      if (!profile.department) {
        setProfileError("Validation Error: Please select your Department.");
        window.scrollTo({ top: 0, behavior: 'smooth' });
        return;
      }
    }

    setSubmitting(true);
    setProfileError(null);
    setProfileSuccess(false);

    const payload = {
      name: profile.name,
      phone_number: profile.phoneNumber,
      is_alex_eng_student: profile.isAlexEngStudent,
      department: profile.isAlexEngStudent ? (profile.department || null) : null,
      batch: profile.isAlexEngStudent ? (profile.batch || null) : null,
      linkedin_url: profile.linkedinUrl || null
    };

    try {
      await updateUserProfile(user.id, payload);
      setBackupProfile(profile);
      setProfileSuccess(true);
      setIsEditing(false);
      window.scrollTo({ top: 0, behavior: 'smooth' });
    } catch (err) {
      console.error("Profile update failure:", err);
      setProfileError(err.message || "Unable to save your changes. Please check your inputs.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-50/50 dark:bg-slate-950 selection:bg-blue-100 dark:selection:bg-blue-900/40">
      <Navbar />

      <main className="pt-[120px] pb-20 px-4 sm:px-6 md:px-12 max-w-5xl mx-auto">
        <button
          onClick={() => navigate(-1)}
          className="mb-8 inline-flex items-center gap-2 px-4 py-2 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-800 rounded-xl text-xs font-bold text-slate-600 dark:text-slate-300 shadow-sm hover:bg-slate-50 dark:hover:bg-slate-800 transition-all"
        >
          <HiOutlineArrowLeft className="w-4 h-4" /> Back
        </button>

        {/* Page Header Section */}
        <div className="mb-10" style={{ animation: "fadeInDown 0.6s ease-out both" }}>
          <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
            <div className="flex items-start sm:items-center gap-4">
              <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-[#4B98C8] to-[#205E85] shadow-lg shadow-blue-200/50 flex items-center justify-center text-white text-2xl font-black tracking-wider shrink-0">
                {user?.email?.[0].toUpperCase() || "U"}
              </div>
              <div>
                <h1 className="text-3xl font-black text-slate-900 dark:text-slate-100 tracking-tight">Account Profile</h1>
                <p className="text-slate-500 dark:text-slate-300 font-medium text-sm mt-1">
                  Manage your official registration details and contact preferences.
                </p>
              </div>
            </div>

            {!loading && !isEditing && (
              <button
                onClick={handleStartEdit}
                className="px-6 py-3 flex items-center gap-2 rounded-2xl border border-slate-200 dark:border-slate-700 bg-white dark:bg-slate-800 shadow-sm text-slate-700 dark:text-slate-100 font-bold text-sm transition-all hover:bg-slate-50 dark:hover:bg-slate-700 hover:border-slate-300 dark:hover:border-slate-600 hover:shadow-md hover:-translate-y-0.5 active:translate-y-0"
              >
                <HiOutlinePencilAlt className="w-5 h-5 text-[#4B98C8]" />
                Edit Information
              </button>
            )}
          </div>
        </div>

        {/* Profile Notification Alerts */}
        {profileSuccess && (
          <div className="mb-6 flex items-center gap-3 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-200 rounded-2xl px-5 py-4 animate-[fadeIn_0.4s_ease] shadow-sm">
            <HiOutlineCheckCircle className="w-6 h-6 text-emerald-500 shrink-0" />
            <p className="font-bold text-sm">Profile saved successfully! Your information is now fully updated.</p>
          </div>
        )}

        {profileError && (
          <div className="mb-6 flex items-center gap-3 bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-800 dark:text-rose-200 rounded-2xl px-5 py-4 animate-[fadeIn_0.4s_ease] shadow-sm">
            <HiOutlineExclamationCircle className="w-6 h-6 text-rose-500 shrink-0" />
            <p className="font-bold text-sm">{profileError}</p>
          </div>
        )}

        {/* Master Profile Display Card */}
        <div
          className="bg-white dark:bg-slate-900 rounded-[2.5rem] border border-slate-100 dark:border-slate-700 shadow-xl shadow-slate-200/30 dark:shadow-slate-950/50 overflow-hidden transition-all duration-500"
          style={{ animation: "fadeInUp 0.8s cubic-bezier(0.22, 1, 0.36, 1) 0.1s both" }}
        >
          {loading ? (
            /* Skeleton Placeholders */
            <div className="p-8 md:p-12 space-y-8 animate-pulse">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                {[1, 2, 3, 4].map((i) => (
                  <div key={i} className="space-y-2">
                    <div className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-24" />
                    <div className="h-14 bg-slate-50 dark:bg-slate-800 rounded-2xl" />
                  </div>
                ))}
              </div>
              <div className="h-14 bg-slate-100 dark:bg-slate-800 rounded-2xl w-full md:w-40 ml-auto mt-8" />
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
              onImageUpload={async (file) => {
                const res = await uploadProfileImage(user.id, file);
                const imageUrl = res.profile_image_url || res;
                setProfile(prev => ({ ...prev, profileImageUrl: imageUrl }));
                setBackupProfile(prev => ({ ...prev, profileImageUrl: imageUrl }));
                return imageUrl;
              }}
            />
          )}
        </div>

        {/* Points Log Section */}
        {!isEditing && !loading && profile.role !== "USER" && (
          <div className="space-y-4">

            <div className="translate-y-[16px]">
              {pointsSuccess && (
                <div className="flex items-center gap-3 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-200 rounded-2xl px-5 py-4 animate-[fadeIn_0.4s_ease] shadow-sm">
                  <HiOutlineCheckCircle className="w-6 h-6 text-emerald-500 shrink-0" />
                  <p className="font-bold text-sm">Points updated successfully!</p>
                </div>
              )}

              {pointsError && (
                <div className="flex items-center gap-3 bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-800 dark:text-rose-200 rounded-2xl px-5 py-4 animate-[fadeIn_0.4s_ease] shadow-sm">
                  <HiOutlineExclamationCircle className="w-6 h-6 text-rose-500 shrink-0" />
                  <p className="font-bold text-sm">{pointsError}</p>
                </div>
              )}
            </div>

            <PointsLogSection
              logs={pointsLogs}
              isAdmin={false}
              page={pointsPage}
              totalPages={totalPages}
              onPageChange={(newPage) => setPointsPage(newPage)}
            />
          </div>
        )}
      </main>
    </div>
  );
};

export default UserProfilePage;