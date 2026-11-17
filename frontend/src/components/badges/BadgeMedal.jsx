import { useId } from "react";
import { medalText } from "./medalText";

// The one badge design used everywhere: a laurel wreath around a glossy medal that hangs from the ACM ribbon.
// The medal's color shows the period (month blue, quarter indigo, season gold), its icon comes from the badge
// type, and the period is printed on it ("NOV / 2026", "Q2 / 2027", "2027 / SEASON"). It keeps its own colors
// in both themes, like a physical medal; `onDark` uses lighter wreath and ribbon colors for deep backgrounds
// (the badge page, the poster).

const PALETTES = {
  MONTH: { a: "#8FD0F5", b: "#3D93CF", c: "#1E5F8E", rim1: "#E9F6FF", rim2: "#7FB6DB", ink: "#FFFFFF", leaf: ["#3D93CF", "#7FBBE6"], leafDark: ["#8FD0F5", "#5BAEE3"] },
  QUARTER: { a: "#6E8BF2", b: "#3651C9", c: "#1D2C7A", rim1: "#EEF1FF", rim2: "#97A6E6", ink: "#FFFFFF", leaf: ["#5E78E6", "#9AABF5"], leafDark: ["#9DB0FA", "#6E8BF2"] },
  SEASON: { a: "#FFE27A", b: "#F5B301", c: "#B26F00", rim1: "#FFF7D6", rim2: "#E9B530", ink: "#5A3A00", leaf: ["#D99A00", "#F5B301"], leafDark: ["#FFE27A", "#F5B301"] },
  NONE: { a: "#CBD5E1", b: "#94A3B8", c: "#475569", rim1: "#F8FAFC", rim2: "#CBD5E1", ink: "#FFFFFF", leaf: ["#94A3B8", "#CBD5E1"], leafDark: ["#CBD5E1", "#94A3B8"] },
};

// The ACM ribbon: two panels with fine white stripes.
const RIBBON = { light: ["#2A7AB0", "#5FA8D8"], dark: ["#4F9FD3", "#A9D5F2"] };

const ICONS = {
  TROPHY: (
    <>
      <path d="M6 9H4.5a2.5 2.5 0 0 1 0-5H6" />
      <path d="M18 9h1.5a2.5 2.5 0 0 0 0-5H18" />
      <path d="M4 22h16" />
      <path d="M10 14.66V17c0 .55-.47.98-.97 1.21C7.85 18.75 7 20.24 7 22" />
      <path d="M14 14.66V17c0 .55.47.98.97 1.21C16.15 18.75 17 20.24 17 22" />
      <path d="M18 2H6v7a6 6 0 0 0 12 0V2Z" />
    </>
  ),
  CROWN: (
    <>
      <path d="m2 5 3.5 11h13L22 5l-6 6-4-7-4 7-6-6z" />
      <path d="M5 20h14" />
    </>
  ),
  USERS: (
    <>
      <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
      <circle cx="9" cy="7" r="4" />
      <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
      <path d="M16 3.13a4 4 0 0 1 0 7.75" />
    </>
  ),
  STAR: <polygon points="12 2 15.09 8.26 22 9.27 17 14.14 18.18 21.02 12 17.77 5.82 21.02 7 14.14 2 9.27 8.91 8.26 12 2" />,
};

const CX = 60;
const CY = 72;
const R = 38;

// Two sprigs of leaves along an arc on each side of the medal, each leaf tilted along its stem.
const wreath = (stem, [outer, inner]) =>
  [-1, 1].flatMap((side) => {
    const point = (deg) => {
      const t = (deg * Math.PI) / 180;
      return [CX + side * R * Math.sin(t), CY - R * Math.cos(t)];
    };
    const [sx, sy] = point(172);
    const [ex, ey] = point(62);
    const items = [
      <path key={`stem${side}`} d={`M${sx.toFixed(2)} ${sy.toFixed(2)} A${R} ${R} 0 0 ${side > 0 ? 0 : 1} ${ex.toFixed(2)} ${ey.toFixed(2)}`} fill="none" stroke={stem} strokeWidth="1.6" strokeLinecap="round" />,
    ];
    for (let i = 0; i < 6; i += 1) {
      const deg = 165 - i * 19;
      const t = (deg * Math.PI) / 180;
      const [x, y] = point(deg);
      const tangent = (Math.atan2(-Math.sin(t), -side * Math.cos(t)) * 180) / Math.PI;
      [1, -1].forEach((out) => {
        const angle = tangent + out * side * 32;
        const lx = x + Math.cos((angle * Math.PI) / 180) * 6;
        const ly = y + Math.sin((angle * Math.PI) / 180) * 6;
        items.push(
          <ellipse key={`leaf${side}${i}${out}`} cx={lx.toFixed(2)} cy={ly.toFixed(2)} rx="6.4" ry="2.7" transform={`rotate(${angle.toFixed(1)} ${lx.toFixed(2)} ${ly.toFixed(2)})`} fill={out > 0 ? outer : inner} />,
        );
      });
    }
    return items;
  });

