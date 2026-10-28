import React from "react";
import { FiChevronLeft, FiChevronRight } from "react-icons/fi";

const pageButtonClass =
  "h-8 px-2.5 rounded-lg inline-flex items-center gap-1 text-sm font-medium text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-ocean-845 disabled:opacity-40 disabled:hover:bg-transparent disabled:cursor-not-allowed transition-colors";

// Previous / next controls for a Spring page ({ number, totalPages }). Renders nothing for lists without paging.
const ResourcePagination = ({ pageInfo, onPageChange }) => {
  if (!pageInfo) return null;

  return (
    <div className="flex items-center justify-between border-t border-slate-200 dark:border-ocean-800 pt-4 mt-6">
      <button
        type="button"
        disabled={pageInfo.number === 0}
        onClick={() => onPageChange(pageInfo.number - 1)}
        className={pageButtonClass}
      >
        <FiChevronLeft className="w-4 h-4" aria-hidden="true" /> Previous
      </button>
      <span className="text-sm text-slate-600 dark:text-slate-400 tabular-nums">
        Page {pageInfo.number + 1} of {pageInfo.totalPages}
      </span>
      <button
        type="button"
        disabled={pageInfo.number >= pageInfo.totalPages - 1}
        onClick={() => onPageChange(pageInfo.number + 1)}
        className={pageButtonClass}
      >
        Next <FiChevronRight className="w-4 h-4" aria-hidden="true" />
      </button>
    </div>
  );
};

export default ResourcePagination;
