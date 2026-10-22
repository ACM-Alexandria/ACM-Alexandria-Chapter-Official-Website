import React from "react";
import {
  HiOutlinePlus,
  HiOutlinePencil,
  HiOutlineTrash,
  HiChevronLeft,
  HiChevronRight,
  HiStar
} from "react-icons/hi";
import { Panel } from "./ProfileViewCard";

const formatDate = (dateString) => {
  if (!dateString) return "—";
  return new Date(dateString).toLocaleDateString(undefined, { year: "numeric", month: "short", day: "numeric" });
};

const formatTime = (dateString) => {
  if (!dateString) return "";
  return new Date(dateString).toLocaleTimeString(undefined, { hour: "2-digit", minute: "2-digit" });
};

// "just now", "5m ago", "3h ago", "2d ago", "1mo ago", "2y ago"
const timeAgo = (dateString) => {
  if (!dateString) return "";
  const seconds = Math.max(0, (Date.now() - new Date(dateString).getTime()) / 1000);
  const units = [
    ["y", 31536000],
    ["mo", 2592000],
    ["d", 86400],
    ["h", 3600],
    ["m", 60],
  ];
  for (const [label, size] of units) {
    if (seconds >= size) return `${Math.floor(seconds / size)}${label} ago`;
  }
  return "just now";
};

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
  const pageButtonClass =
    "h-8 px-2.5 rounded-md inline-flex items-center gap-1 text-sm font-medium text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-slate-800 disabled:opacity-40 disabled:hover:bg-transparent disabled:cursor-not-allowed transition-colors";

  return (
    <Panel
      title="Points history"
      aside={
        isAdmin && (
          <button
            onClick={onAddClick}
            className="h-9 px-3 inline-flex items-center gap-1.5 rounded-md bg-[#4B98C8] hover:bg-[#3d7da6] text-white text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-[#4B98C8] dark:focus-visible:ring-offset-slate-900"
          >
            <HiOutlinePlus className="w-4 h-4" aria-hidden="true" />
            Add points
          </button>
        )
      }
    >
      {logs.length === 0 ? (
        <div className="px-5 py-10 flex flex-col items-center text-center">
          <span className="w-12 h-12 mb-3 rounded-full bg-yellow-50 dark:bg-yellow-400/10 flex items-center justify-center">
            <HiStar className="w-6 h-6 text-yellow-400" aria-hidden="true" />
          </span>
          <p className="text-sm font-medium text-slate-900 dark:text-slate-100">No points yet</p>
          <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">
            Points from events, tasks and committee work will show up here.
          </p>
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full table-fixed text-left border-separate border-spacing-0">
            <thead>
              <tr className="text-sm text-slate-600 dark:text-slate-400 bg-slate-100 dark:bg-slate-800/60">
                <th scope="col" className="hidden sm:table-cell w-48 px-5 py-3 font-medium">Date</th>
                <th scope="col" className="px-3 sm:px-5 py-3 font-medium">Activity</th>
                {isAdmin && <th scope="col" className="hidden sm:table-cell w-52 px-5 py-3 font-medium">Assigned by</th>}
                <th scope="col" className="w-16 sm:w-28 px-3 sm:px-5 py-3 font-medium text-right">Points</th>
                {isAdmin && <th scope="col" className="w-[4.75rem] sm:w-24 px-1 sm:px-3 py-3"><span className="sr-only">Actions</span></th>}
              </tr>
            </thead>
            <tbody>
              {logs.map((log) => {
                const positive = log.points >= 0;
                const createdAt = log.createdAt || log.created_at;
                const updatedAt = log.updatedAt || log.updated_at;
                const edited = isAdmin && log.lastUpdaterName && updatedAt && updatedAt !== createdAt;
                const rowBorder = "border-b border-slate-200 dark:border-slate-800 group-last:border-b-0";

                return (
                  <tr key={log.id} className="group align-middle hover:bg-slate-50 dark:hover:bg-slate-800/40 transition-colors">
                    <td className={`hidden sm:table-cell px-5 py-4 ${rowBorder}`}>
                      <p className="text-sm font-medium text-slate-900 dark:text-slate-100">{timeAgo(createdAt)}</p>
                      <p className="mt-0.5 text-xs text-slate-600 dark:text-slate-400 tabular-nums">
                        {formatDate(createdAt)}, {formatTime(createdAt)}
                      </p>
                    </td>

                    <td className={`px-3 sm:px-5 py-4 ${rowBorder}`}>
                      <p className="text-sm font-medium text-slate-900 dark:text-slate-100 truncate" title={log.reason}>
                        {log.reason}
                      </p>
                      <p className="sm:hidden mt-0.5 text-xs text-slate-600 dark:text-slate-400 truncate">
                        <span className="tabular-nums">{formatDate(createdAt)}</span>
                        {isAdmin && <> · Given by {log.assignerName || "System"}</>}
                      </p>
                    </td>

                    {isAdmin && (
                      <td className={`hidden sm:table-cell px-5 py-4 ${rowBorder}`}>
                        <p className="text-sm font-medium text-slate-900 dark:text-slate-100 truncate" title={log.assignerName || "System"}>
                          {log.assignerName || "System"}
                        </p>
                        {edited && (
                          <p className="mt-0.5 text-xs text-slate-600 dark:text-slate-400 truncate" title={`Edited by ${log.lastUpdaterName}`}>
                            Edited by {log.lastUpdaterName}
                          </p>
                        )}
                      </td>
                    )}

                    <td
                      className={`px-3 sm:px-5 py-4 text-right text-base font-bold tabular-nums whitespace-nowrap ${rowBorder} ${positive ? "text-emerald-700 dark:text-emerald-400" : "text-rose-700 dark:text-rose-400"}`}
                    >
                      {log.points > 0 ? `+${log.points}` : log.points}
                    </td>

                    {isAdmin && (
                      <td className={`px-1 sm:px-3 py-4 ${rowBorder}`}>
                        <div className="flex items-center justify-end">
                          <button
                            onClick={() => onEditClick(log)}
                            className="p-1.5 sm:p-2 rounded-md text-slate-500 hover:text-[#2C6F99] hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
                            aria-label={`Edit "${log.reason}"`}
                            title="Edit"
                          >
                            <HiOutlinePencil className="w-4 h-4" />
                          </button>
                          <button
                            onClick={() => onDeleteClick(log)}
                            className="p-1.5 sm:p-2 rounded-md text-slate-500 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/30 transition-colors"
                            aria-label={`Delete "${log.reason}"`}
                            title="Delete"
                          >
                            <HiOutlineTrash className="w-4 h-4" />
                          </button>
                        </div>
                      </td>
                    )}
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}

      {totalPages > 1 && (
        <div className="px-3 py-2.5 flex items-center justify-between border-t border-slate-200 dark:border-slate-800">
          <button onClick={() => onPageChange(page - 1)} disabled={page === 0} className={pageButtonClass}>
            <HiChevronLeft className="w-4 h-4" aria-hidden="true" /> Previous
          </button>
          <span className="text-sm text-slate-600 dark:text-slate-400 tabular-nums">
            Page {page + 1} of {totalPages}
          </span>
          <button onClick={() => onPageChange(page + 1)} disabled={page >= totalPages - 1} className={pageButtonClass}>
            Next <HiChevronRight className="w-4 h-4" aria-hidden="true" />
          </button>
        </div>
      )}
    </Panel>
  );
};

export default PointsLogSection;
