import React from "react";
import { EXPORT_PRESETS, ROLE_GROUPS, ROLE_OPTIONS } from "../../../constants/roles";

export const COMMITTEE_SCOPED_ROLES = ROLE_OPTIONS
    .filter((role) => role.scope === "committee")
    .map((role) => role.value);
export const CLUB_SCOPED_ROLES = ROLE_OPTIONS
    .filter((role) => role.scope === "club")
    .map((role) => role.value);
export const DEFAULT_MEMBERS_ROLES = [...EXPORT_PRESETS[0].roles];

export const buildMembersFilters = (roles, committeeIds, clubIds) => ({
    roles,
    committeeIds: roles.some((role) => COMMITTEE_SCOPED_ROLES.includes(role)) ? committeeIds : [],
    clubIds: roles.some((role) => CLUB_SCOPED_ROLES.includes(role)) ? clubIds : [],
});

const toggleValue = (values, value) => values.includes(value)
    ? values.filter((item) => item !== value)
    : [...values, value];
const sameSet = (left, right) => left.length === right.length && left.every((value) => right.includes(value));
const labelsFor = (roles) => ROLE_OPTIONS.filter((role) => roles.includes(role.value)).map((role) => role.label);
const scopeHints = (roles) => ({
    enabled: `Applies to ${labelsFor(roles).join(" & ")}`,
    disabled: `Select ${labelsFor(roles).join(" or ")} to filter`,
});
const COMMITTEE_SCOPE_HINTS = scopeHints(COMMITTEE_SCOPED_ROLES);
const CLUB_SCOPE_HINTS = scopeHints(CLUB_SCOPED_ROLES);

const SectionLabel = ({ children }) => (
    <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">{children}</span>
);

const CheckboxTile = ({ checked, onChange, label, hint }) => (
    <label
        className={`flex items-start gap-3 p-3 rounded-xl border cursor-pointer select-none transition-all group/opt ${checked
            ? "border-[#4B98C8] bg-[#4B98C8]/5 dark:bg-[#4B98C8]/10"
            : "border-slate-200 dark:border-slate-700 hover:border-[#4B98C8]/50"
            }`}
    >
        <div
            className={`w-5 h-5 rounded-md border-2 flex items-center justify-center shrink-0 transition-all ${checked
                ? "bg-[#4B98C8] border-[#4B98C8]"
                : "bg-white dark:bg-slate-900 border-slate-300 dark:border-slate-600 group-hover/opt:border-[#4B98C8]/60"
                }`}
        >
            {checked && (
                <svg className="w-3 h-3 text-white" viewBox="0 0 12 12" fill="none" aria-hidden="true">
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

const ToggleChip = ({ active, onClick, disabled = false, children }) => (
    <button
        type="button"
        onClick={onClick}
        disabled={disabled}
        className={`px-3 py-1.5 rounded-full border text-[11px] font-bold transition-all active:scale-95 disabled:cursor-not-allowed disabled:active:scale-100 ${active
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
                        onClick={() => onChange((current) => toggleValue(current, item.id))}
                    >
                        {item.name}
                    </ToggleChip>
                ))}
            </div>
        )}
    </div>
);

const MembersFilter = ({
    selectedRoles,
    setSelectedRoles,
    committees = [],
    clubs = [],
    selectedCommitteeIds,
    setSelectedCommitteeIds,
    selectedClubIds,
    setSelectedClubIds,
    previewCount,
    previewLoading = false,
    previewError = "",
    showRecipientCount = false,
    emptyRoleMessage = "Select at least one role.",
}) => {
    const hasCommitteeScope = selectedRoles.some((role) => COMMITTEE_SCOPED_ROLES.includes(role));
    const hasClubScope = selectedRoles.some((role) => CLUB_SCOPED_ROLES.includes(role));

    return (
        <div className="space-y-5">
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
                    <ToggleChip
                        active={sameSet(ROLE_OPTIONS.map((role) => role.value), selectedRoles)}
                        onClick={() => setSelectedRoles(ROLE_OPTIONS.map((role) => role.value))}
                    >
                        All Accounts
                    </ToggleChip>
                </div>
            </div>

            <div className="space-y-3">
                <SectionLabel>Roles</SectionLabel>
                {ROLE_GROUPS.map((group) => (
                    <div key={group.key} className="space-y-1.5">
                        <span className="text-[11px] font-bold text-slate-500 dark:text-slate-400">{group.label}</span>
                        <div className="grid grid-cols-1 sm:grid-cols-3 gap-2">
                            {ROLE_OPTIONS.filter((role) => role.group === group.key).map((role) => (
                                <CheckboxTile
                                    key={role.value}
                                    label={role.label}
                                    hint={role.scope === "committee" ? "Filtered by committee" : role.scope === "club" ? "Filtered by club" : null}
                                    checked={selectedRoles.includes(role.value)}
                                    onChange={() => setSelectedRoles((current) => toggleValue(current, role.value))}
                                />
                            ))}
                        </div>
                    </div>
                ))}
                {selectedRoles.length === 0 && (
                    <p className="text-[11px] font-bold text-rose-500">{emptyRoleMessage}</p>
                )}
            </div>

            <AssociationPicker
                title="Committees"
                allLabel="All Committees"
                emptyLabel="No committees found."
                items={committees}
                selectedIds={selectedCommitteeIds}
                onChange={setSelectedCommitteeIds}
                enabled={hasCommitteeScope}
                hints={COMMITTEE_SCOPE_HINTS}
            />

            <AssociationPicker
                title="Clubs"
                allLabel="All Clubs"
                emptyLabel="No clubs found."
                items={clubs}
                selectedIds={selectedClubIds}
                onChange={setSelectedClubIds}
                enabled={hasClubScope}
                hints={CLUB_SCOPE_HINTS}
            />

            {showRecipientCount && (
                <div aria-live="polite" className="border-t border-slate-200 pt-3 text-sm dark:border-slate-700">
                    {previewLoading ? (
                        <span className="text-slate-500 dark:text-slate-400">Estimating recipients...</span>
                    ) : previewError ? (
                        <span className="text-amber-700 dark:text-amber-300">Recipient estimate unavailable: {previewError}</span>
                    ) : (
                        <span className="font-semibold text-slate-800 dark:text-slate-100">
                            {previewCount ?? 0} matching account(s)
                        </span>
                    )}
                </div>
            )}
        </div>
    );
};

export default MembersFilter;
