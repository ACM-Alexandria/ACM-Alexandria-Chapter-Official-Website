import React from "react";

// Full-width segmented track: one grey track holding equal segments; the selected segment lifts up white.
// Icons plus short labels on phones so the segments always fit.
// tabs: [{ id, label, shortLabel?, icon }]
const AdminTabs = ({ tabs, active, onChange, ariaLabel = "Sections" }) => (
  <div
    role="tablist"
    aria-label={ariaLabel}
    className="flex w-full gap-1 p-1 rounded-xl bg-[#E4ECF4] dark:bg-[#15273A]/70"
  >
    {tabs.map((tab) => {
      const Icon = tab.icon;
      const selected = active === tab.id;
      return (
        <button
          key={tab.id}
          type="button"
          role="tab"
          aria-selected={selected}
          onClick={() => onChange(tab.id)}
          className={`flex-1 min-w-0 h-10 px-2.5 sm:px-4 inline-flex items-center justify-center gap-2 rounded-lg text-sm font-semibold whitespace-nowrap transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/50 ${selected
            ? "bg-white dark:bg-[#0F1C29] text-[#1D6A9C] dark:text-[#7DBBE0] shadow-sm"
            : "text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100"}`}
        >
          {Icon && <Icon className="w-4 h-4 shrink-0" aria-hidden="true" />}
          <span className="truncate hidden sm:inline">{tab.label}</span>
          <span className="truncate sm:hidden">{tab.shortLabel || tab.label}</span>
        </button>
      );
    })}
  </div>
);

export default AdminTabs;
