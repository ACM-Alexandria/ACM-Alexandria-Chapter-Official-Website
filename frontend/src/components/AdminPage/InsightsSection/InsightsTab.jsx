import React, { useState } from "react";
import { FiRefreshCw } from "react-icons/fi";
import OverviewPanel from "./OverviewPanel";
import SectionPanel from "./SectionPanel";
import useInsights from "../../../hooks/useInsights";
import { noticeClass, fieldClass } from "../SharedComponents/adminStyles";

const PERIODS = [
  { id: "7d", label: "7d", long: "Last 7 days" },
  { id: "30d", label: "30d", long: "Last 30 days" },
  { id: "90d", label: "90d", long: "Last 90 days" },
  { id: "all", label: "All", long: "All time" },
];

// Section tabs under the overview, in the order the backend serves them.
const SECTION_TABS = [
  { id: "committees", label: "Committees" },
  { id: "events", label: "Events" },
  { id: "clubs", label: "Clubs" },
  { id: "programs", label: "Programs" },
  { id: "points", label: "Points" },
  { id: "radio", label: "Radio" },
  { id: "communication", label: "Communication" },
];

const Skeleton = ({ className }) => (
  <div className={`rounded-2xl bg-slate-100 dark:bg-[#15273A]/60 animate-pulse motion-reduce:animate-none ${className}`} />
);

const ErrorNotice = ({ message, onRetry }) => (
  <div role="alert" className={`${noticeClass.error} flex flex-wrap items-center justify-between gap-3`}>
    <span>{message}</span>
    <button type="button" onClick={onRetry} className="text-sm font-medium underline underline-offset-2">
      Try again
    </button>
  </div>
);

// Admin insights: the global overview on top, then tabs for each area of the system.
const InsightsTab = () => {
  const [period, setPeriod] = useState("30d");
  const [section, setSection] = useState("committees");
  const overview = useInsights("overview", period);
  const current = useInsights(section, period);
  const periodLabel = PERIODS.find((p) => p.id === period)?.long;

  const refresh = () => {
    overview.reload();
    current.reload();
  };
  const refreshing = (overview.loading && overview.data) || (current.loading && current.data);

  return (
    <div className="flex flex-col gap-8">
      <header className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div className="min-w-0">
          <h2 className="text-xl font-semibold tracking-tight text-slate-900 dark:text-slate-100">Insights</h2>
          <p className="mt-0.5 text-sm text-slate-600 dark:text-slate-400">
            {periodLabel}. Totals are all-time; “this period” figures, charts and rankings follow the period.
          </p>
        </div>
        <div className="flex items-center gap-2">
          <div role="radiogroup" aria-label="Period" className="inline-flex items-center gap-1">
            {PERIODS.map((p) => {
              const active = p.id === period;
              return (
                <button
                  key={p.id}
                  type="button"
                  role="radio"
                  aria-checked={active}
                  aria-label={p.long}
                  onClick={() => setPeriod(p.id)}
                  className={`h-8 min-w-[2.75rem] px-2.5 rounded-lg border text-sm font-medium tabular-nums transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/50 ${
                    active
                      ? "border-[#4B98C8] text-[#2C6F99] dark:text-[#7DBBE0] bg-[#4B98C8]/10"
                      : "border-slate-200 dark:border-[#1E3248] bg-white dark:bg-[#0F1C29] text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100"
                  }`}
                >
                  {p.label}
                </button>
              );
            })}
          </div>
          <button
            type="button"
            onClick={refresh}
            aria-label="Refresh insights"
            title="Refresh"
            className="w-9 h-9 inline-flex items-center justify-center rounded-lg border border-slate-200 dark:border-[#1E3248] bg-white dark:bg-[#0F1C29] text-slate-600 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-[#15273A] transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/50"
          >
            <FiRefreshCw className={`w-4 h-4 ${refreshing ? "animate-spin motion-reduce:animate-none" : ""}`} aria-hidden="true" />
          </button>
        </div>
      </header>

      {/* Global overview */}
      <section aria-label="Overview">
        {overview.error && !overview.data ? (
          <ErrorNotice message={overview.error} onRetry={overview.reload} />
        ) : !overview.data ? (
          <div className="flex flex-col gap-4" aria-busy="true">
            <div className="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-5 gap-3">
              {Array.from({ length: 5 }, (_, i) => <Skeleton key={i} className="h-32" />)}
            </div>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
              <Skeleton className="h-64" />
              <Skeleton className="h-64" />
            </div>
          </div>
        ) : (
          <OverviewPanel key={period} data={overview.data} />
        )}
      </section>

      {/* Area tabs */}
      <section aria-label="Insights by area" className="flex flex-col gap-4">
        {/* Phones: a full-width picker. Wider screens: the tab bar. */}
        <div className="sm:hidden">
          <label htmlFor="insights-area" className="sr-only">Area</label>
          <select id="insights-area" value={section} onChange={(e) => setSection(e.target.value)} className={`${fieldClass} font-medium`}>
            {SECTION_TABS.map((tab) => (
              <option key={tab.id} value={tab.id}>{tab.label}</option>
            ))}
          </select>
        </div>

        <div
          role="tablist"
          aria-label="Areas"
          className="hidden sm:flex flex-wrap gap-1.5 self-start max-w-full"
        >
          {SECTION_TABS.map((tab) => {
            const active = tab.id === section;
            return (
              <button
                key={tab.id}
                type="button"
                role="tab"
                aria-selected={active}
                onClick={() => setSection(tab.id)}
                className={`h-9 px-3.5 whitespace-nowrap rounded-lg text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/50 ${
                  active ? "bg-[#E2F0FA] dark:bg-[#16364C] text-[#1D6A9C] dark:text-[#7DBBE0]" : "text-slate-600 dark:text-slate-400 hover:bg-white dark:hover:bg-[#0F1C29] hover:text-slate-900 dark:hover:text-slate-100"
                }`}
              >
                {tab.label}
              </button>
            );
          })}
        </div>

        <div role="tabpanel" aria-label={SECTION_TABS.find((t) => t.id === section)?.label}>
          {current.error && !current.data ? (
            <ErrorNotice message={current.error} onRetry={current.reload} />
          ) : !current.data ? (
            <div className="flex flex-col gap-4" aria-busy="true">
              <div className="grid grid-cols-2 lg:grid-cols-4 gap-3">
                {Array.from({ length: 4 }, (_, i) => <Skeleton key={i} className="h-28" />)}
              </div>
              <Skeleton className="h-72" />
            </div>
          ) : (
            <SectionPanel key={`${section}:${period}`} data={current.data} />
          )}
        </div>
      </section>
    </div>
  );
};

export default InsightsTab;
