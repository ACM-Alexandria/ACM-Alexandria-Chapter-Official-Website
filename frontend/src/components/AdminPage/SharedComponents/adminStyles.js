// Shared class names for the admin tools, kept in one place so every tab and dialog
// uses the same sizes, radii and colors as the profile pages.

export const fieldClass =
  "w-full h-10 px-3 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-md outline-none text-slate-900 dark:text-slate-100 text-sm placeholder:text-slate-500 transition-colors focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/25 disabled:opacity-60";

export const textareaClass =
  "w-full px-3 py-2.5 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-md outline-none text-slate-900 dark:text-slate-100 text-sm placeholder:text-slate-500 transition-colors focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/25 disabled:opacity-60";

export const labelClass = "text-sm font-medium text-slate-700 dark:text-slate-300";

export const hintClass = "text-xs text-slate-600 dark:text-slate-400";

const buttonBase =
  "h-9 px-3.5 inline-flex items-center justify-center gap-1.5 rounded-md text-sm font-medium whitespace-nowrap transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-[#4B98C8] dark:focus-visible:ring-offset-slate-900 disabled:opacity-60 disabled:cursor-not-allowed";

export const primaryButtonClass = `${buttonBase} bg-[#4B98C8] hover:bg-[#3d7da6] text-white`;

export const secondaryButtonClass = `${buttonBase} bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-800`;

export const dangerButtonClass = `${buttonBase} bg-rose-600 hover:bg-rose-700 text-white`;

// Square icon button used in table rows
export const iconButtonClass =
  "w-9 h-9 inline-flex items-center justify-center rounded-md border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/40 disabled:opacity-40 disabled:cursor-not-allowed";

export const iconButtonDangerClass =
  "w-9 h-9 inline-flex items-center justify-center rounded-md border border-rose-200 dark:border-rose-900 bg-white dark:bg-slate-900 text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/40 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-rose-400/40 disabled:opacity-40 disabled:cursor-not-allowed";

// Bordered white panel (cards, table containers)
export const panelClass = "rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900";

// Gray table header row and body row divider
export const tableHeadRowClass = "text-sm text-slate-600 dark:text-slate-400 bg-slate-100 dark:bg-slate-800/60";
export const rowBorderClass = "border-b border-slate-200 dark:border-slate-800 group-last:border-b-0";

// Tags / status pills
const tagBase = "inline-flex items-center px-2.5 py-0.5 rounded-full border text-xs font-semibold whitespace-nowrap";
export const tagBrandClass = `${tagBase} bg-[#4B98C8]/10 border-[#4B98C8]/30 text-[#2C6F99] dark:text-[#7DBBE0]`;
export const tagSuccessClass = `${tagBase} bg-emerald-50 dark:bg-emerald-950/40 border-emerald-200 dark:border-emerald-900 text-emerald-700 dark:text-emerald-300`;
export const tagMutedClass = `${tagBase} bg-slate-100 dark:bg-slate-800 border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300`;
export const tagWarnClass = `${tagBase} bg-amber-50 dark:bg-amber-950/40 border-amber-200 dark:border-amber-900 text-amber-800 dark:text-amber-300`;
export const tagDangerClass = `${tagBase} bg-rose-50 dark:bg-rose-950/40 border-rose-200 dark:border-rose-900 text-rose-700 dark:text-rose-300`;

// Inline notices
export const noticeClass = {
  success: "rounded-lg border px-4 py-3 text-sm bg-emerald-50 dark:bg-emerald-950/40 border-emerald-200 dark:border-emerald-900 text-emerald-800 dark:text-emerald-200",
  error: "rounded-lg border px-4 py-3 text-sm bg-rose-50 dark:bg-rose-950/40 border-rose-200 dark:border-rose-900 text-rose-800 dark:text-rose-200",
  warning: "rounded-lg border px-4 py-3 text-sm bg-amber-50 dark:bg-amber-950/40 border-amber-200 dark:border-amber-900 text-amber-900 dark:text-amber-200",
};
