import React from "react";
import { FiPlus, FiSearch } from "react-icons/fi";
import { fieldClass, primaryButtonClass } from "./adminStyles";

// Title, optional "Add new" button and search for a resource section.
// `children` render next to the search box (e.g. a committee selector).
const ResourceToolbar = ({ title, description, onAdd, addLabel = "Add new", search, onSearchChange, searchPlaceholder = "Search by name or description", children }) => (
  <div className="flex flex-col gap-4 mb-5">
    <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
      <div className="min-w-0">
        <h2 className="text-lg font-semibold text-slate-900 dark:text-slate-100 tracking-tight">{title}</h2>
        {description && <p className="mt-0.5 text-sm text-slate-600 dark:text-slate-400">{description}</p>}
      </div>
      {onAdd && (
        <button type="button" onClick={onAdd} className={`${primaryButtonClass} w-full sm:w-auto`}>
          <FiPlus className="w-4 h-4" aria-hidden="true" /> {addLabel}
        </button>
      )}
    </div>

    {onSearchChange && (
      <div className="flex flex-col sm:flex-row gap-3">
        <div className="relative flex-1">
          <span className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-500">
            <FiSearch className="w-4 h-4" aria-hidden="true" />
          </span>
          <input
            type="text"
            aria-label="Search"
            value={search}
            onChange={(e) => onSearchChange(e.target.value)}
            placeholder={searchPlaceholder}
            className={`${fieldClass} pl-9`}
          />
        </div>
        {children}
      </div>
    )}
  </div>
);

export default ResourceToolbar;
