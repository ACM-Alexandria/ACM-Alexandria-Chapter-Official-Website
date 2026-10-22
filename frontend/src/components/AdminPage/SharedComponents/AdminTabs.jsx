import React from "react";

// Segmented tab bar. Icons plus short labels on phones so the tabs always fit.
// tabs: [{ id, label, shortLabel?, icon }]
const AdminTabs = ({ tabs, active, onChange, ariaLabel = "Sections" }) => (
  <div
    role="tablist"
    aria-label={ariaLabel}
    className="flex w-full sm:w-fit gap-1 p-1 rounded-lg bg-slate-200/70 dark:bg-slate-800/70"
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
          className={`flex-1 sm:flex-none min-w-0 h-9 px-2.5 sm:px-4 inline-flex items-center justify-center gap-2 rounded-md text-sm font-medium whitespace-nowrap transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/50 ${selected
            ? "bg-white dark:bg-slate-950 text-[#2C6F99] dark:text-[#7DBBE0] shadow-sm"
            : "text-slate-700 dark:text-slate-300 hover:text-slate-900 dark:hover:text-slate-100"}`}
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
