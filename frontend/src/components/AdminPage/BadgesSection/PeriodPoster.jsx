import BadgeMedal from "../../badges/BadgeMedal";
import logo from "../../../assets/logo/acm-logo-outline.svg";
import { FOCUS, POSTER_FORMATS } from "./posterLayout";
import { POSTER_STRIPS } from "./posterConfetti";

// Fixed poster colors: the poster is an image, so it looks the same in light and dark mode.
const BACKGROUND = "linear-gradient(150deg, #1D6A9C 0%, #205E85 45%, #123E5E 100%)";
const INK = "#0F2A40";
const ACCENT = "#2A7AB0";
const SOFT = "#CFE6F5";
const GOLD = "#FFE7A3";
const PERIOD_WORD = { MONTH: "month", QUARTER: "quarter", SEASON: "season" };
const FONT = "Inter, Roboto, system-ui, -apple-system, 'Segoe UI', sans-serif";

// Header sizes per format; card sizes live in posterLayout.
const HEAD = {
  square: { padding: "52px 64px 48px", logo: 80, chapter: 22, gap: 18, medal: 164, title: 74, pill: 18, content: 26, footer: 27 },
  story: { padding: "104px 88px 96px", logo: 88, chapter: 28, gap: 36, medal: 214, title: 92, pill: 26, content: 40, footer: 36 },
};

// The celebration decoration: a thin gold double frame and a scatter of confetti strips (see posterConfetti).
const Celebration = ({ format, width, height }) => (
  <svg aria-hidden="true" width={width} height={height} viewBox={`0 0 ${width} ${height}`} style={{ position: "absolute", left: 0, top: 0 }}>
    {POSTER_STRIPS[format].map(({ x, y, rotation, length, color, opacity }, i) => (
      <rect key={i} x={-length / 2} y={-length * 0.225} width={length} height={length * 0.45} rx="2" fill={color} opacity={opacity} transform={`translate(${x} ${y}) rotate(${rotation})`} />
    ))}
    <rect x="16" y="16" width={width - 32} height={height - 32} rx="30" fill="none" stroke="#F5B301" strokeOpacity=".55" strokeWidth="2" />
    <rect x="26" y="26" width={width - 52} height={height - 52} rx="22" fill="none" stroke="#F5B301" strokeOpacity=".22" strokeWidth="1.5" />
  </svg>
);

// The circle beside a winner's name. Members have no photos, so every winner gets the same gold star.
const WinnerStar = ({ size }) => (
  <span style={{ width: size, height: size, borderRadius: "50%", flex: "0 0 auto", background: "#FFF3C4", display: "flex", alignItems: "center", justifyContent: "center" }}>
    <svg aria-hidden="true" viewBox="0 0 24 24" width={Math.round(size * 0.56)} height={Math.round(size * 0.56)}>
      <path d="M12 2.6l2.9 6.1 6.7.9-4.9 4.6 1.2 6.6L12 17.6 6.1 20.8l1.2-6.6L2.4 9.6l6.7-.9z" fill="#F5B301" stroke="#C98A00" strokeWidth="1.2" strokeLinejoin="round" />
    </svg>
  </span>
);

