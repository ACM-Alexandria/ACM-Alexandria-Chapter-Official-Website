import { useState } from "react";
import { FiCalendar, FiEdit2, FiPlus, FiTrash2 } from "react-icons/fi";
import { createPeriod, deletePeriod, updatePeriod } from "../../../services/seasonsAdminService";
import useThemedDialog from "../../../hooks/useThemedDialog";
import { messageOf } from "../../../utils/apiError";
import { formatRange, isCurrentPeriod, weeksBetween } from "../../../utils/seasonDates";
import {
  hintClass,
  iconButtonClass,
  iconButtonDangerClass,
  panelClass,
  primaryButtonClass,
  tableClass,
  tableHeadRowClass,
  tagSuccessClass,
} from "../SharedComponents/adminStyles";
import PeriodFormModal from "./PeriodFormModal";


// Seasons, each with its quarters. Months need no setup. `draft` and `setDraft` are owned by the
// Season settings tab, so its current-season card can open the same form.
const PeriodsPanel = ({ periods, onChanged, draft, setDraft }) => {
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const { dialog, confirm, notify } = useThemedDialog();

  const isCurrent = (p) => isCurrentPeriod(p);
  const seasons = periods.filter((p) => p.type === "SEASON");
  const quartersOf = (season) => periods
    .filter((p) => p.type === "QUARTER" && p.season === season)
    .sort((a, b) => a.startDate.localeCompare(b.startDate));

  const open = (d) => { setError(""); setDraft(d); };

  const save = async (payload) => {
    setSaving(true);
    setError("");
    try {
      if (draft.id) await updatePeriod(draft.id, payload);
      else await createPeriod(payload);
      setDraft(null);
      await onChanged();
    } catch (e) {
      setError(messageOf(e));
    } finally {
      setSaving(false);
    }
  };

  const remove = async (p) => {
    const ok = await confirm({
      tone: "danger",
      title: `Delete ${p.label}?`,
      message: "A period that is in use (for example, badges were awarded for it) can't be deleted.",
      confirmLabel: "Delete",
    });
    if (!ok) return;
    try {
      await deletePeriod(p.id);
      await onChanged();
    } catch (e) {
      notify({ tone: "danger", title: "Couldn't delete", message: messageOf(e) });
    }
  };

  const nextSeason = seasons.length ? Math.max(...seasons.map((s) => s.season)) + 1 : new Date().getFullYear();

  return (
    <div className="flex flex-col gap-4">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h3 className="text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-100">Seasons and quarters</h3>
          <p className={hintClass}>Quarters can be any length. Months are calendar months and need no setup.</p>
        </div>
        <button type="button" onClick={() => open({ type: "SEASON", season: nextSeason })} className={primaryButtonClass}>
          <FiPlus className="w-4 h-4" aria-hidden="true" />New season
        </button>
      </div>

      {seasons.length === 0 && (
        <p className={`${panelClass} p-6 text-sm text-slate-600 dark:text-slate-400`}>
          No seasons yet. Add the current season's dates, then its quarters.
        </p>
      )}

      {seasons.map((s) => {
        const quarters = quartersOf(s.season);
        return (
          <div key={s.id} className={`${panelClass} overflow-hidden`}>
            <div className="px-4 sm:px-5 py-3.5 flex flex-wrap items-center justify-between gap-3 border-b border-slate-200 dark:border-ocean-800">
              <div className="flex items-center gap-3 min-w-0">
                <span aria-hidden="true" className="w-9 h-9 shrink-0 rounded-lg inline-flex items-center justify-center bg-ocean-67 text-ocean-637 dark:bg-ocean-808 dark:text-ocean-316">
                  <FiCalendar className="w-4 h-4" />
                </span>
                <div className="min-w-0">
                  <p className="text-base font-semibold text-slate-900 dark:text-slate-100">
                    {s.label} {isCurrent(s) && <span className={`${tagSuccessClass} ml-1.5 align-[2px]`}>Current</span>}
                  </p>
                  <p className="text-sm text-slate-600 dark:text-slate-400">{formatRange(s.startDate, s.endDate)}</p>
                </div>
              </div>
              <div className="flex gap-2">
                <button type="button" onClick={() => open({ ...s })} className={iconButtonClass} aria-label={`Edit ${s.label}`} title="Edit">
                  <FiEdit2 className="w-4 h-4" aria-hidden="true" />
                </button>
                <button type="button" onClick={() => remove(s)} className={iconButtonDangerClass} aria-label={`Delete ${s.label}`} title="Delete">
                  <FiTrash2 className="w-4 h-4" aria-hidden="true" />
                </button>
              </div>
            </div>

            {quarters.length > 0 && (
              <div className="overflow-x-auto">
                <table className={`${tableClass} min-w-[32rem]`}>
                  <thead>
                    <tr className={tableHeadRowClass}>
                      <th scope="col" className="px-4 sm:px-5 py-2.5">Quarter</th>
                      <th scope="col" className="px-3 py-2.5">Dates</th>
                      <th scope="col" className="px-3 py-2.5">Length</th>
                      <th scope="col" className="px-4 sm:px-5 py-2.5"><span className="sr-only">Actions</span></th>
                    </tr>
                  </thead>
                  <tbody>
                    {quarters.map((q) => (
                      <tr key={q.id} className="text-sm text-slate-800 dark:text-slate-200">
                        <td className="px-4 sm:px-5 py-2.5 border-t border-slate-100 dark:border-ocean-800 font-medium">
                          {q.name} {isCurrent(q) && <span className="text-xs font-semibold text-emerald-700 dark:text-emerald-300">· now</span>}
                        </td>
                        <td className="px-3 py-2.5 border-t border-slate-100 dark:border-ocean-800">{formatRange(q.startDate, q.endDate)}</td>
                        <td className="px-3 py-2.5 border-t border-slate-100 dark:border-ocean-800 text-slate-600 dark:text-slate-400">{weeksBetween(q.startDate, q.endDate)}</td>
                        <td className="px-4 sm:px-5 py-2 border-t border-slate-100 dark:border-ocean-800">
                          <div className="flex justify-end gap-2">
                            <button type="button" onClick={() => open({ ...q })} className={iconButtonClass} aria-label={`Edit ${q.label}`} title="Edit">
                              <FiEdit2 className="w-4 h-4" aria-hidden="true" />
                            </button>
                            <button type="button" onClick={() => remove(q)} className={iconButtonDangerClass} aria-label={`Delete ${q.label}`} title="Delete">
                              <FiTrash2 className="w-4 h-4" aria-hidden="true" />
                            </button>
                          </div>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}

            <div className="px-4 sm:px-5 py-2.5 border-t border-slate-200 dark:border-ocean-800 bg-slate-50 dark:bg-ocean-890/60">
              <button
                type="button"
                onClick={() => open({ type: "QUARTER", season: s.season, name: `Q${quarters.length + 1}` })}
                className="h-8 px-2 inline-flex items-center gap-1.5 rounded-lg text-sm font-semibold text-ocean-614 dark:text-ocean-316 hover:bg-ocean-67 dark:hover:bg-ocean-808 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ocean-461/40"
              >
                <FiPlus className="w-4 h-4" aria-hidden="true" />Add quarter
              </button>
            </div>
          </div>
        );
      })}

      <PeriodFormModal draft={draft} onClose={() => setDraft(null)} onSave={save} saving={saving} error={error} />
      {dialog}
    </div>
  );
};

export default PeriodsPanel;
