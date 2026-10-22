import React, { useState } from "react";
import { HiOutlineX } from "react-icons/hi";

// The presets and their default point values
const PRESET_REASONS = {
  "Event Attendance": 10,
  "Volunteering": 10,
  "Task Completed": 10,
  "Penalty": -10, // I made this negative logically, but it can be manually edited to anything!
};

// Mounted only while the modal is open, so each open starts from fresh state
const PointsLogForm = ({ onClose, onSubmit, editingLog }) => {
  const [reason, setReason] = useState(editingLog ? editingLog.reason : "");
  const [points, setPoints] = useState(editingLog ? editingLog.points : 10);
  const [isSubmitting, setIsSubmitting] = useState(false);

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

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/50 backdrop-blur-sm animate-[fadeIn_0.2s_ease-out]">
      <div 
        className="bg-white dark:bg-slate-900 rounded-3xl shadow-2xl w-full max-w-md border border-slate-100 dark:border-slate-800 overflow-hidden"
        style={{ animation: "fadeInUp 0.3s cubic-bezier(0.16, 1, 0.3, 1)" }}
      >
        {/* Header */}
        <div className="px-6 py-4 border-b border-slate-100 dark:border-slate-800 flex justify-between items-center">
          <h3 className="text-xl font-bold text-slate-900 dark:text-white">
            {editingLog ? "Edit Points Log" : "Add Points"}
          </h3>
          <button 
            onClick={onClose}
            className="p-2 rounded-xl text-slate-400 hover:text-slate-600 hover:bg-slate-100 dark:hover:bg-slate-800 transition-colors"
          >
            <HiOutlineX className="w-5 h-5" />
          </button>
        </div>

        {/* Form */}
        <form onSubmit={handleSubmit} className="p-6 space-y-5">
          {/* Reason: free text, with presets that fill it in */}
          <div className="space-y-2">
            <label htmlFor="points-reason" className="text-sm font-bold text-slate-700 dark:text-slate-300">
              Reason for Points
            </label>
            <input
              id="points-reason"
              type="text"
              required
              autoFocus
              value={reason}
              onChange={(e) => setReason(e.target.value)}
              placeholder="e.g., Won a hackathon"
              className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white placeholder:text-slate-500 focus:ring-2 focus:ring-[#4B98C8] focus:border-transparent outline-none transition-all"
            />
            <div className="flex flex-wrap items-center gap-2 pt-1">
              <span className="text-xs text-slate-600 dark:text-slate-400">Quick fill:</span>
              {Object.keys(PRESET_REASONS).map((preset) => (
                <button
                  key={preset}
                  type="button"
                  onClick={() => applyPreset(preset)}
                  className="px-3 py-1 rounded-full border border-[#4B98C8]/30 bg-[#4B98C8]/10 text-xs font-semibold text-[#2C6F99] dark:text-[#7DBBE0] hover:bg-[#4B98C8]/20 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/50"
                >
                  {preset}
                </button>
              ))}
            </div>
          </div>

          {/* Points Input */}
          <div className="space-y-2">
            <label className="text-sm font-bold text-slate-700 dark:text-slate-300">
              Points Value <span className="text-slate-400 font-normal">(Editable)</span>
            </label>
            <input
              type="number"
              required
              value={points}
              onChange={(e) => setPoints(e.target.value)}
              className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-[#4B98C8] outline-none font-bold text-lg"
            />
          </div>

          {/* Actions */}
          <div className="pt-4 flex gap-3">
            <button
              type="button"
              onClick={onClose}
              className="flex-1 px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 text-slate-700 dark:text-slate-300 font-bold hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 px-4 py-3 rounded-xl bg-[#4B98C8] hover:bg-[#3A7CA5] text-white font-bold transition-transform active:scale-95 disabled:opacity-70 disabled:active:scale-100 flex justify-center items-center"
            >
              {isSubmitting ? "Saving..." : "Save Points"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

const PointsLogModal = ({ isOpen, onClose, onSubmit, editingLog }) => {
  if (!isOpen) return null;
  return (
    <PointsLogForm
      key={editingLog ? editingLog.id : "new"}
      onClose={onClose}
      onSubmit={onSubmit}
      editingLog={editingLog}
    />
  );
};

export default PointsLogModal;