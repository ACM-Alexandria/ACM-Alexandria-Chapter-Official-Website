import React, { useEffect, useRef, useState } from "react";
import { FiChevronDown, FiCheck } from "react-icons/fi";

const HR_TAB_IDS = ["users", "committees", "events", "clubs", "programs", "exclusiveForms", "exclusive_forms"];

// Section navigation for resource management: a vertical list from lg up,
// a dropdown picker on phones and tablets.
const ManagementSidebar = ({ activeTab, setActiveTab, tabs, setSearchQuery, isHRMode = false }) => {
  const [pickerOpen, setPickerOpen] = useState(false);
  const pickerRef = useRef(null);

  // Allow only these specific tab IDs for HR; admins see everything
  const visibleTabs = isHRMode ? tabs.filter((tab) => HR_TAB_IDS.includes(tab.id)) : tabs;
  const current = visibleTabs.find((tab) => tab.id === activeTab) || visibleTabs[0];
  const CurrentIcon = current?.icon;

  useEffect(() => {
    if (!pickerOpen) return undefined;
    const handlePointer = (e) => {
      if (pickerRef.current && !pickerRef.current.contains(e.target)) setPickerOpen(false);
    };
    const handleKeyDown = (e) => {
      if (e.key === "Escape") setPickerOpen(false);
    };
    document.addEventListener("mousedown", handlePointer);
    document.addEventListener("keydown", handleKeyDown);
    return () => {
      document.removeEventListener("mousedown", handlePointer);
      document.removeEventListener("keydown", handleKeyDown);
    };
  }, [pickerOpen]);

  const select = (id) => {
    setActiveTab(id);
    setSearchQuery("");
    setPickerOpen(false);
  };

  return (
    <nav aria-label="Resources" className="w-full lg:w-60 shrink-0">
      {/* Phones and tablets: dropdown picker */}
      <div ref={pickerRef} className="relative lg:hidden">
        <button
          type="button"
          onClick={() => setPickerOpen((open) => !open)}
          aria-haspopup="listbox"
          aria-expanded={pickerOpen}
          className="w-full h-11 px-3 flex items-center justify-between gap-3 rounded-lg border border-slate-300 dark:border-[#28405A] bg-white dark:bg-[#0F1C29] text-slate-900 dark:text-slate-100 text-sm font-medium focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/40"
        >
          <span className="flex items-center gap-2.5 min-w-0">
            {CurrentIcon && <CurrentIcon className="w-4 h-4 shrink-0 text-[#2C6F99] dark:text-[#7DBBE0]" aria-hidden="true" />}
            <span className="truncate">{current?.label}</span>
          </span>
          <FiChevronDown className={`w-4 h-4 shrink-0 text-slate-600 dark:text-slate-400 transition-transform ${pickerOpen ? "rotate-180" : ""}`} aria-hidden="true" />
        </button>

        {pickerOpen && (
          <ul
            role="listbox"
            aria-label="Resources"
            className="absolute z-30 left-0 right-0 mt-1.5 max-h-[60vh] overflow-y-auto overscroll-contain rounded-lg border border-slate-200 dark:border-[#28405A] bg-white dark:bg-[#0F1C29] shadow-lg py-1"
          >
            {visibleTabs.map((tab) => {
              const Icon = tab.icon;
              const selected = tab.id === current?.id;
              return (
                <li key={tab.id} role="option" aria-selected={selected}>
                  <button
                    type="button"
                    onClick={() => select(tab.id)}
                    className={`w-full min-h-[2.75rem] px-3 flex items-center gap-2.5 text-left text-sm font-medium transition-colors hover:bg-slate-50 dark:hover:bg-[#15273A] ${selected ? "text-[#2C6F99] dark:text-[#7DBBE0]" : "text-slate-800 dark:text-slate-200"}`}
                  >
                    {Icon && <Icon className="w-4 h-4 shrink-0" aria-hidden="true" />}
                    <span className="flex-1 truncate">{tab.label}</span>
                    {selected && <FiCheck className="w-4 h-4 shrink-0" aria-hidden="true" />}
                  </button>
                </li>
              );
            })}
          </ul>
        )}
      </div>

      {/* Desktop: vertical list */}
      <ul className="hidden lg:flex flex-col gap-1 rounded-2xl border border-slate-200 dark:border-[#1E3248] bg-white dark:bg-[#0F1C29] p-2">
        {visibleTabs.map((tab) => {
          const Icon = tab.icon;
          const selected = tab.id === current?.id;
          return (
            <li key={tab.id}>
              <button
                type="button"
                onClick={() => select(tab.id)}
                aria-current={selected ? "page" : undefined}
                className={`w-full h-10 px-3 flex items-center gap-3 rounded-lg text-left text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/40 ${selected
                  ? "bg-[#E2F0FA] dark:bg-[#16364C] text-[#1D6A9C] dark:text-[#7DBBE0]"
                  : "text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-[#15273A]"}`}
              >
                {Icon && <Icon className="w-4 h-4 shrink-0" aria-hidden="true" />}
                <span className="truncate">{tab.label}</span>
              </button>
            </li>
          );
        })}
      </ul>
    </nav>
  );
};

export default ManagementSidebar;
