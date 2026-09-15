import { useState } from "react";
import { Tree, TreeNode } from "react-organizational-chart";
import CommitteeMemberCard from "./CommitteeMemberCard";

const ClubCard = ({ club, onShowDetails }) => {
  const [imageError, setImageError] = useState(false);

  const orderedBoardRoles = [...(club?.boardRoles || [])].sort((a, b) => {
    const firstOrder = Number.isFinite(Number(a?.order))
      ? Number(a.order)
      : Number.MAX_SAFE_INTEGER;
    const secondOrder = Number.isFinite(Number(b?.order))
      ? Number(b.order)
      : Number.MAX_SAFE_INTEGER;
    return firstOrder - secondOrder;
  });

  const imageUrl = club.imageUrl || club.logoUrl;

  const handleClick = () => {
    if (onShowDetails && club?.id) {
      onShowDetails(club.id);
    }
  };

  const rootLabel = (
    <div
      onClick={handleClick}
      className="relative bg-white dark:bg-slate-800 rounded-3xl sm:rounded-[2.5rem] text-left w-full max-w-[210px] min-[375px]:max-w-[240px] min-[425px]:max-w-[270px] sm:max-w-[320px] md:max-w-sm lg:max-w-lg mx-auto group border border-slate-100 dark:border-slate-700 shadow-2xl shadow-slate-200/50 dark:shadow-slate-950/40 hover:border-slate-200 dark:hover:border-slate-600 transition-all duration-300 cursor-pointer"
    >
      <div className="bg-slate-100 dark:bg-slate-700 h-32 min-[375px]:h-36 min-[425px]:h-40 sm:h-48 md:h-56 lg:h-64 flex items-center justify-center overflow-hidden relative rounded-t-3xl sm:rounded-t-[2.5rem]">
        <div className="absolute inset-0 bg-gradient-to-tr from-[#4B98C8]/10 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 z-10" />
        {!club.isExternal && (
          <div className="absolute top-3 left-3 sm:top-4 sm:left-4 z-20 bg-slate-900/80 backdrop-blur-md text-slate-200 text-[9px] sm:text-[10px] md:text-xs font-black px-2.5 py-1 sm:px-3 sm:py-1.5 rounded-xl shadow-lg flex items-center gap-1.5 border border-slate-700/50">
            <span className="w-1.5 h-1.5 rounded-full bg-slate-400" />
            <span className="tracking-wide uppercase">Internal</span>
          </div>
        )}
        {imageUrl && !imageError ? (
          <img
            src={imageUrl}
            alt={club.name}
            className="w-full h-full object-cover transition-transform duration-700 group-hover:scale-110"
            onError={() => setImageError(true)}
          />
        ) : (
          <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#4B98C8]/20 to-[#205E85]/20">
            <span className="text-slate-300 dark:text-slate-500 text-7xl font-black opacity-40">
              {club.name?.charAt(0) || "C"}
            </span>
          </div>
        )}
      </div>

      <div className="p-4.5 min-[375px]:p-5 min-[425px]:p-6 sm:p-8 md:p-9 lg:p-10">
        <div className="flex items-center justify-between mb-2 min-[375px]:mb-3 sm:mb-4">
          <h3 className="text-lg min-[375px]:text-xl sm:text-xl md:text-2xl lg:text-3xl font-extrabold text-slate-900 dark:text-slate-100 tracking-tight group-hover:text-[#4B98C8] transition-colors leading-tight">
            {club.name}
          </h3>
        </div>
        <p className="text-slate-500 dark:text-slate-300 text-xs min-[375px]:text-sm sm:text-sm md:text-base leading-relaxed mb-4 min-[375px]:mb-5 sm:mb-6 line-clamp-2 font-medium">
          {club.description || "Fostering technological learning and practical skills through specialized club activities."}
        </p>
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
    <div className="overflow-x-auto pb-12 pt-4 px-4 scrollbar-hide w-full">
      <Tree
        lineWidth="2px"
        lineColor="var(--committee-tree-line)"
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

export default ClubCard;
