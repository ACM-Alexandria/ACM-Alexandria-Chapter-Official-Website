import { useState, useEffect } from "react";
import { Tree, TreeNode } from "react-organizational-chart";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../contexts/AuthContext";
import { FiBell, FiCheck, FiArrowRight } from "react-icons/fi";
import UnsubscribeConfirmModal from "../../UnsubscribeConfirmModal";
import {
  subscribeToCommittee,
  unsubscribeFromCommittee,
  fetchCommitteeSubscriptionStatus,
} from "../../../services/homePageService";
import { checkCommitteeRegistrationStatus } from "../../../services/registrationService";
import CommitteeMemberCard from "./CommitteeMemberCard";

const CommitteeCard = ({ committee, onApplyClick, isRegistrationModalOpen }) => {
  const [imageError, setImageError] = useState(false);
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const [subscribed, setSubscribed] = useState(false);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [isRegistered, setIsRegistered] = useState(false);
  const [checkingStatus, setCheckingStatus] = useState(false);

  useEffect(() => {
    const checkRegStatus = async () => {
      if (!isAuthenticated || !committee?.id || !(committee?.open || committee?.isOpen) || !user?.id) {
        setIsRegistered(false);
        return;
      }
      setCheckingStatus(true);
      try {
        const registered = await checkCommitteeRegistrationStatus(committee.id, user.id);
        setIsRegistered(registered);
      } catch (err) {
        console.error("Error checking committee registration:", err);
      } finally {
        setCheckingStatus(false);
      }
    };
    if (!isRegistrationModalOpen) {
      checkRegStatus();
    }
  }, [isAuthenticated, committee?.id, committee?.open, committee?.isOpen, user?.id, isRegistrationModalOpen]);

  useEffect(() => {
    const checkStatus = async () => {
      if (!isAuthenticated || !committee.id) return;
      try {
        const data = await fetchCommitteeSubscriptionStatus(committee.id);
        setSubscribed(data.subscribed);
      } catch (err) {
        console.error("Error fetching committee subscription status:", err);
      }
    };
    checkStatus();
  }, [isAuthenticated, committee.id]);

  const handleToggleSubscribe = async (e) => {
    e.preventDefault();
    e.stopPropagation();
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }
    setModalOpen(true);
  };

  const handleConfirmToggle = async () => {
    setLoading(true);
    try {
      if (subscribed) {
        await unsubscribeFromCommittee(committee.id);
        setSubscribed(false);
      } else {
        await subscribeToCommittee(committee.id);
        setSubscribed(true);
      }
      setModalOpen(false);
    } catch (err) {
      console.error("Error toggling committee subscription:", err);
    } finally {
      setLoading(false);
    }
  };

  const orderedBoardRoles = [...(committee?.boardRoles || [])].sort((a, b) => {
    const firstOrder = Number.isFinite(Number(a?.order))
      ? Number(a.order)
      : Number.MAX_SAFE_INTEGER;
    const secondOrder = Number.isFinite(Number(b?.order))
      ? Number(b.order)
      : Number.MAX_SAFE_INTEGER;
    return firstOrder - secondOrder;
  });

  const isOpen = committee.open || committee.isOpen;

  const rootLabel = (
    <div
      className={`relative bg-white rounded-3xl sm:rounded-[2.5rem] text-left w-full max-w-[210px] min-[375px]:max-w-[240px] min-[425px]:max-w-[270px] sm:max-w-[320px] md:max-w-sm lg:max-w-lg mx-auto group border transition-all duration-300 ${
        isOpen
          ? "border-blue-100 shadow-2xl shadow-blue-100/40 hover:border-blue-300/85 hover:shadow-xl hover:shadow-[#4B98C8]/10"
          : "border-slate-100 shadow-2xl shadow-slate-200/50 hover:border-slate-200"
      }`}
    >
      <button
        onClick={handleToggleSubscribe}
        disabled={loading}
        title={subscribed ? "Unsubscribe from notifications" : "Subscribe to notifications"}
        className={`absolute -top-2.5 -right-2.5 sm:-top-3.5 sm:-right-3.5 lg:-top-4 lg:-right-4 z-20 w-8 h-8 sm:w-9 sm:h-9 lg:w-10 lg:h-10 rounded-full border shadow-md transition-all flex items-center justify-center cursor-pointer ${
          subscribed
            ? "bg-green-50 border-green-200 text-green-600 hover:bg-green-100"
            : "bg-white border-slate-200 text-slate-400 hover:text-[#4B98C8] hover:bg-slate-50"
        }`}
      >
        {subscribed ? (
          <div className="flex items-center justify-center relative">
            <FiBell className="w-3.5 h-3.5 sm:w-4 sm:h-4 lg:w-4.5 lg:h-4.5" />
            <FiCheck className="absolute -bottom-0.5 -right-0.5 w-2 h-2 text-green-600 bg-green-50 rounded-full sm:w-2.5 sm:h-2.5 sm:-bottom-0.5 sm:-right-0.5 lg:w-3 lg:h-3 lg:-bottom-1 lg:-right-1" />
          </div>
        ) : (
          <FiBell className="w-3.5 h-3.5 sm:w-4 sm:h-4 lg:w-4.5 lg:h-4.5" />
        )}
      </button>

      <div className="bg-slate-100 h-32 min-[375px]:h-36 min-[425px]:h-40 sm:h-48 md:h-56 lg:h-64 flex items-center justify-center overflow-hidden relative rounded-t-3xl sm:rounded-t-[2.5rem]">
        <div className="absolute inset-0 bg-gradient-to-tr from-[#4B98C8]/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 z-10" />
        {isOpen && (
          <div className="absolute top-3 left-3 sm:top-4 sm:left-4 z-20 bg-emerald-500/90 backdrop-blur-md text-white text-[9px] sm:text-[10px] md:text-xs font-black px-2.5 py-1 sm:px-3 sm:py-1.5 rounded-xl shadow-lg shadow-emerald-500/20 flex items-center gap-1.5 border border-emerald-400/20">
            <span className="w-1.5 h-1.5 rounded-full bg-white" />
            <span className="tracking-wide uppercase">Open</span>
          </div>
        )}
        {committee.logoUrl && !imageError ? (
          <img
            src={committee.logoUrl}
            alt={committee.name}
            className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
            onError={() => setImageError(true)}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#4B98C8]/20 to-[#205E85]/20">
            <span className="text-slate-300 text-7xl font-black opacity-40">
              {committee.name?.charAt(0) || "C"}
            </span>
          </div>
        )}
      </div>

      <div className="p-4.5 min-[375px]:p-5 min-[425px]:p-6 sm:p-8 md:p-9 lg:p-10">
        <div className="flex items-center justify-between mb-2 min-[375px]:mb-3 sm:mb-4">
          <div className="flex items-center gap-3">
            <h3 className="text-lg min-[375px]:text-xl sm:text-xl md:text-2xl lg:text-3xl font-extrabold text-slate-900 tracking-tight group-hover:text-[#4B98C8] transition-colors leading-tight">
              {committee.name}
            </h3>
          </div>
        </div>
        <p className="text-slate-500 text-xs min-[375px]:text-sm sm:text-sm md:text-base leading-relaxed mb-4 min-[375px]:mb-5 sm:mb-6 line-clamp-3 font-medium">
          {committee.description || "Advancing computing through collaborative efforts and specialized initiatives."}
        </p>

        {isOpen && (
          <div className="mt-5 pt-4 border-t border-slate-100 flex items-center justify-between gap-4">
            <div className="text-left">
              <span className="text-[9px] sm:text-[10px] font-extrabold text-slate-400 uppercase tracking-widest block">Recruiting</span>
              <span className="text-[11px] sm:text-xs text-slate-500 font-semibold block mt-0.5 leading-none">Join this committee</span>
            </div>
            {checkingStatus ? (
              <div className="inline-flex items-center justify-center gap-1.5 bg-slate-50 border border-slate-200 text-slate-400 text-[10px] sm:text-xs font-bold px-3 py-2 sm:px-4 sm:py-2.5 rounded-xl">
                <div className="w-3 h-3 border-2 border-slate-200 border-t-[#4B98C8] rounded-full animate-spin" />
                Checking
              </div>
            ) : isRegistered ? (
              <div className="inline-flex items-center justify-center gap-1.5 bg-emerald-50 text-emerald-600 text-[10px] sm:text-xs font-bold px-3 py-2 sm:px-4 sm:py-2.5 rounded-xl border border-emerald-100">
                <FiCheck className="w-3.5 h-3.5" />
                Applied
              </div>
            ) : (
              <button
                onClick={(e) => {
                  e.preventDefault();
                  e.stopPropagation();
                  if (!isAuthenticated) {
                    const path = window.location.pathname + window.location.search;
                    const delimiter = path.includes("?") ? "&" : "?";
                    const from = `${path}${delimiter}openCommitteeId=${committee.id}`;
                    navigate("/login", { state: { from } });
                  } else {
                    onApplyClick(committee.id);
                  }
                }}
                className="inline-flex items-center justify-center gap-1.5 bg-[#4B98C8] hover:bg-[#205E85] text-white text-[10px] sm:text-xs font-bold px-4 py-2 sm:px-5 sm:py-2.5 rounded-xl transition-all duration-300 shadow-md shadow-blue-100 hover:shadow-lg hover:shadow-blue-200/50 active:scale-[0.97] cursor-pointer"
              >
                Apply Now
                <FiArrowRight className="w-3.5 h-3.5" />
              </button>
            )}
          </div>
        )}
      </div>
    </div>
  );

  if (orderedBoardRoles.length === 0) {
    return (
      <div className="pb-8">
        {rootLabel}
        <UnsubscribeConfirmModal
          open={modalOpen}
          onClose={() => setModalOpen(false)}
          onConfirm={handleConfirmToggle}
          topicName={`${committee.name}`}
          loading={loading}
          isSubscribe={!subscribed}
        />
      </div>
    );
  }

  return (
    <div className="overflow-x-auto pb-12 pt-4 px-4 scrollbar-hide w-full">
      <Tree
        lineWidth="2px"
        lineColor="#e2e8f0"
        lineBorderRadius="24px"
        label={rootLabel}
      >
        {orderedBoardRoles.map((member, index) => (
          <TreeNode
            key={member.id || `${member.name}-${index}`}
            label={<CommitteeMemberCard member={member} />}
          />
        ))}
      </Tree>

      <UnsubscribeConfirmModal
        open={modalOpen}
        onClose={() => setModalOpen(false)}
        onConfirm={handleConfirmToggle}
        topicName={`${committee.name}`}
        loading={loading}
        isSubscribe={!subscribed}
      />
    </div>
  );
};

export default CommitteeCard;
