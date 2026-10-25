import { useSyncExternalStore } from "react";

// The lighthouse scene plays as a scroll animation only on desktop. On tablets and phones, and for visitors who
// ask for reduced motion, it is a still picture of the finished scene instead.
const query = "(max-width: 1024px), (prefers-reduced-motion: reduce)";

const subscribe = (onChange) => {
  const media = window.matchMedia(query);
  media.addEventListener("change", onChange);
  return () => media.removeEventListener("change", onChange);
};

export const useStaticScene = () => useSyncExternalStore(subscribe, () => window.matchMedia(query).matches, () => false);
