import React from "react";

const BRAND = "#4B98C8";
const BRAND_DARK = "#205E85";

const ManagementSidebar = ({ activeTab, setActiveTab, tabs, setSearchQuery }) => {
  return (
    <div className="w-full lg:w-64 bg-white rounded-2xl border border-slate-200/80 p-3 shadow-sm shrink-0">
      <p className="text-[10px] font-bold text-slate-400 uppercase tracking-widest px-3 mb-3">
        Resources
      </p>
      <div className="flex flex-row lg:flex-col overflow-x-auto lg:overflow-x-visible gap-1 pb-2 lg:pb-0">
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
                  : "text-slate-500 hover:text-slate-800 hover:bg-slate-50"
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
