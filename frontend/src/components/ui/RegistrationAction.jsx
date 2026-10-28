import { FiArrowRight, FiCheck, FiLock } from "react-icons/fi";
import { primaryButton } from "./uiStyles";

const notice = "flex w-full items-center justify-center gap-2 rounded-2xl px-5 py-4 text-sm font-semibold";

// The call to action at the bottom of a details panel, by state: `ended`, `closed`, `locked` (members only),
// `checking`, `registered` (with a cancel link), or `open` (the register button).
const RegistrationAction = ({ status, labels = {}, onOpen, onCancel }) => {
  if (status === "checking") {
    return (
      <div className={`${notice} bg-white text-slate-500 ring-1 ring-ocean-106 dark:bg-ocean-871 dark:text-ocean-239 dark:ring-white/10`}>
        <span className="h-4 w-4 animate-spin rounded-full border-2 border-ocean-106 border-t-ocean-461" aria-hidden="true" />Checking your registration
      </div>
    );
  }
  if (status === "registered") {
    return (
      <div className="space-y-3 text-center">
        <div className={`${notice} bg-emerald-50 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-300`}>
          <FiCheck className="h-5 w-5" aria-hidden="true" />{labels.registered || "You're registered"}
        </div>
        {onCancel && <button type="button" onClick={onCancel} className="rounded-lg px-3 py-2 text-sm font-semibold text-red-600 transition-colors hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-950/40">{labels.cancel || "Cancel registration"}</button>}
      </div>
    );
  }
  if (status === "open") {
    return <button type="button" onClick={onOpen} className={`${primaryButton} h-14 w-full rounded-2xl text-base`}>{labels.open || "Register"} <FiArrowRight className="h-5 w-5" aria-hidden="true" /></button>;
  }
  const text = { ended: labels.ended || "This has ended", closed: labels.closed || "Registration is closed", locked: labels.locked || "For chapter members only" }[status];
  return (
    <div className={`${notice} bg-white text-slate-500 ring-1 ring-ocean-106 dark:bg-ocean-871 dark:text-ocean-239 dark:ring-white/10`}>
      {status === "locked" && <FiLock className="h-4 w-4" aria-hidden="true" />}{text}
    </div>
  );
};

export default RegistrationAction;
