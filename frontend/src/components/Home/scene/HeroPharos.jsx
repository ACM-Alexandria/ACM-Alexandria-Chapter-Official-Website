import { useEffect, useMemo, useRef, useState } from "react";
import { Link } from "react-router-dom";
import { FiArrowRight } from "react-icons/fi";
import logo from "../../../assets/logo/acm-logo-no-bg.png";
import Pharos from "./Pharos";
import HeroIntro from "./HeroIntro";
import NightSky from "../../ui/NightSky";
import CommitteeOrbit from "./CommitteeOrbit";
import PharosFinale from "./PharosFinale";
import { pharosFrameClass, pharosLight } from "./pharosGeometry";
import { joinCta } from "../homeContent";
import { shortName } from "../../../utils/format";
import { clamp01, prefersReducedMotion } from "../../../hooks/useScrollFrame";
import TeamLogo from "../../ui/TeamLogo";

const words = "Alexandria lit the way for the ancient world. We light the way into computing.".split(" ");
const stars = Array.from({ length: 60 }, (_, i) => ({ x: (i * 41.3) % 100, y: (i * 23.7) % 62, size: i % 6 === 0 ? 2.5 : 1.5, delay: (i % 5) * 0.8 }));
// Where planets land on the lighthouse, in the drawing's own units (viewBox 200 x 400): two planets per tier,
// from the platform up to the top tier, each pair spread either side of the centre line.
const tierY = [362, 286, 170, 100];
const tierSpread = [40, 30, 22, 14];
const TIERS = tierY.length;
const PLANETS = TIERS * 2;
// Pair k starts falling here; each fall takes FALL of the scroll, then the pair merges into its tier over MERGE.
const tierStart = (k, side) => 0.08 + k * 0.065 + side * 0.018;
const FALL = 0.1;
const MERGE = 0.045;
// The beam turns from BEAM_START (pointing up and left) down to the join button as --sweep goes 0→1, so it comes to
// rest exactly on the button. Something counts as reached once the beam's edge, BEAM_HALF degrees off centre, passes it.
const BEAM_START = 208;
const BEAM_HALF = 6;
const angleTo = (from, el, box) => {
  const r = el.getBoundingClientRect();
  const deg = (Math.atan2(r.top + r.height / 2 - box.top - from.y, r.left + r.width / 2 - box.left - from.x) * 180) / Math.PI;
  return deg < 0 ? deg + 360 : deg;
};

const range = (p, start, length) => clamp01((p - start) / length);
const easeInOut = (t) => (t < 0.5 ? 2 * t * t : 1 - (-2 * t + 2) ** 2 / 2);

