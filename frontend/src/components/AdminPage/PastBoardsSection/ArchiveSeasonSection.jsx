import { useId, useRef, useState } from "react";
import { FiArchive, FiCheckCircle } from "react-icons/fi";
import { fieldClass, hintClass, labelClass, panelClass, primaryButtonClass } from "../SharedComponents/adminStyles";
import { archiveCurrentSeason } from "../../../services/pastBoardsAdminService";
import { formatBoardSeason } from "../../../utils/boardSeasons";
import ThemedDialog from "../../ThemedDialog";
import SpinnerIcon from "../../icons/SpinnerIcon";

const ArchiveSeasonSection = ({ currentSeason, disabled, onBusyChange, onArchived, onConflict }) => {
  const [confirmingSeason, setConfirmingSeason] = useState(null);
  const [confirmation, setConfirmation] = useState("");
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState("");
  const [needsReview, setNeedsReview] = useState(false);
  const [summary, setSummary] = useState(null);
  const [refreshFailed, setRefreshFailed] = useState(false);
  const inFlight = useRef(false);
  const id = useId();

  const close = () => {
    if (inFlight.current) return;
    setConfirmingSeason(null);
    setConfirmation("");
    setError("");
    setNeedsReview(false);
  };

  const archive = async () => {
    if (disabled || inFlight.current || needsReview || confirmation !== String(confirmingSeason)) return;
    inFlight.current = true;
    setBusy(true);
    onBusyChange(true);
    setError("");
    try {
      const result = await archiveCurrentSeason(confirmingSeason);
      setSummary(result);
      setConfirmingSeason(null);
      setConfirmation("");
      const refreshed = await onArchived(result);
      setRefreshFailed(!refreshed);
    } catch (failure) {
      const status = failure.response?.status;
      if (status === 409 || !status || status >= 500) {
        setNeedsReview(true);
        setError(status === 409
          ? failure.response.data?.message || "The season changed or already has past data. Nothing was archived."
          : "The archive outcome could not be confirmed. Refresh the page to check whether it completed before retrying.");
        await onConflict();
      } else {
        setError(failure.response?.data?.message || "The archive could not be confirmed. Close this dialog and refresh the season before retrying.");
      }
    } finally {
      inFlight.current = false;
      setBusy(false);
      onBusyChange(false);
    }
  };

  const counts = summary ? [
    ["High board members", summary.highBoardMembers],
    ["Committees", summary.committees],
    ["Committee board members", summary.committeeBoardMembers],
    ["Clubs", summary.clubs],
    ["Club board members", summary.clubBoardMembers],
    ["Users changed to User", summary.usersDowngraded],
  ] : [];

  return (
    <section aria-label="Archive current season" className="flex flex-col gap-4 rounded-2xl border border-amber-200 bg-amber-50/60 p-4 dark:border-amber-900 dark:bg-amber-950/20 sm:p-6">
      <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
        <div className="min-w-0">
          <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100">Finish the current season</h3>
          <p className={`mt-1 max-w-xl ${hintClass}`}>
            Archive {currentSeason ? formatBoardSeason(currentSeason) : "the current season"} and prepare the live boards for next year.
            This always applies to the current season, even while you browse an older board.
          </p>
        </div>
        <button
          type="button"
          disabled={disabled || busy || !currentSeason || refreshFailed}
          onClick={() => {
            setConfirmingSeason(currentSeason);
            setConfirmation("");
            setError("");
            setNeedsReview(false);
          }}
          className={`${primaryButtonClass} w-full sm:w-auto`}
        >
          {busy ? <SpinnerIcon aria-hidden="true" className="h-4 w-4 animate-spin" /> : <FiArchive aria-hidden="true" className="h-4 w-4" />}
          {busy ? "Archiving…" : "Archive current season"}
        </button>
      </div>

      {summary && (
        <div role="status" className={`${panelClass} p-4`}>
          <p className="flex items-center gap-2 text-sm font-semibold text-emerald-700 dark:text-emerald-300"><FiCheckCircle aria-hidden="true" />Archived {formatBoardSeason(summary.archivedSeason)}</p>
          <p className={`mt-2 ${hintClass}`}>Current season is now {formatBoardSeason(summary.newCurrentSeason)}.</p>
          <dl className="mt-4 grid grid-cols-2 gap-3 sm:grid-cols-3">
            {counts.map(([label, value]) => <div key={label}><dt className={hintClass}>{label}</dt><dd className="mt-1 text-lg font-semibold tabular-nums text-slate-900 dark:text-slate-100">{value}</dd></div>)}
          </dl>
          {refreshFailed && <p className="mt-3 text-xs font-medium text-amber-900 dark:text-amber-300">The archive completed, but the lists could not refresh. Reload the page before archiving again.</p>}
        </div>
      )}

      <ThemedDialog
        open={confirmingSeason !== null}
        tone="danger"
        title={confirmingSeason ? `Archive ${formatBoardSeason(confirmingSeason)}?` : "Archive current season?"}
        confirmLabel={busy ? "Archiving…" : "Archive season"}
        cancelLabel="Cancel"
        confirmDisabled={disabled || confirmation !== String(confirmingSeason) || needsReview}
        busy={busy}
        onConfirm={archive}
        onCancel={close}
        description={(
          <div className="space-y-3 text-left">
            <ul className="list-disc space-y-1 pl-4">
              <li>Copy all current committees, clubs, and their boards into the past tables.</li>
              <li>Remove every member from all three live boards.</li>
              <li>Change those users’ roles to User and clear their committee association. Super admins and ordinary members outside the boards remain unchanged.</li>
              <li>Keep the live committees and clubs, and advance the current season to {confirmingSeason ? formatBoardSeason(confirmingSeason + 1) : "next year"}.</li>
            </ul>
            <p>Pause live-board edits in other sessions until archiving finishes.</p>
            <p className="font-semibold text-rose-700 dark:text-rose-400">This cannot be undone. Back up the database before continuing.</p>
            <label htmlFor={`${id}-confirmation`} className={`block ${labelClass}`}>Type {confirmingSeason} to confirm</label>
            <input
              id={`${id}-confirmation`}
              type="text"
              inputMode="numeric"
              autoComplete="off"
              disabled={busy || needsReview}
              value={confirmation}
              onChange={(event) => setConfirmation(event.target.value)}
              className={fieldClass}
            />
            {error && <p role="alert" className="text-rose-700 dark:text-rose-400">{error}{needsReview && " Close this dialog and review the refreshed current season before confirming again."}</p>}
          </div>
        )}
      />
    </section>
  );
};

export default ArchiveSeasonSection;
