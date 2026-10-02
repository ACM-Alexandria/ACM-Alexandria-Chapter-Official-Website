import React, { useState, useEffect } from "react";
import { FiMail } from "react-icons/fi";

const BRAND = "#4B98C8";
const BRAND_DARK = "#205E85";

const OpenCallConfirmModal = ({ open, onClose, onConfirm, committee, loading }) => {
  const [sendAnnouncement, setSendAnnouncement] = useState(true);

  useEffect(() => {
    if (open) setSendAnnouncement(true);
  }, [open]);

  if (!open || !committee) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]">
      <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-xl dark:shadow-slate-950/60 max-w-sm w-full p-6 text-center animate-[scaleIn_0.25s_ease]">
        <div className="w-12 h-12 rounded-full bg-sky-50 text-[#4B98C8] flex items-center justify-center mx-auto mb-4">
          <FiMail className="w-6 h-6" />
        </div>
        <h3 className="text-base font-extrabold text-slate-800 dark:text-slate-100 tracking-tight">
          Open Call?
        </h3>
        <p className="text-xs text-slate-400 font-semibold mt-2 leading-relaxed">
          Open the call for <span className="font-extrabold text-slate-700 dark:text-slate-100">"{committee.name}"</span>.
        </p>

        <label className="flex items-center gap-3 p-3.5 mt-4 text-left bg-slate-50 dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-600 cursor-pointer">
          <input
            type="checkbox"
            checked={sendAnnouncement}
            onChange={(e) => setSendAnnouncement(e.target.checked)}
            className="w-4 h-4 accent-[#4B98C8]"
          />
          <span>
            <span className="block text-xs font-extrabold text-slate-700 dark:text-slate-200">Email subscribers</span>
            <span className="block text-[10px] text-slate-400 dark:text-slate-300 font-medium mt-0.5">
              Send the call message now (you can resend it later)
            </span>
          </span>
        </label>

        <div className="flex gap-3 justify-center mt-6">
          <button
            onClick={onClose}
            className="px-4 py-2.5 border border-slate-200 dark:border-slate-600 text-slate-500 dark:text-slate-300 hover:text-slate-800 dark:hover:text-slate-100 hover:bg-slate-50 dark:hover:bg-slate-800 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all"
          >
            Cancel
          </button>
          <button
            onClick={() => onConfirm(sendAnnouncement)}
            disabled={loading}
            className="px-4 py-2.5 text-white text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow disabled:opacity-40"
            style={{ background: `linear-gradient(135deg, ${BRAND}, ${BRAND_DARK})` }}
          >
            {loading ? "Opening..." : "Open Call"}
          </button>
        </div>
      </div>
    </div>
  );
};

export default OpenCallConfirmModal;
