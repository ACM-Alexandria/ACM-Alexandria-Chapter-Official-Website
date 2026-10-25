import { muted } from "./uiStyles";

// A friendly block for "nothing here yet" or "couldn't load", with an optional action.
const EmptyState = ({ icon, title, text, tone = "neutral", children }) => {
  const Icon = icon;
  return (
    <div className={`rounded-[2rem] px-6 py-16 text-center ring-1 ${tone === "error" ? "bg-red-50 ring-red-100 dark:bg-red-950/30 dark:ring-red-900/50" : "bg-white ring-[#D6E6F2] dark:bg-[#0F2233] dark:ring-white/10"}`}>
      {Icon && <span className={`mx-auto flex h-14 w-14 items-center justify-center rounded-2xl ${tone === "error" ? "bg-red-100 text-red-600 dark:bg-red-950/60 dark:text-red-300" : "bg-[#E2F0FA] text-[#205E85] dark:bg-[#16364C] dark:text-[#7DBBE0]"}`}><Icon className="h-6 w-6" aria-hidden="true" /></span>}
      <p className="mt-5 text-lg font-semibold text-slate-900 dark:text-white">{title}</p>
      {text && <p className={`mx-auto mt-2 max-w-md text-sm ${muted}`}>{text}</p>}
      {children && <div className="mt-6 flex justify-center gap-3">{children}</div>}
    </div>
  );
};

export default EmptyState;
