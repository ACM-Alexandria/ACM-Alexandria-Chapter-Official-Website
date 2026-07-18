import React, { useState, useEffect } from "react";
import { FiX, FiExternalLink, FiRefreshCw, FiFileText, FiUsers, FiAward, FiBook, FiArrowLeft } from "react-icons/fi";
import { fetchCommitteeCalls, fetchRegistrationAnalysis, syncRegistrationSheet } from "../../../services/adminService";

const BRAND = "#4B98C8";
const BRAND_DARK = "#205E85";

const RegistrationPanelModal = ({
  open,
  onClose,
  resourceId,
  resourceName,
  resourceType,
  analysis,
  loading,
  syncLoading,
  onSyncSheet,
  error,
}) => {
  const [calls, setCalls] = useState([]);
  const [callsLoading, setCallsLoading] = useState(false);
  const [selectedCall, setSelectedCall] = useState(null);
  const [callAnalysis, setCallAnalysis] = useState(null);
  const [callAnalysisLoading, setCallAnalysisLoading] = useState(false);
  const [callSyncLoading, setCallSyncLoading] = useState(false);
  const [callError, setCallError] = useState(null);

  // Load committee calls history
  useEffect(() => {
    if (open && resourceType === "committee" && resourceId) {
      setCalls([]);
      setSelectedCall(null);
      setCallAnalysis(null);
      setCallError(null);

      const loadCalls = async () => {
        setCallsLoading(true);
        try {
          const data = await fetchCommitteeCalls(resourceId);
          setCalls(data || []);
        } catch (err) {
          console.error("Error loading committee calls:", err);
          setCallError(err.message || "Failed to load open calls history.");
        } finally {
          setCallsLoading(false);
        }
      };

      loadCalls();
    }
  }, [open, resourceId, resourceType]);

  if (!open) return null;

  const handleSelectCall = async (call) => {
    setSelectedCall(call);
    setCallAnalysis(null);
    setCallAnalysisLoading(true);
    setCallError(null);
    try {
      const data = await fetchRegistrationAnalysis("committeeCall", call.id);
      setCallAnalysis(data);
    } catch (err) {
      console.error("Error fetching call analysis:", err);
      setCallError(err.message || "Failed to load call registration analysis.");
    } finally {
      setCallAnalysisLoading(false);
    }
  };

  const handleSyncCallSheet = async () => {
    if (!selectedCall) return;
    setCallSyncLoading(true);
    setCallError(null);
    try {
      const updated = await syncRegistrationSheet("committeeCall", selectedCall.id);
      setCallAnalysis(updated);
      setCalls((prev) =>
        prev.map((c) =>
          c.id === selectedCall.id
            ? { ...c, googleSheetUrl: updated.googleSheetUrl, sheetLastUpdatedAt: updated.sheetLastUpdatedAt }
            : c
        )
      );
    } catch (err) {
      console.error("Error syncing call sheet:", err);
      setCallError(err.message || "Failed to sync spreadsheet.");
    } finally {
      setCallSyncLoading(false);
    }
  };

  const isCommittee = resourceType === "committee";

  // Helpers to select either static props (events/clubs) or dynamic state (committee calls)
  const currentAnalysis = isCommittee ? callAnalysis : analysis;
  const currentLoading = isCommittee ? callAnalysisLoading : loading;
  const currentSyncLoading = isCommittee ? callSyncLoading : syncLoading;
  const currentError = isCommittee ? callError : error;
  const currentSyncHandler = isCommittee ? handleSyncCallSheet : onSyncSheet;

  const total = currentAnalysis?.totalRegistrations || 0;
  const alexCount = currentAnalysis?.alexUniStudentCount || 0;
  const nonAlexCount = currentAnalysis?.nonAlexUniStudentCount || 0;
  const alexPct = total > 0 ? Math.round((alexCount / total) * 100) : 0;
  const nonAlexPct = total > 0 ? Math.round((nonAlexCount / total) * 100) : 0;

  const formatDate = (dateString) => {
    if (!dateString) return "";
    return new Date(dateString).toLocaleDateString("en-US", {
      year: "numeric",
      month: "short",
      day: "numeric",
      hour: "2-digit",
      minute: "2-digit",
    });
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl max-w-2xl w-full p-6 animate-[scaleIn_0.25s_ease] max-h-[90vh] flex flex-col">
        {/* Modal Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 shrink-0">
          <div>
            <h3 className="text-base font-extrabold text-slate-800 tracking-tight">
              {isCommittee ? `Committee Applications: ${resourceName}` : `Registration Panel: ${resourceName}`}
            </h3>
            <p className="text-[10px] text-slate-400 font-medium mt-0.5 uppercase tracking-wider">
              {resourceType === "event" ? "Event" : resourceType === "club" ? "Club" : resourceType === "program" ? "Program" : "Committee"} Registration Insights
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 hover:bg-slate-100 text-slate-400 hover:text-slate-600 rounded-lg transition-colors"
          >
            <FiX className="w-4.5 h-4.5" />
          </button>
        </div>

        {/* Scrollable Content Container */}
        <div className="overflow-y-auto py-5 space-y-5 flex-1 pr-1">
          {/* Error Banner */}
          {currentError && (
            <div className="p-3.5 bg-rose-50 border border-rose-100 rounded-xl text-rose-600 text-xs font-bold shrink-0">
              {currentError}
            </div>
          )}

          {isCommittee && !selectedCall ? (
            // Committee Calls List View
            <div className="space-y-4">
              <div className="flex justify-between items-center">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                  Open Calls History
                </span>
                <span className="text-xs font-semibold text-slate-500">
                  {calls.length} cycles recorded
                </span>
              </div>

              {callsLoading ? (
                <div className="flex flex-col items-center justify-center py-12">
                  <div className="w-8 h-8 border-4 border-slate-200 border-t-[#4B98C8] rounded-full animate-spin mb-3" />
                  <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Loading calls...</p>
                </div>
              ) : calls.length === 0 ? (
                <div className="text-center py-12 border border-dashed border-slate-200 rounded-xl">
                  <p className="text-slate-400 text-xs font-medium">No open calls have been run for this committee yet.</p>
                </div>
              ) : (
                <div className="overflow-x-auto border border-slate-150 rounded-2xl">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="bg-slate-50/70 border-b border-slate-150 text-slate-400 text-[10px] font-bold uppercase tracking-wider">
                        <th className="p-3">Opened At</th>
                        <th className="p-3">Status</th>
                        <th className="p-3 text-center">Applicants</th>
                        <th className="p-3 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 text-xs font-semibold text-slate-700">
                      {calls.map((call) => (
                        <tr key={call.id} className="hover:bg-slate-50/30 transition-colors">
                          <td className="p-3 font-bold text-slate-800">
                            {formatDate(call.openedAt)}
                          </td>
                          <td className="p-3">
                            {call.closedAt ? (
                              <span className="px-2 py-0.5 rounded-full text-[9px] font-bold bg-slate-100 text-slate-500 border border-slate-200/50">
                                Closed {call.closedAt.split("T")[0]}
                              </span>
                            ) : (
                              <span className="px-2 py-0.5 rounded-full text-[9px] font-bold bg-emerald-50 text-emerald-600 border border-emerald-200/60 animate-pulse">
                                Active Call
                              </span>
                            )}
                          </td>
                          <td className="p-3 text-center font-bold text-slate-600">
                            {call.registrationsCount}
                          </td>
                          <td className="p-3 text-right space-x-2">
                            {call.googleSheetUrl && (
                              <a
                                href={call.googleSheetUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="inline-flex p-1.5 bg-white hover:bg-slate-50 border border-slate-200 text-emerald-600 hover:text-emerald-700 rounded-xl transition-all shadow-sm"
                                title="Open Spreadsheet"
                              >
                                <FiExternalLink className="w-3.5 h-3.5" />
                              </a>
                            )}
                            <button
                              onClick={() => handleSelectCall(call)}
                              className="px-3 py-1.5 bg-[#4B98C8]/10 hover:bg-[#4B98C8]/20 text-[#205E85] rounded-xl font-bold uppercase tracking-wider text-[10px] active:scale-95 transition-all"
                            >
                              Details
                            </button>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          ) : (
            // Call Details Analysis View (Scoped call or Event/Club)
            <>
              {isCommittee && (
                <button
                  onClick={() => setSelectedCall(null)}
                  className="flex items-center gap-1.5 text-xs font-bold text-slate-500 hover:text-[#4B98C8] transition-colors pb-1 w-fit"
                >
                  <FiArrowLeft className="w-4 h-4" /> Back to calls history
                </button>
              )}

              {/* Google Sheets Integration Card */}
              <div className="relative overflow-hidden bg-gradient-to-r from-emerald-50/20 to-slate-50 border border-slate-200 rounded-2xl p-5 hover:shadow-sm transition-all duration-300">
                <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                  <div className="flex items-start gap-4">
                    <div className={`p-3 rounded-xl ${currentAnalysis?.googleSheetUrl ? 'bg-emerald-50 text-emerald-600 border-emerald-100' : 'bg-slate-100 text-slate-500 border-slate-200/50'} shrink-0 shadow-sm border`}>
                      <svg className="w-5.5 h-5.5" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-2 10H7v-2h10v2zm0-4H7V7h10v2zm0 8H7v-2h10v2z" />
                      </svg>
                    </div>

                    <div className="space-y-1">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="text-[9px] font-black text-slate-400 uppercase tracking-widest">
                          Registration Sheet
                        </span>
                        {currentAnalysis?.googleSheetUrl ? (
                          <span className="px-2 py-0.5 rounded-full text-[9px] font-black uppercase tracking-wider bg-emerald-50 text-emerald-700 border border-emerald-100/80 flex items-center gap-1">
                            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                            Generated
                          </span>
                        ) : (
                          <span className="px-2 py-0.5 rounded-full text-[9px] font-black uppercase tracking-wider bg-slate-100 text-slate-600 border border-slate-200">
                            Not Exported
                          </span>
                        )}
                      </div>

                      {currentAnalysis?.googleSheetUrl ? (
                        <div className="space-y-0.5">
                          {currentAnalysis?.sheetLastUpdatedAt && (
                            <p className="text-[10px] text-slate-500 font-medium">
                              Last sync: <span className="text-slate-700 font-semibold">{new Date(currentAnalysis.sheetLastUpdatedAt).toLocaleString()}</span>
                            </p>
                          )}
                        </div>
                      ) : (
                        <div className="space-y-0.5">
                          <h4 className="text-xs font-bold text-slate-800">
                            Export Registrations List
                          </h4>
                          <p className="text-[10px] text-slate-400 font-medium">
                            Export all registration columns and custom answers.
                          </p>
                        </div>
                      )}
                    </div>
                  </div>

                  <div className="flex items-center gap-2 w-full sm:w-auto shrink-0 self-stretch sm:self-auto flex-wrap">
                    {currentAnalysis?.googleSheetUrl && (
                      <a
                        href={currentAnalysis.googleSheetUrl}
                        target="_blank"
                        rel="noopener noreferrer"
                        className="flex-1 sm:flex-initial px-4 py-2.5 bg-white hover:bg-slate-50 border border-slate-200 text-slate-700 hover:text-slate-900 text-xs font-bold uppercase tracking-wider rounded-xl transition-all shadow-sm flex items-center justify-center gap-1.5 active:scale-95"
                      >
                        View Sheet <FiExternalLink className="w-3.5 h-3.5" />
                      </a>
                    )}

                    <button
                      onClick={currentSyncHandler}
                      disabled={currentSyncLoading}
                      className="flex-1 sm:flex-initial px-5 py-2.5 text-white text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow disabled:opacity-50 flex items-center justify-center gap-2 bg-gradient-to-r from-emerald-600 to-emerald-700 hover:from-emerald-700 hover:to-emerald-800"
                    >
                      {currentSyncLoading ? (
                        <>
                          <FiRefreshCw className="w-3.5 h-3.5 animate-spin" />
                          Syncing...
                        </>
                      ) : (
                        <>
                          <FiRefreshCw className="w-3.5 h-3.5" />
                          {currentAnalysis?.googleSheetUrl ? "Sync Data" : "Export List"}
                        </>
                      )}
                    </button>
                  </div>
                </div>
              </div>

              {currentLoading ? (
                <div className="flex flex-col items-center justify-center py-12">
                  <div className="w-8 h-8 border-4 border-slate-200 border-t-[#4B98C8] rounded-full animate-spin mb-3" />
                  <p className="text-xs text-slate-400 font-bold uppercase tracking-wider animate-pulse">
                    Fetching Insights...
                  </p>
                </div>
              ) : (
                <>
                  {/* Stat Overview Cards */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {/* Total Registrations */}
                    <div className="bg-white border border-slate-200 rounded-2xl p-5 flex items-center gap-4 hover:shadow-md transition-shadow">
                      <div className="w-12 h-12 rounded-xl flex items-center justify-center bg-sky-50 text-[#4B98C8]">
                        <FiUsers className="w-6 h-6" />
                      </div>
                      <div>
                        <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">
                          Total Applications
                        </span>
                        <span className="text-2xl font-black text-slate-800 leading-tight">
                          {total}
                        </span>
                      </div>
                    </div>

                    {/* University Affiliation */}
                    <div className="bg-white border border-slate-200 rounded-2xl p-5 hover:shadow-md transition-shadow flex flex-col justify-center">
                      <div className="flex justify-between items-center mb-2">
                        <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                          University Classification
                        </span>
                        <span className="text-xs font-bold text-slate-700">
                          {alexCount} / {total} (Alex Eng)
                        </span>
                      </div>
                      {total > 0 ? (
                        <div className="space-y-1.5">
                          <div className="w-full h-2.5 bg-slate-100 rounded-full overflow-hidden flex">
                            <div
                              className="h-full bg-[#4B98C8] transition-all"
                              style={{ width: `${alexPct}%` }}
                              title={`Alex Eng: ${alexPct}%`}
                            />
                            <div
                              className="h-full bg-slate-300 transition-all"
                              style={{ width: `${nonAlexPct}%` }}
                              title={`Non-Alex: ${nonAlexPct}%`}
                            />
                          </div>
                          <div className="flex justify-between text-[9px] font-extrabold uppercase tracking-wide">
                            <span className="text-[#4B98C8]">Alex Eng ({alexPct}%)</span>
                            <span className="text-slate-400">Other ({nonAlexPct}%)</span>
                          </div>
                        </div>
                      ) : (
                        <p className="text-[11px] text-slate-400 italic">No application data available.</p>
                      )}
                    </div>
                  </div>

                  {/* Breakdowns Grid */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-5 pt-2">
                    {/* Department Distribution */}
                    <div className="bg-white border border-slate-200 rounded-2xl p-5 space-y-4">
                      <div className="flex items-center gap-2 border-b border-slate-50 pb-2 mb-2">
                        <FiAward className="w-4 h-4 text-[#4B98C8]" />
                        <h4 className="text-xs font-black text-slate-800 uppercase tracking-wider">
                          Department Breakdown
                        </h4>
                      </div>

                      <div className="space-y-3">
                        {currentAnalysis?.departmentCounts && Object.keys(currentAnalysis.departmentCounts).length > 0 ? (
                          Object.entries(currentAnalysis.departmentCounts).map(([dept, count]) => {
                            const pct = total > 0 ? Math.round((count / total) * 100) : 0;
                            return (
                              <div key={dept} className="space-y-1">
                                <div className="flex justify-between text-[11px] font-bold text-slate-600">
                                  <span className="uppercase">{dept}</span>
                                  <span>
                                    {count} ({pct}%)
                                  </span>
                                </div>
                                <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                                  <div
                                    className="h-full rounded-full transition-all"
                                    style={{
                                      width: `${pct}%`,
                                      background: `linear-gradient(90deg, ${BRAND}, ${BRAND_DARK})`,
                                    }}
                                  />
                                </div>
                              </div>
                            );
                          })
                        ) : (
                          <p className="text-xs text-slate-400 italic py-2">No departments to display.</p>
                        )}
                      </div>
                    </div>

                    {/* Batch Distribution */}
                    <div className="bg-white border border-slate-200 rounded-2xl p-5 space-y-4">
                      <div className="flex items-center gap-2 border-b border-slate-50 pb-2 mb-2">
                        <FiBook className="w-4 h-4 text-[#4B98C8]" />
                        <h4 className="text-xs font-black text-slate-800 uppercase tracking-wider">
                          Batch Breakdown
                        </h4>
                      </div>

                      <div className="space-y-3 max-h-[175px] overflow-y-auto pr-1">
                        {currentAnalysis?.batchCounts && Object.keys(currentAnalysis.batchCounts).length > 0 ? (
                          Object.entries(currentAnalysis.batchCounts)
                            .sort((a, b) => b[1] - a[1])
                            .map(([batch, count]) => {
                              const pct = total > 0 ? Math.round((count / total) * 100) : 0;
                              return (
                                <div key={batch} className="space-y-1">
                                  <div className="flex justify-between text-[11px] font-bold text-slate-600">
                                    <span>{batch}</span>
                                    <span>
                                      {count} ({pct}%)
                                    </span>
                                  </div>
                                  <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                                    <div
                                      className="h-full rounded-full bg-slate-400 transition-all"
                                      style={{ width: `${pct}%` }}
                                    />
                                  </div>
                                </div>
                              );
                            })
                        ) : (
                          <p className="text-xs text-slate-400 italic py-2">No batches to display.</p>
                        )}
                      </div>
                    </div>
                  </div>
                </>
              )}
            </>
          )}
        </div>

        {/* Footer */}
        <div className="flex justify-end pt-4 border-t border-slate-100 shrink-0">
          <button
            type="button"
            onClick={onClose}
            className="px-5 py-2.5 border border-slate-200 text-slate-500 hover:text-slate-800 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default RegistrationPanelModal;
