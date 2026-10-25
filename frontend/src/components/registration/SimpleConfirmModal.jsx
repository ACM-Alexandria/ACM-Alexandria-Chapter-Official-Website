import { FiAlertCircle, FiHelpCircle } from "react-icons/fi";
import SuccessView from "./SuccessView";
import { registrationCopy } from "./registrationCopy";
import { errorBox, muted, primaryButton, secondaryButton } from "../ui/uiStyles";

// Registration with no questions: one confirmation, then the success view.
const SimpleConfirmModal = ({ onClose, type, entityName, onSubmit, submitting, error, success }) => {
  const words = registrationCopy(type);
  if (success) return <SuccessView title={words.done} entityName={entityName} onClose={onClose} />;
  return (
    <form onSubmit={onSubmit} className="px-6 pb-6 pt-8 text-center sm:px-8">
      <span className="mx-auto flex h-14 w-14 items-center justify-center rounded-2xl bg-[#E2F0FA] text-[#205E85] dark:bg-[#16364C] dark:text-[#7DBBE0]"><FiHelpCircle className="h-6 w-6" aria-hidden="true" /></span>
      <p className="mt-5 text-xs font-semibold text-[#2A7AB0] dark:text-[#7DBBE0]">{words.eyebrow}</p>
      <h2 id="registration-title" className="mt-1 text-xl font-semibold tracking-tight text-slate-900 dark:text-white" style={{ textWrap: "balance" }}>
        {words.verb[0].toUpperCase() + words.verb.slice(1)} {entityName}?
      </h2>
      <p className={`mt-2 text-sm ${muted}`}>We'll use the details in your profile.</p>
      {error && <p className={`mt-5 text-left ${errorBox}`}><FiAlertCircle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />{error}</p>}
      <div className="mt-7 grid grid-cols-2 gap-3">
        <button type="button" onClick={onClose} disabled={submitting} className={secondaryButton}>Not now</button>
        <button type="submit" disabled={submitting} className={primaryButton}>{submitting ? "Sending…" : "Yes, confirm"}</button>
      </div>
    </form>
  );
};

export default SimpleConfirmModal;
