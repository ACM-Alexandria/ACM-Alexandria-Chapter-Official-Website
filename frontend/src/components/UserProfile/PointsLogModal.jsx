import React, { useState, useEffect } from "react";
import { HiOutlineX } from "react-icons/hi";

// The presets and their default point values
const PRESET_REASONS = {
  "Event Attendance": 10,
  "Volunteering": 10,
  "Task Completed": 10,
  "Penalty": -10, // I made this negative logically, but it can be manually edited to anything!
};

const PointsLogModal = ({ isOpen, onClose, onSubmit, editingLog }) => {
  const [reason, setReason] = useState("Event Attendance");
  const [customReason, setCustomReason] = useState("");
  const [points, setPoints] = useState(10);
  const [isSubmitting, setIsSubmitting] = useState(false);

  // When the modal opens or the log being edited changes, reset the form
  useEffect(() => {
    if (isOpen) {
      if (editingLog) {
        // If editing, figure out if the reason is a preset or a custom one
        const isPreset = Object.keys(PRESET_REASONS).includes(editingLog.reason);
        if (isPreset) {
          setReason(editingLog.reason);
          setCustomReason("");
        } else {
          setReason("Custom");
          setCustomReason(editingLog.reason);
        }
        setPoints(editingLog.points);
      } else {
        // If adding new, reset to defaults
        setReason("Event Attendance");
        setCustomReason("");
        setPoints(10);
      }
      setIsSubmitting(false);
    }
  }, [isOpen, editingLog]);

  const handleReasonChange = (e) => {
    const selectedReason = e.target.value;
    setReason(selectedReason);
    
    // Automatically update the points input to the default value for the selected preset
    if (selectedReason !== "Custom") {
      setPoints(PRESET_REASONS[selectedReason]);
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsSubmitting(true);
    
    const finalReason = reason === "Custom" ? customReason : reason;
    
    // Send data back to the parent page
    await onSubmit({ 
      reason: finalReason, 
      points: Number(points) 
    });
    
    setIsSubmitting(false);
  };

  if (!isOpen) return null;

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
          {/* Reason Selection */}
          <div className="space-y-2">
            <label className="text-sm font-bold text-slate-700 dark:text-slate-300">
              Reason for Points
            </label>
            <select
              value={reason}
              onChange={handleReasonChange}
              className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-[#4B98C8] focus:border-transparent outline-none transition-all"
            >
              {Object.keys(PRESET_REASONS).map((preset) => (
                <option key={preset} value={preset}>{preset}</option>
              ))}
              <option value="Custom">Other (Custom Reason)</option>
            </select>
          </div>

          {/* Custom Reason Input (Only shows if "Custom" is selected) */}
          {reason === "Custom" && (
            <div className="space-y-2 animate-[fadeIn_0.2s_ease-out]">
              <label className="text-sm font-bold text-slate-700 dark:text-slate-300">
                Specify Custom Reason
              </label>
              <input
                type="text"
                required
                value={customReason}
                onChange={(e) => setCustomReason(e.target.value)}
                placeholder="e.g., Won a hackathon"
                className="w-full px-4 py-3 rounded-xl border border-slate-200 dark:border-slate-700 bg-slate-50 dark:bg-slate-800 text-slate-900 dark:text-white focus:ring-2 focus:ring-[#4B98C8] outline-none"
              />
            </div>
          )}

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

export default PointsLogModal;