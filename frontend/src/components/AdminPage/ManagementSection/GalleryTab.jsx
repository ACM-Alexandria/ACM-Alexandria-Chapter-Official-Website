import React, { useState, useEffect, useCallback, useRef } from "react";
import {
  FiUploadCloud,
  FiTrash2,
  FiLoader,
  FiImage,
  FiChevronLeft,
  FiChevronRight,
  FiMaximize2,
  FiX,
  FiRefreshCw,
} from "react-icons/fi";
import api from "../../../services/api";
import adminService from "../../../services/adminService";

/* ── upload a single file via the authenticated axios instance ── */
const uploadFile = (file) => {
  const formData = new FormData();
  formData.append("file", file);
  return api
    .post("/api/images/upload", formData, { headers: { "Content-Type": "multipart/form-data" } })
    .then((res) => res.data)
    .catch((err) => {
      throw new Error(err.response?.data?.message || err.message || "Upload failed");
    });
};

/* ── Lightbox ── */
const GalleryLightbox = ({ images, startIndex, onClose }) => {
  const [current, setCurrent] = useState(startIndex);
  const [direction, setDirection] = useState("next");
  const touchStartX = useRef(null);

  const prev = useCallback(() => {
    setDirection("prev");
    setCurrent((c) => (c - 1 + images.length) % images.length);
  }, [images.length]);

  const next = useCallback(() => {
    setDirection("next");
    setCurrent((c) => (c + 1) % images.length);
  }, [images.length]);

  useEffect(() => {
    const handler = (e) => {
      if (e.key === "ArrowLeft") prev();
      if (e.key === "ArrowRight") next();
      if (e.key === "Escape") onClose();
    };
    window.addEventListener("keydown", handler);
    return () => window.removeEventListener("keydown", handler);
  }, [prev, next, onClose]);

  const handleTouchStart = (e) => { touchStartX.current = e.touches[0].clientX; };
  const handleTouchEnd = (e) => {
    if (touchStartX.current === null) return;
    const diff = touchStartX.current - e.changedTouches[0].clientX;
    if (Math.abs(diff) > 50) {
      if (diff > 0) next();
      else prev();
    }
    touchStartX.current = null;
  };

  return (
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center bg-slate-950/88 backdrop-blur-md animate-[fadeIn_0.2s_ease]"
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
    >
      <style>{`
        @keyframes slideInFromRight { from { opacity: 0; transform: translateX(60px) scale(0.97); } to { opacity: 1; transform: translateX(0) scale(1); } }
        @keyframes slideInFromLeft  { from { opacity: 0; transform: translateX(-60px) scale(0.97); } to { opacity: 1; transform: translateX(0) scale(1); } }
        .slide-next { animation: slideInFromRight 0.28s cubic-bezier(0.25,0.46,0.45,0.94) both; }
        .slide-prev { animation: slideInFromLeft  0.28s cubic-bezier(0.25,0.46,0.45,0.94) both; }
      `}</style>

      {/* Close */}
      <button
        onClick={onClose}
        className="absolute right-6 top-6 z-[110] w-12 h-12 flex items-center justify-center rounded-2xl bg-slate-900/80 text-white border border-slate-700/50 shadow-xl hover:bg-slate-900 hover:scale-105 active:scale-95 transition-all"
        aria-label="Close"
      >
        <FiX className="w-6 h-6" />
      </button>

      {/* Counter */}
      <div className="absolute top-6 left-1/2 -translate-x-1/2 z-[110] px-4 py-1.5 rounded-full bg-slate-800/70 text-white text-[11px] font-bold tracking-wider">
        {current + 1} / {images.length}
      </div>

      {/* Caption */}
      {images[current]?.caption && (
        <div className="absolute bottom-6 left-1/2 -translate-x-1/2 z-[110] px-5 py-2 rounded-2xl bg-slate-900/70 text-white text-xs font-semibold max-w-md text-center">
          {images[current].caption}
        </div>
      )}

      {/* Prev */}
      {images.length > 1 && (
        <button
          onClick={prev}
          className="absolute left-5 z-[110] w-12 h-12 flex items-center justify-center rounded-2xl bg-slate-900/70 text-white border border-slate-700/40 shadow-xl hover:bg-slate-800 hover:scale-105 active:scale-95 transition-all"
          aria-label="Previous"
        >
          <FiChevronLeft className="w-7 h-7" />
        </button>
      )}

      {/* Image */}
      <img
        key={current}
        src={images[current]?.imageUrl}
        alt={images[current]?.caption || `Gallery ${current + 1}`}
        className={`max-w-[90vw] max-h-[85vh] rounded-2xl object-contain shadow-2xl ${direction === "next" ? "slide-next" : "slide-prev"}`}
      />

      {/* Next */}
      {images.length > 1 && (
        <button
          onClick={next}
          className="absolute right-5 z-[110] w-12 h-12 flex items-center justify-center rounded-2xl bg-slate-900/70 text-white border border-slate-700/40 shadow-xl hover:bg-slate-800 hover:scale-105 active:scale-95 transition-all"
          aria-label="Next"
        >
          <FiChevronRight className="w-7 h-7" />
        </button>
      )}
    </div>
  );
};

