import { Link } from "react-router-dom";
import { FiArrowRight } from "react-icons/fi";
import CountUp from "../../ui/CountUp";
import { heroStats, tagline } from "../homeContent";

// The hero's text column: headline, tagline, a join button for visitors (none once signed in), and the chapter's totals once
// they have loaded. `compact` is for the phone and tablet hero, where it sits inside the video's 16:9 frame: just the
// headline (sized to the width) and the button, with the tagline and totals (HeroFacts) below the frame.
const primary = "inline-flex h-11 items-center gap-2 rounded-xl bg-white px-4 sm:h-12 sm:px-6 text-sm font-semibold text-ocean-676 transition-transform hover:-translate-y-px dark:bg-ocean-461 dark:text-ocean-910";
const primaryCompact = "inline-flex h-9 items-center gap-2 rounded-xl bg-white px-3.5 text-xs sm:h-11 sm:px-5 sm:text-sm font-semibold text-ocean-676 transition-transform hover:-translate-y-px dark:bg-ocean-461 dark:text-ocean-910";

const Join = ({ className }) => (
  <Link to="/register" className={className}>Join us <FiArrowRight className="h-4 w-4" aria-hidden="true" /></Link>
);

const Tagline = ({ className = "" }) => (
  <p className={`max-w-lg text-base leading-relaxed sm:text-lg text-ocean-114 ${className}`} style={{ animation: "hp-rise 0.8s ease-out 0.25s both" }}>{tagline}.</p>
);

const Totals = ({ stats }) => {
  const totals = heroStats(stats);
  if (totals.length === 0) return null;
  return (
    <dl className="mt-8 flex max-w-lg divide-x divide-white/15 sm:mt-10" style={{ animation: "hp-rise 0.8s ease-out 0.15s both" }}>
      {totals.map((item) => (
        <div key={item.key} className="flex flex-1 flex-col-reverse px-3 first:pl-0 sm:px-6">
          <dt className="mt-1 whitespace-nowrap text-xs font-medium text-ocean-145 sm:text-sm">{item.label}</dt>
          <dd className="text-2xl font-semibold tracking-tight text-white sm:text-3xl"><CountUp value={item.value} suffix={item.suffix} /></dd>
        </div>
      ))}
    </dl>
  );
};

// The tagline and totals, for below the compact hero's video frame.
export const HeroFacts = ({ stats }) => (
  <>
    <Tagline />
    <Totals stats={stats} />
  </>
);

const HeroIntro = ({ stats, signedIn, compact = false }) => (
  <>
    <h1 className={`font-semibold leading-[1.03] tracking-[-0.035em] ${compact ? "text-[clamp(1.6rem,7vw,3.75rem)]" : "text-[2.6rem] sm:text-6xl lg:text-[4.25rem] xl:text-[4.75rem]"}`} style={{ animation: "hp-rise 0.8s cubic-bezier(0.22,1,0.36,1) 0.1s both" }}>
      <span className="whitespace-nowrap">Learn it, build it,</span><br /><span className="text-ocean-316">then ship it.</span>
    </h1>
    {!compact && <Tagline className="mt-4 sm:mt-6" />}
    {!signedIn && (
      <div className={compact ? "mt-[3vw]" : "mt-8 sm:mt-10"} style={{ animation: "hp-rise 0.8s ease-out 0.4s both" }}>
        <Join className={compact ? primaryCompact : primary} />
      </div>
    )}
    {!compact && <Totals stats={stats} />}
  </>
);

export default HeroIntro;
