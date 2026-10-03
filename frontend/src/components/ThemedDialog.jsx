import React from "react";
import { createPortal } from "react-dom";
import { FiAlertTriangle, FiCheckCircle, FiInfo, FiSend } from "react-icons/fi";

// Same look as UnsubscribeConfirmModal, so confirmations, errors and hints all match across the site
const TONES = {
  danger: {
    icon: FiAlertTriangle,
    iconClass: "bg-red-50 dark:bg-red-950/40 text-red-500 dark:text-red-400",
    buttonClass: "bg-red-600 hover:bg-red-700",
  },
  warning: {
    icon: FiAlertTriangle,
    iconClass: "bg-amber-50 dark:bg-amber-950/40 text-amber-500 dark:text-amber-400",
    buttonClass: "bg-amber-500 hover:bg-amber-600",
  },
  success: {
    icon: FiCheckCircle,
    iconClass: "bg-green-50 dark:bg-green-950/40 text-green-600 dark:text-green-400",
    buttonClass: "bg-green-600 hover:bg-green-700",
  },
  brand: {
    icon: FiSend,
    iconClass: "bg-sky-50 dark:bg-sky-950/40 text-[#4B98C8]",
    buttonClass: "bg-gradient-to-br from-[#4B98C8] to-[#205E85] hover:brightness-110",
  },
  info: {
    icon: FiInfo,
    iconClass: "bg-sky-50 dark:bg-sky-950/40 text-[#4B98C8]",
    buttonClass: "bg-gradient-to-br from-[#4B98C8] to-[#205E85] hover:brightness-110",
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
}) => {
  const dismiss = onCancel || onConfirm;

  React.useEffect(() => {
    if (!open) return;
    const handleKeyDown = (e) => {
      if (e.key === "Escape") {
        e.preventDefault();
        if (dismiss) dismiss();
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
      onClick={dismiss}
      className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]"
    >
      <div
        onClick={(e) => e.stopPropagation()}
        role="alertdialog"
        aria-modal="true"
        className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-xl max-w-sm w-full p-6 text-center animate-[scaleIn_0.25s_ease]"
      >
        <div className={`w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-4 ${theme.iconClass}`}>
          <Icon className="w-6 h-6" />
        </div>
        <h3 className="text-base font-extrabold text-slate-800 dark:text-slate-100 tracking-tight">{title}</h3>
        {content && (
          <div className="text-xs text-slate-400 dark:text-slate-300 font-semibold mt-2 leading-relaxed">{content}</div>
        )}
        <div className="flex gap-3 justify-center mt-6">
          {cancelLabel && (
            <button
              onClick={onCancel}
              autoFocus={tone === "danger"}
              className="px-4 py-2.5 border border-slate-200 dark:border-slate-600 text-slate-500 dark:text-slate-300 hover:text-slate-800 dark:hover:text-slate-100 hover:bg-slate-50 dark:hover:bg-slate-700 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all cursor-pointer"
            >
              {cancelLabel}
            </button>
          )}
          <button
            onClick={onConfirm}
            autoFocus={tone !== "danger" || !cancelLabel}
            className={`px-4 py-2.5 text-white text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow cursor-pointer ${theme.buttonClass}`}
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
  <span className="font-extrabold text-slate-700 dark:text-slate-200">"{children}"</span>
);

export default ThemedDialog;
