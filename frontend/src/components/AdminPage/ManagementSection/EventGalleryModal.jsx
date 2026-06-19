import React, { useState, useEffect, useCallback } from "react";
import {
  FiX,
  FiUploadCloud,
  FiTrash2,
  FiLoader,
  FiImage,
  FiChevronLeft,
  FiChevronRight,
  FiMaximize2,
} from "react-icons/fi";
import api from "../../../services/api";
import adminService from "../../../services/adminService";
import { fetchEventById } from "../../../services/homePageService";

const BRAND = "#4B98C8";
const BRAND_DARK = "#205E85";

/* ── upload one file; resolves with server response data ── */
const uploadFile = (file) => {
  const formData = new FormData();
  formData.append("file", file);
  return api
    .post("/api/images/upload", formData, {
      headers: { "Content-Type": "multipart/form-data" },
    })
    .then((res) => res.data)
    .catch((err) => {
      throw new Error(err.response?.data?.message || err.message || "Upload failed");
    });
};

/* ── Lightbox (used inside EventGalleryModal) ── */
const AdminLightbox = ({ images, startIndex, onClose }) => {
  const [current, setCurrent] = useState(startIndex);

  const prev = useCallback(() => setCurrent((c) => (c - 1 + images.length) % images.length), [images.length]);
  const next = useCallback(() => setCurrent((c) => (c + 1) % images.length), [images.length]);

  useEffect(() => {
    const handler = (e) => {
      if (e.key === "ArrowLeft") prev();
      if (e.key === "ArrowRight") next();
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, [prev, next, onClose]);

  return (
    <div className="fixed inset-0 z-[200] flex items-center justify-center bg-slate-950/90 backdrop-blur-md animate-[fadeIn_0.2s_ease]">
      {/* Close */}
      <button
        onClick={onClose}
        className="absolute right-5 top-5 z-[210] w-11 h-11 flex items-center justify-center rounded-2xl bg-slate-800/90 text-white border border-slate-700/50 shadow-xl hover:bg-slate-800 hover:scale-105 active:scale-95 transition-all"
        aria-label="Close"
      >
        <FiX className="w-5 h-5" />
      </button>

      {/* Counter */}
      <div className="absolute top-5 left-1/2 -translate-x-1/2 z-[210] px-3 py-1 rounded-full bg-slate-800/70 text-white text-[11px] font-bold tracking-wider">
        {current + 1} / {images.length}
      </div>

      {/* Prev */}
      {images.length > 1 && (
        <button
          onClick={prev}
          className="absolute left-4 z-[210] w-12 h-12 flex items-center justify-center rounded-2xl bg-slate-800/80 text-white border border-slate-700/40 shadow-xl hover:bg-slate-700 hover:scale-105 active:scale-95 transition-all"
          aria-label="Previous"
        >
          <FiChevronLeft className="w-6 h-6" />
        </button>
      )}

      {/* Image */}
      <img
        key={current}
        src={images[current]}
        alt={`Gallery ${current + 1}`}
        className="max-w-[90vw] max-h-[85vh] rounded-2xl object-contain shadow-2xl animate-[scaleIn_0.2s_ease]"
      />

      {/* Next */}
      {images.length > 1 && (
        <button
          onClick={next}
          className="absolute right-4 z-[210] w-12 h-12 flex items-center justify-center rounded-2xl bg-slate-800/80 text-white border border-slate-700/40 shadow-xl hover:bg-slate-700 hover:scale-105 active:scale-95 transition-all"
          aria-label="Next"
        >
          <FiChevronRight className="w-6 h-6" />
        </button>
      )}
    </div>
  );
};

/* ── Main Modal ── */
const EventGalleryModal = ({ open, onClose, event, onSave }) => {
  if (!open || !event) return null;

  const [attachedImages, setAttachedImages] = useState([]);
  const [loadingEvent, setLoadingEvent] = useState(false);
  // Single batch progress: null = no upload running/done yet
  // { total, done, percent, failed } — resets on every new batch
  const [batchProgress, setBatchProgress] = useState(null);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [lightboxIndex, setLightboxIndex] = useState(null);

  const isUploading = batchProgress !== null && batchProgress.done < batchProgress.total;

  useEffect(() => {
    if (open && event?.id) {
      setBatchProgress(null);
      setError(null);
      setLightboxIndex(null);
      const load = async () => {
        setLoadingEvent(true);
        try {
          const full = await fetchEventById(event.id);
          setAttachedImages(full.attachedImages || []);
        } catch {
          setError("Failed to load current gallery images.");
        } finally {
          setLoadingEvent(false);
        }
      };
      load();
    }
  }, [open, event]);

  const handleFiles = async (files) => {
    setError(null);
    const toProcess = Array.from(files);

    // Validate sizes first
    for (const f of toProcess) {
      if (f.size > 10 * 1024 * 1024) {
        setError(`"${f.name}" exceeds the 10 MB limit.`);
        return;
      }
    }

    const total = toProcess.length;
    // Reset (replace) the single batch progress bar
    setBatchProgress({ total, done: 0, percent: 0, failed: 0 });

    // Upload sequentially — bar advances only after server ACK
    const uploadedUrls = [];
    for (let i = 0; i < total; i++) {
      try {
        const data = await uploadFile(toProcess[i]);
        if (data?.url) uploadedUrls.push(data.url);
        // ACK received — advance to next step
        setBatchProgress({ total, done: i + 1, percent: Math.round(((i + 1) / total) * 100), failed: 0 });
      } catch (err) {
        setBatchProgress((prev) => ({ ...prev, done: i + 1, failed: (prev.failed ?? 0) + 1, percent: Math.round(((i + 1) / total) * 100) }));
        setError(err.message || `Failed to upload file ${i + 1}.`);
      }
    }

    if (uploadedUrls.length > 0) {
      setAttachedImages((prev) => [...prev, ...uploadedUrls]);
    }
  };

  const handleFileChange = (e) => handleFiles(e.target.files);
  const handleDragOver = (e) => e.preventDefault();
  const handleDrop = (e) => { e.preventDefault(); handleFiles(e.dataTransfer.files); };

  const handleRemoveImage = (idx) => {
    setAttachedImages((prev) => prev.filter((_, i) => i !== idx));
    if (lightboxIndex !== null) {
      if (idx === lightboxIndex) setLightboxIndex(null);
      else if (idx < lightboxIndex) setLightboxIndex((l) => l - 1);
    }
  };

  const handleSave = async () => {
    setSaving(true);
    setError(null);
    try {
      const fullEvent = await fetchEventById(event.id);
      await adminService.updateEvent(event.id, { ...fullEvent, attachedImages });
      if (onSave) await onSave();
      onClose();
    } catch (err) {
      setError(err.message || "Failed to save gallery changes.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <>
      <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]">
        <div className="bg-white rounded-2xl border border-slate-200 shadow-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto p-6 animate-[scaleIn_0.25s_ease] flex flex-col">

          {/* Header */}
          <div className="flex items-center justify-between pb-4 border-b border-slate-100 mb-6 shrink-0">
            <div>
              <h3 className="text-base font-extrabold text-slate-800 tracking-tight">Manage Event Gallery</h3>
              <p className="text-xs text-slate-400 font-semibold mt-0.5">Event: {event.name}</p>
            </div>
            <button
              onClick={onClose}
              disabled={isUploading || saving || loadingEvent}
              className="p-1.5 hover:bg-slate-100 text-slate-400 hover:text-slate-600 rounded-lg transition-colors disabled:opacity-40"
            >
              <FiX className="w-4.5 h-4.5" />
            </button>
          </div>

          {/* Body */}
          <div className="flex-1 overflow-y-auto min-h-0 space-y-6">
            {loadingEvent ? (
              <div className="flex flex-col items-center justify-center py-20">
                <FiLoader className="w-8 h-8 text-[#4B98C8] animate-spin mb-2" />
                <p className="text-xs font-semibold text-slate-500">Loading gallery images...</p>
              </div>
            ) : (
              <>
                {/* Gallery grid */}
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-3">
                    Gallery Images ({attachedImages.length})
                  </label>

                  {attachedImages.length === 0 ? (
                    <div className="flex flex-col items-center justify-center p-8 bg-slate-50 border border-slate-100 rounded-xl text-center">
                      <FiImage className="w-8 h-8 text-slate-300 mb-2" />
                      <p className="text-xs font-semibold text-slate-500">No images yet.</p>
                      <p className="text-[10px] text-slate-400 mt-1">Upload images below to attach them to this event.</p>
                    </div>
                  ) : (
                    <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-3">
                      {attachedImages.map((url, idx) => (
                        <div
                          key={idx}
                          className="group relative aspect-video rounded-xl overflow-hidden border border-slate-200 shadow-sm bg-slate-50 cursor-pointer"
                          onClick={() => setLightboxIndex(idx)}
                        >
                          <img
                            src={url}
                            alt={`Gallery ${idx + 1}`}
                            className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                          />
                          {/* Hover overlay with view icon */}
                          <div className="absolute inset-0 bg-slate-900/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                            <FiMaximize2 className="w-6 h-6 text-white drop-shadow-md" />
                          </div>
                          {/* Delete button – top-right corner, always visible */}
                          <button
                            type="button"
                            onClick={(e) => { e.stopPropagation(); handleRemoveImage(idx); }}
                            disabled={isUploading || saving}
                            className="absolute top-1.5 right-1.5 z-10 w-6 h-6 flex items-center justify-center bg-red-600/90 hover:bg-red-700 text-white rounded-md shadow transition-all active:scale-90 disabled:opacity-40"
                            title="Remove image"
                          >
                            <FiTrash2 className="w-3 h-3" />
                          </button>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

                {/* Single batch progress bar */}
                {batchProgress !== null && (
                  <div className="bg-slate-50 border border-slate-100 rounded-xl px-4 py-3">
                    <div className="flex items-center justify-between mb-2 gap-2">
                      <span className="text-[10px] font-bold text-slate-500 uppercase tracking-wider">
                        {isUploading
                          ? `Uploading… ${batchProgress.done}/${batchProgress.total}`
                          : batchProgress.failed > 0
                          ? `Done — ${batchProgress.failed} failed`
                          : `All ${batchProgress.total} uploaded ✓`}
                      </span>
                      <span className={`text-[11px] font-extrabold shrink-0 ${
                        isUploading ? "text-[#4B98C8]" : batchProgress.failed > 0 ? "text-red-500" : "text-emerald-600"
                      }`}>
                        {batchProgress.percent}%
                      </span>
                    </div>
                    <div className="w-full h-2 bg-slate-200 rounded-full overflow-hidden">
                      <div
                        className={`h-full rounded-full transition-all duration-300 ${
                          isUploading ? "bg-[#4B98C8]" : batchProgress.failed > 0 ? "bg-red-400" : "bg-emerald-500"
                        }`}
                        style={{ width: `${batchProgress.percent}%` }}
                      />
                    </div>
                  </div>
                )}

                {/* Drop zone */}
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-2">
                    Add New Images
                  </label>
                  <div
                    onDragOver={handleDragOver}
                    onDrop={handleDrop}
                    className={`relative border-2 border-dashed rounded-xl p-8 text-center flex flex-col items-center justify-center cursor-pointer transition-all ${
                      isUploading
                        ? "border-[#4B98C8]/40 bg-[#4B98C8]/5"
                        : "border-slate-200 hover:border-[#4B98C8]/50 hover:bg-slate-50"
                    }`}
                  >
                    <input
                      type="file"
                      multiple
                      accept="image/*"
                      onChange={handleFileChange}
                      disabled={isUploading || saving}
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
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
                        <p className="text-[9px] text-slate-400 mt-1">PNG, JPG, JPEG — up to 10 MB each</p>
                      </>
                    )}
                  </div>
                </div>
              </>
            )}

            {error && (
              <div className="bg-red-50 border border-red-200 text-red-600 rounded-xl p-3">
                <span className="text-xs font-semibold">{error}</span>
              </div>
            )}
          </div>

          {/* Footer */}
          <div className="flex justify-end gap-3 pt-4 border-t border-slate-100 mt-6 shrink-0">
            <button
              type="button"
              onClick={onClose}
              disabled={isUploading || saving || loadingEvent}
              className="px-4 py-2.5 border border-slate-200 text-slate-500 hover:text-slate-800 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all disabled:opacity-40"
            >
              Cancel
            </button>
            <button
              type="button"
              onClick={handleSave}
              disabled={isUploading || saving || loadingEvent}
              className="px-5 py-2.5 text-white text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow disabled:opacity-40"
              style={{ background: `linear-gradient(135deg, ${BRAND}, ${BRAND_DARK})` }}
            >
              {saving ? "Saving…" : "Save Gallery"}
            </button>
          </div>
        </div>
      </div>

      {/* Lightbox portal (above the modal) */}
      {lightboxIndex !== null && (
        <AdminLightbox
          images={attachedImages}
          startIndex={lightboxIndex}
          onClose={() => setLightboxIndex(null)}
        />
      )}
    </>
  );
};

export default EventGalleryModal;
