const LogoutConfirmModal = ({ open, onCancel, onConfirm }) => {
  if (!open) {
    return null;
  }

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4">
      <div
        className="w-full max-w-sm rounded-2xl bg-white p-6 text-[#17324f] shadow-2xl"
        role="dialog"
        aria-modal="true"
        aria-labelledby="logout-modal-title"
        aria-describedby="logout-modal-description"
      >
        <h2 id="logout-modal-title" className="text-xl font-bold">
          Log out?
        </h2>
        <p id="logout-modal-description" className="mt-3 text-sm text-slate-600">
          Are you sure you want to log out from your account?
        </p>

        <div className="mt-6 flex justify-end gap-3">
          <button
            type="button"
            className="rounded-md border border-slate-300 px-4 py-2 font-semibold text-slate-700 hover:bg-slate-100 transition-colors"
            onClick={onCancel}
          >
            Cancel
          </button>
          <button
            type="button"
            className="rounded-md bg-[#205E85] px-4 py-2 font-semibold text-white hover:bg-[#174967] transition-colors"
            onClick={onConfirm}
          >
            Log out
          </button>
        </div>
      </div>
    </div>
  );
};

export default LogoutConfirmModal;