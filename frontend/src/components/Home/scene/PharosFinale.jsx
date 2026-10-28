import { useCallback, useRef } from "react";
import { FiArrowRight } from "react-icons/fi";
import Pharos from "./Pharos";
import { pharosFrameClass, pharosLight } from "./pharosGeometry";
import { meetCta } from "../homeContent";
import { shortName } from "../../../utils/format";
import { useScrollFrame, clamp01 } from "../../../hooks/useScrollFrame";
import TeamLogo from "../../ui/TeamLogo";

// The lighthouse in the still scene is sized from its own screen, which is shorter than the viewport. On phones the
// screen is shorter still, with no empty sky above, so the lighthouse takes more of its height.
const stillFrameClass = "absolute bottom-[14%] right-[-4%] h-[78%] sm:bottom-[13%] sm:right-[4%] sm:h-[66%]";

const sentence = "Alexandria lit the way for the ancient world. We light the way into computing.";
const words = sentence.split(" ");
const stars = Array.from({ length: 60 }, (_, i) => ({ x: (i * 41.3) % 100, y: (i * 23.7) % 62, size: i % 6 === 0 ? 2.5 : 1.5, delay: (i % 5) * 0.8 }));

// Sky slots for the committee and club chips, ordered from the lighthouse outward (right to left),
// which is the order the beam reaches them as it sweeps down.
const slots = [[60, 12], [52, 19], [44, 11], [36, 18], [28, 11], [20, 18], [12, 11], [4, 18]];
const landing = (i) => `clamp(0, calc((var(--arrive) - ${(i * 0.07).toFixed(2)}) / 0.5), 1)`;
const chipLit = (i) => `clamp(0, calc((var(--sweep) - ${(0.04 + i * 0.055).toFixed(3)}) * 12), 1)`;

