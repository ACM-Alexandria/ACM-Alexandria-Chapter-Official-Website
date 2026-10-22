import { useEffect, useState, useRef } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import { useAuth } from "../contexts/AuthContext";
import { adminFetchUserProfile } from "../services/userService";
import { fetchUserPointsLogs, addUserPointsLog, updateUserPointsLog, deleteUserPointsLog } from "../services/pointsLogService";
import ConfirmDeleteModal from "../components/UserProfile/ConfirmDeleteModal";

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
        committeeName: "",
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

    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
    const [logToDelete, setLogToDelete] = useState(null);

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
                    committeeName: data.committee_name || "",
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

                if (
                    editingLog.reason === formData.reason &&
                    Number(editingLog.points) === Number(formData.points)
                ) {
                    setIsModalOpen(false);
                    setEditingLog(null);
                    return;
                }

                await updateUserPointsLog(editingLog.id, {
                    lastUpdater: adminUser.id,
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

    // 1. Opens the modal when the delete button is clicked
    const handleDeletePointsClick = (log) => {
        setLogToDelete(log);
        setIsDeleteModalOpen(true);
    };

    // 2. Actually deletes the log when "Yes, Delete" is clicked inside the modal
    const handleConfirmDelete = async () => {
        if (!logToDelete) return;

        try {
            await deleteUserPointsLog(logToDelete.id);
            setProfile((prev) => ({
                ...prev,
                seasonPoints: (prev.seasonPoints || 0) - logToDelete.points
            }));

            const data = await fetchUserPointsLogs(targetUserId, pointsPage);
            setPointsLogs(data.content || []);
            setTotalPages(data.totalPages || 0);
            setSuccessMsg("Points log deleted.");
            setSuccess(true);
        } catch (err) {
            console.error("Failed to delete points log:", err);
            setError("Failed to delete points log. Please try again.");
        } finally {
            setIsDeleteModalOpen(false);
            setLogToDelete(null);
        }
    };

    if (!targetUserId) return null;

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
                    <ProfileViewCard user={profile} profile={profile} />
                )}

                {!loading && (
                    <div className="flex flex-col gap-3">
                        <div>
                            {success && (
                                <div className="flex items-start gap-2.5 rounded-lg border px-4 py-3 text-sm bg-emerald-50 dark:bg-emerald-950/40 border-emerald-200 dark:border-emerald-900 text-emerald-800 dark:text-emerald-200">
                                    <HiOutlineCheckCircle className="w-5 h-5 shrink-0 mt-px" aria-hidden="true" />
                                    {/* <-- UPDATED TO SHOW DYNAMIC MESSAGE --> */}
                                    <p>{successMsg}</p>
                                </div>
                            )}
                            {error && (
                                <div className="flex items-start gap-2.5 rounded-lg border px-4 py-3 text-sm bg-rose-50 dark:bg-rose-950/40 border-rose-200 dark:border-rose-900 text-rose-800 dark:text-rose-200">
                                    <HiOutlineExclamationCircle className="w-5 h-5 shrink-0 mt-px" aria-hidden="true" />
                                    <p>{error}</p>
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
                <ConfirmDeleteModal
                    isOpen={isDeleteModalOpen}
                    onClose={() => setIsDeleteModalOpen(false)}
                    onConfirm={handleConfirmDelete}
                    itemName={logToDelete?.reason || "this log"}
                />

            </main>
        </div>
    );
};

export default AdminUserProfileView;