const CommitteeCard = ({ card, c, width }) => (
  <div style={{ width, boxSizing: "border-box", background: "#FFFFFF", color: INK, borderRadius: 26, padding: c.padding, display: "flex", flexDirection: "column", gap: c.titleGap, boxShadow: "0 18px 40px -20px rgba(0,0,0,0.45)" }}>
    <p style={{ margin: 0, height: c.title, display: "flex", alignItems: "center", fontSize: c.titleSize, fontWeight: 700, color: ACCENT, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>
      {card.name}{card.continued ? " (continued)" : ""}
    </p>
    <div style={{ display: "flex", flexDirection: "column", gap: c.rowGap }}>
      {card.winners.map((w) => (
        <div key={w.id} style={{ height: c.row, display: "flex", alignItems: "center", gap: Math.round(c.row * 0.26) }}>
          <WinnerStar size={c.row} />
          <p style={{ margin: 0, minWidth: 0, fontSize: c.nameSize, lineHeight: 1.2, fontWeight: 600, display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical", overflow: "hidden", overflowWrap: "anywhere" }}>
            {w.member.name}
          </p>
        </div>
      ))}
    </div>
  </div>
);

// The one-committee layout: the committee's name large, then its winners in big rows.
const FocusBlock = ({ card, format, marginTop }) => {
  const focus = FOCUS[format];
  const two = card.winners.length > focus.one.max;
  const L = two ? focus.two : focus.one;
  const nameSize = card.name.length > 22 ? Math.round(focus.nameSize * 0.78) : focus.nameSize;
  return (
    <div style={{ position: "relative", marginTop, height: POSTER_FORMATS[format].contentHeight, display: "flex", flexDirection: "column", alignItems: "center", justifyContent: "center", gap: focus.nameGap }}>
      <div style={{ textAlign: "center", maxWidth: 900 }}>
        <p style={{ margin: 0, fontSize: Math.round(focus.nameSize * 0.3), fontWeight: 700, letterSpacing: "0.22em", textTransform: "uppercase", color: GOLD }}>Committee winners</p>
        <h2 style={{ margin: `${Math.round(focus.nameSize * 0.18)}px 0 0`, fontSize: nameSize, lineHeight: 1.05, fontWeight: 800, letterSpacing: "-0.03em" }}>
          {card.name}{card.continued ? " (continued)" : ""}
        </h2>
        <span style={{ display: "inline-block", marginTop: Math.round(focus.nameSize * 0.28), width: Math.round(focus.nameSize * 1.6), height: 4, background: "#F5B301", borderRadius: 2 }} />
      </div>
      <div style={{ display: "grid", gridTemplateColumns: two ? "1fr 1fr" : "1fr", gap: L.gap, width: L.width }}>
        {card.winners.map((w) => (
          <div key={w.id} style={{ height: L.row, boxSizing: "border-box", background: "#FFFFFF", color: INK, borderRadius: 26, padding: `0 ${Math.round(L.row * 0.22)}px`, display: "flex", alignItems: "center", gap: Math.round(L.avatar * 0.3), boxShadow: "0 18px 40px -20px rgba(0,0,0,0.45)" }}>
            <WinnerStar size={L.avatar} />
            <p style={{ margin: 0, minWidth: 0, fontSize: L.name, lineHeight: 1.15, fontWeight: 700, display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical", overflow: "hidden", overflowWrap: "anywhere" }}>
              {w.member.name}
            </p>
          </div>
        ))}
      </div>
    </div>
  );
};

// One page of the winners poster for a badge and period: the ACM logo and chapter name, the badge's medal,
// title and period, then a white card per committee with its winners, each beside a gold star. No photos, no ranks
// and no points. In `committee` mode `cards` holds one committee and the page focuses on it. `ref` is the
// element turned into the PNG.
const PeriodPoster = ({ ref, format, mode = "all", badge, periodTitle, periodLabel, periodType, cards }) => {
  const f = POSTER_FORMATS[format];
  const h = HEAD[format];
  const story = format === "story";
  const cardWidth = (f.width - f.sidePadding * 2 - f.gap * (f.columns - 1)) / f.columns;
  return (
    <div
      ref={ref}
      style={{
        width: f.width, height: f.height, boxSizing: "border-box", position: "relative", overflow: "hidden",
        background: BACKGROUND, color: "#FFFFFF", fontFamily: FONT, padding: h.padding,
        display: "flex", flexDirection: "column",
      }}
    >
      <div aria-hidden="true" style={{ position: "absolute", width: story ? 760 : 520, height: story ? 760 : 520, borderRadius: "50%", background: "rgba(255,255,255,0.06)", right: story ? -260 : -140, top: story ? -240 : -160 }} />
      <Celebration format={format} width={f.width} height={f.height} />

      <div style={{ position: "relative", display: "flex", alignItems: "center", gap: Math.round(h.logo * 0.28), justifyContent: story ? "center" : "flex-start" }}>
        <img src={logo} alt="" style={{ height: Math.round(h.logo * 1.15), width: "auto", flex: "0 0 auto" }} />
        <p style={{ margin: 0, fontSize: h.chapter, fontWeight: 600, color: SOFT }}>ACM Alexandria Student Chapter</p>
      </div>

      <div style={{ position: "relative", marginTop: h.gap, display: "flex", flexDirection: story ? "column" : "row", alignItems: "center", gap: story ? 26 : 26, textAlign: story ? "center" : "left" }}>
        <div style={{ position: "relative", flex: "0 0 auto", width: h.medal, height: h.medal }}>
          {/* A soft golden light behind the medal, kept gentle. */}
          <span aria-hidden="true" style={{ position: "absolute", inset: "-26%", background: "radial-gradient(closest-side, rgba(255,231,163,0.28) 0%, rgba(255,231,163,0.05) 60%, rgba(255,231,163,0) 100%)" }} />
          <div style={{ position: "relative" }}>
            <BadgeMedal periodType={badge.periodType} periodLabel={periodLabel} icon={badge.icon} size={h.medal} onDark />
          </div>
        </div>
        <div style={{ minWidth: 0 }}>
          <h1 style={{ margin: 0, fontSize: h.title, lineHeight: 1.02, fontWeight: 800, letterSpacing: "-0.035em" }}>{badge.name}s</h1>
          <span style={{ display: "inline-block", marginTop: story ? 22 : 14, background: "rgba(255,255,255,0.14)", borderRadius: 999, padding: story ? "10px 24px" : "7px 16px", fontSize: h.pill, fontWeight: 600, color: GOLD }}>
            {periodTitle}
          </span>
        </div>
      </div>

      {mode === "committee" ? (
        <FocusBlock card={cards[0]} format={format} marginTop={h.content} />
      ) : (
        <div style={{ position: "relative", marginTop: h.content, height: f.contentHeight, display: "flex", flexWrap: "wrap", justifyContent: "center", alignContent: "center", alignItems: "stretch", gap: f.gap }}>
          {cards.map((card, i) => <CommitteeCard key={i} card={card} c={f.card} width={cardWidth} />)}
        </div>
      )}

      <p style={{ position: "relative", margin: "auto 0 0", textAlign: "center", fontSize: h.footer, lineHeight: 1.3, fontWeight: 600, color: "#FFFFFF" }}>
        Congratulations, and thank you{story ? <br /> : " "}for an amazing {PERIOD_WORD[periodType] || "season"}!
      </p>
    </div>
  );
};

export default PeriodPoster;
