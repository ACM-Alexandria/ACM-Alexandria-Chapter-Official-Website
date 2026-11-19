import { useId } from "react";
import { pharosFlame, pharosFlameCore, pharosLevels, pharosViewBox, pharosWindows, pointsBox } from "./pharosGeometry";

// The lighthouse from the chapter logo. All three inputs are 0..1 and accept numbers or CSS expressions such as
// "var(--build)". `plan` draws a thin blueprint outline of the whole tower. `build` then fills the tiers from
// the ground up, each tier rising behind a bright scan line. `light` turns the logo's blue flame into a warm
// burning one and lights the windows.
const level = (build, k) => `clamp(0, calc(${build} * 4 - ${k}), 1)`;

const bounds = pharosLevels.map((parts) => {
  const boxes = parts.map((part) => pointsBox(part.points));
  const top = Math.min(...boxes.map((b) => b.top));
  const bottom = Math.max(...boxes.map((b) => b.bottom));
  return { top, bottom, left: Math.min(...boxes.map((b) => b.left)), right: Math.max(...boxes.map((b) => b.right)), height: bottom - top };
});

const windowShape = (w) => (w.arch
  ? `M${w.x} ${w.y + w.h} V${w.y + w.w / 2} A${w.w / 2} ${w.w / 2} 0 0 1 ${w.x + w.w} ${w.y + w.w / 2} V${w.y + w.h} Z`
  : `M${w.x} ${w.y} h${w.w} v${w.h} h${-w.w} Z`);

const Pharos = ({ build = 1, light = 1, plan = 0, className = "", tower = "var(--color-ocean-525)", shade = "var(--color-ocean-659)" }) => {
  const glowId = `pharos-glow-${useId().replace(/:/g, "")}`;
  const top = level(build, pharosLevels.length - 1);
  return (
    <svg viewBox={pharosViewBox} className={`overflow-visible ${className}`} aria-hidden="true">
      <defs>
        <radialGradient id={glowId} cx="50%" cy="50%" r="50%">
          <stop offset="0%" stopColor="var(--color-gold-100)" stopOpacity="0.95" />
          <stop offset="45%" stopColor="var(--color-gold-261)" stopOpacity="0.35" />
          <stop offset="100%" stopColor="var(--color-gold-261)" stopOpacity="0" />
        </radialGradient>
      </defs>

      <g fill="none" stroke="var(--color-ocean-222)" strokeWidth="1" strokeLinejoin="round" style={{ opacity: `calc(${plan} * 0.75)` }}>
        {pharosLevels.flat().map((part, i) => (
          <polygon key={i} points={part.points} pathLength="1" strokeDasharray="1" style={{ strokeDashoffset: `calc(1 - ${plan})` }} />
        ))}
        <path d={pharosFlame} pathLength="1" strokeDasharray="1" style={{ strokeDashoffset: `calc(1 - ${plan})` }} />
      </g>

      <circle cx="100" cy="36" r="72" fill={`url(#${glowId})`} style={{ opacity: light, transformBox: "fill-box", transformOrigin: "center", transform: `scale(calc(0.4 + 0.6 * ${light}))` }} />

      {pharosLevels.map((parts, k) => {
        const progress = level(build, k);
        const box = bounds[k];
        return (
          <g key={k}>
            <g style={{ clipPath: `inset(calc((1 - ${progress}) * 100%) 0 0 0)` }}>
              {parts.map((part, i) => (
                <g key={i}>
                  <polygon points={part.points} fill={tower} />
                  <polygon points={part.shade} fill={shade} />
                </g>
              ))}
              {pharosWindows.filter((w) => w.level === k).map((w, i) => (
                <g key={`w${i}`}>
                  <path d={windowShape(w)} fill="var(--color-ocean-78)" />
                  <path d={windowShape(w)} fill="var(--color-gold-261)" style={{ opacity: light }} />
                </g>
              ))}
            </g>
            <rect
              x={box.left - 6}
              y={box.bottom - 1.5}
              width={box.right - box.left + 12}
              height="3"
              rx="1.5"
              fill="var(--color-ocean-43)"
              style={{
                transformBox: "view-box",
                transform: `translateY(calc(${progress} * -${box.height}px))`,
                opacity: `calc(min(${progress}, 1 - ${progress}) * 6)`,
                filter: "drop-shadow(0 0 4px color-mix(in srgb, var(--color-ocean-222) 90%, transparent))",
              }}
            />
          </g>
        );
      })}

      <g style={{ opacity: `calc(${top} * (1 - ${light}))` }}>
        <path d={pharosFlame} fill="var(--color-ocean-461)" />
        <path d={pharosFlameCore} fill="var(--color-ocean-614)" />
      </g>
      <g style={{ opacity: light, transformBox: "fill-box", transformOrigin: "50% 100%", transform: `scale(calc(0.6 + 0.4 * ${light}))`, animation: "hp-flicker 2.4s ease-in-out infinite" }}>
        <path d={pharosFlame} fill="var(--color-gold-329)" />
        <path d={pharosFlameCore} fill="var(--color-gold-100)" />
      </g>
    </svg>
  );
};

export default Pharos;
