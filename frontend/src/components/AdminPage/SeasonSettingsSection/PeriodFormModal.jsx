import { useState } from "react";
import ModalShell from "../SharedComponents/ModalShell";
import { fieldClass, hintClass, labelClass, noticeClass, primaryButtonClass, secondaryButtonClass } from "../SharedComponents/adminStyles";
import { seasonLabel } from "../../../utils/seasonDates";

// Create or edit a season, or a quarter inside one. `draft` is
// { type, id?, name?, startDate?, endDate?, season? }. The server checks overlaps and returns the reason.
// "Current, no end date yet" saves it without an end; adding the next one later ends it the day before.
const PeriodFormModal = ({ draft, ...props }) =>
  draft ? <PeriodForm key={`${draft.type}-${draft.id ?? "new"}-${draft.season ?? ""}`} draft={draft} {...props} /> : null;

const PeriodForm = ({ draft, onClose, onSave, saving, error }) => {
  const [form, setForm] = useState(() => ({
    name: draft.name ?? "",
    startDate: draft.startDate ?? "",
    endDate: draft.endDate ?? "",
    season: draft.season != null ? String(draft.season) : "",
    // New periods start as the ongoing one; an existing period is ongoing when it has no end date.
    ongoing: draft.id ? !draft.endDate : true,
  }));

  const isQuarter = draft.type === "QUARTER";
  const seasonNumber = Number(form.season);
  const title = `${draft.id ? "Edit" : "New"} ${isQuarter ? "quarter" : "season"}`;
  const valid = form.startDate && (form.ongoing || (form.endDate && form.endDate >= form.startDate))
    && seasonNumber >= 2000 && (!isQuarter || form.name.trim());

  const submit = (e) => {
    e.preventDefault();
    if (!valid || saving) return;
    onSave({
      type: draft.type,
      name: isQuarter ? form.name.trim() : null,
      startDate: form.startDate,
      endDate: form.ongoing ? null : form.endDate,
      season: seasonNumber,
    });
  };

  const set = (key) => (e) => setForm((f) => ({ ...f, [key]: e.target.value }));

  return (
    <ModalShell
      open
      onClose={onClose}
      onSubmit={submit}
      busy={saving}
      size="sm"
      title={title}
      subtitle={isQuarter ? `Season ${seasonLabel(Number(draft.season))}` : undefined}
      footer={
        <>
          <button type="button" onClick={onClose} disabled={saving} className={secondaryButtonClass}>Cancel</button>
          <button type="submit" disabled={!valid || saving} className={primaryButtonClass}>{saving ? "Saving…" : "Save"}</button>
        </>
      }
    >
      <div className="flex flex-col gap-4">
        {isQuarter ? (
          <div className="flex flex-col gap-1.5">
            <label htmlFor="period-name" className={labelClass}>Name</label>
            <input id="period-name" value={form.name} onChange={set("name")} maxLength={60} placeholder="Q1" className={fieldClass} />
          </div>
        ) : (
          <div className="flex flex-col gap-1.5">
            <label htmlFor="period-season" className={labelClass}>Season year</label>
            <input id="period-season" type="number" min="2000" value={form.season} onChange={set("season")} placeholder="2026" className={fieldClass} />
            {seasonNumber >= 2000 && <p className={hintClass}>Shown as Season {seasonLabel(seasonNumber)}</p>}
          </div>
        )}
        <label className="flex items-start gap-3 rounded-lg border border-slate-200 dark:border-ocean-745 bg-slate-50 dark:bg-ocean-916/60 p-3 cursor-pointer">
          <input
            type="checkbox"
            checked={form.ongoing}
            onChange={(e) => setForm((f) => ({ ...f, ongoing: e.target.checked }))}
            className="mt-0.5 w-4 h-4 accent-ocean-573"
          />
          <span>
            <span className="block text-sm font-medium text-slate-900 dark:text-slate-100">Current {isQuarter ? "quarter" : "season"}, no end date yet</span>
            <span className={`block mt-0.5 ${hintClass}`}>
              Adding the next {isQuarter ? "quarter" : "season"} ends this one the day before it starts. You can also set the end date later.
            </span>
          </span>
        </label>
        <div className={`grid gap-3 ${form.ongoing ? "grid-cols-1" : "grid-cols-2"}`}>
          <div className="flex flex-col gap-1.5">
            <label htmlFor="period-start" className={labelClass}>Starts</label>
            <input id="period-start" type="date" value={form.startDate} onChange={set("startDate")} className={fieldClass} />
          </div>
          {!form.ongoing && (
            <div className="flex flex-col gap-1.5">
              <label htmlFor="period-end" className={labelClass}>Ends</label>
              <input id="period-end" type="date" value={form.endDate} min={form.startDate || undefined} onChange={set("endDate")} className={fieldClass} />
            </div>
          )}
        </div>
        <p className={noticeClass.warning}>
          {isQuarter
            ? "Must start inside the season and not overlap another quarter. Changing dates doesn't change anything already awarded for it."
            : "Must not overlap another season. Its quarters have to stay inside its dates."}
        </p>
        {error && <p role="alert" className={noticeClass.error}>{error}</p>}
      </div>
    </ModalShell>
  );
};

export default PeriodFormModal;
