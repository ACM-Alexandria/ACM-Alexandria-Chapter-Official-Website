import { FiChevronLeft, FiChevronRight } from "react-icons/fi";

// Page numbers with previous / next; long ranges collapse around the current page. `page` is zero-based.
const Pagination = ({ page, totalPages, onChange }) => {
  if (totalPages <= 1) return null;
  const numbers = Array.from({ length: totalPages }, (_, i) => i).filter((i) => i === 0 || i === totalPages - 1 || Math.abs(i - page) <= 1);
  const item = "flex h-11 min-w-11 items-center justify-center rounded-xl px-3 text-sm font-semibold transition-colors";
  return (
    <nav aria-label="Pages" className="mt-14 flex items-center justify-center gap-2">
      <button type="button" onClick={() => onChange(page - 1)} disabled={page === 0} aria-label="Previous page" className={`${item} text-slate-600 ring-1 ring-ocean-106 hover:bg-white disabled:opacity-40 dark:text-ocean-239 dark:ring-white/15 dark:hover:bg-white/5`}><FiChevronLeft className="h-4 w-4" aria-hidden="true" /></button>
      {numbers.map((n, i) => (
        <span key={n} className="flex items-center gap-2">
          {i > 0 && n - numbers[i - 1] > 1 && <span className="px-1 text-slate-400" aria-hidden="true">…</span>}
          <button type="button" onClick={() => onChange(n)} aria-current={n === page ? "page" : undefined} className={`${item} ${n === page ? "bg-ocean-676 text-white dark:bg-ocean-461 dark:text-ocean-910" : "text-slate-700 ring-1 ring-ocean-106 hover:bg-white dark:text-slate-200 dark:ring-white/15 dark:hover:bg-white/5"}`}>{n + 1}</button>
        </span>
      ))}
      <button type="button" onClick={() => onChange(page + 1)} disabled={page >= totalPages - 1} aria-label="Next page" className={`${item} text-slate-600 ring-1 ring-ocean-106 hover:bg-white disabled:opacity-40 dark:text-ocean-239 dark:ring-white/15 dark:hover:bg-white/5`}><FiChevronRight className="h-4 w-4" aria-hidden="true" /></button>
    </nav>
  );
};

export default Pagination;
