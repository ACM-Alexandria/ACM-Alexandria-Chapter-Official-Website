import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { fetchUserProfile, updateUserProfile, uploadProfileImage } from "../services/userService";
import { fetchUserPointsLogs } from "../services/pointsLogService";
import Navbar from "../components/HomePage/Navbar";
import { ProfileHeader, ProfileDetails, Panel } from "../components/UserProfile/ProfileViewCard";
import ProfileEditForm from "../components/UserProfile/ProfileEditForm";
import PointsLogSection from "../components/UserProfile/PointsLogSection";

import {
  HiOutlineCheckCircle,
  HiOutlineExclamationCircle,
  HiOutlineArrowLeft,
  HiOutlinePencil
} from "react-icons/hi";

const Alert = ({ tone, children }) => {
  const success = tone === "success";
  const Icon = success ? HiOutlineCheckCircle : HiOutlineExclamationCircle;
  return (
    <div
      role={success ? "status" : "alert"}
      className={`flex items-start gap-2.5 rounded-lg border px-4 py-3 text-sm ${success
        ? "bg-emerald-50 dark:bg-emerald-950/40 border-emerald-200 dark:border-emerald-900 text-emerald-800 dark:text-emerald-200"
        : "bg-rose-50 dark:bg-rose-950/40 border-rose-200 dark:border-rose-900 text-rose-800 dark:text-rose-200"
        }`}
    >
      <Icon className="w-5 h-5 shrink-0 mt-px" aria-hidden="true" />
      <p>{children}</p>
    </div>
  );
};

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

    const linkedinRegex = /^https?:\/\/([a-z]{2,3}\.)?linkedin\.com\/(in|pub)\/[A-Za-z0-9_%.-]+\/?([?#].*)?$/;
    if (profile.linkedinUrl && !linkedinRegex.test(profile.linkedinUrl.trim())) {
      setProfileError("Invalid LinkedIn URL: Use your profile link, like https://linkedin.com/in/username.");
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
      linkedin_url: profile.linkedinUrl.trim() || null
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
    <div className="min-h-screen bg-slate-50 dark:bg-slate-950 selection:bg-[#4B98C8]/20">
      <Navbar />

      <main className="pt-[106px] pb-20 px-4 sm:px-6 md:px-12 max-w-[1600px] mx-auto flex flex-col gap-8">
        <button
          onClick={() => navigate(-1)}
          className="w-fit -ml-1 inline-flex items-center gap-1.5 px-1 text-sm font-medium text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 transition-colors rounded focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/40"
        >
          <HiOutlineArrowLeft className="w-4 h-4" aria-hidden="true" /> Back
        </button>

        {loading ? (
          <div className="flex flex-col gap-8 animate-pulse motion-reduce:animate-none" aria-busy="true" aria-label="Loading profile">
            <div className="flex items-start gap-5">
              <div className="w-20 h-20 rounded-full bg-slate-200 dark:bg-slate-800 shrink-0" />
              <div className="flex-1 space-y-3 pt-1">
                <div className="h-7 w-48 rounded bg-slate-200 dark:bg-slate-800" />
                <div className="h-4 w-64 max-w-full rounded bg-slate-200 dark:bg-slate-800" />
                <div className="h-6 w-40 rounded-full bg-slate-200 dark:bg-slate-800" />
              </div>
            </div>
            <div className="h-64 rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900" />
          </div>
        ) : (
          <>
            <ProfileHeader
              user={user}
              profile={profile}
              actions={!isEditing && (
                <button
                  onClick={handleStartEdit}
                  className="h-9 px-3.5 inline-flex items-center gap-1.5 rounded-md border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-sm font-medium text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/40"
                >
                  <HiOutlinePencil className="w-4 h-4" aria-hidden="true" />
                  Edit profile
                </button>
              )}
            />

            {profileSuccess && <Alert tone="success">Profile saved.</Alert>}
            {profileError && <Alert tone="error">{profileError}</Alert>}

            <Panel title={isEditing ? "Edit details" : "Details"}>
              {isEditing ? (
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
              ) : (
                <ProfileDetails profile={profile} />
              )}
            </Panel>

            {!isEditing && profile.role !== "USER" && (
              <div className="flex flex-col gap-3">
                {pointsSuccess && <Alert tone="success">Points updated.</Alert>}
                {pointsError && <Alert tone="error">{pointsError}</Alert>}
                <PointsLogSection
                  logs={pointsLogs}
                  isAdmin={false}
                  page={pointsPage}
                  totalPages={totalPages}
                  onPageChange={(newPage) => setPointsPage(newPage)}
                />
              </div>
            )}
          </>
        )}
      </main>
    </div>
  );
};

export default UserProfilePage;