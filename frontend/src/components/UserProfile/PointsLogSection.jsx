import React, { useState } from "react";
import {
  HiOutlineStar,
  HiOutlinePlus,
  HiOutlinePencil,
  HiOutlineTrash,
  HiChevronLeft,
  HiChevronRight,
  HiChevronDown
} from "react-icons/hi";

const PointsLogSection = ({
  logs = [],
  isAdmin,
  page,
  totalPages,
  onPageChange,
  onAddClick,
  onEditClick,
  onDeleteClick
}) => {

  // State to track which log is currently expanded
  const [expandedLogId, setExpandedLogId] = useState(null);

  const toggleExpand = (id) => {
    setExpandedLogId(expandedLogId === id ? null : id);
  };

  // Helper to format dates cleanly
  const formatDateTime = (dateString) => {
    if (!dateString) return "N/A";
    const date = new Date(dateString);
    return `${date.toLocaleDateString(undefined, { year: 'numeric', month: 'short', day: 'numeric' })} • ${date.toLocaleTimeString(undefined, { hour: '2-digit', minute: '2-digit' })}`;
  };

  return (
    <div className="mt-8 bg-white dark:bg-slate-900 rounded-[2.5rem] border border-slate-100 dark:border-slate-700 shadow-xl shadow-slate-200/30 dark:shadow-slate-950/50 overflow-hidden transition-all duration-500">

      {/* Header Section */}
      <div className="p-6 md:p-8 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 border-b border-slate-100 dark:border-slate-800">
        <div className="flex items-center gap-3">
          <div className="w-10 h-10 rounded-xl bg-amber-50 dark:bg-amber-900/30 flex items-center justify-center text-amber-500">
            <HiOutlineStar className="w-6 h-6" />
          </div>
          <h2 className="text-xl md:text-2xl font-bold text-slate-900 dark:text-slate-100 tracking-tight">
            Points History
          </h2>
        </div>

        {/* Admin Add Button */}
        {isAdmin && (
          <button
            onClick={onAddClick}
            className="px-4 py-2 flex items-center gap-2 rounded-xl bg-[#4B98C8] hover:bg-[#3A7CA5] text-white font-bold text-sm transition-all shadow-md shadow-blue-500/20 hover:-translate-y-0.5"
          >
            <HiOutlinePlus className="w-5 h-5" />
            Add Points
          </button>
        )}
      </div>

      {/* Logs List Section */}
      <div className="p-6 md:p-8">
        {logs.length === 0 ? (
          <div className="text-center py-10 text-slate-500 dark:text-slate-400 font-medium">
            No points logs found for this user.
          </div>
        ) : (
          <div className="flex flex-col gap-4">
            {logs.map((log) => {
              const isExpanded = expandedLogId === log.id;

              return (
                <div
                  key={log.id}
                  className="flex flex-col p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 hover:border-slate-300 dark:hover:border-slate-600 transition-colors overflow-hidden"
                >
                  {/* Top Visible Row */}
                  <div className="flex items-center justify-between">

                    {/* Clickable Area for Expansion */}
                    <div
                      className="flex items-center gap-3 cursor-pointer flex-1 group"
                      onClick={() => toggleExpand(log.id)}
                    >
                      <button className={`p-1.5 rounded-full bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-600 group-hover:border-[#4B98C8] transition-all duration-300 ${isExpanded ? 'rotate-180 bg-[#4B98C8]/10' : ''}`}>
                        <HiChevronDown className={`w-4 h-4 transition-colors ${isExpanded ? 'text-[#4B98C8]' : 'text-slate-400'}`} />
                      </button>
                      <span className="text-sm md:text-base font-medium text-slate-900 dark:text-slate-200 select-none">
                        {log.reason}
                      </span>
                    </div>

                    {/* Points & Actions */}
                    <div className="flex items-center gap-4">
                      <span className={`font-bold text-lg ${log.points >= 0 ? 'text-emerald-500' : 'text-rose-500'}`}>
                        {log.points > 0 ? `+${log.points}` : log.points}
                      </span>

                      {/* Admin Edit/Delete Buttons */}
                      {isAdmin && (
                        <div className="flex items-center gap-1">
                          <button
                            onClick={() => onEditClick(log)}
                            className="p-2 rounded-lg text-slate-400 hover:text-[#4B98C8] hover:bg-blue-50 dark:hover:bg-slate-700 transition-all"
                            title="Edit Log"
                          >
                            <HiOutlinePencil className="w-5 h-5" />
                          </button>
                          <button
                            onClick={() => onDeleteClick(log)}
                            className="p-2 rounded-lg text-slate-400 hover:text-rose-500 hover:bg-rose-50 dark:hover:bg-rose-950/30 transition-all"
                            title="Delete Log"
                          >
                            <HiOutlineTrash className="w-5 h-5" />
                          </button>
                        </div>
                      )}
                    </div>
                  </div>

                  {/* Expandable Details Grid */}
                  <div
                    className={`grid transition-all duration-500 ease-in-out ${isExpanded ? "grid-rows-[1fr] opacity-100 mt-4" : "grid-rows-[0fr] opacity-0 mt-0"
                      }`}
                  >
                    <div className="overflow-hidden">
                      <div className="pt-4 border-t border-slate-200 dark:border-slate-700 grid grid-cols-1 sm:grid-cols-2 gap-y-4 gap-x-8 text-xs">

                        {/* Created At */}
                        <div className="flex flex-col gap-1">
                          <span className="text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider text-[10px]">Date Created</span>
                          <span className="text-slate-700 dark:text-slate-300 font-medium">
                            {formatDateTime(log.createdAt || log.created_at)}
                          </span>
                        </div>

                        {/* Updated At */}
                        <div className="flex flex-col gap-1">
                          <span className="text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider text-[10px]">Last Updated</span>
                          <span className="text-slate-700 dark:text-slate-300 font-medium">
                            {formatDateTime(log.updatedAt || log.updated_at)}
                          </span>
                        </div>

                        {isAdmin && (
                          <>
                            {/* Assigner */}
                            <div className="flex flex-col gap-1">
                              <span className="text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider text-[10px]">Assigned By</span>
                              <span className="text-slate-700 dark:text-slate-300 font-medium">
                                {log.assignerName || "System"}
                              </span>
                            </div>

                            {/* Last Updater */}
                            <div className="flex flex-col gap-1">
                              <span className="text-slate-400 dark:text-slate-500 font-bold uppercase tracking-wider text-[10px]">Last Modified By</span>
                              <span className="text-slate-700 dark:text-slate-300 font-medium">
                                {log.lastUpdaterName || "N/A"}
                              </span>
                            </div>
                          </>
                        )}
                      </div>
                    </div>
                  </div>

                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Pagination Controls */}
      {totalPages > 1 && (
        <div className="px-6 py-4 border-t border-slate-100 dark:border-slate-800 flex items-center justify-between bg-slate-50/50 dark:bg-slate-900/50">
          <button
            onClick={() => onPageChange(page - 1)}
            disabled={page === 0}
            className="p-2 rounded-xl text-slate-600 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700 disabled:opacity-30 disabled:hover:bg-transparent transition-all flex items-center gap-1 text-sm font-semibold"
          >
            <HiChevronLeft className="w-5 h-5" /> Previous
          </button>

          <span className="text-sm font-medium text-slate-500 dark:text-slate-400">
            Page {page + 1} of {totalPages}
          </span>

          <button
            onClick={() => onPageChange(page + 1)}
            disabled={page >= totalPages - 1}
            className="p-2 rounded-xl text-slate-600 dark:text-slate-300 hover:bg-slate-200 dark:hover:bg-slate-700 disabled:opacity-30 disabled:hover:bg-transparent transition-all flex items-center gap-1 text-sm font-semibold"
          >
            Next <HiChevronRight className="w-5 h-5" />
          </button>
        </div>
      )}
    </div>
  );
};

export default PointsLogSection;