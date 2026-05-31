import React from "react";
import SkeletonBody from "./SkeletonBody";
import MetricCard from "./MetricCard";
import UserGrowthChart from "./UserGrowthChart";
import DepartmentChart from "./DepartmentChart";
import BatchChart from "./BatchChart";
import PopularityLeaderboard from "./PopularityLeaderboard";
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
      ]
    : [];

  return (
    <div style={{ animation: "fadeIn 0.4s ease both" }}>
      {loading && !insights ? (
        <SkeletonBody />
      ) : error ? (
        <div className="bg-white rounded-2xl border border-red-200 p-10 text-center max-w-md mx-auto shadow-sm">
          <div className="w-12 h-12 rounded-xl bg-red-50 text-red-500 flex items-center justify-center mx-auto mb-4">
            <FiAlertCircle className="w-6 h-6" />
          </div>
          <p className="text-red-700 font-bold text-sm mb-4">{error}</p>
          <button
            onClick={onRefresh}
            className="px-6 py-2.5 bg-red-600 hover:bg-red-700 text-white rounded-xl text-xs font-bold uppercase tracking-wider shadow active:scale-95 transition-all"
          >
            Try Again
          </button>
        </div>
      ) : (
        <div className="space-y-6">
          {/* Row 1: KPI Metrics Grid */}
          <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 xl:grid-cols-5 gap-4">
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
