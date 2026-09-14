import React from "react";

const BRAND = "#4B98C8";
const BRAND_DARK = "#205E85";

const ManagementSidebar = ({ activeTab, setActiveTab, tabs, setSearchQuery }) => {
  return (
    <div className="w-full lg:w-64 bg-white dark:bg-slate-900 rounded-2xl border border-slate-200/80 dark:border-slate-700 p-3 shadow-sm dark:shadow-slate-950/40 shrink-0">
      <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest px-3 mb-3">
        Resources
      </p>
      <div className="flex flex-row lg:flex-col overflow-x-auto lg:overflow-x-visible gap-1 pb-2 lg:pb-0 scrollbar-hide">
        {tabs.map((tab) => {
          const Icon = tab.icon;
          const active = activeTab === tab.id;
          return (
            <button
              key={tab.id}
              onClick={() => {
                setActiveTab(tab.id);
                setSearchQuery("");
              }}
              className={`flex items-center gap-3 px-4 py-3 rounded-xl text-xs font-bold uppercase tracking-wider transition-all duration-200 shrink-0 lg:shrink text-left ${
                active
                  ? "text-white shadow-sm"
                  : "text-slate-500 dark:text-slate-300 hover:text-slate-800 dark:hover:text-slate-100 hover:bg-slate-50 dark:hover:bg-slate-800"
              }`}
              style={active ? { background: `linear-gradient(135deg, ${BRAND}, ${BRAND_DARK})` } : {}}
            >
              <Icon className="w-4 h-4" />
              {tab.label}
            </button>
          );
        })}
      </div>
    </div>
  );
};

export default ManagementSidebar;
