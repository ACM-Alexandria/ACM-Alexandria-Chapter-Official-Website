/* ─── Brand Palette ─── */
export const BRAND = "#4B98C8";
export const BRAND_DARK = "#205E85";

export const CHART_COLORS = [
  "#4B98C8", "#205E85", "#2CBFA1", "#7C5CDB",
  "#E8724A", "#F5A623", "#D94F7B", "#5A9BD5",
];

/* Smooth cubic spline path builder for SVG line charts */
export const buildSpline = (pts) => {
  if (pts.length < 2) return "";
  let d = `M ${pts[0].x} ${pts[0].y}`;
  for (let i = 0; i < pts.length - 1; i++) {
    const curr = pts[i], next = pts[i + 1];
    const cpx1 = curr.x + (next.x - curr.x) * 0.4;
    const cpx2 = curr.x + (next.x - curr.x) * 0.6;
    d += ` C ${cpx1} ${curr.y}, ${cpx2} ${next.y}, ${next.x} ${next.y}`;
  }
  return d;
};

/* Shared shimmer CSS class for skeleton placeholders */
export const shimmerClass =
  "relative overflow-hidden before:absolute before:inset-0 before:-translate-x-full before:animate-[shimmer_1.8s_infinite] before:bg-gradient-to-r before:from-transparent before:via-white/60 before:to-transparent";
