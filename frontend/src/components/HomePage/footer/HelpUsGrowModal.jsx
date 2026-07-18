import React, { useState, useEffect } from "react";
import {
  FiX,
  FiUploadCloud,
  FiTrash2,
  FiLoader,
  FiCheckCircle,
  FiAlertCircle,
  FiZap,
  FiAlertTriangle,
  FiImage,
} from "react-icons/fi";
import { submitFeature, submitBug, uploadScreenshot } from "../../../services/feedbackService";

const BRAND = "#4B98C8";
const BRAND_DARK = "#205E85";

const HelpUsGrowModal = ({ open, onClose }) => {
  const [animate, setAnimate] = useState(false);
  const [step, setStep] = useState(1); // 1 = choice, 2 = feature form, 3 = bug form, 4 = success
  const [choice, setChoice] = useState(null); // 'feature' | 'bug'

  // Form Fields
  const [name, setName] = useState("");
  const [description, setDescription] = useState("");
  const [attachedImages, setAttachedImages] = useState([]);

  // Loading & Upload states
  const [submitting, setSubmitting] = useState(false);
  const [batchProgress, setBatchProgress] = useState(null);
  const [error, setError] = useState(null);

  const isUploading = batchProgress !== null && batchProgress.done < batchProgress.total;

  useEffect(() => {
    if (open) {
      setStep(1);
      setChoice(null);
      setName("");
      setDescription("");
      setAttachedImages([]);
      setBatchProgress(null);
      setError(null);
      const raf = requestAnimationFrame(() => setAnimate(true));
      return () => cancelAnimationFrame(raf);
    } else {
      setAnimate(false);
    }
  }, [open]);

  if (!open) return null;

  const handleFiles = async (files) => {
    setError(null);
    const toProcess = Array.from(files);

    // Validate size limit (5MB) and type
    for (const f of toProcess) {
      const sizeInMB = (f.size / (1024 * 1024)).toFixed(2);
      if (f.size > 5 * 1024 * 1024) {
        setError(`File "${f.name}" exceeds the maximum allowed size of 5 MB (actual size: ${sizeInMB} MB). Please choose a smaller image.`);
        return;
      }
      if (!f.type.startsWith("image/")) {
        setError(`"${f.name}" is not an image file.`);
        return;
      }
    }

    const total = toProcess.length;
    setBatchProgress({ total, done: 0, percent: 0, failed: 0 });

    const uploadedUrls = [];
    for (let i = 0; i < total; i++) {
      try {
        const data = await uploadScreenshot(toProcess[i]);
        if (data?.url) {
          uploadedUrls.push(data.url);
        }
        setBatchProgress({
          total,
          done: i + 1,
          percent: Math.round(((i + 1) / total) * 100),
          failed: 0,
        });
      } catch (err) {
        setBatchProgress((prev) => ({
          ...prev,
          done: i + 1,
          failed: (prev.failed ?? 0) + 1,
          percent: Math.round(((i + 1) / total) * 100),
        }));
        setError(err.message || `Failed to upload file ${i + 1}.`);
      }
    }

    if (uploadedUrls.length > 0) {
      setAttachedImages((prev) => [...prev, ...uploadedUrls]);
    }
  };

  const handleFileChange = (e) => handleFiles(e.target.files);
  const handleDragOver = (e) => e.preventDefault();
  const handleDrop = (e) => {
    e.preventDefault();
    handleFiles(e.dataTransfer.files);
  };

  const handleRemoveImage = (idx) => {
    setAttachedImages((prev) => prev.filter((_, i) => i !== idx));
  };

  const handleChoiceSelect = (selected) => {
    setChoice(selected);
    setStep(selected === "feature" ? 2 : 3);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!name.trim()) {
      setError("Please fill in a name/title.");
      return;
    }
    if (!description.trim()) {
      setError("Please provide a description.");
      return;
    }

    setSubmitting(true);
    setError(null);

    try {
      if (choice === "feature") {
        await submitFeature({ name, description });
      } else {
        await submitBug({ name, description, imageUrls: attachedImages });
      }
      setStep(4); // Success step
    } catch (err) {
      setError(err.message || "Failed to submit. Please try again.");
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
        className={`w-full max-w-2xl bg-white rounded-3xl shadow-2xl border border-slate-100 p-8 transition-all duration-500 transform ease-[cubic-bezier(0.34,1.56,0.64,1)] flex flex-col max-h-[90vh] overflow-y-auto ${
          animate ? "scale-100 translate-y-0" : "scale-90 translate-y-8"
        }`}
      >
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 mb-6 shrink-0">
          <div>
            <h3 className="text-lg font-black text-slate-800 tracking-tight">Help Us Grow</h3>
            <p className="text-xs text-slate-400 font-semibold mt-0.5">
              {step === 1 && "Select feedback type"}
              {step === 2 && "Suggest a new feature"}
              {step === 3 && "Report a bug or website issue"}
              {step === 4 && "Submission successful"}
            </p>
          </div>
          {step !== 4 && (
            <button
              onClick={onClose}
              disabled={isUploading || submitting}
              className="p-1.5 hover:bg-slate-100 text-slate-400 hover:text-slate-600 rounded-lg transition-colors disabled:opacity-40"
            >
              <FiX className="w-5 h-5" />
            </button>
          )}
        </div>

        {/* Error Alert */}
        {error && (
          <div className="mb-6 bg-rose-50 border border-rose-200 text-rose-600 rounded-2xl p-4 flex items-center gap-3 shrink-0">
            <FiAlertCircle className="w-5 h-5 shrink-0" />
            <p className="text-xs font-bold">{error}</p>
          </div>
        )}

        {/* Step 1: Selection Choice */}
        {step === 1 && (
          <div className="flex flex-col gap-4 my-4 w-full">
            <button
              type="button"
              onClick={() => handleChoiceSelect("feature")}
              className="w-full flex flex-col items-start justify-center p-6 bg-emerald-50/20 border-2 border-emerald-100 hover:border-emerald-400/70 hover:bg-emerald-50/50 rounded-2xl cursor-pointer text-left group transition-all duration-300 shadow-sm animate-[fadeIn_0.35s_ease]"
            >
              <h4 className="text-sm font-bold text-slate-800 tracking-wide uppercase">Suggest a Feature</h4>
              <p className="text-[11px] text-slate-400 mt-2 font-medium leading-relaxed">
                Tell us about new tools, sections, or improvements you would love to see.
              </p>
            </button>

            <button
              type="button"
              onClick={() => handleChoiceSelect("bug")}
              className="w-full flex flex-col items-start justify-center p-6 bg-rose-50/20 border-2 border-rose-100 hover:border-rose-400/70 hover:bg-rose-50/50 rounded-2xl cursor-pointer text-left group transition-all duration-300 shadow-sm animate-[fadeIn_0.35s_ease]"
            >
              <h4 className="text-sm font-bold text-slate-800 tracking-wide uppercase">Report a Bug</h4>
              <p className="text-[11px] text-slate-400 mt-2 font-medium leading-relaxed">
                Found something broken? Share details and screenshots so we can patch it up.
              </p>
            </button>
          </div>
        )}

        {/* Step 2 & 3: Forms */}
        {(step === 2 || step === 3) && (
          <form onSubmit={handleSubmit} className="space-y-6 flex-1 animate-[fadeIn_0.3s_ease]">
            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2">
                {choice === "feature" ? "Feature Title" : "Bug Name"}
              </label>
              <input
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                placeholder={
                  choice === "feature"
                    ? "e.g., Member certificate generator"
                    : "e.g., Sidebar overlapping on mobile viewports"
                }
                disabled={isUploading || submitting}
                className="w-full px-4 py-3 bg-slate-50 border border-slate-200 text-slate-800 placeholder-slate-400 text-xs font-semibold rounded-2xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all disabled:opacity-50"
              />
            </div>

            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2">
                {choice === "feature" ? "Description" : "Description & Ways to Reproduce"}
              </label>
              <textarea
                required
                rows="4"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder={
                  choice === "feature"
                    ? "Describe the feature, how it should work, and why it is useful."
                    : "Describe the issue, what actions lead to it, and what the expected behavior was."
                }
                disabled={isUploading || submitting}
                className="w-full px-4 py-3 bg-slate-50 border border-slate-200 text-slate-800 placeholder-slate-400 text-xs font-semibold rounded-2xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all disabled:opacity-50 resize-none"
              />
            </div>

            {/* Bug Report - Screenshot Uploads */}
            {choice === "bug" && (
              <div className="space-y-4">
                {/* Uploaded image previews */}
                {attachedImages.length > 0 && (
                  <div>
                    <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2">
                      Screenshots ({attachedImages.length})
                    </label>
                    <div className="grid grid-cols-3 sm:grid-cols-4 gap-3">
                      {attachedImages.map((url, idx) => (
                        <div
                          key={idx}
                          className="group relative aspect-video rounded-xl overflow-hidden border border-slate-200 shadow-sm bg-slate-50"
                        >
                          <img
                            src={url}
                            alt={`Bug Screenshot ${idx + 1}`}
                            className="w-full h-full object-cover"
                          />
                          <button
                            type="button"
                            onClick={() => handleRemoveImage(idx)}
                            disabled={isUploading || submitting}
                            className="absolute top-1 right-1 w-6 h-6 flex items-center justify-center bg-red-600/90 hover:bg-red-700 text-white rounded-md shadow transition-all active:scale-90 disabled:opacity-40"
                            title="Delete image"
                          >
                            <FiTrash2 className="w-3 h-3" />
                          </button>
                        </div>
                      ))}
                    </div>
                  </div>
                )}

                {/* Batch Progress Bar */}
                {batchProgress !== null && (
                  <div className="bg-slate-50 border border-slate-100 rounded-2xl px-4 py-3">
                    <div className="flex items-center justify-between mb-2 gap-2">
                      <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">
                        {isUploading
                          ? `Uploading… ${batchProgress.done}/${batchProgress.total}`
                          : batchProgress.failed > 0
                          ? `Done — ${batchProgress.failed} failed`
                          : `All ${batchProgress.total} uploaded ✓`}
                      </span>
                      <span
                        className={`text-[11px] font-extrabold shrink-0 ${
                          isUploading
                            ? "text-[#4B98C8]"
                            : batchProgress.failed > 0
                            ? "text-red-500"
                            : "text-emerald-600"
                        }`}
                      >
                        {batchProgress.percent}%
                      </span>
                    </div>
                    <div className="w-full h-2 bg-slate-200 rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all duration-300 ${
                          isUploading
                            ? "bg-[#4B98C8]"
                            : batchProgress.failed > 0
                            ? "bg-red-400"
                            : "bg-emerald-500"
                        }`}
                        style={{ width: `${batchProgress.percent}%` }}
                      />
                    </div>
                  </div>
                )}

                {/* Dropzone */}
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2">
                    Attach Screenshots (Optional)
                  </label>
                  <div
                    onDragOver={handleDragOver}
                    onDrop={handleDrop}
                    className={`relative border-2 border-dashed rounded-2xl p-6 text-center flex flex-col items-center justify-center cursor-pointer transition-all ${
                      isUploading
                        ? "border-[#4B98C8]/40 bg-[#4B98C8]/5 cursor-not-allowed"
                        : "border-slate-200 hover:border-[#4B98C8]/50 hover:bg-slate-50"
                    }`}
                  >
                    <input
                      type="file"
                      multiple
                      accept="image/*"
                      onChange={handleFileChange}
                      disabled={isUploading || submitting}
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer disabled:cursor-not-allowed"
                    />
                    {isUploading ? (
                      <>
                        <FiLoader className="w-8 h-8 text-[#4B98C8] animate-spin mb-2" />
                        <p className="text-[11px] font-bold text-slate-600">Uploading to Cloudinary…</p>
                        <p className="text-[9px] text-slate-400 mt-1">Please wait.</p>
                      </>
                    ) : (
                      <>
                        <FiUploadCloud className="w-8 h-8 text-slate-400 mb-2" />
                        <p className="text-[11px] font-bold text-slate-600">Click or drag & drop to upload</p>
                        <p className="text-[9px] text-slate-400 mt-1">PNG, JPG, JPEG — up to 5 MB each</p>
                      </>
                    )}
                  </div>
                </div>
              </div>
            )}

            {/* Footer Buttons */}
            <div className="flex justify-between items-center pt-4 border-t border-slate-100 shrink-0">
              <button
                type="button"
                onClick={() => setStep(1)}
                disabled={isUploading || submitting}
                className="px-5 py-2.5 border border-slate-200 text-slate-500 hover:text-slate-800 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all disabled:opacity-40"
              >
                Back
              </button>

              <button
                type="submit"
                disabled={isUploading || submitting}
                className="px-6 py-2.5 text-white text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow-md disabled:opacity-40 flex items-center gap-2"
                style={{ background: `linear-gradient(135deg, ${BRAND}, ${BRAND_DARK})` }}
              >
                {submitting && <FiLoader className="w-3.5 h-3.5 animate-spin" />}
                {submitting ? "Submitting…" : "Submit"}
              </button>
            </div>
          </form>
        )}

        {/* Step 4: Success View */}
        {step === 4 && (
          <div className="text-center py-8 space-y-6 flex-grow flex flex-col justify-center items-center animate-[fadeIn_0.3s_ease]">
            <div className="w-16 h-16 rounded-full bg-emerald-50 text-emerald-500 flex items-center justify-center animate-[successPop_0.6s_ease_both]">
              <FiCheckCircle className="w-10 h-10" />
            </div>

            <div className="space-y-2">
              <h4 className="text-lg font-black text-slate-900 tracking-tight">Thank You!</h4>
              <p className="text-xs text-slate-500 font-medium max-w-sm mx-auto leading-relaxed">
                Your feedback has been successfully submitted and stored. Our dev team will look into it
                to make our chapter platform even better!
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

export default HelpUsGrowModal;
