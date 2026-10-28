import { useEffect, useState } from "react";
import { useInView } from "../../hooks/useInView";

// Counts up to `value` once when scrolled into view, then shows `suffix` (such as "+"); shows the final number
// for reduced motion.
const CountUp = ({ value, duration = 1200, suffix = "" }) => {
  const [ref, seen] = useInView(0.4);
  const [shown, setShown] = useState(0);

  useEffect(() => {
    if (!seen) return undefined;
    if (window.matchMedia?.("(prefers-reduced-motion: reduce)").matches) {
      const frame = requestAnimationFrame(() => setShown(value));
      return () => cancelAnimationFrame(frame);
    }
    let frame;
    const start = performance.now();
    const tick = (now) => {
      const progress = Math.min((now - start) / duration, 1);
      setShown(Math.round(value * (1 - (1 - progress) ** 3)));
      if (progress < 1) frame = requestAnimationFrame(tick);
    };
    frame = requestAnimationFrame(tick);
    return () => cancelAnimationFrame(frame);
  }, [seen, value, duration]);

  return <span ref={ref} className="tabular-nums">{shown}{shown === value ? suffix : ""}</span>;
};

export default CountUp;
