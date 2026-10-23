// Shared class names for the admin tools, kept in one place so every tab and dialog
// looks the same. Style: soft rounded squares (8px controls, 12px row cards, 16px panels),
// brand blue for actions and a soft blue tint for the active state.

export const fieldClass =
  "w-full h-10 px-3 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg outline-none text-slate-900 dark:text-slate-100 text-sm placeholder:text-slate-500 transition-colors focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/25 disabled:opacity-60";

export const textareaClass =
  "w-full px-3 py-2.5 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg outline-none text-slate-900 dark:text-slate-100 text-sm placeholder:text-slate-500 transition-colors focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/25 disabled:opacity-60";

export const labelClass = "text-sm font-medium text-slate-700 dark:text-slate-300";

export const hintClass = "text-xs text-slate-600 dark:text-slate-400";

const buttonBase =
  "h-9 px-3.5 inline-flex items-center justify-center gap-1.5 rounded-lg text-sm font-semibold whitespace-nowrap transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-[#4B98C8] dark:focus-visible:ring-offset-slate-900 disabled:opacity-60 disabled:cursor-not-allowed";

// #2A7AB0 keeps white text at AA contrast; #4B98C8 stays the accent for focus and charts
export const primaryButtonClass = `${buttonBase} bg-[#2A7AB0] hover:bg-[#23699A] text-white`;

export const secondaryButtonClass = `${buttonBase} bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-800`;

export const dangerButtonClass = `${buttonBase} bg-rose-600 hover:bg-rose-700 text-white`;

// Square icon button used in table rows
export const iconButtonClass =
  "w-9 h-9 inline-flex items-center justify-center rounded-lg border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/40 disabled:opacity-40 disabled:cursor-not-allowed";

export const iconButtonDangerClass =
  "w-9 h-9 inline-flex items-center justify-center rounded-lg border border-rose-200 dark:border-rose-900 bg-white dark:bg-slate-900 text-rose-600 dark:text-rose-400 hover:bg-rose-50 dark:hover:bg-rose-950/40 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-rose-400/40 disabled:opacity-40 disabled:cursor-not-allowed";

// Bordered white panel (cards, content areas)
export const panelClass = "rounded-2xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900";

// Tables: one framed white card, a tinted header band, thin dividers between rows and a tint
// on hover. Wrap the table in tableFrameClass, use <table className={tableClass}>, and put
// rowBorderClass on every <td> of a "group" row.
export const tableFrameClass = `${panelClass} overflow-hidden @container`;
export const tableClass = "w-full text-left border-separate border-spacing-0";
export const tableHeadRowClass = "text-xs font-semibold text-slate-600 dark:text-slate-400 bg-[#EEF4F9] dark:bg-slate-800/60";
export const rowBorderClass = "border-t border-slate-200 dark:border-slate-800 transition-colors group-hover:bg-[#F7FAFD] dark:group-hover:bg-slate-800/40";

// Tags / status pills
const tagBase = "inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-semibold whitespace-nowrap";
export const tagBrandClass = `${tagBase} bg-[#E2F0FA] dark:bg-[#16364C] text-[#1D6A9C] dark:text-[#7DBBE0]`;
export const tagSuccessClass = `${tagBase} bg-emerald-50 dark:bg-emerald-950/40 text-emerald-700 dark:text-emerald-300`;
export const tagMutedClass = `${tagBase} bg-slate-100 dark:bg-slate-800 text-slate-700 dark:text-slate-300`;
export const tagWarnClass = `${tagBase} bg-amber-50 dark:bg-amber-950/40 text-amber-800 dark:text-amber-300`;
export const tagDangerClass = `${tagBase} bg-rose-50 dark:bg-rose-950/40 text-rose-700 dark:text-rose-300`;

// Inline notices
export const noticeClass = {
  success: "rounded-xl border px-4 py-3 text-sm bg-emerald-50 dark:bg-emerald-950/40 border-emerald-200 dark:border-emerald-900 text-emerald-800 dark:text-emerald-200",
  error: "rounded-xl border px-4 py-3 text-sm bg-rose-50 dark:bg-rose-950/40 border-rose-200 dark:border-rose-900 text-rose-800 dark:text-rose-200",
  warning: "rounded-xl border px-4 py-3 text-sm bg-amber-50 dark:bg-amber-950/40 border-amber-200 dark:border-amber-900 text-amber-900 dark:text-amber-200",
};
