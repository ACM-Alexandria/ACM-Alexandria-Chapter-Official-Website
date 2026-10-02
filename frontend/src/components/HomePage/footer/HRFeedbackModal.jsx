import React, { useState, useEffect } from "react";
import { FiX, FiLoader, FiCheckCircle, FiAlertCircle } from "react-icons/fi";
import hrFeedbackService from "../../../services/hrFeedbackService";

const BRAND = "#4B98C8";
const BRAND_DARK = "#205E85";

const HRFeedbackModal = ({ open, onClose }) => {
  const [animate, setAnimate] = useState(false);
  const [step, setStep] = useState(1); // 1 = form, 2 = success

  const [type, setType] = useState("SUGGESTION");
  const [content, setContent] = useState("");
  const [isAnonymous, setIsAnonymous] = useState(true);

  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (open) {
      setStep(1);
      setType("SUGGESTION");
      setContent("");
      setIsAnonymous(true);
      setError(null);
      const raf = requestAnimationFrame(() => setAnimate(true));
      return () => cancelAnimationFrame(raf);
    } else {
      setAnimate(false);
    }
  }, [open]);

  if (!open) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!content.trim()) {
      setError("Please write your suggestion or complaint before submitting.");
      return;
    }
    
    setError(null);
    setSubmitting(true);

    try {
      await hrFeedbackService.submitHRFeedback({ type, content, isAnonymous });
      setStep(2); // Success step
    } catch (err) {
      setError(err.message || "Failed to submit feedback. Please try again.");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div
      className={`fixed inset-0 z-[999] flex items-center justify-center p-4 bg-slate-950/40 backdrop-blur-sm transition-all duration-500 ${
        animate ? "opacity-100" : "opacity-0 pointer-events-none"
      }`}
      onClick={onClose}
      role="dialog"
      aria-modal="true"
    >
      <div
        onClick={(e) => e.stopPropagation()}
        className={`w-full max-w-2xl bg-white dark:bg-slate-900 rounded-3xl shadow-2xl border border-slate-100 dark:border-slate-700 p-8 transition-all duration-500 transform ease-[cubic-bezier(0.34,1.56,0.64,1)] flex flex-col max-h-[90vh] overflow-y-auto ${
          animate ? "scale-100 translate-y-0" : "scale-90 translate-y-8"
        }`}
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-slate-700 mb-6 shrink-0">
          <div>
            <h3 className="text-lg font-black text-slate-800 dark:text-slate-100 tracking-tight">
              HR Suggestions & Complaints
            </h3>
            <p className="text-xs text-slate-400 dark:text-slate-300 font-semibold mt-0.5">
              {step === 1 && "Help us improve. Your feedback is sent directly to the HR Board."}
              {step === 2 && "Submission successful"}
            </p>
          </div>
          {step !== 2 && (
            <button
              onClick={onClose}
              disabled={submitting}
              className="p-1.5 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400 dark:text-slate-300 hover:text-slate-600 dark:hover:text-slate-100 rounded-lg transition-colors disabled:opacity-40"
            >
              <FiX className="w-5 h-5" />
            </button>
          )}
        </div>

        {/* Error Alert */}
        {error && (
          <div className="mb-6 bg-rose-50 dark:bg-rose-950/40 border border-rose-200 dark:border-rose-800 text-rose-600 dark:text-rose-300 rounded-2xl p-4 flex items-center gap-3 shrink-0">
            <FiAlertCircle className="w-5 h-5 shrink-0" />
            <p className="text-xs font-bold">{error}</p>
          </div>
        )}

        {/* Step 1: Form */}
        {step === 1 && (
          <form onSubmit={handleSubmit} className="space-y-6 flex-1 animate-[fadeIn_0.3s_ease]">
            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2">
                Feedback Type
              </label>
              <div className="flex gap-4">
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="radio"
                    name="feedbackType"
                    value="SUGGESTION"
                    checked={type === "SUGGESTION"}
                    onChange={(e) => setType(e.target.value)}
                    className="text-blue-600 focus:ring-blue-500 w-4 h-4 cursor-pointer"
                  />
                  <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">Suggestion</span>
                </label>
                <label className="flex items-center gap-2 cursor-pointer">
                  <input
                    type="radio"
                    name="feedbackType"
                    value="COMPLAINT"
                    checked={type === "COMPLAINT"}
                    onChange={(e) => setType(e.target.value)}
                    className="text-rose-600 focus:ring-rose-500 w-4 h-4 cursor-pointer"
                  />
                  <span className="text-sm font-semibold text-slate-700 dark:text-slate-300">Complaint</span>
                </label>
              </div>
            </div>

            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2">
                Your Message
              </label>
              <textarea
                required
                rows="5"
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder={`Write your ${type.toLowerCase()} here...`}
                disabled={submitting}
                className="w-full px-4 py-3 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 text-slate-800 dark:text-slate-100 placeholder-slate-400 text-xs font-semibold rounded-2xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all disabled:opacity-50 resize-none"
              />
            </div>

            <div className="flex items-start gap-3 mt-4 bg-slate-50 dark:bg-slate-800/50 p-4 rounded-2xl border border-slate-200 dark:border-slate-700">
              <div className="flex items-center h-5 mt-0.5">
                <input
                  id="isAnonymous"
                  type="checkbox"
                  checked={isAnonymous}
                  onChange={(e) => setIsAnonymous(e.target.checked)}
                  className="w-4 h-4 text-[#4B98C8] bg-white border-slate-300 rounded focus:ring-[#4B98C8] dark:focus:ring-[#4B98C8] dark:ring-offset-slate-800 focus:ring-2 dark:bg-slate-700 dark:border-slate-600 cursor-pointer"
                />
              </div>
              <div className="text-sm">
                <label htmlFor="isAnonymous" className="text-xs font-bold text-slate-800 dark:text-slate-100 cursor-pointer uppercase tracking-wider">
                  Submit Anonymously
                </label>
                <p className="text-xs text-slate-500 dark:text-slate-400 mt-1 font-medium leading-relaxed">
                  If checked, your identity will be hidden. If unchecked, your name and email will be shared with the HR Board.
                </p>
              </div>
            </div>

            {/* Footer Buttons */}
            <div className="flex justify-end gap-3 items-center pt-4 border-t border-slate-100 dark:border-slate-700 shrink-0">
              <button
                type="button"
                onClick={onClose}
                disabled={submitting}
                className="px-5 py-2.5 border border-slate-200 dark:border-slate-600 text-slate-500 dark:text-slate-300 hover:text-slate-800 dark:hover:text-slate-100 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all disabled:opacity-40"
              >
                Cancel
              </button>

              <button
                type="submit"
                disabled={submitting}
                className="px-6 py-2.5 text-white text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow-md disabled:opacity-40 flex items-center gap-2 min-w-[120px] justify-center"
                style={{ background: `linear-gradient(135deg, ${BRAND}, ${BRAND_DARK})` }}
              >
                {submitting && <FiLoader className="w-3.5 h-3.5 animate-spin" />}
                {submitting ? "Submitting…" : "Submit"}
              </button>
            </div>
          </form>
        )}

        {/* Step 2: Success View */}
        {step === 2 && (
          <div className="text-center py-8 space-y-6 flex-grow flex flex-col justify-center items-center animate-[fadeIn_0.3s_ease]">
            <div className="w-16 h-16 rounded-full bg-emerald-50 dark:bg-emerald-950/40 text-emerald-500 dark:text-emerald-300 flex items-center justify-center animate-[successPop_0.6s_ease_both]">
              <FiCheckCircle className="w-10 h-10" />
            </div>

            <div className="space-y-2">
              <h4 className="text-lg font-black text-slate-900 dark:text-slate-100 tracking-tight">Thank You!</h4>
              <p className="text-xs text-slate-500 dark:text-slate-300 font-medium max-w-sm mx-auto leading-relaxed">
                Your feedback has been successfully submitted and stored. The HR Board will review it shortly.
              </p>
            </div>

            <button
              type="button"
              onClick={onClose}
              className="px-6 py-3 text-white text-xs font-bold uppercase tracking-wider rounded-2xl active:scale-95 transition-all shadow-md mt-4"
              style={{ background: `linear-gradient(135deg, ${BRAND}, ${BRAND_DARK})` }}
            >
              Dismiss
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default HRFeedbackModal;
