import React from "react";
import { HiOutlineExclamation } from "react-icons/hi";
import ModalShell from "../AdminPage/SharedComponents/ModalShell";
import { dangerButtonClass, secondaryButtonClass } from "../AdminPage/SharedComponents/adminStyles";

const ConfirmDeleteModal = ({ isOpen, onClose, onConfirm, itemName }) => (
  <ModalShell
    open={isOpen}
    onClose={onClose}
    title="Delete points log"
    size="sm"
    footer={
      <>
        <button type="button" onClick={onClose} className={secondaryButtonClass}>
          Cancel
        </button>
        <button type="button" onClick={onConfirm} className={dangerButtonClass}>
          Delete
        </button>
      </>
    }
  >
    <div className="flex gap-4">
      <div className="w-10 h-10 shrink-0 rounded-full bg-rose-50 dark:bg-rose-950/40 flex items-center justify-center">
        <HiOutlineExclamation className="w-5 h-5 text-rose-600 dark:text-rose-400" aria-hidden="true" />
      </div>
      <p className="text-sm text-slate-700 dark:text-slate-300 break-words">
        Are you sure you want to delete <span className="font-medium text-slate-900 dark:text-slate-100">&ldquo;{itemName}&rdquo;</span>?
        This action cannot be undone and will permanently remove these points from the user.
      </p>
    </div>
  </ModalShell>
);

export default ConfirmDeleteModal;
