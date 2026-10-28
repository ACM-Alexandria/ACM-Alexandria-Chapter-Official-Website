import React from "react";
import { createPortal } from "react-dom";
import useScrollLock from "../hooks/useScrollLock";
import { FiAlertTriangle, FiCheckCircle, FiInfo, FiSend } from "react-icons/fi";

const TONES = {
  danger: {
    icon: FiAlertTriangle,
    iconClass: "bg-rose-50 dark:bg-rose-950/40 text-rose-600 dark:text-rose-400",
    buttonClass: "bg-rose-600 hover:bg-rose-700",
  },
  warning: {
    icon: FiAlertTriangle,
    iconClass: "bg-amber-50 dark:bg-amber-950/40 text-amber-600 dark:text-amber-400",
    buttonClass: "bg-amber-600 hover:bg-amber-700",
  },
  success: {
    icon: FiCheckCircle,
    iconClass: "bg-emerald-50 dark:bg-emerald-950/40 text-emerald-600 dark:text-emerald-400",
    buttonClass: "bg-emerald-600 hover:bg-emerald-700",
  },
  brand: {
    icon: FiSend,
    iconClass: "bg-ocean-461/10 text-ocean-614 dark:text-ocean-316",
    buttonClass: "bg-ocean-461 hover:bg-ocean-555",
  },
  info: {
    icon: FiInfo,
    iconClass: "bg-ocean-461/10 text-ocean-614 dark:text-ocean-316",
    buttonClass: "bg-ocean-461 hover:bg-ocean-555",
  },
};

// Without cancelLabel it is a notice with a single button (errors and hints); with it, a confirmation
const ThemedDialog = ({
  open,
  tone = "info",
  icon,
  title,
  message,
  description,
  confirmLabel = "OK",
  cancelLabel,
  onConfirm,
  onCancel,
  confirmDisabled = false,
  busy = false,
}) => {
  const dismiss = busy ? undefined : onCancel || onConfirm;
  const dialogRef = React.useRef(null);
  const id = React.useId();
  useScrollLock(open);

  React.useEffect(() => {
    if (!open) return;
    const previous = document.activeElement;
    return () => {
      if (previous?.isConnected) previous.focus();
    };
  }, [open]);

  React.useEffect(() => {
    if (!open) return;
    const handleKeyDown = (e) => {
      const overlays = document.querySelectorAll("[data-modal-overlay]");
      if (overlays[overlays.length - 1] !== dialogRef.current?.closest("[data-modal-overlay]")) return;
      if (e.key === "Escape") {
        e.preventDefault();
        if (dismiss) dismiss();
      }
      if (e.key === "Tab") {
        const elements = [...dialogRef.current.querySelectorAll(
          "input:not(:disabled), select:not(:disabled), textarea:not(:disabled), button:not(:disabled), a[href]"
        )];
        const first = elements[0];
        const last = elements.at(-1);
        if (!first) {
          e.preventDefault();
        } else if (!dialogRef.current.contains(document.activeElement)) {
          e.preventDefault();
          first.focus();
        } else if (e.shiftKey && document.activeElement === first) {
          e.preventDefault();
          last.focus();
        } else if (!e.shiftKey && document.activeElement === last) {
          e.preventDefault();
          first.focus();
        }
      }
    };
    window.addEventListener("keydown", handleKeyDown);
    return () => window.removeEventListener("keydown", handleKeyDown);
  }, [open, dismiss]);

  if (!open) return null;

  const content = description !== undefined ? description : message;
  const theme = TONES[tone] || TONES.info;
  const Icon = icon || theme.icon;

  return createPortal(
    <div
      data-modal-overlay
      onClick={dismiss}
      className="fixed inset-0 z-[80] flex items-center justify-center p-4 bg-slate-900/50 animate-[fadeIn_0.2s_ease-out] motion-reduce:animate-none"
    >
      <div
        ref={dialogRef}
        onClick={(e) => e.stopPropagation()}
        role="alertdialog"
        aria-modal="true"
        aria-labelledby={`${id}-title`}
        aria-describedby={content ? `${id}-content` : undefined}
        aria-busy={busy}
        className="flex max-h-[90dvh] w-full max-w-sm flex-col rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-xl overflow-hidden animate-[scaleIn_0.2s_ease-out] motion-reduce:animate-none"
      >
        <div className="min-h-0 overflow-y-auto p-5 flex gap-4">
          <div className={`w-10 h-10 shrink-0 rounded-full flex items-center justify-center ${theme.iconClass}`}>
            <Icon className="w-5 h-5" aria-hidden="true" />
          </div>
          <div className="min-w-0">
            <h3 id={`${id}-title`} className="text-base font-semibold text-slate-900 dark:text-slate-100">{title}</h3>
            {content && <div id={`${id}-content`} className="mt-1.5 text-sm text-slate-700 dark:text-slate-300 break-words">{content}</div>}
          </div>
        </div>
        <div className="shrink-0 px-5 py-4 flex flex-col-reverse sm:flex-row sm:justify-end gap-2 border-t border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900/60">
          {cancelLabel && (
            <button
              type="button"
              onClick={onCancel}
              disabled={busy}
              autoFocus={tone === "danger"}
              className="h-9 px-4 rounded-md text-sm font-medium text-slate-700 dark:text-slate-200 bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors disabled:cursor-not-allowed disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ocean-461/40"
            >
              {cancelLabel}
            </button>
          )}
          <button
            type="button"
            onClick={onConfirm}
            disabled={confirmDisabled || busy}
            autoFocus={tone !== "danger" || !cancelLabel}
            className={`h-9 px-4 rounded-md text-sm font-medium text-white transition-colors disabled:cursor-not-allowed disabled:opacity-50 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-ocean-461 dark:focus-visible:ring-offset-slate-900 ${theme.buttonClass}`}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>,
    document.body
  );
};

// Highlights a name inside a dialog message, like "ACM Newsletter Updates" in the unsubscribe modal
export const Highlight = ({ children }) => (
  <span className="font-semibold text-slate-900 dark:text-slate-100">"{children}"</span>
);

export default ThemedDialog;
