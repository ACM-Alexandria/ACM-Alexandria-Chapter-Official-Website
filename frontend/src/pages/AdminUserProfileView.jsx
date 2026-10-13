import { useEffect, useState, useRef } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { adminFetchUserProfile} from "../services/userService"; 
import { fetchUserPointsLogs, addUserPointsLog, updateUserPointsLog, deleteUserPointsLog } from "../services/pointsLogService";

import Navbar from "../components/HomePage/Navbar";
import ProfileViewCard from "../components/UserProfile/ProfileViewCard";
import PointsLogSection from "../components/UserProfile/PointsLogSection";
import PointsLogModal from "../components/UserProfile/PointsLogModal";

import {
    HiOutlineCheckCircle,
    HiOutlineExclamationCircle,
    HiOutlineArrowLeft,
    HiOutlineCamera
} from "react-icons/hi";

const AdminUserProfileView = () => {
    const { user: adminUser } = useAuth();
    const location = useLocation();
    const navigate = useNavigate();

    const targetUserId = location.state?.targetUserId;

    const self = (adminUser.id != targetUserId)

    const [profile, setProfile] = useState({
        name: "",
        email: "",
        phoneNumber: "",
        isAlexEngStudent: null,
        department: "",
        batch: "",
        linkedinUrl: "",
        profileImageUrl: "",
        role: "USER",
        seasonPoints: 0,
    });

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);
    const [successMsg, setSuccessMsg] = useState("Action completed successfully!"); // <-- Dynamic success message

    const [pointsLogs, setPointsLogs] = useState([]);
    const [pointsPage, setPointsPage] = useState(0);
    const [totalPages, setTotalPages] = useState(0);

    const [isModalOpen, setIsModalOpen] = useState(false);
    const [editingLog, setEditingLog] = useState(null);

    useEffect(() => {
        if (!targetUserId) {
            navigate("/admin", { replace: true });
        }
    }, [targetUserId, navigate]);

    useEffect(() => {
        if (!targetUserId) return;

        const loadProfile = async () => {
            setLoading(true);
            setError(null);
            try {
                const data = await adminFetchUserProfile(targetUserId);
                const profileData = {
                    name: data.name || "",
                    email: data.email || "",
                    phoneNumber: data.phone_number || "",
                    isAlexEngStudent: data.is_alex_eng_student ?? null,
                    department: data.department || "",
                    batch: data.batch || "",
                    linkedinUrl: data.linkedin_url || "",
                    profileImageUrl: data.profile_image_url || "",
                    role: data.role || "USER",
                    seasonPoints: data.season_points ?? 0
                };
                setProfile(profileData);
            } catch (err) {
                console.error("Profile fetch failure:", err);
                setError("Failed to load user profile.");
            } finally {
                setLoading(false);
            }
        };

        loadProfile();
    }, [targetUserId]);

    useEffect(() => {
        if (!targetUserId) return;

        const loadPointsLogs = async () => {
            try {
                const data = await fetchUserPointsLogs(targetUserId, pointsPage);
                setPointsLogs(data.content || []);
                setTotalPages(data.totalPages || 0);
            } catch (err) {
                console.error("Failed to load points logs:", err);
            }
        };

        loadPointsLogs();
    }, [targetUserId, pointsPage]);

    useEffect(() => {
        if (success) {
            const timer = setTimeout(() => setSuccess(false), 4000);
            return () => clearTimeout(timer);
        }
    }, [success]);

    // --- POINTS HANDLERS ---
    const handleAddPointsClick = () => {
        setEditingLog(null);
        setIsModalOpen(true);
    };

    const handleEditPointsClick = (log) => {
        setEditingLog(log);
        setIsModalOpen(true);
    };

    const handleModalClose = () => {
        setIsModalOpen(false);
        setEditingLog(null);
    };

    const handleModalSubmit = async (formData) => {
        try {
            if (editingLog) {
                await updateUserPointsLog(editingLog.id, {
                    reason: formData.reason,
                    points: formData.points
                });
                setProfile((prev) => ({
                    ...prev,
                    seasonPoints: (prev.seasonPoints || 0) + (formData.points - editingLog.points)
                }));
                setSuccessMsg("Points updated successfully!");
            } else {
                await addUserPointsLog({
                    member: targetUserId,
                    assigner: adminUser.id,
                    reason: formData.reason,
                    points: formData.points
                });
                setProfile((prev) => ({
                    ...prev,
                    seasonPoints: (prev.seasonPoints || 0) + formData.points
                }));
                setSuccessMsg("Points added successfully!");
            }

            handleModalClose();
            const data = await fetchUserPointsLogs(targetUserId, pointsPage);
            setPointsLogs(data.content || []);
            setTotalPages(data.totalPages || 0);
            setSuccess(true);

        } catch (err) {
            console.error("Failed to save points:", err);
            setError("Failed to save points log. Please try again.");
        }
    };

    const handleDeletePointsClick = async (log) => {
        const isConfirmed = window.confirm(`Are you sure you want to delete this log? ("${log.reason}")`);

        if (isConfirmed) {
            try {
                await deleteUserPointsLog(log.id);
                setProfile((prev) => ({
                    ...prev,
                    seasonPoints: (prev.seasonPoints || 0) - log.points
                }));

                const data = await fetchUserPointsLogs(targetUserId, pointsPage);
                setPointsLogs(data.content || []);
                setTotalPages(data.totalPages || 0);
                setSuccessMsg("Points log deleted.");
                setSuccess(true);
            } catch (err) {
                console.error("Failed to delete points log:", err);
                setError("Failed to delete points log. Please try again.");
            }
        }
    };

    if (!targetUserId) return null;

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

                <div className="mb-10" style={{ animation: "fadeInDown 0.6s ease-out both" }}>
                    <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-6">
                        <div className="flex items-start sm:items-center gap-4">

                            {/* <-- UPDATED AVATAR TO DISPLAY THE IMAGE IF IT EXISTS --> */}
                            <div className="w-16 h-16 rounded-2xl bg-gradient-to-br from-[#4B98C8] to-[#205E85] shadow-lg shadow-blue-200/50 flex items-center justify-center text-white text-2xl font-black tracking-wider shrink-0 overflow-hidden">
                                {
                                    profile.name?.[0]?.toUpperCase() || "U"
                                }
                            </div>

                            <div>
                                <h1 className="text-3xl font-black text-slate-900 dark:text-slate-100 tracking-tight">User Profile View</h1>
                                <p className="text-slate-500 dark:text-slate-300 font-medium text-sm mt-1">
                                    Managing profile and points for {profile.name || "User"}
                                </p>
                            </div>
                        </div>
                    </div>
                </div>

                <div
                    className="bg-white dark:bg-slate-900 rounded-[2.5rem] border border-slate-100 dark:border-slate-700 shadow-xl shadow-slate-200/30 dark:shadow-slate-950/50 overflow-hidden transition-all duration-500 mb-8"
                    style={{ animation: "fadeInUp 0.8s cubic-bezier(0.22, 1, 0.36, 1) 0.1s both" }}
                >
                    {loading ? (
                        <div className="p-8 md:p-12 space-y-8 animate-pulse">
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                                {[1, 2, 3, 4].map((i) => (
                                    <div key={i} className="space-y-2">
                                        <div className="h-4 bg-slate-100 dark:bg-slate-800 rounded w-24" />
                                        <div className="h-14 bg-slate-50 dark:bg-slate-800 rounded-2xl" />
                                    </div>
                                ))}
                            </div>
                        </div>
                    ) : (
                        <ProfileViewCard user={profile} profile={profile} />
                    )}
                </div>

                {!loading && (
                    <div className="space-y-4">
                        <div className="translate-y-[0px]">
                            {success && (
                                <div className="mb-6 flex items-center gap-3 bg-emerald-50 dark:bg-emerald-950/40 border border-emerald-200 dark:border-emerald-800 text-emerald-800 dark:text-emerald-200 rounded-2xl px-5 py-4 animate-[fadeIn_0.4s_ease] shadow-sm">
                                    <HiOutlineCheckCircle className="w-6 h-6 text-emerald-500 shrink-0" />
                                    {/* <-- UPDATED TO SHOW DYNAMIC MESSAGE --> */}
                                    <p className="font-bold text-sm">{successMsg}</p>
                                </div>
                            )}
                            {error && (
                                <div className="mb-6 flex items-center gap-3 bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-800 dark:text-rose-200 rounded-2xl px-5 py-4 animate-[fadeIn_0.4s_ease] shadow-sm">
                                    <HiOutlineExclamationCircle className="w-6 h-6 text-rose-500 shrink-0" />
                                    <p className="font-bold text-sm">{error}</p>
                                </div>
                            )}
                        </div>

                        <PointsLogSection
                            logs={pointsLogs}
                            isAdmin={self} // edited to check if user fetchs his profile to prevent him to edit on his points
                            page={pointsPage}
                            totalPages={totalPages}
                            onPageChange={(newPage) => setPointsPage(newPage)}
                            onAddClick={handleAddPointsClick}
                            onEditClick={handleEditPointsClick}
                            onDeleteClick={handleDeletePointsClick}
                        />
                    </div>
                )}

                <PointsLogModal
                    isOpen={isModalOpen}
                    onClose={handleModalClose}
                    onSubmit={handleModalSubmit}
                    editingLog={editingLog}
                />

            </main>
        </div>
    );
};

export default AdminUserProfileView;