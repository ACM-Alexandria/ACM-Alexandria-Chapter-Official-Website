import React, { useState, useEffect } from "react";
import {
  FiCheckCircle,
  FiAlertCircle,
  FiSearch,
  FiFilter,
  FiLoader,
  FiEye,
  FiX,
  FiMaximize2,
  FiChevronLeft,
  FiChevronRight,
} from "react-icons/fi";
import {
  getAllFeatures,
  getAllBugs,
  toggleFeatureStatus,
  toggleBugStatus,
} from "../../../services/feedbackService";

const BRAND = "#4B98C8";
const BRAND_DARK = "#205E85";

const FeedbackTab = () => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters & Search
  const [search, setSearch] = useState("");
  const [typeFilter, setTypeFilter] = useState("all"); // 'all' | 'feature' | 'bug'
  const [statusFilter, setStatusFilter] = useState("all"); // 'all' | 'new' | 'done'

  // Actions loading states
  const [actionLoadingId, setActionLoadingId] = useState(null);

  // Lightbox modal state
  const [lightboxUrls, setLightboxUrls] = useState([]);
  const [lightboxIndex, setLightboxIndex] = useState(null);

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (!lightboxUrls || lightboxUrls.length === 0 || lightboxIndex === null) return;
      if (e.key === "ArrowLeft") {
        setLightboxIndex((prev) => (prev > 0 ? prev - 1 : lightboxUrls.length - 1));
      } else if (e.key === "ArrowRight") {
        setLightboxIndex((prev) => (prev < lightboxUrls.length - 1 ? prev + 1 : 0));
      } else if (e.key === "Escape") {
        setLightboxUrls([]);
        setLightboxIndex(null);
      }
    };

    window.addEventListener("keydown", handleKeyDown);
    return () => {
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [lightboxUrls, lightboxIndex]);

  const fetchFeedback = async () => {
    setLoading(true);
    setError(null);
    try {
      const [features, bugs] = await Promise.all([
        getAllFeatures(),
        getAllBugs(),
      ]);

      // Tag items so we can distinguish and sort them
      const taggedFeatures = features.map((f) => ({ ...f, type: "feature" }));
      const taggedBugs = bugs.map((b) => ({ ...b, type: "bug" }));

      // Merge both arrays
      const merged = [...taggedFeatures, ...taggedBugs];

      // Sort: NEW status first, then by createdAt DESC.
      // NEW status comes first, DONE status comes next.
      merged.sort((a, b) => {
        const aStatus = a.status === "NEW" ? 0 : 1;
        const bStatus = b.status === "NEW" ? 0 : 1;
        if (aStatus !== bStatus) {
          return aStatus - bStatus;
        }
        // If status is the same, sort by date desc
        return new Date(b.createdAt) - new Date(a.createdAt);
      });

      setItems(merged);
    } catch (err) {
      setError(err.message || "Failed to load feedback logs.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFeedback();
  }, []);

  const handleToggleStatus = async (id, itemType, currentStatus) => {
    setError(null);
    setActionLoadingId(id);
    try {
      if (itemType === "feature") {
        await toggleFeatureStatus(id);
      } else {
        await toggleBugStatus(id);
      }

      // Update state locally
      setItems((prevItems) =>
        prevItems
          .map((item) => {
            if (item.id === id && item.type === itemType) {
              const nextStatus = currentStatus === "NEW" ? "DONE" : "NEW";
              return { ...item, status: nextStatus };
            }
            return item;
          })
          .sort((a, b) => {
            const aStatus = a.status === "NEW" ? 0 : 1;
            const bStatus = b.status === "NEW" ? 0 : 1;
            if (aStatus !== bStatus) {
              return aStatus - bStatus;
            }
            return new Date(b.createdAt) - new Date(a.createdAt);
          })
      );
    } catch (err) {
      setError(err.message || "Failed to update feedback status.");
    } finally {
      setActionLoadingId(null);
    }
  };

  // Filter and search computation
  const filteredItems = items.filter((item) => {
    const matchesSearch =
      item.name?.toLowerCase().includes(search.toLowerCase()) ||
      item.description?.toLowerCase().includes(search.toLowerCase()) ||
      item.reporterName?.toLowerCase().includes(search.toLowerCase()) ||
      item.reporterEmail?.toLowerCase().includes(search.toLowerCase());

    const matchesType = typeFilter === "all" || item.type === typeFilter;
    const matchesStatus = statusFilter === "all" || item.status?.toLowerCase() === statusFilter;

    return matchesSearch && matchesType && matchesStatus;
  });

  const formatDate = (dateStr) => {
    if (!dateStr) return "";
    const date = new Date(dateStr);
    return date.toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div className="space-y-6 flex-1 flex flex-col justify-between">
      {/* Tab Header Controls */}
      <div>
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
          <div>
            <h2 className="text-lg font-extrabold text-slate-800 tracking-tight">
              Grow & Feedback Logs
            </h2>
            <p className="text-xs text-slate-400 font-medium">
              View user feature suggestions, bug reports, and mark resolved items as done.
            </p>
          </div>
          <button
            onClick={fetchFeedback}
            className="flex items-center gap-2 px-4 py-2.5 bg-slate-50 hover:bg-slate-100 text-slate-600 rounded-xl text-xs font-bold uppercase tracking-wide border border-slate-200 shadow-sm transition-all active:scale-95 cursor-pointer"
          >
            Refresh Logs
          </button>
        </div>

        {/* Filters and search row */}
        <div className="flex flex-col lg:flex-row gap-4 mb-6">
          {/* Search Box */}
          <div className="relative flex-1">
            <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
              <FiSearch className="w-4 h-4" />
            </span>
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by title, description, or reporter..."
              className="w-full pl-10 pr-4 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 placeholder-slate-400 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
            />
          </div>

          <div className="flex flex-wrap gap-3">
            {/* Type Filter */}
            <div className="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-xl px-3 py-1.5">
              <FiFilter className="w-3.5 h-3.5 text-slate-400" />
              <select
                value={typeFilter}
                onChange={(e) => setTypeFilter(e.target.value)}
                className="bg-transparent text-slate-700 text-xs font-bold focus:outline-none cursor-pointer"
              >
                <option value="all">All Types</option>
                <option value="feature">Feature Requests</option>
                <option value="bug">Bug Reports</option>
              </select>
            </div>

            {/* Status Filter */}
            <div className="flex items-center gap-2 bg-slate-50 border border-slate-200 rounded-xl px-3 py-1.5">
              <FiCheckCircle className="w-3.5 h-3.5 text-slate-400" />
              <select
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value)}
                className="bg-transparent text-slate-700 text-xs font-bold focus:outline-none cursor-pointer"
              >
                <option value="all">All Statuses</option>
                <option value="new">New (Unresolved)</option>
                <option value="done">Done (Resolved)</option>
              </select>
            </div>
          </div>
        </div>

        {/* Error Alert */}
        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-600 rounded-2xl p-4 flex items-center gap-3">
            <FiAlertCircle className="w-5 h-5 shrink-0" />
            <p className="text-xs font-bold">{error}</p>
          </div>
        )}

        {/* Main List */}
        {loading ? (
          <div className="flex flex-col items-center justify-center py-20 gap-3">
            <FiLoader className="w-10 h-10 text-[#4B98C8] animate-spin" />
            <p className="text-xs text-slate-400 font-bold uppercase tracking-wider">
              Loading Feedback logs…
            </p>
          </div>
        ) : filteredItems.length === 0 ? (
          <div className="text-center py-20 border-2 border-dashed border-slate-100 rounded-3xl bg-slate-50/50">
            <p className="text-sm font-bold text-slate-500">No feedback items match your criteria.</p>
            <p className="text-xs text-slate-400 mt-1">Try relaxing filters or search terms.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-6">
            {filteredItems.map((item, idx) => {
              const isFeature = item.type === "feature";
              const isDone = item.status === "DONE";
              const isActionLoading = actionLoadingId === item.id;

              return (
                <div
                  key={`${item.type}-${item.id}`}
                  className={`border rounded-2xl p-6 transition-all bg-white relative overflow-hidden ${
                    isDone
                      ? "border-slate-150 bg-slate-50/30 opacity-75"
                      : isFeature
                      ? "border-emerald-100 hover:shadow-md hover:border-emerald-200"
                      : "border-rose-100 hover:shadow-md hover:border-rose-200"
                  }`}
                >
                  {/* Status & Type Badges Row */}
                  <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
                    <div className="flex items-center gap-2.5">
                      {/* Type Badge */}
                      <span
                        className={`text-[9px] font-black uppercase tracking-wider px-2.5 py-1 rounded-full ${
                          isFeature
                            ? "bg-emerald-50 text-emerald-600 border border-emerald-100"
                            : "bg-rose-50 text-rose-600 border border-rose-100"
                        }`}
                      >
                        {isFeature ? " Feature" : " Bug"}
                      </span>

                      {/* Status Badge */}
                      <span
                        className={`text-[9px] font-black uppercase tracking-wider px-2.5 py-1 rounded-full ${
                          isDone
                            ? "bg-slate-100 text-slate-500 border border-slate-200"
                            : "bg-amber-50 text-amber-600 border border-amber-200 animate-pulse"
                        }`}
                      >
                        {isDone ? "Done" : "New"}
                      </span>
                    </div>

                    {/* Timestamp */}
                    <span className="text-[10px] text-slate-400 font-bold">
                      {formatDate(item.createdAt)}
                    </span>
                  </div>

                  {/* Title & Description */}
                  <div className="space-y-2 mb-4">
                    <h3 className="text-sm font-black text-slate-800 tracking-tight leading-snug">
                      {item.name}
                    </h3>
                    <p className="text-xs text-slate-500 font-medium leading-relaxed whitespace-pre-wrap">
                      {item.description}
                    </p>
                  </div>

                  {/* Screenshot Gallery for Bugs */}
                  {!isFeature && item.imageUrls && item.imageUrls.length > 0 && (
                    <div className="mb-4 space-y-2">
                      <p className="text-[9px] font-bold text-slate-400 uppercase tracking-wider">
                        Attached Screenshots ({item.imageUrls.length})
                      </p>
                      <div className="flex flex-wrap gap-3">
                        {item.imageUrls.map((url, imgIdx) => (
                          <div
                            key={imgIdx}
                            onClick={() => {
                              setLightboxUrls(item.imageUrls);
                              setLightboxIndex(imgIdx);
                            }}
                            className="group relative w-20 h-14 rounded-lg overflow-hidden border border-slate-200 cursor-zoom-in bg-slate-50 shadow-sm"
                          >
                            <img
                              src={url}
                              alt="Attached Bug screenshot"
                              className="w-full h-full object-cover transition-transform group-hover:scale-110"
                            />
                            <div className="absolute inset-0 bg-slate-900/30 opacity-0 group-hover:opacity-100 flex items-center justify-center transition-all">
                              <FiEye className="w-3.5 h-3.5 text-white" />
                            </div>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Reporter Info & Action Row */}
                  <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 pt-4 border-t border-slate-100/60 mt-4">
                    {/* Reporter */}
                    <div className="text-[10px] font-semibold text-slate-400">
                      Reporter:{" "}
                      {item.reporterName ? (
                        <span className="text-slate-600 font-extrabold">
                          {item.reporterName} ({item.reporterEmail})
                        </span>
                      ) : item.reporterEmail ? (
                        <span className="text-slate-600 font-extrabold">
                          Anonymous ({item.reporterEmail})
                        </span>
                      ) : (
                        <span className="text-slate-400 italic">Anonymous</span>
                      )}
                    </div>

                    {/* Action Button */}
                    <button
                      onClick={() => handleToggleStatus(item.id, item.type, item.status)}
                      disabled={isActionLoading}
                      className={`px-4 py-2 text-[10px] font-black uppercase tracking-wider rounded-xl shadow active:scale-95 transition-all disabled:opacity-50 flex items-center gap-1.5 cursor-pointer ml-auto sm:ml-0 ${
                        isDone
                          ? "bg-slate-100 hover:bg-slate-200 text-slate-600 border border-slate-200"
                          : "bg-emerald-500 hover:bg-emerald-600 text-white"
                      }`}
                    >
                      {isActionLoading && <FiLoader className="w-3 h-3 animate-spin" />}
                      {isActionLoading ? "Saving…" : isDone ? "Mark as New" : "Mark as Done"}
                    </button>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>

      {/* Fullscreen Lightbox Modal */}
      {lightboxIndex !== null && lightboxUrls.length > 0 && (
        <div
          className="fixed inset-0 z-[1000] flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md"
          onClick={() => {
            setLightboxUrls([]);
            setLightboxIndex(null);
          }}
        >
          {/* Left Arrow Button */}
          {lightboxUrls.length > 1 && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                setLightboxIndex((prev) => (prev > 0 ? prev - 1 : lightboxUrls.length - 1));
              }}
              className="absolute left-6 z-[1001] p-3 text-white/70 hover:text-white bg-white/5 hover:bg-white/10 rounded-full transition-all active:scale-90 cursor-pointer"
              title="Previous image"
            >
              <FiChevronLeft className="w-8 h-8" />
            </button>
          )}

          <div
            onClick={(e) => e.stopPropagation()}
            className="relative max-w-5xl max-h-[90vh] flex flex-col items-center"
          >
            {/* Close Button */}
            <button
              onClick={() => {
                setLightboxUrls([]);
                setLightboxIndex(null);
              }}
              className="absolute -top-12 right-0 p-2 text-white/70 hover:text-white hover:bg-white/10 rounded-full transition-colors cursor-pointer"
              title="Close image"
            >
              <FiX className="w-6 h-6" />
            </button>

            {/* Maximize / External link */}
            <a
              href={lightboxUrls[lightboxIndex]}
              target="_blank"
              rel="noreferrer"
              className="absolute -top-12 right-12 p-2 text-white/70 hover:text-white hover:bg-white/10 rounded-full transition-colors cursor-pointer"
              title="Open in new tab"
            >
              <FiMaximize2 className="w-5 h-5" />
            </a>

            {/* Image Counter */}
            {lightboxUrls.length > 1 && (
              <span className="absolute -top-10 left-0 text-white/70 text-xs font-bold uppercase tracking-wider">
                Image {lightboxIndex + 1} of {lightboxUrls.length}
              </span>
            )}

            <img
              src={lightboxUrls[lightboxIndex]}
              alt="Fullscreen screenshot"
              className="w-full h-auto max-h-[80vh] rounded-2xl shadow-2xl border border-white/10 object-contain"
            />
          </div>

          {/* Right Arrow Button */}
          {lightboxUrls.length > 1 && (
            <button
              onClick={(e) => {
                e.stopPropagation();
                setLightboxIndex((prev) => (prev < lightboxUrls.length - 1 ? prev + 1 : 0));
              }}
              className="absolute right-6 z-[1001] p-3 text-white/70 hover:text-white bg-white/5 hover:bg-white/10 rounded-full transition-all active:scale-90 cursor-pointer"
              title="Next image"
            >
              <FiChevronRight className="w-8 h-8" />
            </button>
          )}
        </div>
      )}
    </div>
  );
};

export default FeedbackTab;
