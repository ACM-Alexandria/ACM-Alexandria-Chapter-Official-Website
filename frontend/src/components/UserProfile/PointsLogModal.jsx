import React, { useEffect, useState } from "react";
import { HiX } from "react-icons/hi";
import useScrollLock from "../../hooks/useScrollLock";

// The presets and their default point values
const PRESET_REASONS = {
  "Event Attendance": 10,
  "Volunteering": 10,
  "Task Completed": 10,
  "Penalty": -10, // I made this negative logically, but it can be manually edited to anything!
};

// Mounted only while the modal is open, so each open starts from fresh state
const PointsLogForm = ({ onClose, onSubmit, editingLog, memberName }) => {
  const [reason, setReason] = useState(editingLog ? editingLog.reason : "");
  const [points, setPoints] = useState(editingLog ? editingLog.points : 10);
  const [isSubmitting, setIsSubmitting] = useState(false);
  useScrollLock(true);

  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === "Escape" && !isSubmitting) onClose();
    };
    document.addEventListener("keydown", handleKeyDown);
    return () => document.removeEventListener("keydown", handleKeyDown);
  }, [onClose, isSubmitting]);

  // Clicking a preset fills the text box (still editable) and its default points
  const applyPreset = (preset) => {
    setReason(preset);
    setPoints(PRESET_REASONS[preset]);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);

    // Send data back to the parent page
    await onSubmit({
      reason: reason.trim(),
      points: Number(points)
    });

    setIsSubmitting(false);
  };

  const fieldClass =
    "w-full h-10 px-3 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-md outline-none text-slate-900 dark:text-slate-100 text-sm placeholder:text-slate-500 transition-colors focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/25";

  const negative = Number(points) < 0;

  return (
    <div
      data-modal-overlay
      className="fixed inset-0 z-[70] flex items-center justify-center p-4 bg-slate-900/50 animate-[fadeIn_0.2s_ease-out] motion-reduce:animate-none"
      onMouseDown={(e) => { if (e.target === e.currentTarget && !isSubmitting) onClose(); }}
    >
      <div
        role="dialog"
        aria-modal="true"
        aria-labelledby="points-dialog-title"
        className="w-full max-w-lg rounded-xl border border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-900 shadow-xl overflow-hidden animate-[scaleIn_0.2s_ease-out] motion-reduce:animate-none"
      >
        {/* Header */}
        <div className="px-5 py-4 flex items-start justify-between gap-4 border-b border-slate-200 dark:border-slate-800">
          <div className="min-w-0">
            <h3 id="points-dialog-title" className="text-base font-semibold text-slate-900 dark:text-slate-100">
              {editingLog ? "Edit points" : "Add points"}
            </h3>
            {memberName && (
              <p className="mt-0.5 text-sm text-slate-600 dark:text-slate-400 truncate">For {memberName}</p>
            )}
          </div>
          <button
            type="button"
            onClick={onClose}
            aria-label="Close"
            title="Close"
            className="-mr-1.5 -mt-1 w-8 h-8 shrink-0 inline-flex items-center justify-center rounded-md text-slate-500 hover:text-slate-900 dark:hover:text-slate-100 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/40"
          >
            <HiX className="w-5 h-5" aria-hidden="true" />
          </button>
        </div>

        <form onSubmit={handleSubmit}>
          <div className="px-5 py-5 flex flex-col gap-5">
            {/* Reason: free text, with presets that fill it in */}
            <div className="flex flex-col gap-1.5">
              <label htmlFor="points-reason" className="text-sm font-medium text-slate-700 dark:text-slate-300">
                Reason
              </label>
              <input
                id="points-reason"
                type="text"
                required
                autoFocus
                value={reason}
                onChange={(e) => setReason(e.target.value)}
                placeholder="e.g. Won a hackathon"
                className={fieldClass}
              />
              <div className="mt-1 flex flex-wrap items-center gap-2" role="group" aria-label="Quick fill reasons">
                {Object.keys(PRESET_REASONS).map((preset) => (
                  <button
                    key={preset}
                    type="button"
                    onClick={() => applyPreset(preset)}
                    className="h-7 px-2.5 inline-flex items-center rounded-full border border-[#4B98C8]/30 bg-[#4B98C8]/10 text-xs font-medium text-[#2C6F99] dark:text-[#7DBBE0] hover:bg-[#4B98C8]/20 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/50"
                  >
                    {preset}
                  </button>
                ))}
              </div>
            </div>

            {/* Points */}
            <div className="flex flex-col gap-1.5">
              <label htmlFor="points-value" className="text-sm font-medium text-slate-700 dark:text-slate-300">
                Points
              </label>
              <input
                id="points-value"
                type="number"
                required
                value={points}
                onChange={(e) => setPoints(e.target.value)}
                className={`${fieldClass} font-semibold tabular-nums ${negative ? "text-rose-700 dark:text-rose-400" : ""}`}
              />
              <p className="text-xs text-slate-600 dark:text-slate-400">Use a negative number to take points away.</p>
            </div>
          </div>

          {/* Actions */}
          <div className="px-5 py-4 flex flex-col-reverse sm:flex-row sm:justify-end gap-2 border-t border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900/60">
            <button
              type="button"
              onClick={onClose}
              disabled={isSubmitting}
              className="h-9 px-4 rounded-md text-sm font-medium text-slate-700 dark:text-slate-200 bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors disabled:opacity-50"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="h-9 px-4 rounded-md text-sm font-medium text-white bg-[#4B98C8] hover:bg-[#3d7da6] transition-colors inline-flex items-center justify-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-[#4B98C8] dark:focus-visible:ring-offset-slate-900"
            >
              {isSubmitting && (
                <span className="w-4 h-4 rounded-full border-2 border-white/30 border-t-white animate-spin motion-reduce:animate-none" aria-hidden="true" />
              )}
              {isSubmitting ? "Saving…" : "Save points"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

const PointsLogModal = ({ isOpen, onClose, onSubmit, editingLog, memberName }) => {
  if (!isOpen) return null;
  return (
    <PointsLogForm
      key={editingLog ? editingLog.id : "new"}
      onClose={onClose}
      onSubmit={onSubmit}
      editingLog={editingLog}
      memberName={memberName}
    />
  );
};

export default PointsLogModal;