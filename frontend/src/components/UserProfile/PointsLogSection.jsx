import React from "react";
import {
  HiOutlineStar,
  HiOutlinePlus,
  HiOutlinePencil,
  HiOutlineTrash,
  HiChevronLeft,
  HiChevronRight
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
            {logs.map((log) => (
              <div
                key={log.id}
                className="flex items-center justify-between p-4 rounded-2xl bg-slate-50 dark:bg-slate-800/50 border border-slate-200 dark:border-slate-700 hover:border-slate-300 dark:hover:border-slate-600 transition-colors"
              >
                <div className="flex flex-col gap-1">
                  <span className="text-sm md:text-base font-medium text-slate-900 dark:text-slate-200">
                    {log.reason}
                  </span>
                  {/* You can add a date here if your DTO gets a createdAt field later! */}
                  {(log.updated_at || log.updatedAt || log.created_at || log.createdAt) && (
                    <span className="text-[11px] text-slate-400 dark:text-slate-500 font-semibold tracking-wide mt-0.5">
                      {new Date(log.updated_at || log.updatedAt || log.created_at || log.createdAt).toLocaleDateString(undefined, {
                        year: 'numeric',
                        month: 'short',
                        day: 'numeric'
                      })}
                      <span className="mx-1.5">•</span>
                      {new Date(log.updated_at || log.updatedAt || log.created_at || log.createdAt).toLocaleTimeString(undefined, {
                        hour: '2-digit',
                        minute: '2-digit'
                      })}
                    </span>
                  )}
                </div>

                <div className="flex items-center gap-4">
                  <span className={`font-bold text-lg ${log.points >= 0 ? 'text-emerald-500' : 'text-rose-500'}`}>
                    {log.points > 0 ? `+${log.points}` : log.points}
                  </span>

                  {/* Admin Edit Button */}
                  {isAdmin && (
                    <div className="flex items-center gap-1">
                      <button
                        onClick={() => onEditClick(log)}
                        className="p-2 rounded-lg text-slate-400 hover:text-[#4B98C8] hover:bg-blue-50 dark:hover:bg-slate-700 transition-all"
                        title="Edit Log"
                      >
                        <HiOutlinePencil className="w-5 h-5" />
                      </button>

                      {/* 👇 ADD THE DELETE BUTTON HERE 👇 */}
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
            ))}
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