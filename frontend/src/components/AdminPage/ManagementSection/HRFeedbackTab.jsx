import React, { useState, useEffect } from "react";
import { FiAlertCircle, FiSearch, FiLoader, FiFilter } from "react-icons/fi";
import { getAllHRFeedback } from "../../../services/hrFeedbackService";

const HRFeedbackTab = () => {
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  // Filters & Search
  const [search, setSearch] = useState("");
  const [typeFilter, setTypeFilter] = useState("all");

  const fetchFeedback = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await getAllHRFeedback();
      const sorted = data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
      setItems(sorted);
    } catch (err) {
      setError(err.message || "Failed to load HR feedback.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchFeedback();
  }, []);

  const filteredItems = items.filter((item) => {
    const matchesSearch =
      item.content?.toLowerCase().includes(search.toLowerCase()) ||
      item.reporterName?.toLowerCase().includes(search.toLowerCase()) ||
      item.reporterEmail?.toLowerCase().includes(search.toLowerCase());

    const matchesType = typeFilter === "all" || item.type === typeFilter;

    return matchesSearch && matchesType;
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
      <div>
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
          <div>
            <h2 className="text-lg font-extrabold text-slate-800 dark:text-slate-100 tracking-tight">
              HR Feedback
            </h2>
            <p className="text-xs text-slate-400 font-medium">
              View anonymous suggestions and complaints submitted by members.
            </p>
          </div>
          <button
            onClick={fetchFeedback}
            className="flex items-center gap-2 px-4 py-2.5 bg-slate-50 dark:bg-slate-800 hover:bg-slate-100 dark:hover:bg-slate-700 text-slate-600 dark:text-slate-200 rounded-xl text-xs font-bold uppercase tracking-wide border border-slate-200 dark:border-slate-600 shadow-sm transition-all active:scale-95 cursor-pointer"
          >
            Refresh
          </button>
        </div>

        <div className="flex flex-col lg:flex-row gap-4 mb-6">
          <div className="relative flex-1">
            <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
              <FiSearch className="w-4 h-4" />
            </span>
            <input
              type="text"
              value={search}
              onChange={(e) => setSearch(e.target.value)}
              placeholder="Search by content or reporter..."
              className="w-full pl-10 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 text-slate-800 dark:text-slate-100 placeholder-slate-400 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
            />
          </div>

          <div className="flex items-center gap-2 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 rounded-xl px-3 py-1.5">
            <FiFilter className="w-3.5 h-3.5 text-slate-400" />
            <select
              value={typeFilter}
              onChange={(e) => setTypeFilter(e.target.value)}
              className="bg-slate-50 dark:bg-slate-800 text-slate-800 dark:text-slate-100 text-xs font-bold focus:outline-none"
            >
              <option value="all">All Types</option>
              <option value="SUGGESTION">Suggestions</option>
              <option value="COMPLAINT">Complaints</option>
            </select>
          </div>
        </div>

        {error && (
          <div className="mb-6 bg-red-50 border border-red-200 text-red-600 rounded-2xl p-4 flex items-center gap-3">
            <FiAlertCircle className="w-5 h-5 shrink-0" />
            <p className="text-xs font-bold">{error}</p>
          </div>
        )}

        {loading ? (
          <div className="flex flex-col items-center justify-center py-20 gap-3">
            <FiLoader className="w-10 h-10 text-[#4B98C8] animate-spin" />
            <p className="text-xs text-slate-400 font-bold uppercase tracking-wider">
              Loading HR Feedback…
            </p>
          </div>
        ) : filteredItems.length === 0 ? (
          <div className="text-center py-20 border-2 border-dashed border-slate-100 dark:border-slate-700 rounded-3xl bg-slate-50/50 dark:bg-slate-800/50">
            <p className="text-sm font-bold text-slate-500">No feedback items match your criteria.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 gap-6">
            {filteredItems.map((item, idx) => {
              const isSuggestion = item.type === "SUGGESTION";

              return (
                <div
                  key={item.id}
                  className={`border rounded-2xl p-6 transition-all bg-white dark:bg-slate-900 relative overflow-hidden ${
                    isSuggestion
                      ? "border-blue-100 hover:shadow-md hover:border-blue-200"
                      : "border-orange-100 hover:shadow-md hover:border-orange-200"
                  }`}
                >
                  <div className="flex flex-wrap items-center justify-between gap-3 mb-4">
                    <span
                      className={`text-[9px] font-black uppercase tracking-wider px-2.5 py-1 rounded-full ${
                        isSuggestion
                          ? "bg-blue-50 text-blue-600 border border-blue-100"
                          : "bg-orange-50 text-orange-600 border border-orange-100"
                      }`}
                    >
                      {isSuggestion ? " Suggestion" : " Complaint"}
                    </span>
                    <span className="text-[10px] text-slate-400 font-bold">
                      {formatDate(item.createdAt)}
                    </span>
                  </div>

                  <div className="space-y-2 mb-4">
                    <p className="text-sm text-slate-700 dark:text-slate-300 font-medium leading-relaxed whitespace-pre-wrap">
                      {item.content}
                    </p>
                  </div>

                  <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 pt-4 border-t border-slate-100/60 mt-4">
                    <div className="text-[10px] font-semibold text-slate-400">
                      Reporter:{" "}
                      {item.isAnonymous ? (
                        <span className="text-slate-400 italic">Anonymous</span>
                      ) : (
                        <span className="text-slate-600 dark:text-slate-300 font-extrabold">
                          {item.reporterName} ({item.reporterEmail})
                        </span>
                      )}
                    </div>
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </div>
  );
};

export default HRFeedbackTab;
