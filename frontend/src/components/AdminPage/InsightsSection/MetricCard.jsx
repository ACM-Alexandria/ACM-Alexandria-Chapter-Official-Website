import { createElement } from "react";

// One number on the insights dashboard. A single brand tint for every icon keeps the
// page calm; the chart colors below carry the variety.
const MetricCard = ({ label, value, subText, icon }) => (
  <div className="rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 p-4 flex flex-col items-start gap-3 sm:flex-row sm:items-center min-w-0">
    <div
      className="w-10 h-10 rounded-lg flex items-center justify-center shrink-0 bg-[#4B98C8]/10 text-[#2C6F99] dark:text-[#7DBBE0]"
      aria-hidden="true"
    >
      {createElement(icon, { className: "w-5 h-5" })}
    </div>
    <div className="min-w-0">
      <p className="text-sm text-slate-600 dark:text-slate-400 leading-snug">{label}</p>
      <p className="mt-0.5 text-2xl font-semibold text-slate-900 dark:text-slate-100 tabular-nums leading-none">{value}</p>
      {subText && <p className="mt-1 text-xs text-slate-600 dark:text-slate-400 truncate">{subText}</p>}
    </div>
  </div>
);

export default MetricCard;