/* ── Main Gallery Tab ── */
const GalleryTab = () => {
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [batchProgress, setBatchProgress] = useState(null); // { total, done, percent, failed }
  const [deletingId, setDeletingId] = useState(null);
  const [lightboxIndex, setLightboxIndex] = useState(null);

  const isUploading = batchProgress !== null && batchProgress.done < batchProgress.total;

  const loadImages = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminService.fetchGalleryImages();
      setImages(data);
    } catch (err) {
      setError(err.message || "Failed to load gallery images.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadImages();
  }, [loadImages]);

  const handleFiles = async (files) => {
    setError(null);
    const toProcess = Array.from(files);

    for (const f of toProcess) {
      if (f.size > 10 * 1024 * 1024) {
        setError(`"${f.name}" exceeds the 10 MB limit.`);
        return;
      }
    }

    const total = toProcess.length;
    setBatchProgress({ total, done: 0, percent: 0, failed: 0 });

    for (let i = 0; i < total; i++) {
      try {
        const uploaded = await uploadFile(toProcess[i]);
        if (uploaded?.url) {
          // Save to backend immediately
          const saved = await adminService.addGalleryImage({ imageUrl: uploaded.url, caption: "" });
          setImages((prev) => [...prev, saved]);
        }
        setBatchProgress({ total, done: i + 1, percent: Math.round(((i + 1) / total) * 100), failed: 0 });
      } catch (err) {
        setBatchProgress((prev) => ({
          ...prev,
          done: i + 1,
          failed: (prev?.failed ?? 0) + 1,
          percent: Math.round(((i + 1) / total) * 100),
        }));
        setError(err.message || `Failed to upload file ${i + 1}.`);
      }
    }
  };

  const handleFileChange = (e) => handleFiles(e.target.files);
  const handleDragOver = (e) => e.preventDefault();
  const handleDrop = (e) => { e.preventDefault(); handleFiles(e.dataTransfer.files); };

  const handleDelete = async (image, idx) => {
    setDeletingId(image.id);
    try {
      await adminService.deleteGalleryImage(image.id);
      setImages((prev) => prev.filter((_, i) => i !== idx));
      if (lightboxIndex !== null) {
        if (idx === lightboxIndex) setLightboxIndex(null);
        else if (idx < lightboxIndex) setLightboxIndex((l) => l - 1);
      }
    } catch (err) {
      setError(err.message || "Failed to delete image.");
    } finally {
      setDeletingId(null);
    }
  };

  return (
    <>
      <div className="space-y-6">
        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-extrabold text-slate-800 tracking-tight">ACM Gallery</h2>
            <p className="text-xs text-slate-400 font-medium mt-0.5">
              {images.length} image{images.length !== 1 ? "s" : ""} · Changes are saved immediately
            </p>
          </div>
          <button
            onClick={loadImages}
            disabled={loading || isUploading}
            className="p-2 hover:bg-slate-100 text-slate-400 hover:text-slate-600 rounded-xl transition-colors disabled:opacity-40"
            title="Refresh"
          >
            <FiRefreshCw className={`w-4 h-4 ${loading ? "animate-spin" : ""}`} />
          </button>
        </div>

        {/* Error */}
        {error && (
          <div className="bg-red-50 border border-red-200 text-red-600 rounded-xl p-3 flex items-center gap-2">
            <span className="text-xs font-semibold">{error}</span>
          </div>
        )}

        {/* Loading skeleton */}
        {loading && images.length === 0 ? (
          <div className="flex flex-col items-center justify-center py-20">
            <FiLoader className="w-8 h-8 text-[#4B98C8] animate-spin mb-2" />
            <p className="text-xs font-semibold text-slate-500">Loading gallery...</p>
          </div>
        ) : (
          <>
            {/* Image grid */}
            {images.length === 0 ? (
              <div className="flex flex-col items-center justify-center p-10 bg-slate-50 border border-slate-100 rounded-2xl text-center">
                <FiImage className="w-10 h-10 text-slate-300 mb-3" />
                <p className="text-sm font-semibold text-slate-500">No images yet</p>
                <p className="text-[11px] text-slate-400 mt-1">Upload images below to build the ACM gallery.</p>
              </div>
            ) : (
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 xl:grid-cols-5 gap-3">
                {images.map((img, idx) => (
                  <div
                    key={img.id}
                    className="group relative aspect-video rounded-xl overflow-hidden border border-slate-200 shadow-sm bg-slate-50 cursor-pointer"
                    onClick={() => setLightboxIndex(idx)}
                  >
                    <img
                      src={img.imageUrl}
                      alt={img.caption || `Gallery ${idx + 1}`}
                      className="w-full h-full object-cover transition-transform duration-300 group-hover:scale-105"
                    />
                    {/* Hover overlay */}
                    <div className="absolute inset-0 bg-slate-900/50 opacity-0 group-hover:opacity-100 transition-opacity flex items-center justify-center">
                      <FiMaximize2 className="w-6 h-6 text-white drop-shadow-md" />
                    </div>
                    {/* Delete — top-right corner */}
                    <button
                      type="button"
                      onClick={(e) => { e.stopPropagation(); handleDelete(img, idx); }}
                      disabled={deletingId === img.id || isUploading}
                      className="absolute top-1.5 right-1.5 z-10 w-6 h-6 flex items-center justify-center bg-red-600/90 hover:bg-red-700 text-white rounded-md shadow transition-all active:scale-90 disabled:opacity-40"
                      title="Delete image"
                    >
                      {deletingId === img.id ? (
                        <FiLoader className="w-3 h-3 animate-spin" />
                      ) : (
                        <FiTrash2 className="w-3 h-3" />
                      )}
                    </button>
                  </div>
                ))}
              </div>
            )}

            {/* Upload progress bar */}
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
                Add Images
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
                  disabled={isUploading}
                  className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                />
                {isUploading ? (
                  <>
                    <FiLoader className="w-8 h-8 text-[#4B98C8] animate-spin mb-2" />
                    <p className="text-[11px] font-bold text-slate-600">Uploading…</p>
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
          </>
        )}
      </div>

      {/* Lightbox */}
      {lightboxIndex !== null && images.length > 0 && (
        <GalleryLightbox
          images={images}
          startIndex={lightboxIndex}
          onClose={() => setLightboxIndex(null)}
        />
      )}
    </>
  );
};

export default GalleryTab;
