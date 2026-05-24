import React from "react";
import { FiAlertTriangle } from "react-icons/fi";

const DeleteConfirmModal = ({ open, onClose, onConfirm, deletingItem, loading }) => {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xl max-w-sm w-full p-6 text-center animate-[scaleIn_0.25s_ease]">
        <div className="w-12 h-12 rounded-full bg-red-50 text-red-500 flex items-center justify-center mx-auto mb-4">
          <FiAlertTriangle className="w-6 h-6" />
        </div>
        <h3 className="text-base font-extrabold text-slate-800 tracking-tight">
          Delete Record?
        </h3>
        <p className="text-xs text-slate-400 font-semibold mt-2 leading-relaxed">
          Are you sure you want to delete <span className="font-extrabold text-slate-700">"{deletingItem?.name}"</span>?
          This action is permanent and cannot be undone.
        </p>
        <div className="flex gap-3 justify-center mt-6">
          <button
            onClick={onClose}
            className="px-4 py-2.5 border border-slate-200 text-slate-500 hover:text-slate-800 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all"
          >
            No, Keep It
          </button>
          <button
            onClick={onConfirm}
            disabled={loading}
            className="px-4 py-2.5 bg-red-600 hover:bg-red-700 text-white text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow disabled:opacity-40"
          >
            {loading ? "Deleting..." : "Yes, Delete"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default DeleteConfirmModal;
