import { useEffect } from "react";

export const clamp01 = (value) => Math.min(Math.max(value, 0), 1);

export const prefersReducedMotion = () => window.matchMedia("(prefers-reduced-motion: reduce)").matches;

// Runs `onFrame` once on mount and then at most once per animation frame while the page scrolls or resizes.
// Scroll-linked effects write CSS variables from here, so React never re-renders per frame.
export const useScrollFrame = (onFrame) => {
  useEffect(() => {
    let frame;
    const schedule = () => {
      cancelAnimationFrame(frame);
      frame = requestAnimationFrame(onFrame);
    };
    onFrame();
    window.addEventListener("scroll", schedule, { passive: true });
    window.addEventListener("resize", schedule);
    return () => {
      cancelAnimationFrame(frame);
      window.removeEventListener("scroll", schedule);
      window.removeEventListener("resize", schedule);
    };
  }, [onFrame]);
};
