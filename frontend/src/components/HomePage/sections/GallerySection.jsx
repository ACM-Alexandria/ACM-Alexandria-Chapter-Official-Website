import React, { useState, useEffect, useCallback, useRef } from "react";
import { createPortal } from "react-dom";
import {
  FiChevronLeft,
  FiChevronRight,
  FiMaximize2,
  FiX,
  FiLoader,
  FiImage,
} from "react-icons/fi";
import { fetchGalleryImages } from "../../../services/homePageService";

/* ── Lightbox Component ── */
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

  // Disable background scrolling when lightbox is open
  useEffect(() => {
    document.body.style.overflow = "hidden";
    return () => {
      document.body.style.overflow = "";
    };
  }, []);

  const handleTouchStart = (e) => {
    touchStartX.current = e.touches[0].clientX;
  };

  const handleTouchEnd = (e) => {
    if (touchStartX.current === null) return;
    const diff = touchStartX.current - e.changedTouches[0].clientX;
    if (Math.abs(diff) > 50) {
      if (diff > 0) next();
      else prev();
    }
    touchStartX.current = null;
  };

  return createPortal(
    <div
      className="fixed inset-0 z-[100] flex items-center justify-center bg-slate-950/90 backdrop-blur-md animate-[fadeIn_0.2s_ease]"
      onTouchStart={handleTouchStart}
      onTouchEnd={handleTouchEnd}
    >
      <style>{`
        @keyframes slideInFromRight { 
          from { opacity: 0; transform: translateX(60px) scale(0.97); } 
          to { opacity: 1; transform: translateX(0) scale(1); } 
        }
        @keyframes slideInFromLeft  { 
          from { opacity: 0; transform: translateX(-60px) scale(0.97); } 
          to { opacity: 1; transform: translateX(0) scale(1); } 
        }
        .slide-next { animation: slideInFromRight 0.28s cubic-bezier(0.25,0.46,0.45,0.94) both; }
        .slide-prev { animation: slideInFromLeft  0.28s cubic-bezier(0.25,0.46,0.45,0.94) both; }
      `}</style>

      {/* Close Button */}
      <button
        onClick={onClose}
        className="absolute right-6 top-6 z-[110] w-12 h-12 flex items-center justify-center rounded-2xl bg-slate-900/80 text-white border border-slate-700/50 shadow-xl hover:bg-slate-900 hover:scale-105 active:scale-95 transition-all cursor-pointer"
        aria-label="Close lightbox"
      >
        <FiX className="w-6 h-6" />
      </button>

      {/* Counter Indicator */}
      <div className="absolute top-6 left-1/2 -translate-x-1/2 z-[110] px-4 py-1.5 rounded-full bg-slate-800/70 text-white text-[11px] font-bold tracking-wider">
        {current + 1} / {images.length}
      </div>

      {/* Caption Overlay */}
      {images[current]?.caption && (
        <div className="absolute bottom-6 left-1/2 -translate-x-1/2 z-[110] px-6 py-3 rounded-2xl bg-slate-900/80 border border-slate-700/30 text-white text-xs font-semibold max-w-md text-center shadow-2xl backdrop-blur-sm">
          {images[current].caption}
        </div>
      )}

      {/* Left Navigation Arrow */}
      {images.length > 1 && (
        <button
          onClick={prev}
          className="absolute left-5 z-[110] w-12 h-12 flex items-center justify-center rounded-2xl bg-slate-900/70 text-white border border-slate-700/40 shadow-xl hover:bg-slate-800 hover:scale-105 active:scale-95 transition-all cursor-pointer"
          aria-label="Previous image"
        >
          <FiChevronLeft className="w-7 h-7" />
        </button>
      )}

      {/* Active Lightbox Image */}
      <img
        key={current}
        src={images[current]?.imageUrl}
        alt={images[current]?.caption || `Gallery Image ${current + 1}`}
        className={`max-w-[90vw] max-h-[85vh] rounded-2xl object-contain shadow-2xl ${
          direction === "next" ? "slide-next" : "slide-prev"
        }`}
      />

      {/* Right Navigation Arrow */}
      {images.length > 1 && (
        <button
          onClick={next}
          className="absolute right-5 z-[110] w-12 h-12 flex items-center justify-center rounded-2xl bg-slate-900/70 text-white border border-slate-700/40 shadow-xl hover:bg-slate-800 hover:scale-105 active:scale-95 transition-all cursor-pointer"
          aria-label="Next image"
        >
          <FiChevronRight className="w-7 h-7" />
        </button>
      )}
    </div>,
    document.body
  );
};

