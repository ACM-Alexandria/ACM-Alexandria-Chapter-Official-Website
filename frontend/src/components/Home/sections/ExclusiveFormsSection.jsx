import { useEffect, useState } from "react";
import { FiArrowUpRight, FiChevronLeft, FiChevronRight, FiClock, FiFileText } from "react-icons/fi";
import Reveal from "../../ui/Reveal";
import { prefersReducedMotion } from "../../../hooks/useScrollFrame";
import { skeleton } from "../../ui/uiStyles";
import { summary } from "../../../utils/format";

const ROTATE_MS = 7000;
const arrow = "flex h-10 w-10 items-center justify-center rounded-full bg-white/10 text-white ring-1 ring-white/15 transition-colors hover:bg-white/20 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#7DBBE0]";

// Limited-time registrations as one navy spotlight banner under the gallery, with the lighthouse beam sweeping
// across it. With several forms the banner moves to the next one every few seconds (paused on hover or focus);
// the dots and arrows switch by hand. The button opens the form's details.
const ExclusiveFormsSection = ({ loading, forms, onShowDetails }) => {
  const [index, setIndex] = useState(0);
  const [paused, setPaused] = useState(false);
  const count = forms.length;
  const active = count ? index % count : 0;

  useEffect(() => {
    if (count < 2 || paused || prefersReducedMotion()) return undefined;
    const timer = setTimeout(() => setIndex((i) => (i + 1) % count), ROTATE_MS);
    return () => clearTimeout(timer);
  }, [index, count, paused]);

  if (!loading && count === 0) return null;
  const form = forms[active];
  const step = (by) => setIndex((active + by + count) % count);

  return (
    <section id="exclusive-forms" aria-label="Exclusive opportunities" className="mx-auto max-w-[1280px] scroll-mt-20 px-5 pt-16 sm:px-8 sm:pt-24">
      {loading ? <div className={`h-96 rounded-[2rem] ${skeleton}`} /> : (
        <Reveal>
          <div
            onMouseEnter={() => setPaused(true)}
            onMouseLeave={() => setPaused(false)}
            onFocus={() => setPaused(true)}
            onBlur={() => setPaused(false)}
            className="relative overflow-hidden rounded-[2rem] bg-[#0F3A5A] text-white dark:bg-[#0F2233] dark:ring-1 dark:ring-white/10"
          >
            <span aria-hidden="true" className="pointer-events-none absolute inset-y-0 left-0 w-1/4 bg-white/[0.06]" style={{ animation: "hp-sweep 9s ease-in-out 1s infinite" }} />
            <div key={form.id} className="relative grid items-center gap-8 p-6 sm:p-10 md:grid-cols-[1.1fr_0.9fr] md:gap-10 lg:gap-12 lg:p-14" style={{ animation: "hp-fade 600ms ease-out" }}>
              <div>
                <p className="flex items-center gap-2 text-sm font-semibold text-[#7DBBE0]">
                  <FiClock className="h-4 w-4" aria-hidden="true" />Open for a limited time{count > 1 && <span className="text-[#CFE6F5]/70">· {active + 1} of {count}</span>}
                </p>
                <h2 className="mt-4 text-3xl font-semibold tracking-tight sm:text-5xl" style={{ textWrap: "balance" }}>{form.title}</h2>
                <p className="mt-4 max-w-xl text-lg leading-relaxed text-[#CFE6F5]">{summary(form.description || "", 220) || "Limited seats. Secure your spot before registration closes."}</p>
                <button
                  type="button"
                  onClick={() => onShowDetails(form.id)}
                  className="mt-8 inline-flex h-11 items-center gap-2 rounded-xl bg-white px-5 text-sm font-semibold text-[#205E85] transition-transform hover:-translate-y-px focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#7DBBE0] focus-visible:ring-offset-2 focus-visible:ring-offset-[#0F3A5A]"
                >
                  View details and register <FiArrowUpRight className="h-4 w-4" aria-hidden="true" />
                </button>
              </div>
              <div className="aspect-[16/10] overflow-hidden rounded-3xl bg-[#16364C] ring-1 ring-white/10">
                {form.imageUrl
                  ? <img src={form.imageUrl} alt="" className="h-full w-full object-cover" loading="lazy" />
                  : <span className="flex h-full w-full items-center justify-center"><FiFileText className="h-14 w-14 text-[#7DBBE0]/50" aria-hidden="true" /></span>}
              </div>
            </div>
            {count > 1 && (
              <div className="relative flex items-center justify-between gap-4 px-6 pb-6 sm:px-10 sm:pb-8 lg:px-14">
                <div className="flex items-center gap-2">
                  {forms.map((f, i) => (
                    <button
                      key={f.id}
                      type="button"
                      onClick={() => setIndex(i)}
                      aria-label={`Show ${f.title}`}
                      aria-current={i === active}
                      className={`h-2 rounded-full transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#7DBBE0] ${i === active ? "w-7 bg-white" : "w-2 bg-white/30 hover:bg-white/50"}`}
                    />
                  ))}
                </div>
                <div className="flex gap-2">
                  <button type="button" onClick={() => step(-1)} aria-label="Previous form" className={arrow}><FiChevronLeft className="h-5 w-5" aria-hidden="true" /></button>
                  <button type="button" onClick={() => step(1)} aria-label="Next form" className={arrow}><FiChevronRight className="h-5 w-5" aria-hidden="true" /></button>
                </div>
              </div>
            )}
          </div>
        </Reveal>
      )}
    </section>
  );
};

export default ExclusiveFormsSection;