// The Pharos scene under the hero. The hero's orbiting committees drift out of their orbit and float down
// into this night sky; the screen pins while the lighthouse rises tier by tier and its flame ignites. The
// beam then sweeps down, lighting each committee in the sky and then each word of the line, and comes to
// rest on the join button. Every step follows scroll position and reverses on the way up. With `still` (tablets,
// phones, reduced motion) it is one short screen showing the finished scene without the committee chips.
const PharosFinale = ({ chips = [], still = false }) => {
  const stageRef = useRef(null);

  const onFrame = useCallback(() => {
    const node = stageRef.current;
    if (!node) return;
    const rect = node.getBoundingClientRect();
    const distance = rect.height - window.innerHeight;
    const progress = still ? 1 : clamp01(distance > 0 ? -rect.top / distance : 1);
    node.style.setProperty("--arrive", clamp01(progress / 0.3).toFixed(4));
    node.style.setProperty("--build", clamp01(progress / 0.32).toFixed(4));
    node.style.setProperty("--ignite", clamp01((progress - 0.3) / 0.08).toFixed(4));
    node.style.setProperty("--sweep", clamp01((progress - 0.38) / 0.46).toFixed(4));
    node.style.setProperty("--cta", clamp01((progress - 0.84) / 0.1).toFixed(4));
  }, [still]);
  useScrollFrame(onFrame);

  return (
    <section ref={stageRef} aria-label="Join ACM Alexandria" className={`relative bg-ocean-794 dark:bg-ocean-910 ${still ? "h-[23rem] sm:h-[min(100svh,40rem)]" : "h-[360vh]"}`} style={{ "--arrive": 0, "--build": 0, "--ignite": 0, "--sweep": 0, "--cta": 0 }}>
      <div className={`sticky top-0 overflow-hidden ${still ? "h-full" : "h-screen"} bg-[linear-gradient(180deg,#0F3A5A_0%,#0A2740_20%,#06121D_48%,#0B2236_78%,#0F3A5A_100%)] dark:bg-[linear-gradient(180deg,#081826_0%,#06121D_42%,#0B2236_78%,#0F3A5A_100%)]`}>
        <div aria-hidden="true" className="absolute inset-0">
          {stars.map((s, i) => (
            <span key={i} className="absolute rounded-full bg-ocean-102" style={{ left: `${s.x}%`, top: `${s.y}%`, width: s.size, height: s.size, animation: `hp-twinkle ${3.5 + s.delay}s ease-in-out ${s.delay}s infinite`, opacity: "calc(1 - var(--ignite) * 0.4)" }} />
          ))}
        </div>

        {!still && <ul aria-label="Committees and clubs" className="absolute inset-0">
          {chips.slice(0, slots.length).map((chip, i) => (
            <li
              key={chip.id}
              className="absolute"
              style={{
                left: `${slots[i][0]}%`,
                top: `${slots[i][1]}%`,
                opacity: `calc(min(1, ${landing(i)} * 3) * (0.4 + 0.6 * ${chipLit(i)}))`,
                transform: `translateY(calc((1 - ${landing(i)}) * (1 - ${landing(i)}) * -${36 + (i % 3) * 10}vh)) rotate(calc((1 - ${landing(i)}) * ${(i % 2 ? 1 : -1) * (20 + i * 6)}deg))`,
              }}
            >
              <span className="relative inline-flex items-center gap-2 whitespace-nowrap rounded-full bg-ocean-820 py-1 pl-1 pr-3 text-xs font-semibold text-ocean-102 ring-1 ring-white/10 sm:pr-4 sm:text-sm">
                <span className="absolute -inset-1 rounded-full bg-gold-180/25 blur-md" style={{ opacity: chipLit(i) }} aria-hidden="true" />
                <TeamLogo src={chip.image} name={chip.name} className="relative h-6 w-6 rounded-full sm:h-7 sm:w-7" />
                <span className="relative">{shortName(chip.name)}</span>
              </span>
            </li>
          ))}
        </ul>}

        <div className={still ? stillFrameClass : pharosFrameClass} style={{ aspectRatio: "1 / 2" }}>
          <div
            aria-hidden="true"
            className="pointer-events-none absolute h-[106vmax] w-[150vmax] origin-left"
            style={{
              left: `${pharosLight.x * 100}%`,
              top: `${pharosLight.y * 100}%`,
              marginTop: "-53vmax",
              transform: "rotate(calc(208deg - var(--sweep) * 42deg))",
              opacity: "calc(var(--ignite) * 0.9)",
              clipPath: "polygon(0 49.4%, 100% 32%, 100% 68%, 0 50.6%)",
              background: "linear-gradient(90deg, color-mix(in srgb, var(--color-gold-167) 75%, transparent) 0%, color-mix(in srgb, var(--color-gold-225) 28%, transparent) 35%, color-mix(in srgb, var(--color-gold-225) 0%, transparent) 85%)",
            }}
          />
          <Pharos build="var(--build)" light="var(--ignite)" className="relative h-full w-full" />
        </div>

        <div aria-hidden="true" className="absolute inset-x-0 bottom-0 h-[16%] overflow-hidden">
          {[0, 1, 2].map((layer) => (
            <svg key={layer} viewBox="0 0 1200 60" preserveAspectRatio="none" className="absolute bottom-0 h-full w-[200%]" style={{ opacity: 0.35 + layer * 0.25, bottom: `${-layer * 18}%`, animation: `hp-wave ${18 - layer * 4}s linear infinite` }}>
              <path d="M0 30 Q 75 10 150 30 T 300 30 T 450 30 T 600 30 T 750 30 T 900 30 T 1050 30 T 1200 30 V 60 H 0 Z" fill={["var(--color-ocean-682)", "var(--color-ocean-735)", "var(--color-ocean-794)"][layer]} />
            </svg>
          ))}
        </div>

        <div className={`relative mx-auto flex h-full max-w-[1280px] flex-col px-5 sm:px-8 ${still ? "justify-end pb-[18%] sm:justify-center sm:pb-[14vh] sm:pt-[16vh]" : "justify-center pb-[14vh] pt-[16vh]"}`}>
          <p className="max-w-[15ch] text-2xl font-semibold leading-[1.08] tracking-[-0.035em] sm:text-4xl md:max-w-[16ch] md:text-5xl lg:max-w-[18ch] lg:text-7xl">
            {words.map((word, i) => (
              <span key={i} className="inline-block pr-[0.25em] text-gold-100" style={{ opacity: `calc(0.12 + 0.88 * clamp(0, calc((var(--sweep) - 0.45) * ${((words.length + 2) / 0.55).toFixed(2)} - ${i}), 1))` }}>{word}</span>
            ))}
          </p>
          <div className="mt-6 flex max-w-[62%] flex-wrap items-center gap-3 sm:mt-10 sm:gap-4 lg:max-w-none" style={{ opacity: "var(--cta)", transform: "translateY(calc((1 - var(--cta)) * 24px))" }}>
            <a href={meetCta.href} className="inline-flex h-11 items-center gap-2 rounded-xl bg-gold-180 px-5 text-sm sm:h-12 sm:px-7 font-semibold text-ocean-931 shadow-[0_0_40px_color-mix(in_srgb,var(--color-gold-245)_45%,transparent)] transition-transform hover:-translate-y-px">
              {meetCta.label} <FiArrowRight className="h-4 w-4" aria-hidden="true" />
            </a>
          </div>
        </div>
      </div>
    </section>
  );
};

export default PharosFinale;
