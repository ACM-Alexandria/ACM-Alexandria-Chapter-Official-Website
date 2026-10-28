import React from "react";

// A setting with a title, a short description and an on/off switch.
const ToggleRow = ({ title, description, checked, onChange, disabled = false }) => (
  <div className="flex items-center justify-between gap-4 p-4 rounded-lg border border-slate-200 dark:border-ocean-745 bg-slate-50 dark:bg-ocean-916/60">
    <div className="min-w-0">
      <p className="text-sm font-medium text-slate-900 dark:text-slate-100">{title}</p>
      {description && <p className="mt-0.5 text-xs text-slate-600 dark:text-slate-400">{description}</p>}
    </div>
    <button
      type="button"
      role="switch"
      aria-checked={checked}
      aria-label={title}
      disabled={disabled}
      onClick={onChange}
      className={`relative inline-flex h-6 w-11 shrink-0 items-center rounded-full transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ocean-461/50 focus-visible:ring-offset-2 dark:focus-visible:ring-offset-ocean-890 disabled:opacity-50 ${checked ? "bg-ocean-461" : "bg-slate-300 dark:bg-slate-600"}`}
    >
      <span
        aria-hidden="true"
        className={`inline-block h-4 w-4 transform rounded-full bg-white shadow transition-transform ${checked ? "translate-x-6" : "translate-x-1"}`}
      />
    </button>
  </div>
);

export default ToggleRow;
