import { useEffect, useId, useRef, useState } from "react";
import { FiArrowRight, FiCalendar, FiChevronDown, FiPlus, FiX } from "react-icons/fi";
import { formatBoardSeason } from "../../../utils/boardSeasons";
import { isPastBoardSeason } from "../../../utils/validation";

const PastSeasonPicker = ({ currentSeason, seasons, season, disabled, onSelect }) => {
  const [adding, setAdding] = useState(false);
  const [year, setYear] = useState("");
  const [error, setError] = useState("");
  const id = useId();
  const inputRef = useRef(null);
  const addButtonRef = useRef(null);
  const available = [...new Set([...seasons, ...(season ? [Number(season)] : [])])].sort((a, b) => b - a);
  const validYear = isPastBoardSeason(year, currentSeason);
  const existingYear = seasons.includes(Number(year));

  useEffect(() => {
    if (adding) inputRef.current?.focus();
  }, [adding]);

  const close = () => {
    setAdding(false);
    setError("");
    addButtonRef.current?.focus();
  };

  const submit = (event) => {
    event.preventDefault();
    if (!validYear) {
      setError(`Enter a starting year between 2000 and ${currentSeason - 1}.`);
      inputRef.current?.focus();
      return;
    }
    onSelect(Number(year));
    close();
  };

  return (
    <div className="overflow-hidden rounded-2xl border border-slate-200 bg-white shadow-sm dark:border-slate-700 dark:bg-slate-900">
      <div className="flex flex-col gap-4 p-5 sm:flex-row sm:items-end sm:justify-between sm:p-6">
        <div className="min-w-0 flex-1 sm:max-w-xs">
          <label htmlFor={`${id}-season`} className="mb-2 block text-xs font-bold uppercase tracking-wider text-slate-500 dark:text-slate-300">
            Past season
          </label>
          <div className="relative">
            <FiCalendar aria-hidden="true" className="pointer-events-none absolute left-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-[#4B98C8]" />
            <select
              id={`${id}-season`}
              disabled={disabled || !available.length}
              value={season}
              onChange={(event) => onSelect(Number(event.target.value))}
              className="w-full appearance-none rounded-xl border border-slate-200 bg-slate-50 py-3 pl-10 pr-10 text-sm font-bold text-slate-800 outline-none transition focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/20 disabled:opacity-60 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100"
            >
              {!available.length && <option value="">No past seasons yet</option>}
              {available.map((value) => <option key={value} value={value}>{formatBoardSeason(value)}</option>)}
            </select>
            <FiChevronDown aria-hidden="true" className="pointer-events-none absolute right-3.5 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
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
          className="inline-flex items-center justify-center gap-2 rounded-xl border border-[#4B98C8]/30 bg-[#4B98C8]/5 px-4 py-3 text-sm font-bold text-[#205E85] transition hover:border-[#4B98C8]/60 hover:bg-[#4B98C8]/10 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8] disabled:cursor-not-allowed disabled:opacity-50 dark:bg-[#4B98C8]/10 dark:text-sky-300 dark:hover:bg-[#4B98C8]/20"
        >
          {adding ? <FiX aria-hidden="true" /> : <FiPlus aria-hidden="true" />}
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
          className="border-t border-slate-200 bg-slate-50/70 p-5 dark:border-slate-700 dark:bg-slate-800/40 sm:p-6"
        >
          <h3 className="text-sm font-extrabold text-slate-800 dark:text-slate-100">Choose a starting year</h3>
          <p id={`${id}-hint`} className="mt-1 text-xs leading-relaxed text-slate-500 dark:text-slate-300">
            Use a year before the current season. The season is saved when you add its first record.
          </p>
          <fieldset disabled={disabled} className="mt-4 flex flex-col gap-4 sm:flex-row sm:items-end">
            <div className="sm:w-36">
              <label htmlFor={`${id}-year`} className="block text-xs font-bold text-slate-600 dark:text-slate-300">Starting year</label>
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
                className="mt-1.5 w-full rounded-xl border border-slate-300 bg-white px-3.5 py-3 text-sm font-bold tabular-nums text-slate-800 outline-none focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/20 aria-invalid:border-red-400 dark:border-slate-600 dark:bg-slate-900 dark:text-slate-100"
              />
            </div>
            <div className="flex min-h-11 items-center gap-3 text-sm" aria-live="polite">
              <FiArrowRight aria-hidden="true" className="hidden text-slate-400 sm:block" />
              <span className="rounded-lg border border-[#4B98C8]/15 bg-[#4B98C8]/10 px-3 py-2 font-bold tabular-nums text-[#205E85] dark:text-sky-300">
                {validYear ? formatBoardSeason(Number(year)) : "YYYY/YYYY"}
              </span>
            </div>
            <div className="flex gap-2 sm:ml-auto">
              <button type="button" onClick={close} className="flex-1 rounded-xl px-4 py-3 text-sm font-bold text-slate-500 hover:bg-slate-200/60 dark:text-slate-300 dark:hover:bg-slate-700 sm:flex-none">Cancel</button>
              <button type="submit" className="flex-1 rounded-xl bg-gradient-to-br from-[#4B98C8] to-[#205E85] px-5 py-3 text-sm font-bold text-white shadow-sm transition hover:brightness-110 disabled:opacity-50 sm:flex-none">
                {existingYear ? "Open season" : "Use this season"}
              </button>
            </div>
          </fieldset>
          {error && <p id={`${id}-error`} role="alert" className="mt-3 text-xs font-semibold text-red-600 dark:text-red-400">{error}</p>}
        </form>
      )}
    </div>
  );
};

export default PastSeasonPicker;
