import React, { useLayoutEffect, useRef } from "react";

const DURATION = 1000;
const easeOutCubic = (t) => 1 - (1 - t) ** 3;

// Counts up from 0 to the value when it first shows, and from the old value when it changes.
// Writes to the DOM directly so a frame does not re-render React. Reduced motion shows the value at once.
const AnimatedNumber = ({ value }) => {
  const ref = useRef(null);
  const shown = useRef(0);

  useLayoutEffect(() => {
    const node = ref.current;
    if (!node) return undefined;
    const reduce = window.matchMedia?.("(prefers-reduced-motion: reduce)").matches;
    const from = shown.current;
    if (reduce || from === value) {
      node.textContent = value.toLocaleString();
      shown.current = value;
      return undefined;
    }

    node.textContent = from.toLocaleString();
    const start = performance.now();
    let frame = 0;
    const tick = (now) => {
      const t = Math.min(1, (now - start) / DURATION);
      const current = Math.round(from + (value - from) * easeOutCubic(t));
      node.textContent = current.toLocaleString();
      shown.current = current;
      if (t < 1) frame = requestAnimationFrame(tick);
    };
    frame = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(frame);
  }, [value]);

  return <span ref={ref}>{value.toLocaleString()}</span>;
};

export default AnimatedNumber;
