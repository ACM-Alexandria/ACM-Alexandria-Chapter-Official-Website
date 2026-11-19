import { useCallback, useEffect, useState } from "react";
import { FiAlertTriangle, FiCalendar, FiEdit2, FiPlus } from "react-icons/fi";
import { fetchCurrentSeason, fetchPeriods } from "../../../services/seasonsAdminService";
import { messageOf } from "../../../utils/apiError";
import { formatRange, isCurrentPeriod } from "../../../utils/seasonDates";
import { hintClass, noticeClass, panelClass, secondaryButtonClass, tagWarnClass } from "../SharedComponents/adminStyles";
import ArchiveSeasonSection from "./ArchiveSeasonSection";
import PeriodsPanel from "./PeriodsPanel";

// Everything about the chapter's seasons, for the super admin: the current season (the latest one),
// its dates and quarters (used by boards, badges and anything else that works per period), and
// finishing the season, which archives the boards and starts the next season.
const SeasonSettingsTab = () => {
  const [current, setCurrent] = useState(null);
  const [periods, setPeriods] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [archiving, setArchiving] = useState(false);
  const [draft, setDraft] = useState(null);

  const load = useCallback(async () => {
    setError("");
    try {
      const [c, p] = await Promise.all([fetchCurrentSeason(), fetchPeriods()]);
      setCurrent(c);
      setPeriods(p);
      return true;
    } catch (e) {
      setError(messageOf(e));
      return false;
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  if (loading) return <p className="text-sm text-slate-600 dark:text-slate-400">Loading season settings…</p>;
  if (error) {
    return (
      <div role="alert" className={`flex flex-wrap items-center gap-3 ${noticeClass.error}`}>
        <FiAlertTriangle className="w-5 h-5 shrink-0" aria-hidden="true" />
        <p className="flex-1">{error}</p>
        <button type="button" onClick={load} className={secondaryButtonClass}>Try again</button>
      </div>
    );
  }

  const period = current?.period;
  const quarters = period ? periods.filter((p) => p.type === "QUARTER" && p.season === current.season) : [];
  const nowQuarter = quarters.find((q) => isCurrentPeriod(q));
  // The current season is the latest one; flag when today falls outside its dates.
  const datesOutOfStep = period && !isCurrentPeriod(period);

  return (
    <div className="flex flex-col gap-6">
      <section aria-labelledby="current-season-title" className={`${panelClass} p-4 sm:p-6`}>
        <div className="flex flex-col gap-4 sm:flex-row sm:items-start sm:justify-between">
          <div className="flex items-start gap-3 min-w-0">
            <span aria-hidden="true" className="w-11 h-11 shrink-0 rounded-xl inline-flex items-center justify-center bg-ocean-67 text-ocean-637 dark:bg-ocean-808 dark:text-ocean-316">
              <FiCalendar className="w-5 h-5" />
            </span>
            <div className="min-w-0">
              <p className={hintClass}>Current season</p>
              <h2 id="current-season-title" className="text-xl font-semibold tracking-tight text-slate-900 dark:text-slate-100">{current?.label}</h2>
              {period ? (
                <p className="mt-0.5 text-sm text-slate-700 dark:text-slate-300">
                  {formatRange(period.startDate, period.endDate)}
                  {" · "}
                  {quarters.length === 0 ? "no quarters yet" : `${quarters.length} ${quarters.length === 1 ? "quarter" : "quarters"}`}
                  {nowQuarter && <>{" · "}now in {nowQuarter.name}</>}
                </p>
              ) : (
                <p className="mt-1"><span className={tagWarnClass}>Dates not set</span></p>
              )}
              {datesOutOfStep && (
                <p className="mt-2 text-sm text-amber-800 dark:text-amber-300">Today is outside these dates. Check the dates, or archive the season if it has ended.</p>
              )}
            </div>
          </div>
          <button
            type="button"
            onClick={() => setDraft(period ? { ...period } : { type: "SEASON", season: current.season })}
            className={`${secondaryButtonClass} w-full sm:w-auto`}
          >
            {period ? <FiEdit2 className="w-4 h-4" aria-hidden="true" /> : <FiPlus className="w-4 h-4" aria-hidden="true" />}
            {period ? "Edit dates" : "Set dates"}
          </button>
        </div>
      </section>

      <ArchiveSeasonSection
        currentSeason={current?.season}
        disabled={loading || Boolean(error)}
        onBusyChange={setArchiving}
        onArchived={() => load()}
        onConflict={() => load()}
      />

      <PeriodsPanel periods={periods} onChanged={load} draft={archiving ? null : draft} setDraft={setDraft} />
    </div>
  );
};

export default SeasonSettingsTab;
