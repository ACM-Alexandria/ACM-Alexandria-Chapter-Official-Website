import { Link } from "react-router-dom";
import { FiArrowRight } from "react-icons/fi";
import CountUp from "../../ui/CountUp";
import { heroStats, tagline } from "../homeContent";

// The hero's text column: chapter badge, headline, tagline, the two buttons, and the chapter's totals once
// they have loaded.
const HeroIntro = ({ stats }) => {
  const totals = heroStats(stats);
  return (
  <>
    <p className="inline-flex items-center gap-2 rounded-full bg-white/10 px-3.5 py-1.5 text-xs font-medium sm:text-sm text-[#BFE0F5] ring-1 ring-white/15" style={{ animation: "hp-rise 0.7s ease-out both" }}>
      <span className="h-2 w-2 rounded-full bg-[#7DBBE0]" aria-hidden="true" />ACM student chapter · Alexandria
    </p>
    <h1 className="mt-5 text-[2.6rem] font-semibold sm:mt-6 sm:text-6xl leading-[1.03] tracking-[-0.035em] lg:text-[4.25rem] xl:text-[4.75rem]" style={{ animation: "hp-rise 0.8s cubic-bezier(0.22,1,0.36,1) 0.1s both" }}>
      <span className="whitespace-nowrap">Learn it, build it,</span><br /><span className="text-[#7DBBE0]">then ship it.</span>
    </h1>
    <p className="mt-4 max-w-lg text-base leading-relaxed sm:mt-6 sm:text-lg text-[#CFE6F5]" style={{ animation: "hp-rise 0.8s ease-out 0.25s both" }}>{tagline}.</p>
    <div className="mt-8 flex flex-wrap gap-3 sm:mt-10" style={{ animation: "hp-rise 0.8s ease-out 0.4s both" }}>
      <a href="#people" className="inline-flex h-11 items-center gap-2 rounded-xl bg-white px-4 sm:h-12 sm:px-6 text-sm font-semibold text-[#205E85] transition-transform hover:-translate-y-px dark:bg-[#4B98C8] dark:text-[#081826]">Meet the chapter <FiArrowRight className="h-4 w-4" aria-hidden="true" /></a>
      <Link to="/boards" className="inline-flex h-11 items-center rounded-xl border border-white/25 px-4 sm:h-12 sm:px-6 text-sm font-semibold text-white transition-colors hover:bg-white/10">See past boards</Link>
    </div>
    {totals.length > 0 && (
      <dl className="mt-8 flex max-w-lg divide-x divide-white/15 sm:mt-10" style={{ animation: "hp-rise 0.8s ease-out 0.15s both" }}>
        {totals.map((item) => (
          <div key={item.key} className="flex flex-1 flex-col-reverse px-3 first:pl-0 sm:px-6">
            <dt className="mt-1 whitespace-nowrap text-xs font-medium text-[#BFE0F5] sm:text-sm">{item.label}</dt>
            <dd className="text-2xl font-semibold tracking-tight text-white sm:text-3xl"><CountUp value={item.value} suffix={item.suffix} /></dd>
          </div>
        ))}
      </dl>
    )}
  </>
  );
};

export default HeroIntro;
