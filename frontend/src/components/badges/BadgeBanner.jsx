import BadgeMedal from "./BadgeMedal";
import logo from "../../assets/logo/acm-logo-no-bg.png";
import { WIDE_STRIPS } from "./confetti";

// The badge banner, laid out exactly like the 1200×627 LinkedIn image: the medal on the left, the chapter, the
// member's name (the largest text), the badge, the period and committee, and the award date on the right, the
// ACM logo in the top right corner, a gold frame and a few confetti strips. Everything is placed in design
// pixels and scaled with the card's width (container units), so it keeps its proportions at any size.
// Keep it in step with BadgeCardRenderer in the backend.
const W = 1200;
const H = 627;
const cq = (px) => `${((px / W) * 100).toFixed(4)}cqw`;

// Long names get a smaller size and may wrap onto a second line.
const nameSize = (name = "") => (name.length <= 14 ? 76 : name.length <= 19 ? 60 : 52);

// The description shrinks as it gets longer, and shows fewer lines when the name needs two or there is a
// button, so everything stays inside the frame. The full text is still there for screen readers.
const noteSize = (note) => (note.length <= 100 ? 24 : note.length <= 200 ? 21 : 19);
const noteLines = (name, hasButton) => (name.length > 22 ? 3 : 5) - (hasButton ? 1 : 0);

const LOGO_BOX = 79;
const LOGO_SCALE = Math.min(LOGO_BOX / 484, LOGO_BOX / 515);
const LOGO_W = 484 * LOGO_SCALE;
const LOGO_H = 515 * LOGO_SCALE;
const LOGO_CX = W - 60 - 46;
const LOGO_CY = 52 + 46;

// `children` is the Share on LinkedIn button, shown under the date for the badge's owner.
const BadgeBanner = ({ badge, awardedOn, className = "", children }) => (
  <div
    className={`relative w-full overflow-hidden rounded-[1.5rem] text-white ${className}`}
    style={{ containerType: "inline-size", aspectRatio: `${W} / ${H}`, background: "#0F3A5A" }}
  >
    <svg aria-hidden="true" viewBox={`0 0 ${W} ${H}`} className="pointer-events-none absolute inset-0 h-full w-full">
      <defs>
        <radialGradient id="banner-glow" gradientUnits="userSpaceOnUse" cx="230" cy={H / 2} r="300">
          <stop offset="0" stopColor="#FFE7A3" stopOpacity=".28" />
          <stop offset=".6" stopColor="#FFE7A3" stopOpacity=".05" />
          <stop offset="1" stopColor="#FFE7A3" stopOpacity="0" />
        </radialGradient>
      </defs>
      <circle cx="230" cy={H / 2} r="300" fill="url(#banner-glow)" />
      {WIDE_STRIPS.map(([x, y, rotation, length, color, opacity], i) => (
        <rect key={i} x={-length / 2} y={-length * 0.225} width={length} height={length * 0.45} rx="1.5" fill={color} opacity={opacity} transform={`translate(${x} ${y}) rotate(${rotation})`} />
      ))}
      <rect x="18" y="18" width={W - 36} height={H - 36} rx="14" fill="none" stroke="#F5B301" strokeOpacity=".55" strokeWidth="1.5" />
      <rect x="28" y="28" width={W - 56} height={H - 56} rx="9" fill="none" stroke="#F5B301" strokeOpacity=".22" strokeWidth="1" />
      <circle cx={LOGO_CX} cy={LOGO_CY + 5} r="46" fill="#000" opacity=".16" />
      <circle cx={LOGO_CX} cy={LOGO_CY} r="46" fill="#fff" />
      <image href={logo} x={LOGO_CX - LOGO_W / 2} y={LOGO_CY - LOGO_H / 2} width={LOGO_W} height={LOGO_H} />
    </svg>

    <div className="absolute" style={{ left: `${((230 - 170) / W) * 100}%`, top: `${((H / 2 - 170) / H) * 100}%`, width: `${(340 / W) * 100}%` }}>
      <BadgeMedal
        periodType={badge.periodType}
        periodLabel={badge.periodLabel}
        icon={badge.icon}
        onDark
        size={340}
        label={`${badge.name}, ${badge.periodLabel}`}
        className="h-auto w-full"
      />
    </div>

    <div className="absolute flex flex-col justify-center" style={{ left: cq(452), width: cq(668), top: cq(40), bottom: cq(40) }}>
      <p style={{ fontSize: cq(26), fontWeight: 700, lineHeight: 1.2, color: "#FFE7A3" }}>ACM Alexandria Student Chapter</p>
      <h1 style={{ marginTop: cq(28), fontSize: cq(nameSize(badge.member.name)), fontWeight: 800, lineHeight: 1.05, letterSpacing: "-0.02em", overflowWrap: "anywhere" }}>
        {badge.member.name}
      </h1>
      <p style={{ marginTop: cq(20), fontSize: cq(38), fontWeight: 700, lineHeight: 1.2, color: "#CFE6F5" }}>{badge.name}</p>
      <p style={{ marginTop: cq(12), fontSize: cq(28), lineHeight: 1.25, color: "#CFE6F5" }}>{badge.periodLabel} · {badge.committeeName}</p>
      {badge.note && (
        <p
          title={badge.note}
          style={{
            marginTop: cq(24), paddingLeft: cq(16), borderLeft: `${cq(3)} solid rgba(255,255,255,0.25)`,
            fontSize: cq(noteSize(badge.note)), lineHeight: 1.4, color: "rgba(255,255,255,0.9)",
            display: "-webkit-box", WebkitBoxOrient: "vertical", WebkitLineClamp: noteLines(badge.member.name || "", Boolean(children)), overflow: "hidden",
          }}
        >
          <span className="sr-only">Description: </span>“{badge.note}”
        </p>
      )}
      <p style={{ marginTop: cq(badge.note ? 26 : 40), display: "flex", alignItems: "center", gap: cq(16), fontSize: cq(22), color: "#BFE0F5" }}>
        <span aria-hidden="true" style={{ width: cq(44), height: cq(4), background: "#F5B301", flex: "0 0 auto" }} />
        Awarded {awardedOn}
      </p>
      {children && <div style={{ marginTop: cq(24), fontSize: cq(21) }}>{children}</div>}
    </div>
  </div>
);

export default BadgeBanner;
