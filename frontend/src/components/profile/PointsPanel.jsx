import { FaStar } from "react-icons/fa";
import Pagination from "../ui/Pagination";
import { card, muted } from "../ui/uiStyles";

const date = (iso) => (iso ? new Date(iso).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" }) : "");

// The Points tab: this season's total as a big number, then the history as rows, newest first.
// `page` is zero-based; `error` replaces the history when it could not be loaded.
const PointsPanel = ({ total, logs, page, totalPages, onPageChange, error }) => (
  <div className="flex flex-col gap-8">
    <div className="flex items-center gap-5">
      <span aria-hidden="true" className="flex h-16 w-16 shrink-0 items-center justify-center rounded-2xl bg-gold-116 dark:bg-gold-851">
        <FaStar className="h-8 w-8 text-gold-518" style={{ animation: "hp-star-beat 3.6s ease-in-out infinite" }} />
      </span>
      <div>
        <p className="text-5xl font-semibold leading-none tracking-tight text-ocean-676 tabular-nums dark:text-white">{total}</p>
        <p className={`mt-1.5 text-sm font-medium ${muted}`}>points this season</p>
      </div>
    </div>

    {error ? (
      <p role="alert" className="rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700 dark:bg-red-950/40 dark:text-red-200">{error}</p>
    ) : logs.length === 0 ? (
      <div className={`rounded-3xl px-6 py-12 text-center ${card}`}>
        <p className="font-semibold text-slate-900 dark:text-white">No points yet</p>
        <p className={`mx-auto mt-1.5 max-w-sm text-sm ${muted}`}>Points from events, tasks, and committee work will show up here.</p>
      </div>
    ) : (
      <>
        <ul className={`divide-y divide-ocean-69 overflow-hidden rounded-3xl dark:divide-white/10 ${card}`}>
          {logs.map((log) => {
            const created = log.createdAt || log.created_at;
            return (
              <li key={log.id} className="flex items-center gap-4 px-5 py-4 sm:px-6">
                <div className="min-w-0 flex-1">
                  <p className="truncate font-semibold text-slate-900 dark:text-white" title={log.reason}>{log.reason}</p>
                  <p className={`mt-0.5 text-sm ${muted}`}>{date(created)}</p>
                </div>
                <span className={`shrink-0 text-lg font-semibold tabular-nums ${log.points >= 0 ? "text-emerald-700 dark:text-emerald-400" : "text-red-600 dark:text-red-400"}`}>{log.points > 0 ? `+${log.points}` : log.points}</span>
              </li>
            );
          })}
        </ul>
        <div className="-mt-6"><Pagination page={page} totalPages={totalPages} onChange={onPageChange} /></div>
      </>
    )}
  </div>
);

export default PointsPanel;
