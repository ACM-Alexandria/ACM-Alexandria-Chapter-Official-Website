import { useId } from "react";
import Dialog from "./Dialog";
import { dangerButton, errorBox, muted, primaryButton, secondaryButton } from "./uiStyles";

// A short yes/no question. `tone` "danger" makes the confirm button red.
const ConfirmDialog = ({ open, onClose, onConfirm, icon, tone = "primary", title, children, confirmLabel = "Confirm", cancelLabel = "Cancel", loading = false, loadingLabel, error }) => {
  const id = useId();
  const Icon = icon;
  return (
    <Dialog open={open} onClose={onClose} size="sm" dismissable={!loading} labelledBy={id} z="z-[70]">
      <div className="px-6 pb-6 pt-8 text-center sm:px-8">
        {Icon && (
          <span className={`mx-auto flex h-14 w-14 items-center justify-center rounded-2xl ${tone === "danger" ? "bg-red-50 text-red-600 dark:bg-red-950/40 dark:text-red-300" : "bg-ocean-67 text-ocean-676 dark:bg-ocean-808 dark:text-ocean-316"}`}>
            <Icon className="h-6 w-6" aria-hidden="true" />
          </span>
        )}
        <h2 id={id} className="mt-5 text-xl font-semibold tracking-tight text-slate-900 dark:text-white">{title}</h2>
        <div className={`mt-2 text-sm leading-relaxed ${muted}`}>{children}</div>
        {error && <p className={`mt-4 text-left ${errorBox}`}>{error}</p>}
        <div className="mt-7 grid grid-cols-2 gap-3">
          <button type="button" onClick={onClose} disabled={loading} className={secondaryButton}>{cancelLabel}</button>
          <button type="button" onClick={onConfirm} disabled={loading} className={tone === "danger" ? dangerButton : primaryButton}>{loading ? loadingLabel || "One moment…" : confirmLabel}</button>
        </div>
      </div>
    </Dialog>
  );
};

export default ConfirmDialog;
