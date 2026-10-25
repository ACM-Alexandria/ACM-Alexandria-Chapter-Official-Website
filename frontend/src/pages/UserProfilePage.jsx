import { useEffect, useState } from "react";
import { useAuth } from "../contexts/useAuth";
import { fetchUserProfile, updateUserProfile, uploadProfileImage } from "../services/userService";
import { fetchUserPointsLogs } from "../services/pointsLogService";
import { FiAlertCircle, FiCheckCircle } from "react-icons/fi";
import SiteLayout from "../components/Layout/SiteLayout";
import PageHero from "../components/Layout/PageHero";
import ProfileSideCard from "../components/profile/ProfileSideCard";
import ProfileTabs from "../components/profile/ProfileTabs";
import ProfileDetails from "../components/profile/ProfileDetails";
import ProfileForm from "../components/profile/ProfileForm";
import PointsPanel from "../components/profile/PointsPanel";
import { errorBox, skeleton } from "../components/ui/uiStyles";

const Alert = ({ tone, children }) => {
  const success = tone === "success";
  const Icon = success ? FiCheckCircle : FiAlertCircle;
  return (
    <div role={success ? "status" : "alert"} className={success ? "flex items-start gap-3 rounded-xl bg-emerald-50 px-4 py-3 text-sm font-medium text-emerald-800 dark:bg-emerald-950/40 dark:text-emerald-200" : errorBox}>
      <Icon className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />
      <p>{children}</p>
    </div>
  );
};

const UserProfilePage = () => {
  const { user } = useAuth();

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
  const [tab, setTab] = useState("details");

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
        setPointsError(null);
        setPointsLogs(data.content || []);
        setTotalPages(data.totalPages || 0);
      } catch (err) {
        console.error("Failed to load points logs:", err);
        setPointsError("We couldn't load your points history. Please try again later.");
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

  const showPoints = profile.role !== "USER";
  const tabs = [["details", "Details"], ...(showPoints ? [["points", "Points"]] : [])];
  const activeTab = isEditing || !showPoints ? "details" : tab;

  return (
    <SiteLayout>
      <PageHero eyebrow="Your profile" title="Your details and points" text="Keep your contact details current so committees and event organisers can reach you." />
      <div className="mx-auto max-w-[1280px] px-5 py-12 sm:px-8 sm:py-16">
        {loading ? (
          <div className="grid gap-8 lg:grid-cols-[320px_1fr]" aria-busy="true" aria-label="Loading profile">
            <div className={`h-80 rounded-[2rem] ${skeleton}`} />
            <div className={`h-96 rounded-[2rem] ${skeleton}`} />
          </div>
        ) : (
          <div className="grid gap-8 lg:grid-cols-[320px_1fr] lg:items-start">
            <ProfileSideCard profile={profile} email={user?.email} onEdit={isEditing ? null : handleStartEdit} />

            <div className="flex min-w-0 flex-col gap-6">
              {profileSuccess && <Alert tone="success">Profile saved.</Alert>}
              {profileError && <Alert tone="error">{profileError}</Alert>}
              {pointsSuccess && <Alert tone="success">Points updated.</Alert>}

              {isEditing ? (
                <h2 className="text-2xl font-semibold tracking-tight text-slate-900 dark:text-white">Edit details</h2>
              ) : (
                <ProfileTabs tabs={tabs} active={activeTab} onChange={setTab} />
              )}

              <div role="tabpanel" id={`profile-panel-${activeTab}`} aria-labelledby={isEditing ? undefined : `profile-tab-${activeTab}`}>
                {activeTab === "points" ? (
                  <PointsPanel total={profile.seasonPoints || 0} logs={pointsLogs} page={pointsPage} totalPages={totalPages} onPageChange={setPointsPage} error={pointsError} />
                ) : isEditing ? (
                  <ProfileForm
                    email={user?.email}
                    profile={profile}
                    onChange={handleInputChange}
                    onToggleStudent={handleToggleAlexEng}
                    onCancel={handleCancelEdit}
                    onSubmit={handleSubmit}
                    submitting={submitting}
                    onImageUpload={async (file) => {
                      const res = await uploadProfileImage(user.id, file);
                      const imageUrl = res.profile_image_url || res;
                      setProfile((prev) => ({ ...prev, profileImageUrl: imageUrl }));
                      setBackupProfile((prev) => ({ ...prev, profileImageUrl: imageUrl }));
                      return imageUrl;
                    }}
                  />
                ) : (
                  <ProfileDetails profile={profile} />
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </SiteLayout>
  );
};

export default UserProfilePage;