// One ring piece's own break progress, read from --brk so the pieces crack one after another.
const breakAt = (d) => `clamp(0, calc((var(--brk) - ${d.toFixed(2)}) / ${(1 - d).toFixed(2)}), 1)`;
const polar = (r, a) => [50 + r * Math.cos(a), 50 + r * Math.sin(a)];
const BrokenRing = ({ radius, pieces, dashed = false, delay }) => (
  <g>
    {Array.from({ length: pieces }, (_, j) => {
      const step = (Math.PI * 2) / pieces;
      const a0 = j * step + 0.03;
      const a1 = (j + 1) * step - 0.03;
      const mid = (a0 + a1) / 2;
      const [x0, y0] = polar(radius, a0);
      const [x1, y1] = polar(radius, a1);
      const f = breakAt(delay + (j % 4) * 0.05);
      const spin = (j % 2 ? 1 : -1) * (25 + (j % 3) * 18);
      return (
        <path
          key={j}
          d={`M${x0.toFixed(2)} ${y0.toFixed(2)} A${radius} ${radius} 0 0 1 ${x1.toFixed(2)} ${y1.toFixed(2)}`}
          fill="none"
          stroke="#FFFFFF"
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

// The hero and the Pharos are one pinned scene, so nothing jumps between sections. At the top it is the
// hero: committees and clubs orbit the logo. Scrolling cracks the rings while the blue turns to night and a
// blueprint of the lighthouse draws itself. The same planets then fall out of orbit in pairs onto the
// lighthouse, ground floor first, and each pair merges into its tier as the tier fills in, so the chapter's
// teams build the Pharos. Its flame ignites, and the beam lights the closing line word by word before coming
// to rest on the join button.
// One animation loop reads the clock (for the orbit) and the scroll position (for everything else),
// so a planet always falls from exactly where it is in its orbit.
const HeroPharos = ({ committees, clubs, stats, cta = joinCta }) => {
  const [reduced] = useState(prefersReducedMotion);
  const stageRef = useRef(null);
  const sceneRef = useRef(null);
  const orbitRef = useRef(null);
  const introRef = useRef(null);
  const ctaRef = useRef(null);
  const lighthouseRef = useRef(null);
  const wordRefs = useRef([]);
  const rippleRefs = useRef([]);
  const chipRefs = useRef([]);

  const planets = useMemo(() => [
    ...committees.map((c, i) => ({ id: `c${c.id}`, name: c.name, image: c.logoUrl, radius: 0.44, period: 70, dir: 1, base: (i / committees.length) * Math.PI * 2 - Math.PI / 2, small: false })),
    ...clubs.map((c, i) => ({ id: `k${c.id}`, name: c.name, image: c.imageUrl, radius: 0.26, period: 50, dir: -1, base: (i / clubs.length) * Math.PI * 2 - Math.PI / 2, small: true })),
  ].slice(0, PLANETS).map((planet, i) => ({ ...planet, tier: Math.floor(i / 2), side: i % 2, spin: (i % 2 ? 1 : -1) * (25 + i * 8) })), [committees, clubs]);

  useEffect(() => {
    if (reduced) return undefined;
    let frame;
    const started = performance.now();
    const loop = (now) => {
      const stage = stageRef.current;
      const scene = sceneRef.current;
      const orbit = orbitRef.current;
      if (stage && scene && orbit) {
        const rect = stage.getBoundingClientRect();
        const distance = rect.height - window.innerHeight;
        const p = clamp01(distance > 0 ? -rect.top / distance : 0);
        const v = {
          brk: range(p, 0.02, 0.12),
          out: range(p, 0.02, 0.1),
          night: range(p, 0.04, 0.14),
          words: range(p, 0.13, 0.1),
          plan: range(p, 0.05, 0.12),
          logo: range(p, 0.08, 0.1),
          ignite: range(p, 0.43, 0.06),
          sweep: range(p, 0.5, 0.36),
        };
        Object.entries(v).forEach(([key, value]) => stage.style.setProperty(`--${key}`, value.toFixed(4)));
        if (introRef.current) introRef.current.style.pointerEvents = v.out > 0.5 ? "none" : "auto";

        const box = scene.getBoundingClientRect();
        const tower = lighthouseRef.current.getBoundingClientRect();

        // Light each word, and then the button, only once the beam actually sweeps over it. Near the end of the
        // scroll everything finishes lighting anyway, so nothing stays dark if a small screen puts it out of reach.
        const lantern = { x: tower.left - box.left + pharosLight.x * tower.width, y: tower.top - box.top + pharosLight.y * tower.height };
        const button = ctaRef.current?.firstElementChild;
        const beamEnd = button ? angleTo(lantern, button, box) : 156;
        const beam = BEAM_START - v.sweep * (BEAM_START - beamEnd);
        stage.style.setProperty("--beam", `${beam.toFixed(2)}deg`);
        const reached = (el, fade) => Math.max(clamp01((angleTo(lantern, el, box) + BEAM_HALF - beam) / fade), range(p, 0.9, 0.05));
        wordRefs.current.forEach((el) => {
          if (el) el.style.opacity = (0.12 + 0.88 * (v.ignite > 0 ? reached(el, 5) : 0)).toFixed(3);
        });
        const cta = range(v.sweep, 0.9, 0.1);
        stage.style.setProperty("--cta", cta.toFixed(4));
        if (ctaRef.current) ctaRef.current.style.pointerEvents = cta > 0.5 ? "auto" : "none";
        const ring = orbit.getBoundingClientRect();
        const cx = ring.left - box.left + ring.width / 2;
        const cy = ring.top - box.top + ring.height / 2;
        const seconds = (now - started) / 1000;
        // How much of each tier its planets have merged into; a tier with no planets builds on its own schedule.
        const tierDone = Array.from({ length: TIERS }, (_, k) => {
          const members = planets.filter((planet) => planet.tier === k);
          if (!members.length) return range(p, tierStart(k, 1) + FALL, MERGE);
          return members.reduce((sum, planet) => sum + range(p, tierStart(k, planet.side) + FALL, MERGE), 0) / members.length;
        });
        stage.style.setProperty("--build", (tierDone.reduce((sum, done) => sum + done, 0) / TIERS).toFixed(4));

        planets.forEach((planet, i) => {
          const el = chipRefs.current[i];
          if (!el) return;
          const angle = planet.base + planet.dir * seconds * ((Math.PI * 2) / planet.period);
          const ox = cx + planet.radius * ring.width * Math.cos(angle);
          const oy = cy + planet.radius * ring.width * Math.sin(angle);
          const tx = tower.left - box.left + ((100 + (planet.side ? 1 : -1) * tierSpread[planet.tier]) / 200) * tower.width;
          const ty = tower.top - box.top + (tierY[planet.tier] / 400) * tower.height;
          const start = tierStart(planet.tier, planet.side);
          const f = easeInOut(range(p, start, FALL));
          const merge = range(p, start + FALL, MERGE);
          const arc = Math.sin(Math.PI * f);
          const x = ox + (tx - ox) * f;
          const y = oy + (ty - oy) * f - arc * 60;
          el.style.transform = `translate3d(${x.toFixed(1)}px, ${y.toFixed(1)}px, 0) translate(-50%, -50%) rotate(${(arc * planet.spin).toFixed(1)}deg) scale(${(1 - 0.15 * f - 0.6 * merge).toFixed(3)})`;
          el.style.opacity = (1 - merge).toFixed(3);
          const ripple = rippleRefs.current[i];
          if (ripple) {
            ripple.style.transform = `translate3d(${tx.toFixed(1)}px, ${ty.toFixed(1)}px, 0) translate(-50%, -50%) scale(${(0.4 + merge * 2.6).toFixed(3)})`;
            ripple.style.opacity = merge > 0 && merge < 1 ? (0.9 * (1 - merge)).toFixed(3) : "0";
          }
        });
      }
      frame = requestAnimationFrame(loop);
    };
    frame = requestAnimationFrame(loop);
    return () => cancelAnimationFrame(frame);
  }, [reduced, planets]);

  if (reduced) {
    return (
      <>
        <section id="top" className="relative overflow-hidden bg-[#0F3A5A] text-white dark:bg-[#081826]">
          <div className="relative mx-auto grid max-w-[1280px] items-center gap-14 px-5 pb-20 pt-32 sm:px-8 lg:grid-cols-[1.05fr_1fr] lg:pb-28 lg:pt-40">
            <div><HeroIntro stats={stats} /></div>
            <CommitteeOrbit committees={committees} clubs={clubs} className="mx-auto max-w-[560px]" />
          </div>
        </section>
        <PharosFinale chips={planets} cta={cta} />
      </>
    );
  }

  return (
    <section
      ref={stageRef}
      id="top"
      aria-label="ACM Alexandria"
      className="relative h-[520vh] bg-[#0F3A5A] text-white dark:bg-[#081826]"
      style={{ "--brk": 0, "--out": 0, "--night": 0, "--words": 0, "--plan": 0, "--build": 0, "--beam": `${BEAM_START}deg`, "--logo": 0, "--ignite": 0, "--sweep": 0, "--cta": 0 }}
    >
      <div ref={sceneRef} className="sticky top-0 h-screen overflow-hidden">
        <div aria-hidden="true" className="pointer-events-none absolute inset-0 dark:hidden" style={{ opacity: "calc(0.08 * (1 - var(--night)))", backgroundImage: "radial-gradient(#ffffff 1px, transparent 1px)", backgroundSize: "28px 28px" }} />
        <NightSky />
        <div aria-hidden="true" className="absolute inset-0 bg-[linear-gradient(180deg,#081826_0%,#06121D_45%,#0B2236_78%,#0F3A5A_100%)]" style={{ opacity: "var(--night)" }} />
        <div aria-hidden="true" className="absolute inset-0" style={{ opacity: "var(--night)" }}>
          {stars.map((s, i) => (
            <span key={i} className="absolute rounded-full bg-[#D3E9F7]" style={{ left: `${s.x}%`, top: `${s.y}%`, width: s.size, height: s.size, animation: `hp-twinkle ${3.5 + s.delay}s ease-in-out ${s.delay}s infinite` }} />
          ))}
        </div>

        <div ref={lighthouseRef} className={pharosFrameClass} style={{ aspectRatio: "1 / 2" }}>
          <div
            aria-hidden="true"
            className="pointer-events-none absolute h-[106vmax] w-[150vmax] origin-left"
            style={{
              left: `${pharosLight.x * 100}%`,
              top: `${pharosLight.y * 100}%`,
              marginTop: "-53vmax",
              transform: "rotate(var(--beam))",
              opacity: "calc(var(--ignite) * 0.9)",
              clipPath: "polygon(0 49.4%, 100% 32%, 100% 68%, 0 50.6%)",
              background: "linear-gradient(90deg, rgba(255,236,170,0.75) 0%, rgba(255,226,140,0.28) 35%, rgba(255,226,140,0) 85%)",
            }}
          />
          <Pharos plan="var(--plan)" build="var(--build)" light="var(--ignite)" className="relative h-full w-full" />
        </div>

        <div aria-hidden="true" className="absolute inset-x-0 bottom-0 h-[16%] overflow-hidden" style={{ transform: "translateY(calc((1 - var(--night)) * 100%))" }}>
          {[0, 1, 2].map((layer) => (
            <svg key={layer} viewBox="0 0 1200 60" preserveAspectRatio="none" className="absolute bottom-0 h-full w-[200%]" style={{ opacity: 0.35 + layer * 0.25, bottom: `${-layer * 18}%`, animation: `hp-wave ${18 - layer * 4}s linear infinite` }}>
              <path d="M0 30 Q 75 10 150 30 T 300 30 T 450 30 T 600 30 T 750 30 T 900 30 T 1050 30 T 1200 30 V 60 H 0 Z" fill={["#1D5A85", "#174A70", "#0F3A5A"][layer]} />
            </svg>
          ))}
        </div>

        <div className="absolute inset-0 mx-auto flex max-w-[1280px] flex-col justify-center px-5 pb-[14vh] pt-[16vh] sm:px-8" style={{ opacity: "var(--words)" }}>
          <p className="max-w-[15ch] text-2xl font-semibold leading-[1.08] tracking-[-0.035em] sm:text-4xl md:max-w-[16ch] md:text-5xl lg:max-w-[18ch] lg:text-7xl">
            {words.map((word, i) => (
              <span key={i} ref={(el) => { wordRefs.current[i] = el; }} className="inline-block pr-[0.25em] text-[#FFF4CC]" style={{ opacity: 0.12 }}>{word}</span>
            ))}
          </p>
          <div ref={ctaRef} className="pointer-events-none mt-6 flex max-w-[62%] flex-wrap items-center gap-3 sm:mt-10 sm:gap-4 lg:max-w-none" style={{ opacity: "var(--cta)", transform: "translateY(calc((1 - var(--cta)) * 24px))" }}>
            <Link to={cta.to} className="inline-flex h-11 items-center gap-2 rounded-xl bg-[#FFE7A3] px-5 text-sm sm:h-12 sm:px-7 font-semibold text-[#06121D] shadow-[0_0_40px_rgba(255,220,130,0.45)] transition-transform hover:-translate-y-px">
              {cta.label} <FiArrowRight className="h-4 w-4" aria-hidden="true" />
            </Link>
            <span className="text-xs text-[#BFE0F5] sm:text-sm">{cta.note}</span>
          </div>
        </div>

        <div className="relative mx-auto grid h-full max-w-[1280px] content-center items-center gap-8 px-5 pt-20 sm:px-8 lg:grid-cols-[1.05fr_1fr] lg:gap-10 lg:pt-16">
          <div ref={introRef} style={{ opacity: "calc(1 - var(--out))", transform: "translateY(calc(var(--out) * -90px))" }}>
            <HeroIntro stats={stats} />
          </div>
          <div ref={orbitRef} className="relative mx-auto aspect-square w-full max-w-[min(560px,86vw,calc(100svh-590px))] lg:max-w-[min(560px,58vh)]" aria-hidden="true">
            <svg viewBox="0 0 100 100" className="absolute inset-0 h-full w-full overflow-visible" style={{ animation: "hp-ring 1s ease-out 0.2s both" }}>
              <BrokenRing radius={44} pieces={10} delay={0} />
              <BrokenRing radius={26} pieces={7} dashed delay={0.05} />
            </svg>
            <div className="absolute inset-[37%]" style={{ opacity: "calc(1 - var(--logo))", scale: "calc(1 - var(--logo) * 0.4)" }}>
              <div className="flex h-full w-full items-center justify-center rounded-full bg-white shadow-[0_0_0_12px_rgba(255,255,255,0.07),0_0_0_28px_rgba(255,255,255,0.04)] dark:bg-[#E9F4FB]" style={{ animation: "hp-pop 0.8s cubic-bezier(0.22,1,0.36,1) 0.15s both" }}>
                <img src={logo} alt="" className="h-3/4 w-3/4 object-contain" />
              </div>
            </div>
          </div>
        </div>

        <div aria-hidden="true" className="pointer-events-none absolute inset-0">
          {planets.map((planet, i) => (
            <span key={planet.id} ref={(el) => { rippleRefs.current[i] = el; }} className="absolute left-0 top-0 h-10 w-10 rounded-full border-2 sm:h-16 sm:w-16 border-[#9FD0EE] opacity-0 shadow-[0_0_18px_rgba(159,208,238,0.7)]" />
          ))}
        </div>

        <ul aria-label="Committees and clubs" className="pointer-events-none absolute inset-0">
          {planets.map((planet, i) => (
            <li key={planet.id} ref={(el) => { chipRefs.current[i] = el; }} className="absolute left-0 top-0 will-change-transform">
              <span
                className={`relative inline-flex items-center gap-2 whitespace-nowrap rounded-full bg-white py-1 pl-1 font-semibold text-[#205E85] shadow-[0_10px_30px_-10px_rgba(4,24,40,0.6)] ring-1 ring-black/5 dark:bg-[#12304A] dark:text-[#D3E9F7] dark:ring-white/10 pr-1 ${planet.small ? "text-xs sm:pr-3" : "text-sm sm:pr-4"}`}
                style={{ animation: `hp-pop 0.6s cubic-bezier(0.22,1,0.36,1) ${0.5 + i * 0.12}s both` }}
              >
                <TeamLogo src={planet.image} name={planet.name} className={`relative ${planet.small ? "h-6 w-6" : "h-8 w-8"} rounded-full`} />
                <span className="relative hidden sm:inline">{shortName(planet.name)}</span>
              </span>
            </li>
          ))}
        </ul>
      </div>
    </section>
  );
};

export default HeroPharos;
