import BadgeMedal from "./BadgeMedal";
import logo from "../../assets/logo/acm-logo-outline.svg";
import { WIDE_NODES, linesOf } from "./network";

// The badge banner, laid out exactly like the 1200×627 LinkedIn image: the medal on the left, the chapter, the
// member's name (the largest text), the badge, the period and committee, and the award date on the right, the
// ACM logo in a small disc before the chapter name, on a deep blue gradient with a web of nodes and lines. Everything is placed in design
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

const LINES = linesOf(WIDE_NODES, 210);

// `children` is the Share on LinkedIn button, shown under the date for the badge's owner.
const BadgeBanner = ({ badge, awardedOn, className = "", children }) => (
  <div
    className={`relative w-full overflow-hidden rounded-[1.5rem] text-white ${className}`}
    style={{ containerType: "inline-size", aspectRatio: `${W} / ${H}`, background: "linear-gradient(135deg, #0B2E49 0%, #0F3A5A 55%, #15507A 100%)" }}
  >
    <svg aria-hidden="true" viewBox={`0 0 ${W} ${H}`} className="pointer-events-none absolute inset-0 h-full w-full">
      <defs>
        <radialGradient id="banner-orb-a" gradientUnits="userSpaceOnUse" cx="980" cy="90" r="320">
          <stop offset="0" stopColor="#5FA8D8" stopOpacity=".5" />
          <stop offset="1" stopColor="#5FA8D8" stopOpacity="0" />
        </radialGradient>
        <radialGradient id="banner-orb-b" gradientUnits="userSpaceOnUse" cx="150" cy="560" r="260">
          <stop offset="0" stopColor="#2F86C4" stopOpacity=".45" />
          <stop offset="1" stopColor="#2F86C4" stopOpacity="0" />
        </radialGradient>
      </defs>
      <circle cx="980" cy="90" r="320" fill="url(#banner-orb-a)" />
      <circle cx="150" cy="560" r="260" fill="url(#banner-orb-b)" />
      {LINES.map(([x1, y1, x2, y2, opacity], i) => <line key={i} x1={x1} y1={y1} x2={x2} y2={y2} stroke="#8FD0F5" strokeOpacity={opacity} strokeWidth="1" />)}
      {WIDE_NODES.map(([x, y, r], i) => <circle key={i} cx={x} cy={y} r={r} fill="#BFE0F5" opacity=".6" />)}
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
      <div style={{ display: "flex", alignItems: "center", gap: cq(16) }}>
        <img src={logo} alt="" style={{ height: cq(66), width: "auto", flex: "0 0 auto" }} />
        <p style={{ fontSize: cq(26), fontWeight: 700, lineHeight: 1.2, color: "#FFE7A3" }}>ACM Alexandria Student Chapter</p>
      </div>
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
