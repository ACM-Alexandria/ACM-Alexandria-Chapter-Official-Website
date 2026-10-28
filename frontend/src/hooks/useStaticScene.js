import { useSyncExternalStore } from "react";

// Whether a media query matches, kept up to date as the window changes.
export const useMediaQuery = (query) => useSyncExternalStore(
  (onChange) => {
    const media = window.matchMedia(query);
    media.addEventListener("change", onChange);
    return () => media.removeEventListener("change", onChange);
  },
  () => window.matchMedia(query).matches,
  () => false,
);

// The lighthouse scene plays as a scroll animation only on desktop. On tablets and phones, and for visitors who
// ask for reduced motion, it is a still picture of the finished scene instead.
export const useStaticScene = () => useMediaQuery("(max-width: 1024px), (prefers-reduced-motion: reduce)");
