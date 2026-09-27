import React, { useState, useEffect, useMemo, useRef } from "react";
import { FiX, FiExternalLink, FiRefreshCw, FiRotateCcw, FiUsers } from "react-icons/fi";
import { previewMembersExport, exportMembersSheet } from "../../../services/adminService";
import { ROLE_OPTIONS, ROLE_GROUPS, EXPORT_PRESETS } from "../../../constants/roles";

const MAX_TITLE_LENGTH = 150;
const PREVIEW_DEBOUNCE_MS = 300;
const DEFAULT_ROLES = EXPORT_PRESETS[0].roles;

// Leadership first, then members, then other accounts (matches the sheet's row order)
const ORDERED_ROLE_OPTIONS = ROLE_GROUPS.flatMap((g) => ROLE_OPTIONS.filter((r) => r.group === g.key));
const COMMITTEE_SCOPED_ROLES = ROLE_OPTIONS.filter((r) => r.scope === "committee").map((r) => r.value);
const CLUB_SCOPED_ROLES = ROLE_OPTIONS.filter((r) => r.scope === "club").map((r) => r.value);

const labelsFor = (roles) => ROLE_OPTIONS.filter((r) => roles.includes(r.value)).map((r) => r.label);
const scopeHints = (roles) => ({
  enabled: `Applies to ${labelsFor(roles).join(" & ")}`,
  disabled: `Select ${labelsFor(roles).join(" or ")} to filter`,
});
const COMMITTEE_SCOPE_HINTS = scopeHints(COMMITTEE_SCOPED_ROLES);
const CLUB_SCOPE_HINTS = scopeHints(CLUB_SCOPED_ROLES);

const toggleValue = (list, value) => (list.includes(value) ? list.filter((v) => v !== value) : [...list, value]);
const sameSet = (a, b) => a.length === b.length && a.every((v) => b.includes(v));

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

const SectionLabel = ({ children }) => (
  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">{children}</span>
);

const CheckboxTile = ({ checked, onChange, label, hint }) => (
  <label
    className={`flex items-start gap-3 p-3 rounded-xl border cursor-pointer select-none transition-all group/opt ${
      checked
        ? "border-[#4B98C8] bg-[#4B98C8]/5 dark:bg-[#4B98C8]/10"
        : "border-slate-200 dark:border-slate-700 hover:border-[#4B98C8]/50"
    }`}
  >
    <div
      className={`w-5 h-5 rounded-md border-2 flex items-center justify-center shrink-0 transition-all ${
        checked
          ? "bg-[#4B98C8] border-[#4B98C8]"
          : "bg-white dark:bg-slate-900 border-slate-300 dark:border-slate-600 group-hover/opt:border-[#4B98C8]/60"
      }`}
    >
      {checked && (
        <svg className="w-3 h-3 text-white" viewBox="0 0 12 12" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M2 6L5 9L10 3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" />
        </svg>
      )}
    </div>
    <input type="checkbox" className="sr-only" checked={checked} onChange={onChange} />
    <div className="min-w-0">
      <span className="block text-xs font-bold text-slate-700 dark:text-slate-200">{label}</span>
      {hint && <span className="block text-[10px] text-slate-400 font-medium mt-0.5">{hint}</span>}
    </div>
  </label>
);

const ToggleChip = ({ active, onClick, disabled, children }) => (
  <button
    type="button"
    onClick={onClick}
    disabled={disabled}
    className={`px-3 py-1.5 rounded-full border text-[11px] font-bold transition-all active:scale-95 disabled:cursor-not-allowed disabled:active:scale-100 ${
      active
        ? "bg-[#4B98C8] border-[#4B98C8] text-white shadow-md"
        : "bg-white dark:bg-slate-800 border-slate-200 dark:border-slate-600 text-slate-500 dark:text-slate-300 hover:border-[#4B98C8]/50"
    }`}
  >
    {children}
  </button>
);

const AssociationPicker = ({ title, allLabel, emptyLabel, items, selectedIds, onChange, enabled, hints }) => (
  <div className={`space-y-2.5 transition-opacity ${enabled ? "" : "opacity-50"}`}>
    <div className="flex items-center justify-between gap-2 flex-wrap">
      <SectionLabel>{title}</SectionLabel>
      <span className="text-[10px] text-slate-400 font-medium">{enabled ? hints.enabled : hints.disabled}</span>
    </div>
    {items.length === 0 ? (
      <p className="text-[11px] text-slate-400 italic">{emptyLabel}</p>
    ) : (
      <div className="flex flex-wrap gap-2">
        <ToggleChip active={selectedIds.length === 0} disabled={!enabled} onClick={() => onChange([])}>
          {allLabel}
        </ToggleChip>
        {items.map((item) => (
          <ToggleChip
            key={item.id}
            active={selectedIds.includes(item.id)}
            disabled={!enabled}
            onClick={() => onChange(toggleValue(selectedIds, item.id))}
          >
            {item.name}
          </ToggleChip>
        ))}
      </div>
    )}
  </div>
);

