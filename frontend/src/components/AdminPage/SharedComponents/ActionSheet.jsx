import React, { useEffect } from "react";
import { FiX } from "react-icons/fi";
import useScrollLock from "../../../hooks/useScrollLock";

// Bottom sheet listing every action for a row. Used on phones, where a row of
// icon buttons doesn't fit. The sheet closes first, then the action runs, so any
// dialog the action opens appears on a clean screen.
const ActionSheet = ({ open, onClose, title, subtitle, actions }) => {
  useScrollLock(open);

  useEffect(() => {
    if (!open) return undefined;
    const handleKeyDown = (e) => {
      if (e.key === "Escape") onClose();
    };
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [open, onClose]);

  if (!open) return null;

  return (
    <div className="fixed inset-0 z-[65] flex items-end">
      <div
        className="absolute inset-0 bg-slate-900/50 animate-[fadeIn_0.2s_ease-out] motion-reduce:animate-none"
        onClick={onClose}
        aria-hidden="true"
      />

      <div
        role="dialog"
        aria-modal="true"
        aria-label={title ? `Actions for ${title}` : "Actions"}
        className="relative w-full max-h-[85dvh] flex flex-col rounded-t-2xl border-t border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-2xl animate-[sheetUp_0.25s_ease-out] motion-reduce:animate-none"
      >
        <div className="px-5 pt-4 pb-3 flex items-start justify-between gap-4 border-b border-slate-200 dark:border-slate-800">
          <div className="min-w-0">
            <p className="text-base font-semibold text-slate-900 dark:text-slate-100 truncate">{title || "Actions"}</p>
            {subtitle && <p className="mt-0.5 text-sm text-slate-600 dark:text-slate-400 truncate">{subtitle}</p>}
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            className="-mr-1.5 -mt-1 w-9 h-9 shrink-0 inline-flex items-center justify-center rounded-md text-slate-600 dark:text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/40"
          >
            <FiX className="w-5 h-5" aria-hidden="true" />
          </button>
        </div>

        <ul className="overflow-y-auto overscroll-contain py-2 pb-[max(0.5rem,env(safe-area-inset-bottom))]">
          {actions.map((action) => {
            const Icon = action.icon;
            return (
              <li key={action.key}>
                <button
                  type="button"
                  disabled={action.disabled}
                  onClick={() => {
                    onClose();
                    action.onClick();
                  }}
                  className={`w-full min-h-[3.25rem] px-5 flex items-center gap-3 text-left text-sm font-medium transition-colors disabled:opacity-40 disabled:cursor-not-allowed focus-visible:outline-none focus-visible:bg-slate-50 dark:focus-visible:bg-slate-800 ${action.danger
                    ? "text-rose-700 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/30"
                    : "text-slate-900 dark:text-slate-100 hover:bg-slate-50 dark:hover:bg-slate-800"}`}
                >
                  <span
                    className={`w-9 h-9 shrink-0 rounded-md flex items-center justify-center ${action.danger
                      ? "bg-rose-50 dark:bg-rose-950/40 text-rose-600 dark:text-rose-400"
                      : "bg-[#4B98C8]/10 text-[#2C6F99] dark:text-[#7DBBE0]"}`}
                  >
                    {Icon && <Icon className="w-4 h-4" aria-hidden="true" />}
                  </span>
                  {action.label}
                </button>
              </li>
            );
          })}
        </ul>
      </div>
    </div>
  );
};

export default ActionSheet;
