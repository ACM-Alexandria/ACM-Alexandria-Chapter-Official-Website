import { useEffect, useId, useRef, useState } from "react";
import { FiCalendar, FiPlus, FiX } from "react-icons/fi";
import { formatBoardSeason } from "../../../utils/boardSeasons";
import { isPastBoardSeason } from "../../../utils/validation";
import { addPastSeason } from "../../../services/pastBoardsAdminService";
import { panelClass, fieldClass, labelClass, hintClass, primaryButtonClass, secondaryButtonClass } from "../SharedComponents/adminStyles";

const messageOf = (failure) => failure.response?.data?.message || failure.message || "Unable to add the season.";

const PastSeasonPicker = ({ currentSeason, seasons, season, disabled, onSelect }) => {
  const [adding, setAdding] = useState(false);
  const [year, setYear] = useState("");
  const [error, setError] = useState("");
  const [checking, setChecking] = useState(false);
  const id = useId();
  const inputRef = useRef(null);
  const addButtonRef = useRef(null);
  const available = [...new Set([...seasons, ...(season ? [Number(season)] : [])])].sort((a, b) => b - a);
  const validYear = isPastBoardSeason(year, currentSeason);

  useEffect(() => {
    if (adding) inputRef.current?.focus();
  }, [adding]);

  const close = () => {
    setAdding(false);
    setError("");
    addButtonRef.current?.focus();
  };

  // The backend decides whether the season is new; an existing one comes back as its error message.
  const submit = async (event) => {
    event.preventDefault();
    if (checking) return;
    if (!validYear) {
      setError(`Enter a season between 2000 and ${currentSeason - 1}.`);
      inputRef.current?.focus();
      return;
    }
    setChecking(true);
    try {
      await addPastSeason(Number(year));
      onSelect(Number(year));
      close();
    } catch (failure) {
      setError(messageOf(failure));
      inputRef.current?.focus();
    } finally {
      setChecking(false);
    }
  };

  return (
    <div className={`${panelClass} p-4 sm:p-6`}>
      <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
        <div className="min-w-0 sm:max-w-xs sm:flex-1">
          <label htmlFor={`${id}-season`} className={`mb-1.5 block ${labelClass}`}>Past season</label>
          <div className="relative">
            <FiCalendar aria-hidden="true" className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-ocean-614 dark:text-ocean-316" />
            <select
              id={`${id}-season`}
              disabled={disabled || !available.length}
              value={season}
              onChange={(event) => onSelect(Number(event.target.value))}
              className={`${fieldClass} pl-9 tabular-nums`}
            >
              {!available.length && <option value="">No past seasons yet</option>}
              {available.map((value) => <option key={value} value={value}>{formatBoardSeason(value)}</option>)}
            </select>
          </div>
        </div>
        <button
          ref={addButtonRef}
          type="button"
          disabled={disabled || !currentSeason}
          aria-expanded={adding}
          aria-controls={`${id}-add`}
          onClick={() => {
            if (adding) close();
            else {
              setYear(String(currentSeason - 1));
              setError("");
              setAdding(true);
            }
          }}
          className={`${secondaryButtonClass} w-full sm:w-auto`}
        >
          {adding ? <FiX aria-hidden="true" className="h-4 w-4" /> : <FiPlus aria-hidden="true" className="h-4 w-4" />}
          {adding ? "Close" : "Add past season"}
        </button>
      </div>
      {adding && (
        <form
          id={`${id}-add`}
          onSubmit={submit}
          onKeyDown={(event) => {
            if (event.key === "Escape") {
              event.preventDefault();
              close();
            }
          }}
          noValidate
          className="mt-5 border-t border-slate-200 pt-5 dark:border-ocean-800"
        >
          <h3 className="text-base font-semibold text-slate-900 dark:text-slate-100">Enter the season</h3>
          <p id={`${id}-hint`} className={`mt-1 ${hintClass}`}>
            Enter the year of a season before the current one. It is saved right away; add its records afterwards.
          </p>
          <fieldset disabled={disabled} className="mt-4 flex flex-col gap-4 sm:flex-row sm:items-end">
            <div className="sm:w-40">
              <label htmlFor={`${id}-year`} className={`block ${labelClass}`}>Season</label>
              <input
                ref={inputRef}
                id={`${id}-year`}
                type="text"
                inputMode="numeric"
                maxLength={4}
                autoComplete="off"
                placeholder="e.g. 2024"
                value={year}
                aria-describedby={`${id}-hint${error ? ` ${id}-error` : ""}`}
                aria-invalid={Boolean(error)}
                onChange={(event) => {
                  setYear(event.target.value);
                  setError("");
                }}
                className={`mt-1.5 ${fieldClass} ${error ? "border-rose-500 focus:border-rose-500 focus:ring-rose-500/25" : ""}`}
              />
            </div>
            <div className="flex gap-2 sm:ml-auto">
              <button type="button" onClick={close} disabled={checking} className={secondaryButtonClass}>Cancel</button>
              <button type="submit" disabled={checking} className={primaryButtonClass}>{checking ? "Checking…" : "Use this season"}</button>
            </div>
          </fieldset>
          {error && <p id={`${id}-error`} role="alert" className="mt-3 text-xs font-medium text-rose-700 dark:text-rose-400">{error}</p>}
        </form>
      )}
    </div>
  );
};

export default PastSeasonPicker;