// periodType: "MONTH" | "QUARTER" | "SEASON" | null; icon: "TROPHY" | "CROWN" | "USERS" | "STAR";
// periodLabel: the period's label ("Nov 2026"), printed on the medal (without one, the icon fills it).
// Pass `label` to announce it to screen readers; without one the medal is decorative.
const BadgeMedal = ({ periodType, periodLabel, icon = "TROPHY", size = 48, label, onDark = false, className = "" }) => {
  const id = useId().replace(/:/g, "");
  const p = PALETTES[periodType] || PALETTES.NONE;
  const [ribbonDeep, ribbonLight] = onDark ? RIBBON.dark : RIBBON.light;
  const { top, bottom } = medalText(periodType, periodLabel);
  const a11y = label ? { role: "img", "aria-label": label } : { "aria-hidden": true };
  const k = 26 / 28;
  const glyph = ICONS[icon] || ICONS.TROPHY;
  const stroke = { fill: "none", stroke: p.ink, strokeLinecap: "round", strokeLinejoin: "round" };
  return (
    <svg width={size} height={size} viewBox="0 0 120 120" className={`shrink-0 ${className}`} {...a11y}>
      <defs>
        <linearGradient id={`${id}f`} x1="0" y1="0" x2="0" y2="1">
          <stop offset="0" stopColor={p.a} />
          <stop offset=".55" stopColor={p.b} />
          <stop offset="1" stopColor={p.c} />
        </linearGradient>
        <linearGradient id={`${id}r`} x1="0" y1="0" x2="1" y2="1">
          <stop offset="0" stopColor={p.rim1} />
          <stop offset=".5" stopColor={p.rim2} />
          <stop offset="1" stopColor={p.rim1} />
        </linearGradient>
      </defs>

      <path d="M36 2 H52 L66 40 H50Z" fill={ribbonDeep} />
      <path d="M84 2 H68 L54 40 H70Z" fill={ribbonLight} />
      <path d="M41.5 2 H45 L58.5 40 H55Z" fill="#fff" opacity=".85" />
      <path d="M78.5 2 H75 L61.5 40 H65Z" fill="#fff" opacity=".85" />

      {wreath(onDark ? p.leafDark[0] : p.leaf[0], onDark ? p.leafDark : p.leaf)}

      <circle cx={CX} cy={CY} r="31" fill={`url(#${id}r)`} />
      <circle cx={CX} cy={CY} r="26" fill={`url(#${id}f)`} />
      <circle cx={CX} cy={CY} r="23" fill="none" stroke="#fff" strokeOpacity=".3" strokeDasharray="1.5 2.5" />
      <path d={`M${CX - 22} ${CY - 8} A25 25 0 0 1 ${CX + 22} ${CY - 8} C${CX + 10} ${CY - 14} ${CX - 10} ${CY - 14} ${CX - 22} ${CY - 8}Z`} fill="#fff" opacity=".22" />

      {top ? (
        <>
          <g transform={`translate(${CX - 12 * 0.78 * k} ${CY - 9 * k - 12 * 0.78 * k}) scale(${0.78 * k})`} strokeWidth="1.9" {...stroke}>{glyph}</g>
          <text x={CX} y={CY + 9 * k} textAnchor="middle" fontFamily="Inter, system-ui, sans-serif" fontSize={11.5 * k} fontWeight="900" letterSpacing={0.6} fill={p.ink}>{top}</text>
          <text x={CX} y={CY + 17.5 * k} textAnchor="middle" fontFamily="Inter, system-ui, sans-serif" fontSize={6.2 * k} fontWeight="700" letterSpacing={1} fill={p.ink}>{bottom}</text>
        </>
      ) : (
        <g transform={`translate(${CX - 12 * 1.4 * k} ${CY - 12 * 1.4 * k}) scale(${1.4 * k})`} strokeWidth="1.9" {...stroke}>{glyph}</g>
      )}
    </svg>
  );
};

export default BadgeMedal;
