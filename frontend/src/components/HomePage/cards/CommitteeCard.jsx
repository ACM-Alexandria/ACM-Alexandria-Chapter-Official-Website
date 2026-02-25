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
    <div className="bg-white rounded-lg shadow-lg overflow-hidden hover:shadow-xl transition-shadow duration-300 text-left max-w-md mx-auto">
      <div className="bg-gradient-to-r from-[#4B98C8] to-[#205E85] h-56 flex items-center justify-center overflow-hidden">
        {committee.logoUrl && !imageError ? (
          <img
            src={committee.logoUrl}
            alt={committee.name}
            className="w-full h-full object-cover"
            onError={() => setImageError(true)}
          />
        ) : (
          <span className="text-white text-6xl font-bold opacity-20">
            {committee.name?.charAt(0) || "C"}
          </span>
        )}
      </div>

      <div className="p-8">
        <h3 className="text-3xl font-bold text-gray-800 mb-3">
          {committee.name}
        </h3>
        <p className="text-gray-600 text-base mb-4 line-clamp-4">
          {committee.description || "Learn more about this committee"}
        </p>

        {committee.isOpen && (
          <span className="inline-block bg-green-100 text-green-800 text-sm font-semibold px-4 py-2 rounded-full">
            Applications Open
          </span>
        )}
      </div>
    </div>
  );

  if (orderedBoardRoles.length === 0) {
    return (
      <div data-aos="fade-up" className="overflow-x-auto pb-3">
        {rootLabel}
      </div>
    );
  }

  return (
    <div data-aos="fade-up" className="overflow-x-auto pb-3">
      <Tree
        lineWidth="2px"
        lineColor="#d1d5db"
        lineBorderRadius="10px"
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