const MembersExportModal = ({ onClose, committees = [], clubs = [], initialFilters = {} }) => {
  const [selectedRoles, setSelectedRoles] = useState(() =>
    initialFilters.role ? [initialFilters.role] : DEFAULT_ROLES
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

  const hasCommitteeScope = selectedRoles.some((r) => COMMITTEE_SCOPED_ROLES.includes(r));
  const hasClubScope = selectedRoles.some((r) => CLUB_SCOPED_ROLES.includes(r));

  // Only send association filters for roles they actually narrow
  const filters = useMemo(
    () => ({
      roles: selectedRoles,
      committeeIds: hasCommitteeScope ? selectedCommittees : [],
      clubIds: hasClubScope ? selectedClubs : [],
    }),
    [selectedRoles, selectedCommittees, selectedClubs, hasCommitteeScope, hasClubScope]
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
    if (!canExport) return;
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
      setExporting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]">
      <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-2xl dark:shadow-slate-950/60 max-w-3xl w-full p-6 animate-[scaleIn_0.25s_ease] max-h-[90vh] flex flex-col">
        {/* Modal Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-slate-700 shrink-0">
          <div>
            <h3 className="text-base font-extrabold text-slate-800 dark:text-slate-100 tracking-tight">Export Members</h3>
            <p className="text-[10px] text-slate-400 font-medium mt-0.5 uppercase tracking-wider">
              New Google Sheet in the members folder
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400 dark:text-slate-300 hover:text-slate-600 dark:hover:text-slate-100 rounded-lg transition-colors"
          >
            <FiX className="w-4.5 h-4.5" />
          </button>
        </div>

        {/* Scrollable Content Container */}
        <div className="overflow-y-auto py-5 space-y-5 flex-1 pr-1">
          {/* Error Banner */}
          {error && (
            <div className="p-3.5 bg-rose-50 border border-rose-100 rounded-xl text-rose-600 text-xs font-bold shrink-0">
              {error}
            </div>
          )}

          {/* Google Sheets Export Card */}
          <div className="relative overflow-hidden bg-gradient-to-r from-emerald-50/20 to-slate-50 dark:to-slate-800 border border-slate-200 dark:border-slate-700 rounded-2xl p-5 space-y-4">
            <div className="flex items-start gap-4">
              <div
                className={`p-3 rounded-xl ${lastExport ? "bg-emerald-50 text-emerald-600 border-emerald-100" : "bg-slate-100 text-slate-500 border-slate-200/50"} shrink-0 shadow-sm border`}
              >
                <svg className="w-5.5 h-5.5" viewBox="0 0 24 24" fill="currentColor">
                  <path d="M19 3H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-2 10H7v-2h10v2zm0-4H7V7h10v2zm0 8H7v-2h10v2z" />
                </svg>
              </div>

              <div className="flex-1 min-w-0 space-y-1.5">
                <div className="flex items-center gap-2 flex-wrap">
                  <span className="text-[9px] font-black text-slate-400 uppercase tracking-widest">Sheet Name</span>
                  {lastExport ? (
                    <span className="px-2 py-0.5 rounded-full text-[9px] font-black uppercase tracking-wider bg-emerald-50 text-emerald-700 border border-emerald-100/80 flex items-center gap-1">
                      <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                      Generated
                    </span>
                  ) : (
                    <span className="px-2 py-0.5 rounded-full text-[9px] font-black uppercase tracking-wider bg-slate-100 text-slate-600 border border-slate-200">
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
                    className="w-full pl-3.5 pr-10 py-2.5 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-600 text-slate-800 dark:text-slate-100 placeholder:text-slate-400 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                  />
                  {titleTouched && (
                    <button
                      type="button"
                      onClick={() => {
                        setTitle("");
                        setTitleTouched(false);
                      }}
                      title="Reset to auto-generated name"
                      className="absolute inset-y-0 right-0 pr-3.5 flex items-center text-slate-400 hover:text-[#4B98C8] transition-colors"
                    >
                      <FiRotateCcw className="w-3.5 h-3.5" />
                    </button>
                  )}
                </div>
              </div>
            </div>

            {/* Insights */}
            <div className="grid grid-cols-1 sm:grid-cols-2 gap-3">
              <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-2xl p-4 flex items-center gap-4">
                <div className="w-11 h-11 rounded-xl flex items-center justify-center bg-sky-50 text-[#4B98C8] shrink-0">
                  {previewLoading ? <FiRefreshCw className="w-5 h-5 animate-spin" /> : <FiUsers className="w-5 h-5" />}
                </div>
                <div>
                  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block">Matching Members</span>
                  <span className={`text-2xl font-black text-slate-800 dark:text-slate-100 leading-tight transition-opacity ${previewLoading ? "opacity-40" : ""}`}>
                    {total}
                  </span>
                </div>
              </div>

              <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-2xl p-4 flex flex-col justify-center">
                <div className="flex justify-between items-center mb-2">
                  <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">University Classification</span>
                  <span className="text-xs font-bold text-slate-700 dark:text-slate-200">
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
                      className="inline-flex items-center gap-1.5 px-2.5 py-1 bg-slate-100 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700/80 rounded-lg text-[10px] font-extrabold uppercase tracking-wide text-slate-600 dark:text-slate-300"
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
                        className="inline-flex items-center gap-1.5 px-2 py-0.5 rounded-full text-[10px] font-bold bg-[#4B98C8]/10 text-[#205E85] dark:text-[#4B98C8]"
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
                <p className="w-full sm:w-auto sm:mr-auto text-[11px] font-bold text-amber-600 dark:text-amber-400">
                  Sheet is ready. Your browser blocked the new tab, so use Open Sheet.
                </p>
              )}
              {lastExport?.googleSheetUrl && (
                <a
                  href={lastExport.googleSheetUrl}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="flex-1 sm:flex-initial px-4 py-2.5 bg-white dark:bg-slate-800 hover:bg-slate-50 dark:hover:bg-slate-700 border border-slate-200 dark:border-slate-600 text-slate-700 dark:text-slate-200 hover:text-slate-900 dark:hover:text-slate-100 text-xs font-bold uppercase tracking-wider rounded-xl transition-all shadow-sm flex items-center justify-center gap-1.5 active:scale-95"
                >
                  Open Sheet <FiExternalLink className="w-3.5 h-3.5" />
                </a>
              )}
              <button
                onClick={handleExport}
                disabled={!canExport}
                className={`flex-1 sm:flex-initial px-5 py-2.5 text-white text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow disabled:active:scale-100 flex items-center justify-center gap-2 bg-gradient-to-r from-emerald-600 to-emerald-700 hover:from-emerald-700 hover:to-emerald-800 ${
                  exporting ? "cursor-wait" : "disabled:opacity-50"
                }`}
              >
                <FiRefreshCw className={`w-3.5 h-3.5 ${exporting ? "animate-spin" : ""}`} />
                {exporting ? "Generating Sheet..." : "Export to Sheets"}
              </button>
            </div>
          </div>

          {/* Selection Settings */}
          <div className="bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 rounded-2xl p-5 space-y-5">
            <div className="space-y-2.5">
              <SectionLabel>Quick Presets</SectionLabel>
              <div className="flex flex-wrap gap-2">
                {EXPORT_PRESETS.map((preset) => (
                  <ToggleChip
                    key={preset.key}
                    active={sameSet(preset.roles, selectedRoles)}
                    onClick={() => setSelectedRoles(preset.roles)}
                  >
                    {preset.label}
                  </ToggleChip>
                ))}
              </div>
            </div>

            <div className="space-y-3">
              <SectionLabel>Roles</SectionLabel>
              {ROLE_GROUPS.map((group) => (
                <div key={group.key} className="space-y-1.5">
                  <span className="text-[11px] font-bold text-slate-500 dark:text-slate-400">{group.label}</span>
                  <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                    {ROLE_OPTIONS.filter((r) => r.group === group.key).map((role) => (
                      <CheckboxTile
                        key={role.value}
                        label={role.label}
                        hint={role.scope === "committee" ? "Filtered by committee" : role.scope === "club" ? "Filtered by club" : null}
                        checked={selectedRoles.includes(role.value)}
                        onChange={() => setSelectedRoles((prev) => toggleValue(prev, role.value))}
                      />
                    ))}
                  </div>
                </div>
              ))}
              {selectedRoles.length === 0 && (
                <p className="text-[11px] font-bold text-rose-500">Select at least one role to export.</p>
              )}
            </div>

            <AssociationPicker
              title="Committees"
              allLabel="All Committees"
              emptyLabel="No committees found."
              items={committees}
              selectedIds={selectedCommittees}
              onChange={setSelectedCommittees}
              enabled={hasCommitteeScope}
              hints={COMMITTEE_SCOPE_HINTS}
            />

            <AssociationPicker
              title="Clubs"
              allLabel="All Clubs"
              emptyLabel="No clubs found."
              items={clubs}
              selectedIds={selectedClubs}
              onChange={setSelectedClubs}
              enabled={hasClubScope}
              hints={CLUB_SCOPE_HINTS}
            />
          </div>
        </div>

        {/* Footer */}
        <div className="flex justify-end pt-4 border-t border-slate-100 dark:border-slate-700 shrink-0">
          <button
            type="button"
            onClick={onClose}
            className="px-5 py-2.5 border border-slate-200 dark:border-slate-600 text-slate-500 dark:text-slate-300 hover:text-slate-800 dark:hover:text-slate-100 hover:bg-slate-50 dark:hover:bg-slate-800 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all"
          >
            Close
          </button>
        </div>
      </div>
    </div>
  );
};

export default MembersExportModal;
