import { useEffect, useRef, useState } from "react";

// A sliding row that loops its children without gaps: one set is measured, then repeated enough times to fill
// the row, and the track moves by exactly one set so the loop is invisible. `speed` is in pixels per second, so
// short and long rows move at the same pace. Hover pauses it; reduced motion shows one static set.
const Marquee = ({ children, speed = 60, reverse = false, className = "", label }) => {
  const rowRef = useRef(null);
  const setRef = useRef(null);
  const [size, setSize] = useState({ set: 0, row: 0 });

  useEffect(() => {
    const measure = () => setSize({ set: setRef.current?.offsetWidth || 0, row: rowRef.current?.offsetWidth || 0 });
    measure();
    const observer = new ResizeObserver(measure);
    if (setRef.current) observer.observe(setRef.current);
    if (rowRef.current) observer.observe(rowRef.current);
    return () => observer.disconnect();
  }, []);

  // Enough sets to cover the row plus the one sliding out of view.
  const copies = size.set > 0 ? Math.ceil(size.row / size.set) + 1 : 2;
  const moving = size.set > 0;

  return (
    <div ref={rowRef} className={`group overflow-hidden ${className}`} aria-label={label} role={label ? "region" : undefined}>
      <div
        className="flex w-max group-hover:[animation-play-state:paused]"
        style={moving ? { "--marquee-shift": `-${size.set}px`, animation: `hp-marquee-set ${size.set / speed}s linear infinite${reverse ? " reverse" : ""}` } : undefined}
      >
        <div ref={setRef} className="flex shrink-0 items-center">{children}</div>
        {Array.from({ length: copies - 1 }, (_, i) => (
          <div key={i} className="flex shrink-0 items-center motion-reduce:hidden" aria-hidden="true">{children}</div>
        ))}
      </div>
    </div>
  );
};

export default Marquee;
