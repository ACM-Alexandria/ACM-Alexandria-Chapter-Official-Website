import React, { useState, useEffect, useMemo, useRef } from "react";
import { FiExternalLink, FiRefreshCw, FiRotateCcw, FiUsers } from "react-icons/fi";
import { previewMembersExport, exportMembersSheet } from "../../../services/adminService";
import MembersFilter, { buildMembersFilters, DEFAULT_MEMBERS_ROLES } from "../SharedComponents/MembersFilter";
import { ROLE_OPTIONS, ROLE_GROUPS, EXPORT_PRESETS } from "../../../constants/roles";
import ModalShell from "../SharedComponents/ModalShell";
import {
  noticeClass,
  primaryButtonClass,
  secondaryButtonClass,
} from "../SharedComponents/adminStyles";

const MAX_TITLE_LENGTH = 150;
const PREVIEW_DEBOUNCE_MS = 300;

// Leadership first, then members, then other accounts (matches the sheet's row order)
const ORDERED_ROLE_OPTIONS = ROLE_GROUPS.flatMap((g) => ROLE_OPTIONS.filter((r) => r.group === g.key));

const pad = (n) => String(n).padStart(2, "0");
const formatStamp = (date) =>
  `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}`;

const buildAutoTitle = (roles, scopeNames, date) => {
  const roleLabels = ORDERED_ROLE_OPTIONS.filter((r) => roles.includes(r.value)).map((r) => r.label);
  const title = [
    "ACM Alexandria - Members",
    scopeNames.length ? scopeNames.join(", ") : "All",
    roleLabels.join(", "),
    formatStamp(date),
  ].join(" - ");
  return title.slice(0, MAX_TITLE_LENGTH);
};

