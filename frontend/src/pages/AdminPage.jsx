import React, { useState, useEffect } from "react";
import Navbar from "../components/HomePage/Navbar";
import { fetchInsights } from "../services/adminService";
import UserGrowthChart from "../components/AdminPage/InsightsSection/UserGrowthChart";
import DepartmentChart from "../components/AdminPage/InsightsSection/DepartmentChart";
import BatchChart from "../components/AdminPage/InsightsSection/BatchChart";
import PopularityLeaderboard from "../components/AdminPage/InsightsSection/PopularityLeaderboard";
import MetricCard from "../components/AdminPage/InsightsSection/MetricCard";
import SkeletonBody from "../components/AdminPage/InsightsSection/SkeletonBody";
import {
  FiUsers,
  FiCalendar,
  FiAward,
  FiBookOpen,
  FiUserCheck,
  FiLayers,
  FiCheckCircle,
  FiFileText,
  FiRefreshCw,
  FiLock,
  FiTrendingUp,
  FiSliders,
  FiActivity,
  FiAlertCircle,
} from "react-icons/fi";

/* ─── Brand ─── */
const B = "#4B98C8";
const BD = "#205E85";

/* ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
   ADMIN PAGE
   ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ */
const AdminPage = () => {
  const [insights, setInsights] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [activeTab, setActiveTab] = useState("insights");

  const loadInsights = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await fetchInsights();
      setInsights(data);
    } catch (err) {
      console.error("Error loading admin insights:", err);
      setError(err.message || "Failed to load dashboard insights.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadInsights();
  }, []);

  const metrics = insights
    ? [
        { label: "Total Users",    value: insights.totalUsers,              icon: FiUsers,       color: B },
        { label: "High Board Members", value: insights.totalBoardMembers,   icon: FiUserCheck,   color: BD },
        { label: "Committee Board", value: insights.totalCommitteeBoardMembers, icon: FiUserCheck, color: "#7C5CDB" },
        { label: "Committees",     value: insights.totalCommittees,         icon: FiLayers,      color: "#7C5CDB" },
        { label: "Events",        value: insights.totalEvents,             icon: FiCalendar,    color: "#E8724A" },
        { label: "Clubs",         value: insights.totalClubs,              icon: FiAward,       color: "#2CBFA1" },
        { label: "Programs",      value: insights.totalPrograms,           icon: FiBookOpen,    color: "#5A9BD5" },
        { label: "Events Registrations", value: insights.totalEventRegistrations, icon: FiCheckCircle, color: "#D94F7B" },
        { label: "Clubs Registrations",  value: insights.totalClubRegistrations,  icon: FiCheckCircle, color: "#F5A623" },
        { label: "Programs Subscriptions", value: insights.totalSubscriptions,      icon: FiFileText,    color: "#64748b" },
      ]
    : [];

  const tabs = [
    { id: "insights",   label: "System Insights",    icon: FiTrendingUp },
    { id: "management", label: "Resource Management", icon: FiSliders },
  ];

  return (
    <div className="min-h-screen bg-slate-50 text-slate-800 font-sans">
      <Navbar activeSection="" />

      <main className="pt-[100px] pb-20 px-4 sm:px-6 md:px-10 lg:px-14 max-w-[1400px] mx-auto">
        {/* ── Header ── */}
        <div
          className="flex flex-col sm:flex-row sm:items-end justify-between gap-4 mb-8"
          style={{ animation: "fadeIn 0.5s ease both" }}
        >
          <div>
            <div className="flex items-center gap-2 mb-1">
              <FiActivity className="w-4 h-4" style={{ color: B }} />
              <span className="text-[10px] font-bold uppercase tracking-[0.2em]" style={{ color: B }}>
                Administration
              </span>
            </div>
            <h1 className="text-2xl sm:text-3xl font-extrabold text-slate-900 tracking-tight">
              Dashboard
            </h1>
          </div>

          <button
            onClick={loadInsights}
            disabled={loading}
            className="flex items-center gap-2 px-4 py-2.5 bg-white border border-slate-200 text-slate-600 hover:text-slate-900 hover:border-slate-300 rounded-xl shadow-sm text-xs font-bold tracking-wide uppercase active:scale-95 transition-all disabled:opacity-40"
          >
            <FiRefreshCw className={`w-3.5 h-3.5 ${loading ? "animate-spin" : ""}`} style={{ color: B }} />
            Refresh
          </button>
        </div>

        {/* ── Tab Bar ── */}
        <div className="flex gap-1 bg-white border border-slate-200/80 p-1 rounded-xl w-fit shadow-sm mb-8">
          {tabs.map((tab) => (
            <button
              key={tab.id}
              onClick={() => setActiveTab(tab.id)}
              className={`flex items-center gap-2 px-5 py-2.5 rounded-lg text-xs font-bold uppercase tracking-wider transition-all duration-200 ${
                activeTab === tab.id
                  ? "text-white shadow-md"
                  : "text-slate-500 hover:text-slate-700 hover:bg-slate-50"
              }`}
              style={activeTab === tab.id ? { background: `linear-gradient(135deg, ${B}, ${BD})` } : {}}
            >
              <tab.icon className="w-3.5 h-3.5" />
              {tab.label}
            </button>
          ))}
        </div>

        {/* ━━━━ TAB 1: INSIGHTS ━━━━ */}
        {activeTab === "insights" && (
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
                  onClick={loadInsights}
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
        )}

        {/* ━━━━ TAB 2: MANAGEMENT (Placeholder) ━━━━ */}
        {activeTab === "management" && (
          <div>
          </div>
        )}
      </main>
    </div>
  );
};

export default AdminPage;
