import React, { useEffect, useId, useRef } from "react";
import { FiX } from "react-icons/fi";
import useScrollLock from "../../../hooks/useScrollLock";

const SIZES = {
  sm: "sm:max-w-md",
  md: "sm:max-w-lg",
  lg: "sm:max-w-2xl",
  xl: "sm:max-w-4xl",
};

// Responsive dialog for the admin tools: a centered card from sm up, a full-width
// sheet from the bottom on phones. Pass `onSubmit` to make the dialog a form; `footer`
// is pinned under the scrolling body so the actions are always reachable.
const ModalShell = ({
  open,
  onClose,
  title,
  subtitle,
  children,
  footer,
  onSubmit,
  size = "md",
  busy = false,
}) => {
  const titleId = useId();
  const overlayRef = useRef(null);
  useScrollLock(open);

  useEffect(() => {
    if (!open) return undefined;
    const handleKeyDown = (e) => {
      if (e.key !== "Escape" || busy) return;
      // With dialogs stacked (e.g. a confirmation over this one), Escape closes only the top one
      const overlays = document.querySelectorAll("[data-modal-overlay]");
      if (overlays[overlays.length - 1] !== overlayRef.current) return;
      onClose();
    };
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [open, busy, onClose]);

  if (!open) return null;

  const Container = onSubmit ? "form" : "div";

  return (
    <div
      ref={overlayRef}
      data-modal-overlay
      className="fixed inset-0 z-[70] flex items-end sm:items-center justify-center sm:p-4 bg-slate-900/50 animate-[fadeIn_0.2s_ease-out] motion-reduce:animate-none"
      onMouseDown={(e) => { if (e.target === e.currentTarget && !busy) onClose(); }}
    >
      <Container
        role="dialog"
        aria-modal="true"
        aria-labelledby={titleId}
        onSubmit={onSubmit}
        className={`w-full ${SIZES[size] || SIZES.md} max-h-[92dvh] sm:max-h-[90vh] flex flex-col rounded-t-2xl sm:rounded-2xl border border-slate-200 dark:border-ocean-800 bg-white dark:bg-ocean-890 shadow-xl overflow-hidden animate-[sheetUp_0.25s_ease-out] sm:animate-[scaleIn_0.2s_ease-out] motion-reduce:animate-none`}
      >
        <div className="px-5 py-4 flex items-start justify-between gap-4 border-b border-slate-200 dark:border-ocean-800 shrink-0">
          <div className="min-w-0">
            <h3 id={titleId} className="text-base font-semibold text-slate-900 dark:text-slate-100">{title}</h3>
            {subtitle && <p className="mt-0.5 text-sm text-slate-600 dark:text-slate-400 truncate">{subtitle}</p>}
          </div>
          <button
            type="button"
            onClick={onClose}
            disabled={busy}
            aria-label="Close"
            title="Close"
            className="-mr-1.5 -mt-1 w-9 h-9 shrink-0 inline-flex items-center justify-center rounded-lg text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 hover:bg-slate-100 dark:hover:bg-ocean-845 transition-colors disabled:opacity-40 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ocean-461/40"
          >
            <FiX className="w-5 h-5" aria-hidden="true" />
          </button>
        </div>

        <div className="flex-1 min-h-0 overflow-y-auto overscroll-contain px-5 py-5">{children}</div>

        {footer && (
          <div className="px-5 py-4 flex flex-col-reverse sm:flex-row sm:justify-end gap-2 border-t border-slate-200 dark:border-ocean-800 bg-slate-50 dark:bg-ocean-890/60 pb-[max(1rem,env(safe-area-inset-bottom))] shrink-0">
            {footer}
          </div>
        )}
      </Container>
    </div>
  );
};

export default ModalShell;