const MembersExportModal = ({ onClose, committees = [], clubs = [], initialFilters = {} }) => {
  const [selectedRoles, setSelectedRoles] = useState(() =>
    initialFilters.role ? [initialFilters.role] : DEFAULT_MEMBERS_ROLES
  );
  const [selectedCommittees, setSelectedCommittees] = useState(() =>
    initialFilters.committeeId ? [Number(initialFilters.committeeId)] : []
  );
  const [selectedClubs, setSelectedClubs] = useState(() =>
    initialFilters.clubId ? [Number(initialFilters.clubId)] : []
  );
  const [title, setTitle] = useState("");
  const [titleTouched, setTitleTouched] = useState(false);
  const [stamp, setStamp] = useState(() => new Date());
  const [preview, setPreview] = useState(null);
  const [previewLoading, setPreviewLoading] = useState(false);
  const [exporting, setExporting] = useState(false);
  const [lastExport, setLastExport] = useState(null);
  const [popupBlocked, setPopupBlocked] = useState(false);
  const [error, setError] = useState(null);
  const previewRequestId = useRef(0);
  const exportInFlight = useRef(false);

  const filters = useMemo(
    () => buildMembersFilters(selectedRoles, selectedCommittees, selectedClubs),
    [selectedRoles, selectedCommittees, selectedClubs]
  );

  const scopeNames = useMemo(
    () => [
      ...committees.filter((c) => filters.committeeIds.includes(c.id)).map((c) => c.name),
      ...clubs.filter((c) => filters.clubIds.includes(c.id)).map((c) => c.name),
    ],
    [committees, clubs, filters]
  );

  const autoTitle = buildAutoTitle(selectedRoles, scopeNames, stamp);
  const sheetTitle = titleTouched && title.trim() ? title.trim() : autoTitle;

  // Live insights: debounce selection changes and ignore stale responses
  useEffect(() => {
    const requestId = ++previewRequestId.current;
    if (filters.roles.length === 0) {
      setPreview(null);
      setPreviewLoading(false);
      return;
    }
    setPreviewLoading(true);
    const timer = setTimeout(async () => {
      try {
        const data = await previewMembersExport(filters);
        if (requestId === previewRequestId.current) {
          setPreview(data);
          setError(null);
        }
      } catch (err) {
        if (requestId === previewRequestId.current) {
          setError(err.message || err.error || "Failed to load members preview.");
        }
      } finally {
        if (requestId === previewRequestId.current) {
          setPreviewLoading(false);
        }
      }
    }, PREVIEW_DEBOUNCE_MS);
    return () => clearTimeout(timer);
  }, [filters]);

  const total = preview?.totalMembers || 0;
  const alexCount = preview?.alexUniStudentCount || 0;
  const alexPct = total > 0 ? Math.round((alexCount / total) * 100) : 0;
  const roleCounts = Object.entries(preview?.roleCounts || {});
  const associationCounts = Object.entries(preview?.associationCounts || {});
  const canExport = selectedRoles.length > 0 && total > 0 && !previewLoading && !exporting;

  const handleExport = async () => {
    if (!canExport || exportInFlight.current) return;
    exportInFlight.current = true;
    setExporting(true);
    setError(null);
    setPopupBlocked(false);

    try {
      const result = await exportMembersSheet({ ...filters, title: sheetTitle });
      setLastExport(result);
      setStamp(new Date());
      // Browsers may block tabs opened after a slow request; the "Open Sheet" link covers that case
      const sheetTab = window.open(result.googleSheetUrl, "_blank");
      if (sheetTab) {
        sheetTab.opener = null;
      } else {
        setPopupBlocked(true);
      }
    } catch (err) {
      console.error("Error exporting members sheet:", err);
      setError(err.message || err.error || "Failed to export members sheet.");
    } finally {
      exportInFlight.current = false;
      setExporting(false);
    }
  };

  return (
    <>
      <ModalShell
        open
        onClose={onClose}
        title="Export Members"
        subtitle="New Google Sheet in the members folder"
        size="xl" busy={exporting}
        footer={
          <button type="button" onClick={onClose} className={secondaryButtonClass}>
            Close
          </button>
        }
      >


        {/* Scrollable Content Container */}
        <div className="overflow-y-auto py-5 space-y-5 flex-1 pr-1">
          {/* Error Banner */}
          {error && (
            <div role="alert" className={noticeClass.error}>
              {error}
            </div>
          )}

          {/* Google Sheets Export Card */}
          <div className="relative overflow-hidden bg-slate-50 dark:bg-slate-950/40 border border-slate-200 dark:border-slate-700 rounded-lg p-5 space-y-4">
            <div className="flex items-start gap-4">
              <div
                className={`p-3 rounded-lg ${lastExport ? "bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-300 border-emerald-100 dark:border-emerald-900" : "bg-slate-100 dark:bg-slate-800 text-slate-600 dark:text-slate-400 border-slate-200 dark:border-slate-700"} shrink-0  border`}
              >
                <svg className="w-5.5 h-5.5" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-2 10H7v-2h10v2zm0-4H7V7h10v2zm0 8H7v-2h10v2z" />
                </svg>
              </div>

              <div className="flex-1 min-w-0 space-y-1.5">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-xs font-semibold text-slate-600 dark:text-slate-400">Sheet Name</span>
                  {lastExport ? (
                    <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700 border border-emerald-100/80 flex items-center gap-1">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                      Generated
                    </span>
                  ) : (
                    <span className="px-2 py-0.5 rounded-full text-xs font-semibold bg-slate-100 text-slate-600 border border-slate-200">
                      New Sheet
                    </span>
                  )}
                </div>
                <div className="relative">
                  <input
                    type="text"
                    value={titleTouched ? title : autoTitle}
                    placeholder={autoTitle}
                    maxLength={MAX_TITLE_LENGTH}
                    onChange={(e) => {
                      setTitle(e.target.value);
                      setTitleTouched(true);
                    }}
                    className="w-full pl-3.5 pr-10 py-2.5 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-800 dark:text-slate-100 placeholder:text-slate-500 text-xs font-semibold rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                  />
                  {titleTouched && (
                    <button
                      type="button"
                      onClick={() => {
                        setTitle("");
                        setTitleTouched(false);
                      }}
                      title="Reset to auto-generated name"
                      className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-slate-600 dark:text-slate-400 hover:text-[#4B98C8] transition-colors"
                    >
                      <FiRotateCcw className="w-3.5 h-3.5" />
                    </button>
                  )}
                </div>
              </div>
            </div>

            {/* Insights */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg p-4 flex items-center gap-4">
                <div className="w-11 h-11 rounded-lg flex items-center justify-center bg-sky-50 text-[#4B98C8] shrink-0">
                  {previewLoading ? <FiRefreshCw className="w-5 h-5 animate-spin" /> : <FiUsers className="w-5 h-5" />}
                </div>
                <div>
                  <span className="text-xs font-medium text-slate-600 dark:text-slate-400 block">Matching Members</span>
                  <span className={`text-2xl font-semibold text-slate-800 dark:text-slate-100 leading-tight transition-opacity ${previewLoading ? "opacity-40" : ""}`}>
                    {total}
                  </span>
                </div>
              </div>

              <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg p-4 flex flex-col justify-center">
                <div className="flex justify-between items-center mb-2">
                  <span className="text-xs font-medium text-slate-600 dark:text-slate-400">University Classification</span>
                  <span className="text-sm font-medium text-slate-900 dark:text-slate-100">
                    {alexCount} / {total} (Alex Eng)
                  </span>
                </div>
                <div className="w-full h-2.5 bg-slate-100 dark:bg-slate-700 rounded-full overflow-hidden flex">
                  <div className="h-full bg-[#4B98C8] transition-all" style={{ width: `${alexPct}%` }} />
                </div>
              </div>
            </div>

            {roleCounts.length > 0 && (
              <div className="space-y-2">
                <div className="flex flex-wrap gap-1.5">
                  {roleCounts.map(([label, count]) => (
                    <span
                      key={label}
                      className="inline-flex items-center gap-1.5 px-2.5 py-1 bg-slate-100 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700/80 rounded-lg text-xs font-semibold text-slate-600 dark:text-slate-300"
                    >
                      {label}
                      <span className="text-[#4B98C8]">{count}</span>
                    </span>
                  ))}
                </div>
                {associationCounts.length > 0 && (
                  <div className="flex flex-wrap gap-1.5 max-h-[64px] overflow-y-auto">
                    {associationCounts.map(([name, count]) => (
                      <span
                        key={name}
                        className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-xs font-medium bg-[#4B98C8]/10 text-[#2C6F99] dark:text-[#4B98C8]"
                      >
                        {name}
                        <span className="text-slate-500 dark:text-slate-400">{count}</span>
                      </span>
                    ))}
                  </div>
                )}
              </div>
            )}

            <div className="flex items-center justify-end gap-2 flex-wrap">
              {popupBlocked && (
                <p className="w-full sm:w-auto sm:mr-auto text-xs font-medium text-amber-600 dark:text-amber-400">
                  Sheet is ready. Your browser blocked the new tab, so use Open Sheet.
                </p>
              )}
              {lastExport?.googleSheetUrl && (
                <a
                  href={lastExport.googleSheetUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex-1 sm:flex-initial px-4 py-2.5 bg-white dark:bg-slate-800 hover:bg-slate-50 dark:hover:bg-slate-700 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:text-slate-900 dark:hover:text-slate-100 text-xs font-medium rounded-lg transition-all flex items-center justify-center gap-1.5"
                >
                  Open Sheet <FiExternalLink className="w-3.5 h-3.5" />
                </a>
              )}
              <button
                onClick={handleExport}
                disabled={!canExport}
                className={`${primaryButtonClass} flex-1 sm:flex-initial ${exporting ? "cursor-wait" : ""}`}
              >
                <FiRefreshCw className={`w-4 h-4 ${exporting ? "animate-spin" : ""}`} aria-hidden="true" />
                {exporting ? "Generating sheet…" : "Export to Sheets"}
              </button>
            </div>
          </div>

          {/* Selection Settings */}
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-lg p-5 space-y-5">
            <MembersFilter
              selectedRoles={selectedRoles}
              setSelectedRoles={setSelectedRoles}
              committees={committees}
              clubs={clubs}
              selectedCommitteeIds={selectedCommittees}
              setSelectedCommitteeIds={setSelectedCommittees}
              selectedClubIds={selectedClubs}
              setSelectedClubIds={setSelectedClubs}
              emptyRoleMessage="Select at least one role to export."
            />
          </div>
        </div>
      </ModalShell>
    </>
  );
};

export default MembersExportModal;
