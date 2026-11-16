import BadgeMedal from "../../badges/BadgeMedal";
import logo from "../../../assets/logo/acm-logo-no-bg.png";
import { initials } from "../../../utils/format";
import { POSTER_FORMATS } from "./posterLayout";

// Fixed poster colors: the poster is an image, so it looks the same in light and dark mode.
const BACKGROUND = "linear-gradient(150deg, #1D6A9C 0%, #205E85 45%, #123E5E 100%)";
const INK = "#0F2A40";
const ACCENT = "#2A7AB0";
const SOFT = "#CFE6F5";
const GOLD = "#FFE7A3";
const AVATAR_TINTS = [["#E2F0FA", "#205E85"], ["#FFF3C4", "#3A3012"], ["#DCEEFA", "#174A70"]];
const PERIOD_WORD = { MONTH: "month", QUARTER: "quarter", SEASON: "season" };
const FONT = "Inter, Roboto, system-ui, -apple-system, 'Segoe UI', sans-serif";

// Header sizes per format; card sizes live in posterLayout.
const HEAD = {
  square: { padding: "56px 64px 50px", logo: 80, chapter: 22, gap: 26, medal: 112, title: 74, pill: 18, content: 34, footer: 27 },
  story: { padding: "104px 88px 96px", logo: 88, chapter: 28, gap: 52, medal: 150, title: 92, pill: 26, content: 56, footer: 36 },
};

const WhiteDisc = ({ size, pad, children }) => (
  <span style={{ width: size, height: size, flex: "0 0 auto", borderRadius: "50%", background: "#FFFFFF", display: "flex", alignItems: "center", justifyContent: "center", padding: pad, boxSizing: "border-box", boxShadow: "0 12px 30px -14px rgba(0,0,0,0.45)" }}>
    {children}
  </span>
);

const Avatar = ({ member, size, fontSize, tint }) => {
  const style = { width: size, height: size, borderRadius: "50%", flex: "0 0 auto" };
  if (member.profileImageUrl) {
    return <img src={member.profileImageUrl} alt="" crossOrigin="anonymous" style={{ ...style, objectFit: "cover" }} />;
  }
  return (
    <span style={{ ...style, background: tint[0], color: tint[1], fontSize, fontWeight: 700, display: "flex", alignItems: "center", justifyContent: "center" }}>
      {initials(member.name || "?")}
    </span>
  );
};

const CommitteeCard = ({ card, c, width }) => (
  <div style={{ width, boxSizing: "border-box", background: "#FFFFFF", color: INK, borderRadius: 26, padding: c.padding, display: "flex", flexDirection: "column", gap: c.titleGap, boxShadow: "0 18px 40px -20px rgba(0,0,0,0.45)" }}>
    <p style={{ margin: 0, height: c.title, display: "flex", alignItems: "center", fontSize: c.titleSize, fontWeight: 700, color: ACCENT, whiteSpace: "nowrap", overflow: "hidden", textOverflow: "ellipsis" }}>
      {card.name}{card.continued ? " (continued)" : ""}
    </p>
    <div style={{ display: "flex", flexDirection: "column", gap: c.rowGap }}>
      {card.winners.map((w, i) => (
        <div key={w.id} style={{ height: c.row, display: "flex", alignItems: "center", gap: Math.round(c.row * 0.26) }}>
          <Avatar member={w.member} size={c.row} fontSize={c.avatarText} tint={AVATAR_TINTS[i % AVATAR_TINTS.length]} />
          <p style={{ margin: 0, minWidth: 0, fontSize: c.nameSize, lineHeight: 1.2, fontWeight: 600, display: "-webkit-box", WebkitLineClamp: 2, WebkitBoxOrient: "vertical", overflow: "hidden", overflowWrap: "anywhere" }}>
            {w.member.name}
          </p>
        </div>
      ))}
    </div>
  </div>
);

// One page of the winners poster for a badge and period: the ACM logo and chapter name, the badge's medal,
// title and period, then a white card per committee with its winners (photos for board members, initials
// otherwise). No points. `ref` is the element turned into the PNG.
const PeriodPoster = ({ ref, format, badge, periodTitle, periodType, cards, page, pages }) => {
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

      <div style={{ position: "relative", display: "flex", alignItems: "center", gap: Math.round(h.logo * 0.28), justifyContent: story ? "center" : "flex-start" }}>
        <WhiteDisc size={h.logo} pad={Math.round(h.logo * 0.1)}>
          <img src={logo} alt="" style={{ width: "100%", height: "100%", objectFit: "contain" }} />
        </WhiteDisc>
        <p style={{ margin: 0, fontSize: h.chapter, fontWeight: 600, color: SOFT }}>ACM Alexandria Student Chapter</p>
      </div>

      <div style={{ position: "relative", marginTop: h.gap, display: "flex", flexDirection: story ? "column" : "row", alignItems: "center", gap: story ? 26 : 26, textAlign: story ? "center" : "left" }}>
        <WhiteDisc size={h.medal} pad={Math.round(h.medal * 0.05)}>
          <BadgeMedal periodType={badge.periodType} icon={badge.icon} size={Math.round(h.medal * 0.9)} />
        </WhiteDisc>
        <div style={{ minWidth: 0 }}>
          <h1 style={{ margin: 0, fontSize: h.title, lineHeight: 1.02, fontWeight: 800, letterSpacing: "-0.035em" }}>{badge.name}s</h1>
          <span style={{ display: "inline-block", marginTop: story ? 22 : 14, background: "rgba(255,255,255,0.14)", borderRadius: 999, padding: story ? "10px 24px" : "7px 16px", fontSize: h.pill, fontWeight: 600, color: GOLD }}>
            {periodTitle}
          </span>
        </div>
      </div>

      <div style={{ position: "relative", marginTop: h.content, height: f.contentHeight, display: "flex", flexWrap: "wrap", justifyContent: "center", alignContent: "center", alignItems: "stretch", gap: f.gap }}>
        {cards.map((card, i) => <CommitteeCard key={i} card={card} c={f.card} width={cardWidth} />)}
      </div>

      <p style={{ position: "relative", margin: "auto 0 0", textAlign: "center", fontSize: h.footer, lineHeight: 1.3, fontWeight: 600, color: "#FFFFFF" }}>
        Congratulations, and thank you{story ? <br /> : " "}for an amazing {PERIOD_WORD[periodType] || "season"}!
        {pages > 1 && (
          <span style={{ display: "block", marginTop: 8, fontSize: Math.round(h.footer * 0.6), fontWeight: 500, color: SOFT }}>{page} / {pages}</span>
        )}
      </p>
    </div>
  );
};

export default PeriodPoster;
