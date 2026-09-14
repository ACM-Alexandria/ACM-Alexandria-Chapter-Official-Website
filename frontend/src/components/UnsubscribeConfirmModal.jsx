import React from "react";
import { createPortal } from "react-dom";
import { FiBellOff, FiBell } from "react-icons/fi";

const UnsubscribeConfirmModal = ({ open, onClose, onConfirm, topicName, loading, isSubscribe = false }) => {
  if (!open) return null;

  return createPortal(
    <div
      onClick={onClose}
      className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]"
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className="bg-white dark:bg-slate-800 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-xl max-w-sm w-full p-6 text-center animate-[scaleIn_0.25s_ease]"
      >
        <div className={`w-12 h-12 rounded-full flex items-center justify-center mx-auto mb-4 ${
          isSubscribe ? "bg-green-50 dark:bg-green-950/40 text-green-600 dark:text-green-400" : "bg-red-50 dark:bg-red-950/40 text-red-500 dark:text-red-400"
        }`}>
          {isSubscribe ? <FiBell className="w-6 h-6" /> : <FiBellOff className="w-6 h-6" />}
        </div>
        <h3 className="text-base font-extrabold text-slate-800 dark:text-slate-100 tracking-tight">
          {isSubscribe ? "Subscribe?" : "Unsubscribe?"}
        </h3>
        <p className="text-xs text-slate-400 dark:text-slate-300 font-semibold mt-2 leading-relaxed">
          {isSubscribe ? (
            <>
              Are you sure you want to receive notification when <span className="font-extrabold text-slate-700 dark:text-slate-200">"{topicName}"</span> call is open?
            </>
          ) : (
            <>
              Are you sure you want to stop receiving notifications from <span className="font-extrabold text-slate-700 dark:text-slate-200">"{topicName}"</span>?
            </>
          )}
        </p>
        <div className="flex gap-3 justify-center mt-6">
          <button
            onClick={onClose}
            className="px-4 py-2.5 border border-slate-200 dark:border-slate-600 text-slate-500 dark:text-slate-300 hover:text-slate-800 dark:hover:text-slate-100 hover:bg-slate-50 dark:hover:bg-slate-700 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all cursor-pointer"
          >
            Cancel
          </button>
          <button
            onClick={onConfirm}
            disabled={loading}
            className={`px-4 py-2.5 text-white text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow disabled:opacity-40 cursor-pointer ${
              isSubscribe ? "bg-green-600 hover:bg-green-700" : "bg-red-600 hover:bg-red-700"
            }`}
          >
            {loading ? (isSubscribe ? "Subscribing..." : "Unsubscribing...") : (isSubscribe ? "Yes, Subscribe" : "Yes, Unsubscribe")}
          </button>
        </div>
      </div>
    </div>,
    document.body
  );
};

export default UnsubscribeConfirmModal;
