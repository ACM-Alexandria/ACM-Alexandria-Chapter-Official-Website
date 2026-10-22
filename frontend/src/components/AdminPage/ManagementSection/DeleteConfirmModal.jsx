import React from "react";
import ModalShell from "../SharedComponents/ModalShell";
import { dangerButtonClass, secondaryButtonClass } from "../SharedComponents/adminStyles";

const DeleteConfirmModal = ({ open, onClose, onConfirm, deletingItem, loading }) => (
  <ModalShell
    open={open}
    onClose={onClose}
    title="Delete record?"
    size="sm"
    busy={loading}
    footer={
      <>
        <button type="button" onClick={onClose} disabled={loading} className={secondaryButtonClass}>
          Keep it
        </button>
        <button type="button" onClick={onConfirm} disabled={loading} className={dangerButtonClass}>
          {loading ? "Deleting…" : "Delete"}
        </button>
      </>
    }
  >
    <p className="text-sm text-slate-700 dark:text-slate-300">
      Are you sure you want to delete{" "}
      <span className="font-semibold text-slate-900 dark:text-slate-100">
        "{deletingItem?.name || deletingItem?.title || deletingItem?.platform || `Season ${deletingItem?.seasonNumber}`}"
      </span>
      ? This is permanent and can't be undone.
    </p>
  </ModalShell>
);

export default DeleteConfirmModal;