/* ── Main Gallery Section ── */
const GallerySection = () => {
  const [images, setImages] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [isPaused, setIsPaused] = useState(false);
  const [lightboxIndex, setLightboxIndex] = useState(null);

  useEffect(() => {
    const loadImages = async () => {
      try {
        setLoading(true);
        const data = await fetchGalleryImages();
        setImages(data || []);
      } catch (err) {
        console.error("Failed to load gallery images:", err);
        setError("Could not load gallery images. Please try again later.");
      } finally {
        setLoading(false);
      }
    };
    loadImages();
  }, []);

  // Auto-shift interval: shifts every 3 seconds
  useEffect(() => {
    if (images.length < 3 || isPaused) return;
    const interval = setInterval(() => {
      setCurrentIndex((prev) => (prev + 1) % images.length);
    }, 3000);
    return () => clearInterval(interval);
  }, [images.length, isPaused, currentIndex]);

  const nextCarousel = useCallback(() => {
    if (images.length < 3) return;
    setCurrentIndex((prev) => (prev + 1) % images.length);
  }, [images.length]);

  const prevCarousel = useCallback(() => {
    if (images.length < 3) return;
    setCurrentIndex((prev) => (prev - 1 + images.length) % images.length);
  }, [images.length]);

  const carouselTouchStartX = useRef(null);

  const handleCarouselTouchStart = (e) => {
    carouselTouchStartX.current = e.touches[0].clientX;
  };

  const handleCarouselTouchEnd = (e) => {
    if (carouselTouchStartX.current === null) return;
    const diff = carouselTouchStartX.current - e.changedTouches[0].clientX;
    if (Math.abs(diff) > 50) {
      if (diff > 0) {
        nextCarousel();
      } else {
        prevCarousel();
      }
    }
    carouselTouchStartX.current = null;
  };

  // Calculates the relative display index in the carousel loop
  const getRelativeIndex = (index) => {
    const n = images.length;
    return (index - currentIndex + n) % n;
  };

  // Maps a relative position to its responsive CSS styles
  const getCardStyles = (relIndex, total) => {
    // 1. Check if it is the element currently exiting left (Slot 1 -> Exit)
    if (relIndex === total - 1) {
      return "-left-[16%] w-[32%] opacity-0 scale-90 rotate-0 z-0 pointer-events-none -translate-x-8";
    }
    // 2. Visible Slot 1 (Left)
    if (relIndex === 0) {
      return "left-0 w-[32%] opacity-100 scale-100 rotate-0 z-20 cursor-pointer shadow-lg shadow-slate-200/40 hover:scale-[1.03] hover:-translate-y-1 hover:shadow-xl transition-all duration-500";
    }
    // 3. Visible Slot 2 (Middle)
    if (relIndex === 1) {
      return "left-[34%] w-[32%] opacity-100 scale-100 rotate-0 z-20 cursor-pointer shadow-lg shadow-slate-200/40 hover:scale-[1.03] hover:-translate-y-1 hover:shadow-xl transition-all duration-500";
    }
    // 4. Visible Slot 3 (Right)
    if (relIndex === 2) {
      return "left-[68%] w-[32%] opacity-100 scale-100 rotate-0 z-30 cursor-pointer shadow-xl shadow-slate-200/50 hover:scale-[1.03] hover:-translate-y-1 hover:shadow-2xl transition-all duration-500";
    }
    // 5. Stacked Card 1 (Directly behind Slot 3)
    if (relIndex === 3) {
      return "left-[68%] w-[32%] opacity-70 scale-95 rotate-1 md:rotate-2 translate-x-4 md:translate-x-6 translate-y-1 md:translate-y-2 z-20 pointer-events-none shadow-md";
    }
    // 6. Stacked Card 2 (Further behind Slot 3)
    if (relIndex === 4) {
      return "left-[68%] w-[32%] opacity-40 scale-90 -rotate-1 md:-rotate-2 translate-x-8 md:translate-x-12 translate-y-2 md:translate-y-4 z-10 pointer-events-none shadow-sm";
    }
    // 7. Hidden items inside the stack pool
    return "left-[68%] w-[32%] opacity-0 scale-85 rotate-0 z-0 pointer-events-none translate-x-12 translate-y-6";
  };

  if (loading) {
    return (
      <div className="mt-28">
        <div className="flex flex-col md:flex-row items-center justify-between mb-12 gap-4">
          <div>
            <h3 className="text-3xl font-extrabold text-slate-900 tracking-tight mb-2">
              Captured Moments
            </h3>
            <p className="text-slate-500 font-medium">
              Loading our chapter history...
            </p>
          </div>
          <div className="h-px flex-1 bg-slate-200 hidden md:block mx-8 opacity-50" />
        </div>
        <div className="grid grid-cols-1 sm:grid-cols-3 gap-6 h-[280px] md:h-[380px]">
          {[...Array(3)].map((_, i) => (
            <div
              key={i}
              className="animate-pulse bg-slate-100 rounded-3xl h-full border border-slate-200/50 flex items-center justify-center"
            >
              <FiLoader className="w-8 h-8 text-[#4B98C8] animate-spin" />
            </div>
          ))}
        </div>
      </div>
    );
  }

  // If no images uploaded, don't show the section to keep the UI clean
  if (images.length === 0) {
    return null;
  }

  const isCarousel = images.length >= 3;

  return (
    <div className="mt-28">
      {/* Header */}
      <div className="flex flex-col md:flex-row items-center justify-between mb-12 gap-4">
        <div className="text-center md:text-left">
          <h3
            className="text-3xl font-extrabold text-slate-900 tracking-tight mb-2"
            data-aos="fade-right"
          >
            Captured Moments
          </h3>
          <p
            className="text-slate-500 font-medium"
            data-aos="fade-right"
            data-aos-delay="100"
          >
            A visual archive of our workshops, hackathons, and community events
          </p>
        </div>
        <div className="h-px flex-1 bg-slate-200 hidden md:block mx-8 opacity-50" />
      </div>

      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600 rounded-3xl p-5 text-center mb-6">
          <p className="text-sm font-semibold">{error}</p>
        </div>
      )}

      {/* Render Carousel or Fallback Static List */}
      {isCarousel ? (
        <div
          className="relative w-full h-[180px] sm:h-[280px] md:h-[380px] lg:h-[450px] select-none"
          onMouseEnter={() => setIsPaused(true)}
          onMouseLeave={() => setIsPaused(false)}
          onTouchStart={handleCarouselTouchStart}
          onTouchEnd={handleCarouselTouchEnd}
          data-aos="fade-up"
        >
          {/* Manual Left Arrow Button */}
          <button
            onClick={nextCarousel}
            className="absolute -left-5 top-1/2 -translate-y-1/2 z-40 bg-white/95 hover:bg-white text-slate-800 p-3 rounded-2xl shadow-xl border border-slate-100 hover:scale-105 active:scale-95 transition-all cursor-pointer hidden md:flex items-center justify-center"
            aria-label="Next images"
          >
            <FiChevronLeft className="w-5 h-5" />
          </button>

          {/* Shifting Items Stack */}
          <div className="relative w-full h-full">
            {images.map((image, index) => {
              const relIndex = getRelativeIndex(index);
              const cardStyles = getCardStyles(relIndex, images.length);
              const isVisible = relIndex >= 0 && relIndex <= 2;

              return (
                <div
                  key={image.id || index}
                  onClick={() => isVisible && setLightboxIndex(index)}
                  className={`absolute top-0 h-full rounded-3xl transition-all duration-700 ease-in-out group overflow-hidden ${cardStyles}`}
                >
                  {/* Image */}
                  <img
                    src={image.imageUrl}
                    alt={image.caption || "ACM Gallery Image"}
                    className="w-full h-full object-cover transition-transform duration-700 ease-out group-hover:scale-105"
                    loading="lazy"
                  />

                  {/* Hover Overlay details - only for visible active cards */}
                  {isVisible && (
                    <div className="absolute inset-0 bg-gradient-to-t from-slate-950/80 via-slate-950/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 flex flex-col justify-end p-4 md:p-6">
                      {/* Glass Zoom Icon */}
                      <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-12 h-12 md:w-14 md:h-14 rounded-full bg-white/20 backdrop-blur-md border border-white/30 flex items-center justify-center text-white shadow-lg opacity-0 scale-75 group-hover:opacity-100 group-hover:scale-100 transition-all duration-500">
                        <FiMaximize2 className="w-4 h-4 md:w-5 md:h-5 transition-transform duration-500 group-hover:rotate-12" />
                      </div>

                      {/* Text */}
                      {image.caption && (
                        <div className="translate-y-4 group-hover:translate-y-0 transition-transform duration-500 delay-75">
                          <p className="text-white text-[10px] md:text-xs font-bold uppercase tracking-wider mb-0.5 md:mb-1 opacity-70">
                            ACM Chapter
                          </p>
                          <p className="text-white text-xs md:text-sm font-bold truncate">
                            {image.caption}
                          </p>
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>

          {/* Manual Right Arrow Button */}
          <button
            onClick={prevCarousel}
            className="absolute -right-5 top-1/2 -translate-y-1/2 z-40 bg-white/95 hover:bg-white text-slate-800 p-3 rounded-2xl shadow-xl border border-slate-100 hover:scale-105 active:scale-95 transition-all cursor-pointer hidden md:flex items-center justify-center"
            aria-label="Previous images"
          >
            <FiChevronRight className="w-5 h-5" />
          </button>
        </div>
      ) : (
        /* Fallback for 1 or 2 images: Static Grid layout */
        <div
          className={`grid gap-6 ${
            images.length === 1 ? "grid-cols-1 max-w-2xl mx-auto" : "grid-cols-1 sm:grid-cols-2"
          }`}
          data-aos="fade-up"
        >
          {images.map((image, index) => (
            <div
              key={image.id || index}
              onClick={() => setLightboxIndex(index)}
              className="group relative overflow-hidden rounded-3xl cursor-pointer border border-slate-100/50 shadow-md hover:shadow-xl transition-all duration-500 h-[220px] sm:h-[300px] md:h-[380px]"
            >
              <img
                src={image.imageUrl}
                alt={image.caption || "ACM Gallery Image"}
                className="w-full h-full object-cover transition-transform duration-700 ease-out group-hover:scale-105"
              />
              <div className="absolute inset-0 bg-gradient-to-t from-slate-950/80 via-slate-950/20 to-transparent opacity-0 group-hover:opacity-100 transition-opacity duration-500 flex flex-col justify-end p-6">
                <div className="absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2 w-14 h-14 rounded-full bg-white/20 backdrop-blur-md border border-white/30 flex items-center justify-center text-white shadow-lg opacity-0 scale-75 group-hover:opacity-100 group-hover:scale-100 transition-all duration-500">
                  <FiMaximize2 className="w-5 h-5 transition-transform duration-500 group-hover:rotate-12" />
                </div>
                {image.caption && (
                  <div className="translate-y-4 group-hover:translate-y-0 transition-transform duration-500 delay-75">
                    <p className="text-white text-xs font-bold uppercase tracking-wider mb-1 opacity-70">
                      ACM Chapter
                    </p>
                    <p className="text-white text-sm font-bold truncate">
                      {image.caption}
                    </p>
                  </div>
                )}
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Fullscreen Lightbox Trigger */}
      {lightboxIndex !== null && (
        <GalleryLightbox
          images={images}
          startIndex={lightboxIndex}
          onClose={() => setLightboxIndex(null)}
        />
      )}
    </div>
  );
};

export default GallerySection;
