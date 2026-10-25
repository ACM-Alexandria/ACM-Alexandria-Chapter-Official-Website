import { useCallback, useEffect, useRef, useState } from "react";
import { createPortal } from "react-dom";
import { FiChevronLeft, FiChevronRight, FiX } from "react-icons/fi";

// Full-screen photo viewer with arrows, keyboard (← → Esc) and swipe.
const Lightbox = ({ images, startIndex = 0, onClose }) => {
  const [current, setCurrent] = useState(startIndex);
  const touchX = useRef(null);
  const prev = useCallback(() => setCurrent((c) => (c - 1 + images.length) % images.length), [images.length]);
  const next = useCallback(() => setCurrent((c) => (c + 1) % images.length), [images.length]);

  useEffect(() => {
    const onKey = (event) => {
      if (event.key === "ArrowLeft") prev();
      if (event.key === "ArrowRight") next();
      if (event.key === "Escape") {
        event.stopImmediatePropagation();
        onClose();
      }
    };
    window.addEventListener("keydown", onKey, true);
    return () => window.removeEventListener("keydown", onKey, true);
  }, [prev, next, onClose]);

  const arrow = "absolute top-1/2 flex h-12 w-12 -translate-y-1/2 items-center justify-center rounded-full bg-white/10 text-white backdrop-blur-sm transition-colors hover:bg-white/20";
  return createPortal(
    <div
      data-lenis-prevent
      role="dialog"
      aria-modal="true"
      aria-label="Photo viewer"
      className="fixed inset-0 z-[100] flex items-center justify-center bg-[#06121D]/90 backdrop-blur-md"
      style={{ animation: "hp-fade 0.2s ease both" }}
      onTouchStart={(event) => { touchX.current = event.touches[0].clientX; }}
      onTouchEnd={(event) => {
        if (touchX.current === null) return;
        const diff = touchX.current - event.changedTouches[0].clientX;
        if (Math.abs(diff) > 50) (diff > 0 ? next : prev)();
        touchX.current = null;
      }}
    >
      <button type="button" onClick={onClose} aria-label="Close" className="absolute right-4 top-4 flex h-11 w-11 items-center justify-center rounded-full bg-white/90 text-slate-700 shadow-lg hover:bg-white"><FiX className="h-5 w-5" aria-hidden="true" /></button>
      <span className="absolute left-1/2 top-5 -translate-x-1/2 rounded-full bg-white/10 px-3 py-1 text-xs font-semibold text-white">{current + 1} / {images.length}</span>
      {images.length > 1 && <button type="button" onClick={prev} aria-label="Previous photo" className={`${arrow} left-4`}><FiChevronLeft className="h-6 w-6" aria-hidden="true" /></button>}
      <img key={current} src={images[current]} alt={`Photo ${current + 1} of ${images.length}`} className="max-h-[85vh] max-w-[90vw] rounded-2xl object-contain shadow-2xl" style={{ animation: "hp-pop 0.3s cubic-bezier(0.22,1,0.36,1) both" }} />
      {images.length > 1 && <button type="button" onClick={next} aria-label="Next photo" className={`${arrow} right-4`}><FiChevronRight className="h-6 w-6" aria-hidden="true" /></button>}
    </div>,
    document.body,
  );
};

export default Lightbox;
