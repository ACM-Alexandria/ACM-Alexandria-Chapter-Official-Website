import { useState } from "react";
import { Tree, TreeNode } from "react-organizational-chart";
import CommitteeMemberCard from "./CommitteeMemberCard";

const CommitteeCard = ({ committee }) => {
  const [imageError, setImageError] = useState(false);

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
    <div className="bg-white rounded-[2.5rem] shadow-2xl shadow-slate-200/50 overflow-hidden border border-slate-100 text-left max-w-md mx-auto group">
      <div className="bg-slate-100 h-64 flex items-center justify-center overflow-hidden relative">
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
    </div>
  );
};

export default CommitteeCard;
