import React from "react";
import { FiX } from "react-icons/fi";

const BRAND = "#4B98C8";
const BRAND_DARK = "#205E85";

const CallMessageModal = ({
  open,
  onClose,
  onSubmit,
  selectedCommittee,
  messageSubject,
  setMessageSubject,
  messageBody,
  setMessageBody,
  loading,
}) => {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xl max-w-lg w-full p-6 animate-[scaleIn_0.25s_ease]">
        {/* Modal Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 mb-6">
          <div>
            <h3 className="text-base font-extrabold text-slate-800 tracking-tight">
              Edit Call Email Message
            </h3>
            <p className="text-[10px] text-slate-400 font-medium mt-0.5">
              Set the email template sent automatically to active subscribers when opening calls for {selectedCommittee?.name}.
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 hover:bg-slate-100 text-slate-400 hover:text-slate-600 rounded-lg transition-colors"
          >
            <FiX className="w-4.5 h-4.5" />
          </button>
        </div>

        {/* Modal Form */}
        <form onSubmit={onSubmit} className="space-y-4">
          <div>
            <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
              Email Subject
            </label>
            <input
              type="text"
              required
              value={messageSubject}
              onChange={(e) => setMessageSubject(e.target.value)}
              placeholder="e.g. Call for Members is Open!"
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
            />
          </div>

          <div>
            <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
              Email Body
            </label>
            <textarea
              required
              value={messageBody}
              onChange={(e) => setMessageBody(e.target.value)}
              placeholder="Write the email content here..."
              rows="6"
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all resize-none"
            />
          </div>

          {/* Actions row */}
          <div className="flex justify-end gap-3 pt-4 border-t border-slate-100 mt-6">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 border border-slate-200 text-slate-500 hover:text-slate-800 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-5 py-2.5 text-white text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow disabled:opacity-40"
              style={{ background: `linear-gradient(135deg, ${BRAND}, ${BRAND_DARK})` }}
            >
              {loading ? "Saving..." : "Save Message"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default CallMessageModal;
