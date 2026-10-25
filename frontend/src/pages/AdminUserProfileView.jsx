import { useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import IslandNav from "../components/Layout/IslandNav";
import { siteSections } from "../components/Layout/siteSections";
import MemberProfileView from "../components/UserProfile/MemberProfileView";
import { HiOutlineArrowLeft } from "react-icons/hi";

const AdminUserProfileView = () => {
    const location = useLocation();
    const navigate = useNavigate();

    const targetUserId = location.state?.targetUserId;

    useEffect(() => {
        if (!targetUserId) {
            navigate("/admin", { replace: true });
        }
    }, [targetUserId, navigate]);

    if (!targetUserId) return null;

    return (
        <div className="min-h-screen bg-slate-50 dark:bg-slate-950 selection:bg-[#4B98C8]/20">
            <IslandNav links={siteSections} home={false} solid />

            <main className="pt-[106px] pb-20 px-4 sm:px-6 md:px-12 max-w-[1600px] mx-auto flex flex-col gap-8">
                <button
                    onClick={() => navigate(-1)}
                    className="w-fit -ml-1 inline-flex items-center gap-1.5 px-1 text-sm font-medium text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 transition-colors rounded focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/40"
                >
                    <HiOutlineArrowLeft className="w-4 h-4" aria-hidden="true" /> Back
                </button>

                <MemberProfileView userId={targetUserId} />
            </main>
        </div>
    );
};

export default AdminUserProfileView;
