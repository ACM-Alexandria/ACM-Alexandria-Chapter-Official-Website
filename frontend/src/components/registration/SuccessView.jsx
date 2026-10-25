import { FiCheck } from "react-icons/fi";
import { primaryButton, muted } from "../ui/uiStyles";

// The last step of every registration: a check mark, what happened, and a Done button.
const SuccessView = ({ title, entityName, onClose }) => (
  <div className="flex flex-col items-center px-6 py-10 text-center sm:px-8" style={{ animation: "hp-rise 0.5s cubic-bezier(0.22,1,0.36,1) both" }}>
    <span className="flex h-16 w-16 items-center justify-center rounded-full bg-emerald-50 text-emerald-600 dark:bg-emerald-950/50 dark:text-emerald-300" style={{ animation: "hp-pop 0.5s cubic-bezier(0.22,1,0.36,1) 0.1s both" }}>
      <FiCheck className="h-8 w-8" aria-hidden="true" />
    </span>
    <h2 id="registration-title" className="mt-5 text-xl font-semibold tracking-tight text-slate-900 dark:text-white">{title}</h2>
    <p className={`mt-2 max-w-sm text-sm leading-relaxed ${muted}`}>
      You're all set for <span className="font-semibold text-slate-900 dark:text-white">{entityName}</span>. A confirmation email is on its way to your inbox.
    </p>
    <button type="button" onClick={onClose} className={`mt-7 w-full sm:w-auto sm:min-w-40 ${primaryButton}`}>Done</button>
  </div>
);

export default SuccessView;
