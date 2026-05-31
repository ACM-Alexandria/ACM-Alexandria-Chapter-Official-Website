import { useState, useEffect } from "react";
import { Tree, TreeNode } from "react-organizational-chart";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../../contexts/AuthContext";
import { FiBell, FiCheck } from "react-icons/fi";
import UnsubscribeConfirmModal from "../../UnsubscribeConfirmModal";
import {
  subscribeToCommittee,
  unsubscribeFromCommittee,
  fetchCommitteeSubscriptionStatus,
} from "../../../services/homePageService";
import CommitteeMemberCard from "./CommitteeMemberCard";

const CommitteeCard = ({ committee }) => {
  const [imageError, setImageError] = useState(false);
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const [subscribed, setSubscribed] = useState(false);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);

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

  const rootLabel = (
    <div className="relative bg-white rounded-[2.5rem] shadow-2xl shadow-slate-200/50 border border-slate-100 text-left max-w-md mx-auto group">
      <button
        onClick={handleToggleSubscribe}
        disabled={loading}
        title={subscribed ? "Unsubscribe from notifications" : "Subscribe to notifications"}
        className={`absolute -top-4 -right-4 z-20 w-10 h-10 rounded-full border shadow-md transition-all flex items-center justify-center cursor-pointer ${
          subscribed
            ? "bg-green-50 border-green-200 text-green-600 hover:bg-green-100"
            : "bg-white border-slate-200 text-slate-400 hover:text-[#4B98C8] hover:bg-slate-50"
        }`}
      >
        {subscribed ? (
          <div className="flex items-center justify-center relative">
            <FiBell className="w-4.5 h-4.5" />
            <FiCheck className="absolute -bottom-1 -right-1 w-3 h-3 text-green-600 bg-green-50 rounded-full" />
          </div>
        ) : (
          <FiBell className="w-4.5 h-4.5" />
        )}
      </button>

      <div className="bg-slate-100 h-64 flex items-center justify-center overflow-hidden relative rounded-t-[2.5rem]">
        <div className="absolute inset-0 bg-gradient-to-tr from-[#4B98C8]/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 z-10" />
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

      <div className="p-10">
        <div className="flex items-center justify-between mb-4">
          <div className="flex items-center gap-3">
            <h3 className="text-3xl font-extrabold text-slate-900 tracking-tight group-hover:text-[#4B98C8] transition-colors">
              {committee.name}
            </h3>
            {committee.isOpen && (
              <span className="flex h-3 w-3 relative">
                <span className="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                <span className="relative inline-flex rounded-full h-3 w-3 bg-green-500"></span>
              </span>
            )}
          </div>
        </div>
        <p className="text-slate-500 text-base leading-relaxed mb-6 line-clamp-3 font-medium">
          {committee.description || "Advancing computing through collaborative efforts and specialized initiatives."}
        </p>

        {committee.isOpen && (
          <div className="inline-flex items-center gap-2 bg-green-50 text-green-700 text-xs font-bold px-4 py-2 rounded-xl border border-green-100">
            Applications Open
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
          topicName={`${committee.name} Committee`}
          loading={loading}
          isSubscribe={!subscribed}
        />
      </div>
    );
  }

  return (
    <div className="overflow-x-auto pb-12 pt-4 px-4 scrollbar-hide">
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
        topicName={`${committee.name} Committee`}
        loading={loading}
        isSubscribe={!subscribed}
      />
    </div>
  );
};

export default CommitteeCard;
