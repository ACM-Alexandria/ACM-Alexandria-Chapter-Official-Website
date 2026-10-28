import { createPortal } from "react-dom";
import { FiX } from "react-icons/fi";
import useOverlay from "../../hooks/useOverlay";

const widths = { sm: "max-w-md", md: "max-w-lg", lg: "max-w-2xl" };

// A centred dialog card over a dimmed page. Long content scrolls inside the card, never the page.
// `dismissable` false keeps it open on backdrop clicks and Escape (while something is being saved).
const Dialog = ({ open, onClose, size = "md", dismissable = true, labelledBy, z = "z-[60]", children }) => {
  const visible = useOverlay(open, dismissable ? onClose : undefined);
  if (!open) return null;
  // Rendered into <body> so no transformed ancestor can trap the fixed overlay.
  return createPortal(
    <div data-lenis-prevent className={`fixed inset-0 ${z} flex items-end justify-center p-3 sm:items-center sm:p-6`}>
      <div className={`absolute inset-0 bg-ocean-931/60 backdrop-blur-sm transition-opacity duration-300 ${visible ? "opacity-100" : "opacity-0"}`} onClick={dismissable ? onClose : undefined} />
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby={labelledBy}
        className={`relative flex max-h-[calc(100svh-1.5rem)] w-full flex-col overflow-hidden rounded-[2rem] bg-white shadow-[0_30px_80px_-30px_color-mix(in_srgb,var(--color-ocean-933)_70%,transparent)] ring-1 ring-ocean-106 transition-all duration-300 ease-out sm:max-h-[calc(100svh-3rem)] dark:bg-ocean-871 dark:ring-white/10 ${widths[size]} ${visible ? "translate-y-0 scale-100 opacity-100" : "translate-y-6 scale-[0.98] opacity-0"}`}
      >
        {children}
      </div>
    </div>,
    document.body,
  );
};

// Title row of a dialog, with an optional icon, eyebrow and close button.
export const DialogHeader = ({ icon, eyebrow, title, id, onClose }) => {
  const Icon = icon;
  return (
    <div className="flex shrink-0 items-start gap-4 border-b border-ocean-69 px-6 py-5 sm:px-8 dark:border-white/10">
      {Icon && <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-ocean-67 text-ocean-676 dark:bg-ocean-808 dark:text-ocean-316"><Icon className="h-5 w-5" aria-hidden="true" /></span>}
      <div className="min-w-0 flex-1">
        {eyebrow && <p className="text-xs font-semibold text-ocean-573 dark:text-ocean-316">{eyebrow}</p>}
        <h2 id={id} className="mt-0.5 text-lg font-semibold tracking-tight text-slate-900 dark:text-white">{title}</h2>
      </div>
      {onClose && (
        <button type="button" onClick={onClose} aria-label="Close" className="-mr-2 flex h-10 w-10 shrink-0 items-center justify-center rounded-full text-slate-500 transition-colors hover:bg-ocean-31 hover:text-slate-900 dark:text-ocean-239 dark:hover:bg-white/5 dark:hover:text-white">
          <FiX className="h-5 w-5" aria-hidden="true" />
        </button>
      )}
    </div>
  );
};

// The scrolling middle of a dialog.
export const DialogBody = ({ children, className = "" }) => (
  <div className={`flex-1 overflow-y-auto overscroll-contain px-6 py-6 sm:px-8 ${className}`}>{children}</div>
);

// The action row of a dialog.
export const DialogFooter = ({ children }) => (
  <div className="flex shrink-0 flex-col-reverse gap-3 border-t border-ocean-69 px-6 py-4 sm:flex-row sm:justify-end sm:px-8 dark:border-white/10">{children}</div>
);

export default Dialog;
