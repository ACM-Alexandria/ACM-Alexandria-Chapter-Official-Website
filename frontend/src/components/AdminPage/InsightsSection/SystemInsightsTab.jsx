import React from "react";
import SkeletonBody from "./SkeletonBody";
import MetricCard from "./MetricCard";
import UserGrowthChart from "./UserGrowthChart";
import DepartmentChart from "./DepartmentChart";
import BatchChart from "./BatchChart";
import PopularityLeaderboard from "./PopularityLeaderboard";
import { dangerButtonClass } from "../SharedComponents/adminStyles";
import {
  FiUsers,
  FiFileText,
  FiUserCheck,
  FiLayers,
  FiCalendar,
  FiAward,
  FiCheckCircle,
  FiBookOpen,
  FiAlertCircle,
  FiRadio,
  FiVolume2,
} from "react-icons/fi";

const B = "#4B98C8";
const BD = "#205E85";

const SystemInsightsTab = ({ insights, loading, error, onRefresh }) => {
  const metrics = insights
    ? [
        { label: "Total Users",                  value: insights.totalUsers,                  icon: FiUsers,       color: B },
        { label: "Email Service Subscriptions",  value: insights.totalSubscriptions,          icon: FiFileText,    color: "#64748b" },
        { label: "High Board Members",           value: insights.totalBoardMembers,           icon: FiUserCheck,   color: BD },
        { label: "Committee Board",              value: insights.totalCommitteeBoardMembers,  icon: FiUserCheck,   color: "#7C5CDB" },
        { label: "Committees",                   value: insights.totalCommittees,             icon: FiLayers,      color: "#7C5CDB" },
        { label: "Events",                       value: insights.totalEvents,                 icon: FiCalendar,    color: "#E8724A" },
        { label: "Clubs",                        value: insights.totalClubs,                  icon: FiAward,       color: "#2CBFA1" },
        { label: "Events Registrations",         value: insights.totalEventRegistrations,     icon: FiCheckCircle, color: "#D94F7B" },
        { label: "Clubs Registrations",          value: insights.totalClubRegistrations,      icon: FiCheckCircle, color: "#F5A623" },
        { label: "Programs",                     value: insights.totalPrograms,               icon: FiBookOpen,    color: "#5A9BD5" },
        { label: "Radio Seasons",                value: insights.totalRadioSeasons,           icon: FiRadio,       color: "#8B5CF6" },
        { label: "Radio Episodes",               value: insights.totalRadioEpisodes,          icon: FiVolume2,     color: "#EC4899" },
      ]
    : [];

  return (
    <div style={{ animation: "fadeIn 0.4s ease both" }}>
      {loading && !insights ? (
        <SkeletonBody />
      ) : error ? (
        <div role="alert" className="rounded-xl border border-rose-200 dark:border-rose-900 bg-white dark:bg-slate-900 p-8 text-center max-w-md mx-auto">
          <div className="w-12 h-12 rounded-full bg-rose-50 dark:bg-rose-950/40 text-rose-600 dark:text-rose-400 flex items-center justify-center mx-auto mb-4">
            <FiAlertCircle className="w-6 h-6" aria-hidden="true" />
          </div>
          <p className="text-sm text-slate-800 dark:text-slate-200 mb-4">{error}</p>
          <button type="button" onClick={onRefresh} className={dangerButtonClass}>
            Try again
          </button>
        </div>
      ) : (
        <div className="space-y-6">
          {/* Row 1: KPI Metrics Grid */}
          <div className="grid grid-cols-2 md:grid-cols-3 xl:grid-cols-4 2xl:grid-cols-6 gap-3 sm:gap-4">
            {metrics.map((m, i) => (
              <MetricCard key={i} {...m} />
            ))}
          </div>

          {/* Row 2: User Growth (full width) */}
          <UserGrowthChart data={insights.userGrowth} />

          {/* Row 3: Department + Batch pie charts side by side */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            <DepartmentChart data={insights.usersByDepartment} />
            <BatchChart data={insights.usersByBatch} />
          </div>

          {/* Row 4: Top Events + Top Clubs leaderboards side by side */}
          <div className="grid grid-cols-1 md:grid-cols-2 gap-5">
            <PopularityLeaderboard
              title="Top Events"
              icon={FiCalendar}
              data={insights.popularEvents}
              accentColor="#E8724A"
            />
            <PopularityLeaderboard
              title="Top Clubs"
              icon={FiAward}
              data={insights.popularClubs}
              accentColor="#2CBFA1"
            />
          </div>
        </div>
      )}
    </div>
  );
};

export default SystemInsightsTab;
