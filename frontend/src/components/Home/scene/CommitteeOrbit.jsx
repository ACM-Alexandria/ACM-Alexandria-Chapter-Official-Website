import logo from "../../../assets/logo/acm-logo-no-bg.png";
import { shortName } from "../../../utils/format";
import TeamLogo from "../../ui/TeamLogo";

// Scroll breaks the orbit: --hp (0..1, how far the hero has scrolled away; 0 when unset) cracks both rings
// into fragments that tumble and drop, and knocks each chip out of orbit so it falls toward the next section.
// `fall(d)` is one piece's own progress: it starts once --hp passes `d`, so the pieces go one after another.
const fall = (d) => `clamp(0, calc((var(--hp, 0) - ${d.toFixed(2)}) / ${(1 - d).toFixed(2)}), 1)`;

const polar = (r, a) => [50 + r * Math.cos(a), 50 + r * Math.sin(a)];
const arc = (r, a0, a1) => {
  const [x0, y0] = polar(r, a0);
  const [x1, y1] = polar(r, a1);
  return `M${x0.toFixed(2)} ${y0.toFixed(2)} A${r} ${r} 0 0 1 ${x1.toFixed(2)} ${y1.toFixed(2)}`;
};

// A ring drawn as separate arcs with small gaps, so it can shatter.
const BrokenRing = ({ radius, pieces, dashed = false, delay }) => (
  <g>
    {Array.from({ length: pieces }, (_, j) => {
      const step = (Math.PI * 2) / pieces;
      const a0 = j * step + 0.03;
      const a1 = (j + 1) * step - 0.03;
      const mid = (a0 + a1) / 2;
      const f = fall(delay + (j % 4) * 0.04);
      const spin = (j % 2 ? 1 : -1) * (25 + (j % 3) * 18);
      return (
        <path
          key={j}
          d={arc(radius, a0, a1)}
          fill="none"
          stroke="var(--color-white)"
          strokeOpacity="0.2"
          strokeWidth="0.35"
          strokeDasharray={dashed ? "1 1.2" : undefined}
          style={{
            transformBox: "fill-box",
            transformOrigin: "center",
            transform: `translate(calc(${f} * ${(Math.cos(mid) * 7).toFixed(1)}px), calc(${f} * ${(Math.sin(mid) * 7).toFixed(1)}px + ${f} * ${f} * 70px)) rotate(calc(${f} * ${spin}deg))`,
            opacity: `calc(1 - ${f})`,
          }}
        />
      );
    })}
  </g>
);

// One ring of chips turning around the centre. Each chip turns back the other way so it stays upright,
// which also means its fall (applied on the upright layer) always points straight down the screen.
// Hovering the orbit pauses it, so a chip can be read or clicked.
const Ring = ({ items, radius, duration, reverse = false, small = false, order = 0 }) => {
  const direction = reverse ? " reverse" : "";
  return (
    <div className="absolute inset-0 group-hover:[animation-play-state:paused]" style={{ animation: `hp-orbit ${duration}s linear infinite${direction}` }}>
      {items.map((item, i) => {
        const angle = (i / items.length) * Math.PI * 2 - Math.PI / 2;
        const f = fall(0.08 + ((i * 3 + order) % 7) * 0.07);
        const sideways = ((i + order) % 2 ? 1 : -1) * (24 + (i % 3) * 16);
        const spin = ((i + order) % 2 ? 1 : -1) * (18 + i * 9);
        return (
          <div key={item.id} className="absolute" style={{ left: `${50 + radius * Math.cos(angle)}%`, top: `${50 + radius * Math.sin(angle)}%`, transform: "translate(-50%, -50%)", opacity: `calc(1 - ${f} * ${f})` }}>
            <div className="group-hover:[animation-play-state:paused]" style={{ animation: `hp-counter ${duration}s linear infinite${direction}` }}>
              <span
                className={`inline-flex items-center gap-2 whitespace-nowrap rounded-full bg-white py-1 pl-1 font-semibold text-ocean-676 shadow-[0_10px_30px_-10px_color-mix(in_srgb,var(--color-ocean-914)_60%,transparent)] ring-1 ring-black/5 dark:bg-ocean-820 dark:text-ocean-102 dark:ring-white/10 ${small ? "pr-3 text-xs" : "pr-4 text-sm"}`}
                style={{
                  animation: `hp-pop 0.6s cubic-bezier(0.22,1,0.36,1) ${0.5 + i * 0.12}s both`,
                  translate: `calc(${f} * ${sideways}px) calc(${f} * ${f} * 620px)`,
                  rotate: `calc(${f} * ${spin}deg)`,
                }}
              >
                <TeamLogo src={item.image} name={item.name} className={`${small ? "h-6 w-6" : "h-8 w-8"} rounded-full`} />
                {shortName(item.name)}
              </span>
            </div>
          </div>
        );
      })}
    </div>
  );
};

const CommitteeOrbit = ({ committees, clubs = [], className = "" }) => (
  <div className={`group relative aspect-square w-full ${className}`} style={{ transform: "translateY(calc(var(--hp, 0) * 40px))" }} role="img" aria-label={`Committees and clubs around the chapter: ${[...committees, ...clubs].map((c) => shortName(c.name)).join(", ")}`}>
    <svg viewBox="0 0 100 100" className="absolute inset-0 h-full w-full overflow-visible" style={{ animation: "hp-ring 1s ease-out 0.2s both" }} aria-hidden="true">
      <BrokenRing radius={44} pieces={10} delay={0} />
      <BrokenRing radius={26} pieces={7} dashed delay={0.05} />
    </svg>
    <div className="absolute inset-[37%] flex items-center justify-center rounded-full bg-white shadow-[0_0_0_12px_color-mix(in_srgb,var(--color-white)_7%,transparent),0_0_0_28px_color-mix(in_srgb,var(--color-white)_4%,transparent)] dark:bg-ocean-51" style={{ animation: "hp-pop 0.8s cubic-bezier(0.22,1,0.36,1) 0.15s both", scale: "calc(1 - var(--hp, 0) * 0.12)" }}>
      <img src={logo} alt="" className="h-3/4 w-3/4 object-contain" />
    </div>
    <Ring items={committees.map((c) => ({ id: `c${c.id}`, name: c.name, image: c.logoUrl }))} radius={44} duration={70} />
    {clubs.length > 0 && <Ring items={clubs.map((c) => ({ id: `k${c.id}`, name: c.name, image: c.imageUrl }))} radius={26} duration={50} reverse small order={2} />}
  </div>
);

export default CommitteeOrbit;
