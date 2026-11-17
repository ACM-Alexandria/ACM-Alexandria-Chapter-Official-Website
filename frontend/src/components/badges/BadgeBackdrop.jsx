import { linesOf, makeNodes } from "./network";

const W = 400;
const H = 760;
const NODES = makeNodes(21, 24, W, H);
const LINES = linesOf(NODES, 150);

// The backdrop of the badge card on phones (the wide layout is BadgeBanner): two soft blue glows and a web of
// nodes and fine lines. Decorative only; put it first inside a `relative overflow-hidden` card.
const BadgeBackdrop = () => (
  <svg aria-hidden="true" viewBox={`0 0 ${W} ${H}`} preserveAspectRatio="xMidYMid slice" className="pointer-events-none absolute inset-0 h-full w-full">
    <defs>
      <radialGradient id="backdrop-orb-a" gradientUnits="userSpaceOnUse" cx="330" cy="80" r="240">
        <stop offset="0" stopColor="#5FA8D8" stopOpacity=".5" />
        <stop offset="1" stopColor="#5FA8D8" stopOpacity="0" />
      </radialGradient>
      <radialGradient id="backdrop-orb-b" gradientUnits="userSpaceOnUse" cx="60" cy="700" r="220">
        <stop offset="0" stopColor="#2F86C4" stopOpacity=".45" />
        <stop offset="1" stopColor="#2F86C4" stopOpacity="0" />
      </radialGradient>
    </defs>
    <circle cx="330" cy="80" r="240" fill="url(#backdrop-orb-a)" />
    <circle cx="60" cy="700" r="220" fill="url(#backdrop-orb-b)" />
    {LINES.map(([x1, y1, x2, y2, opacity], i) => <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} stroke="#8FD0F5" strokeOpacity={opacity} strokeWidth="1" />)}
    {NODES.map(([x, y, r], i) => <circle key={i} cx={x} cy={y} r={r} fill="#BFE0F5" opacity=".6" />)}
  </svg>
);

export default BadgeBackdrop;
