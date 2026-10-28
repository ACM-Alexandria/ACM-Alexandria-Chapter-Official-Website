import React, { useState, useEffect, useRef } from "react";
import { FiExternalLink, FiRefreshCw, FiFileText, FiUsers, FiAward, FiBook, FiArrowLeft } from "react-icons/fi";
import { fetchCommitteeCalls, fetchRegistrationAnalysis, syncRegistrationSheet } from "../../../services/adminService";
import useThemedDialog from "../../../hooks/useThemedDialog";
import { Highlight } from "../../ThemedDialog";
import ModalShell from "../SharedComponents/ModalShell";
import {
  noticeClass,
  secondaryButtonClass,
} from "../SharedComponents/adminStyles";


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

  const { dialog, confirm } = useThemedDialog();
  const syncCallSheetInFlight = useRef(false);

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
    if (syncCallSheetInFlight.current) return;
    syncCallSheetInFlight.current = true;

    try {
      const callLabel = resourceName ? `${resourceName} Open Call` : "this call";

      const confirmed = await confirm({
        tone: "danger",
        title: "Overwrite Google Sheet?",
        description: (
          <>
            This will replace the existing spreadsheet contents for <Highlight>{callLabel}</Highlight> with the latest registration data from the website. Any manual notes, formulas, or other changes currently in the Google Sheet may be permanently erased.
          </>
        ),
        confirmLabel: "Yes, Overwrite Sheet",
        cancelLabel: "Cancel",
      });

      if (!confirmed) return;

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
    } finally {
      syncCallSheetInFlight.current = false;
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
    <>
      <ModalShell
        open
        onClose={onClose}
        title={<>{isCommittee ? `Committee Applications: ${resourceName}` : `Registration Panel: ${resourceName}`}</>}
        subtitle={<>{resourceType === "event" ? "Event" : resourceType === "club" ? "Club" : resourceType === "program" ? "Program" : "Committee"} Registration Insights</>}
        size="lg"
        footer={
          <button type="button" onClick={onClose} className={secondaryButtonClass}>
            Close
          </button>
        }
      >


        {/* Scrollable Content Container */}
        <div className="overflow-y-auto py-5 space-y-5 flex-1 pr-1">
          {/* Error Banner */}
          {currentError && (
            <div role="alert" className={noticeClass.error}>
              {currentError}
            </div>
          )}

          {isCommittee && !selectedCall ? (
            // Committee Calls List View
            <div className="space-y-4">
              <div className="flex justify-between items-center">
                <span className="text-xs font-medium text-slate-600 dark:text-slate-400">
                  Open Calls History
                </span>
                <span className="text-xs font-semibold text-slate-600 dark:text-slate-400">
                  {calls.length} cycles recorded
                </span>
              </div>

              {callsLoading ? (
                <div className="flex flex-col items-center justify-center py-12">
                  <div className="w-8 h-8 border-4 border-slate-200 dark:border-[#28405A] border-t-[#4B98C8] rounded-full animate-spin mb-3" />
                  <p className="text-xs text-slate-600 dark:text-slate-400 font-medium">Loading calls…</p>
                </div>
              ) : calls.length === 0 ? (
                <div className="text-center py-12 border border-dashed border-slate-200 dark:border-[#28405A] rounded-lg">
                  <p className="text-slate-600 dark:text-slate-400 text-xs font-medium">No open calls have been run for this committee yet.</p>
                </div>
              ) : (
                <div className="overflow-x-auto border border-slate-150 rounded-lg">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="bg-slate-50/70 dark:bg-[#15273A]/70 border-b border-slate-150 dark:border-[#28405A] text-slate-600 dark:text-slate-400 text-xs font-medium">
                        <th className="p-3">Opened At</th>
                        <th className="p-3">Status</th>
                        <th className="p-3 text-center">Applicants</th>
                        <th className="p-3 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100 dark:divide-[#28405A] text-xs font-semibold text-slate-700 dark:text-slate-200">
                      {calls.map((call) => (
                        <tr key={call.id} className="hover:bg-slate-50/30 dark:hover:bg-[#15273A]/50 transition-colors">
                          <td className="p-3 font-medium text-slate-800 dark:text-slate-100">
                            {formatDate(call.openedAt)}
                          </td>
                          <td className="p-3">
                            {call.closedAt ? (
                              <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-slate-100 dark:bg-[#15273A] text-slate-500 dark:text-slate-300 border border-slate-200/50 dark:border-[#28405A]">
                                Closed {call.closedAt.split("T")[0]}
                              </span>
                            ) : (
                              <span className="px-2 py-0.5 rounded-full text-xs font-medium bg-emerald-50 text-emerald-600 border border-emerald-200/60 animate-pulse">
                                Active Call
                              </span>
                            )}
                          </td>
                          <td className="p-3 text-center font-medium text-slate-600 dark:text-slate-300">
                            {call.registrationsCount}
                          </td>
                          <td className="p-3 text-right space-x-2">
                            {call.googleSheetUrl && (
                              <a
                                href={call.googleSheetUrl}
                                target="_blank"
                                rel="noopener noreferrer"
                                className="inline-flex p-1.5 bg-white dark:bg-[#15273A] hover:bg-slate-50 dark:hover:bg-[#1E3248] border border-slate-200 dark:border-[#28405A] text-emerald-600 hover:text-emerald-700 rounded-lg transition-all"
                                title="Open Spreadsheet"
                              >
                                <FiExternalLink className="w-3.5 h-3.5" />
                              </a>
                            )}
                            <button
                              onClick={() => handleSelectCall(call)}
                              className="px-3 py-1.5 bg-[#4B98C8]/10 hover:bg-[#4B98C8]/20 dark:hover:bg-[#4B98C8]/30 text-[#2C6F99] dark:text-[#4B98C8] rounded-lg font-medium text-xs transition-all"
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
                  className="flex items-center gap-1.5 text-xs font-medium text-slate-600 dark:text-slate-400 hover:text-[#4B98C8] transition-colors pb-1 w-fit"
                >
                  <FiArrowLeft className="w-4 h-4" /> Back to calls history
                </button>
              )}

              {/* Google Sheets Integration Card */}
              <div className="rounded-2xl border border-slate-200 dark:border-[#1E3248] bg-white dark:bg-[#0F1C29] p-5">
                <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
                  <div className="flex items-start gap-4">
                    <div className={`p-3 rounded-lg ${currentAnalysis?.googleSheetUrl ? 'bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-300 border-emerald-100 dark:border-emerald-900' : 'bg-slate-100 dark:bg-[#15273A] text-slate-600 dark:text-slate-400 border-slate-200 dark:border-[#28405A]'} shrink-0  border`}>
                      <svg className="w-5.5 h-5.5" viewBox="0 0 24 24" fill="currentColor">
                        <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-2 10H7v-2h10v2zm0-4H7V7h10v2zm0 8H7v-2h10v2z" />
                      </svg>
                    </div>

                    <div className="space-y-1">
                      <div className="flex items-center gap-2 flex-wrap">
                        <span className="text-xs font-semibold text-slate-600 dark:text-slate-400">
                          Registration Sheet
                        </span>
                        {currentAnalysis?.googleSheetUrl ? (
                          <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-100/80 flex items-center gap-1">
                            <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                            Generated
                          </span>
                        ) : (
                          <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-slate-100 text-slate-600 border border-slate-200">
                            Not Exported
                          </span>
                        )}
                      </div>

                      {currentAnalysis?.googleSheetUrl ? (
                        <div className="space-y-0.5">
                          {currentAnalysis?.sheetLastUpdatedAt && (
                            <p className="text-xs text-slate-500 dark:text-slate-400 font-medium">
                              Last sync: <span className="text-slate-700 dark:text-slate-300 font-semibold">{new Date(currentAnalysis.sheetLastUpdatedAt).toLocaleString()}</span>
                            </p>
                          )}
                        </div>
                      ) : (
                        <div className="space-y-0.5">
                          <h4 className="text-xs font-medium text-slate-800 dark:text-slate-100">
                            Export Registrations List
                          </h4>
                          <p className="text-xs text-slate-600 dark:text-slate-400 font-medium">
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
                        className="flex-1 sm:flex-initial px-4 py-2.5 bg-white dark:bg-[#15273A] hover:bg-slate-50 dark:hover:bg-[#1E3248] border border-slate-200 dark:border-[#28405A] text-slate-700 dark:text-slate-200 hover:text-slate-900 dark:hover:text-slate-100 text-xs font-medium rounded-lg transition-all flex items-center justify-center gap-1.5"
                      >
                        View Sheet <FiExternalLink className="w-3.5 h-3.5" />
                      </a>
                    )}

                    <button
                      onClick={currentSyncHandler}
                      disabled={currentSyncLoading}
                      className="flex-1 sm:flex-initial h-9 px-3.5 inline-flex items-center justify-center gap-1.5 rounded-lg text-sm font-medium text-white bg-[#2A7AB0] hover:bg-[#23699A] transition-colors"
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
                  <div className="w-8 h-8 border-4 border-slate-200 dark:border-[#28405A] border-t-[#4B98C8] rounded-full animate-spin mb-3" />
                  <p className="text-xs text-slate-600 dark:text-slate-400 font-medium animate-pulse">
                    Fetching Insights...
                  </p>
                </div>
              ) : (
                <>
                  {/* Stat Overview Cards */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                    {/* Total Registrations */}
                    <div className="rounded-2xl border border-slate-200 dark:border-[#1E3248] bg-white dark:bg-[#0F1C29] p-5 flex items-center gap-4">
                      <div className="w-12 h-12 rounded-lg flex items-center justify-center bg-[#4B98C8]/10 text-[#2C6F99] dark:text-[#7DBBE0]">
                        <FiUsers className="w-6 h-6" />
                      </div>
                      <div>
                        <span className="text-xs font-medium text-slate-600 dark:text-slate-400 block">
                          Total Applications
                        </span>
                        <span className="text-2xl font-semibold text-slate-800 dark:text-slate-100 leading-tight">
                          {total}
                        </span>
                      </div>
                    </div>

                    {/* University Affiliation */}
                    <div className="rounded-2xl border border-slate-200 dark:border-[#1E3248] bg-white dark:bg-[#0F1C29] p-5 flex flex-col justify-center">
                      <div className="flex justify-between items-center mb-2">
                        <span className="text-xs font-medium text-slate-600 dark:text-slate-400">
                          University Classification
                        </span>
                        <span className="text-xs font-medium text-slate-700 dark:text-slate-200">
                          {alexCount} / {total} (Alex Eng)
                        </span>
                      </div>
                      {total > 0 ? (
                        <div className="space-y-1.5">
                          <div className="w-full h-2.5 bg-slate-100 dark:bg-[#1E3248] rounded-full overflow-hidden flex">
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
                          <div className="flex justify-between text-xs font-semibold">
                            <span className="text-[#4B98C8]">Alex Eng ({alexPct}%)</span>
                            <span className="text-slate-600 dark:text-slate-400">Other ({nonAlexPct}%)</span>
                          </div>
                        </div>
                      ) : (
                        <p className="text-xs text-slate-600 dark:text-slate-400 italic">No application data available.</p>
                      )}
                    </div>
                  </div>

                  {/* Breakdowns Grid */}
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-5 pt-2">
                    {/* Department Distribution */}
                    <div className="bg-white dark:bg-[#0F1C29] border border-slate-200 dark:border-[#28405A] rounded-lg p-5 space-y-4">
                      <div className="flex items-center gap-2 border-b border-slate-50 pb-2 mb-2">
                        <FiAward className="w-4 h-4 text-[#4B98C8]" />
                        <h4 className="text-xs font-semibold dark:text-slate-300 text-slate-800">
                          Department Breakdown
                        </h4>
                      </div>

                      <div className="space-y-3">
                        {currentAnalysis?.departmentCounts && Object.keys(currentAnalysis.departmentCounts).length > 0 ? (
                          Object.entries(currentAnalysis.departmentCounts).map(([dept, count]) => {
                            const pct = total > 0 ? Math.round((count / total) * 100) : 0;
                            return (
                              <div key={dept} className="space-y-1">
                                <div className="flex justify-between text-xs font-medium text-slate-600">
                                  <span className="">{dept}</span>
                                  <span>
                                    {count} ({pct}%)
                                  </span>
                                </div>
                                <div className="w-full h-2 bg-slate-100 rounded-full overflow-hidden">
                                  <div
                                    className="h-full rounded-full transition-all"
                                    style={{
                                      width: `${pct}%`,
                                      background: "#4B98C8",
                                    }}
                                  />
                                </div>
                              </div>
                            );
                          })
                        ) : (
                          <p className="text-xs text-slate-600 dark:text-slate-400 italic py-2">No departments to display.</p>
                        )}
                      </div>
                    </div>

                    {/* Batch Distribution */}
                    <div className="bg-white dark:bg-[#0F1C29] border border-slate-200 dark:border-[#28405A] rounded-lg p-5 space-y-4">
                      <div className="flex items-center gap-2 border-b border-slate-50 pb-2 mb-2">
                        <FiBook className="w-4 h-4 text-[#4B98C8]" />
                        <h4 className="text-xs font-semibold text-slate-800 dark:text-slate-100">
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
                                  <div className="flex justify-between text-xs font-medium text-slate-600">
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
                          <p className="text-xs text-slate-600 dark:text-slate-400 italic py-2">No batches to display.</p>
                        )}
                      </div>
                    </div>
                  </div>
                </>
              )}
            </>
          )}
        </div>
      </ModalShell>
      {dialog}
    </>
  );
};

export default RegistrationPanelModal;
