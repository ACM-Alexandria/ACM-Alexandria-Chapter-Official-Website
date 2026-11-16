// The one badge design used everywhere: a round medal whose colors show the period
// (month, quarter, season, or none for manual badges) and whose icon comes from the badge type.
// It keeps its own colors in both themes, like a physical medal.

const MEDAL_PALETTES = {
  MONTH: { ring: "#4B98C8", bg: "#E2F0FA", fg: "#205E85" },
  QUARTER: { ring: "#7DBBE0", bg: "#205E85", fg: "#FFFFFF" },
  SEASON: { ring: "#F5B301", bg: "#FFE7A3", fg: "#3A3012" },
  NONE: { ring: "#94A3B8", bg: "#F1F5F9", fg: "#334155" },
};

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

// periodType: "MONTH" | "QUARTER" | "SEASON" | null; icon: "TROPHY" | "CROWN" | "USERS" | "STAR".
// Pass `label` to announce it to screen readers; without one the medal is decorative.
const BadgeMedal = ({ periodType, icon = "TROPHY", size = 48, label, className = "" }) => {
  const p = MEDAL_PALETTES[periodType] || MEDAL_PALETTES.NONE;
  const a11y = label ? { role: "img", "aria-label": label } : { "aria-hidden": true };
  return (
    <svg width={size} height={size} viewBox="0 0 100 100" className={`shrink-0 ${className}`} {...a11y}>
      <circle cx="50" cy="50" r="48" fill={p.ring} />
      <circle cx="50" cy="50" r="41" fill={p.bg} />
      <circle cx="50" cy="50" r="35" fill="none" stroke={p.fg} strokeOpacity="0.28" strokeWidth="1.2" strokeDasharray="2 3" />
      <g transform="translate(29 29) scale(1.75)" fill="none" stroke={p.fg} strokeWidth="1.7" strokeLinecap="round" strokeLinejoin="round">
        {ICONS[icon] || ICONS.TROPHY}
      </g>
    </svg>
  );
};

export default BadgeMedal;
