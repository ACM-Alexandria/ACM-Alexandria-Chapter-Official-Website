import { HiOutlineExclamationCircle } from "react-icons/hi";

const RegistrationCancellationModal = ({ open, onClose, onConfirm, entityName, entityLabel = "registration", loading, error }) => {
  if (!open) return null;

  return (
    <div
      className="fixed inset-0 z-[60] flex items-center justify-center bg-slate-950/45 p-4 backdrop-blur-sm"
      role="dialog"
      aria-modal="true"
      aria-labelledby="cancel-registration-title"
    >
      <div className="w-full max-w-md rounded-[2rem] border border-slate-100 bg-white p-8 text-center shadow-2xl dark:border-slate-700 dark:bg-slate-900">
        <div className="mx-auto mb-5 flex h-14 w-14 items-center justify-center rounded-2xl bg-red-50 text-red-500 dark:bg-red-950/40 dark:text-red-300">
          <HiOutlineExclamationCircle className="h-8 w-8" />
        </div>
        <h3 id="cancel-registration-title" className="text-xl font-black text-slate-900 dark:text-slate-100">Cancel {entityLabel}?</h3>
        <p className="mt-3 text-sm font-medium leading-relaxed text-slate-600 dark:text-slate-300">
          Are you sure you want to cancel your {entityLabel} for <span className="font-bold">{entityName}</span>? You can register again afterwards.
        </p>
        {error && <p className="mt-4 rounded-xl border border-red-200 bg-red-50 px-3 py-2 text-xs font-bold text-red-700 dark:border-red-900 dark:bg-red-950/40 dark:text-red-200">{error}</p>}
        <div className="mt-7 flex gap-3">
          <button type="button" disabled={loading} onClick={onClose} className="flex-1 rounded-2xl border border-slate-200 bg-white py-3 text-sm font-bold text-slate-700 transition-colors hover:bg-slate-50 disabled:opacity-50 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-200 dark:hover:bg-slate-700">Keep it</button>
          <button type="button" disabled={loading} onClick={onConfirm} className="flex-1 rounded-2xl bg-red-600 py-3 text-sm font-bold text-white shadow-lg shadow-black-400 transition-all hover:bg-red-700 active:scale-95 disabled:cursor-not-allowed disabled:opacity-60">{loading ? "Cancelling..." : "Yes, cancel"}</button>
        </div>
      </div>
    </div>
  );
};

export default RegistrationCancellationModal;
