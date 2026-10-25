import { useCallback, useRef } from "react";
import { Link } from "react-router-dom";
import { FiArrowUpRight } from "react-icons/fi";
import CountUp from "../../ui/CountUp";
import { useScrollFrame, clamp01, prefersReducedMotion } from "../../../hooks/useScrollFrame";
import { muted, skeleton } from "../../ui/uiStyles";

// Intro with the total on one side; one featured item and three smaller ones on the other.
const ActivitySection = ({ id, icon, label, count, countLabel, text, to, cta, flip = false, tone = "light", featured, more = [], loading = false }) => {
  const blue = tone === "blue";
  const Icon = icon;
  const ref = useRef(null);
  // 0 when the section's top meets the bottom of the screen, 1 once it has risen to the upper part of the screen.
  const onFrame = useCallback(() => {
    const node = ref.current;
    if (!node) return;
    const progress = prefersReducedMotion() ? 1 : clamp01((window.innerHeight - node.getBoundingClientRect().top) / (window.innerHeight * 0.85));
    node.style.setProperty("--sp", progress.toFixed(4));
  }, []);
  useScrollFrame(onFrame);
  return (
    <section ref={ref} id={id} style={{ "--sp": 0 }} className={`scroll-mt-20 py-16 sm:py-24 ${blue ? "bg-[#0F3A5A] text-white dark:bg-[#081826]" : tone === "white" ? "bg-white dark:bg-[#0F2233]" : ""}`}>
      <div className="mx-auto grid max-w-[1280px] gap-10 px-5 sm:gap-12 sm:px-8 lg:grid-cols-[0.7fr_1.3fr] lg:items-start">
        <div className={`lg:sticky lg:top-28 ${flip ? "lg:order-2" : ""}`} style={{ transform: "translateY(calc((1 - var(--sp)) * 48px))", opacity: "calc(0.15 + var(--sp) * 0.85)" }}>
          <p className={`flex items-center gap-2 text-sm font-semibold ${blue ? "text-[#7DBBE0]" : "text-[#2A7AB0] dark:text-[#7DBBE0]"}`}><Icon className="h-4 w-4" aria-hidden="true" />{label}</p>
          <p className={`mt-4 text-6xl font-semibold tracking-tight sm:text-7xl ${blue ? "text-white" : "text-[#205E85] dark:text-white"}`}><CountUp value={count} /></p>
          <p className={`mt-1 text-lg font-semibold ${blue ? "text-white" : "text-slate-900 dark:text-white"}`}>{countLabel}</p>
          <p className={`mt-4 max-w-sm text-lg leading-relaxed ${blue ? "text-[#CFE6F5]" : muted}`}>{text}</p>
          <Link to={to} className={`mt-7 inline-flex h-11 items-center gap-2 rounded-xl px-5 text-sm font-semibold transition-transform hover:-translate-y-px ${blue ? "bg-white text-[#205E85]" : "bg-[#205E85] text-white dark:bg-[#4B98C8] dark:text-[#081826]"}`}>{cta} <FiArrowUpRight className="h-4 w-4" aria-hidden="true" /></Link>
        </div>
        <div className={`flex flex-col gap-5 ${flip ? "lg:order-1" : ""}`}>
          {loading ? (
            <>
              <div className={`aspect-[16/9] rounded-[2rem] ${skeleton}`} />
              <div className="grid grid-cols-3 gap-4 sm:gap-5">
                {[0, 1, 2].map((i) => <div key={i} className={`aspect-[4/3] rounded-3xl ${skeleton}`} />)}
              </div>
            </>
          ) : (
            <>
              {featured}
              {more.length > 0 && <div className="-mx-5 flex snap-x snap-mandatory gap-4 overflow-x-auto px-5 pb-2 [scrollbar-width:none] sm:mx-0 sm:grid sm:grid-cols-3 sm:gap-5 sm:overflow-visible sm:px-0 sm:pb-0">{more}</div>}
            </>
          )}
        </div>
      </div>
    </section>
  );
};

export default ActivitySection;
