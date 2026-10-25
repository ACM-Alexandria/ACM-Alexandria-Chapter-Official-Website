import { useCallback, useRef } from "react";
import { FiArrowUpRight, FiBookOpen, FiClock } from "react-icons/fi";
import SectionIntro from "./SectionIntro";
import { useScrollFrame, clamp01, prefersReducedMotion } from "../../../hooks/useScrollFrame";
import { card, muted, skeleton } from "../../ui/uiStyles";
import { eventDate, summary } from "../../../utils/format";

const SHOWN = 4;
// Each card sticks this much lower than the one before it, so the stack shows a sliver of every card.
const OFFSET = 22;

const dateRange = (start, end) => {
  if (!start) return null;
  const from = eventDate(start);
  if (!end) return `From ${from.day} ${from.month}`;
  const to = eventDate(end);
  return `${from.day} ${from.month} to ${to.day} ${to.month}`;
};

// Programs as a deck of cards: each one sticks near the top while the next slides up over it, and the card
// underneath shrinks and dims as it is covered. A card opens the program's details.
const ProgramsSection = ({ loading, programs, count, onShowProgram }) => {
  const stackRef = useRef(null);
  // --cover goes 0→1 on a card while the next card slides over it.
  const onFrame = useCallback(() => {
    const cards = stackRef.current?.querySelectorAll("[data-stack-card]");
    if (!cards) return;
    cards.forEach((node, i) => {
      const next = cards[i + 1];
      if (!next || prefersReducedMotion()) return node.style.setProperty("--cover", "0");
      // offsetHeight, unlike the bounding box, ignores the card's own shrink.
      const height = node.offsetHeight;
      const covered = clamp01((node.getBoundingClientRect().top + height - next.getBoundingClientRect().top) / (height - OFFSET));
      node.style.setProperty("--cover", covered.toFixed(4));
    });
  }, []);
  useScrollFrame(onFrame);

  const shown = programs.slice(0, SHOWN);

  return (
    <section id="programs" className="scroll-mt-20 py-16 sm:py-24">
      <div className="mx-auto grid max-w-[1280px] gap-10 px-5 sm:gap-12 sm:px-8 lg:grid-cols-[1.3fr_0.7fr] lg:items-start">
        <div className="lg:sticky lg:top-28 lg:order-2">
          <SectionIntro icon={FiBookOpen} label="Programs" count={count} countLabel={count === 1 ? "training program" : "training programs"} text="Longer tracks that meet every week and build real skills." to="/programs" cta="See all programs" />
        </div>

        {loading ? <div className={`h-[420px] rounded-[2rem] lg:order-1 ${skeleton}`} /> : (
          <div ref={stackRef} className="flex flex-col gap-6 pb-4 lg:order-1">
            {shown.map((program, i) => {
              const range = dateRange(program.startDate, program.endDate);
              return (
                <div
                  key={program.id}
                  data-stack-card
                  className="sticky top-[var(--stack-top)] origin-top will-change-transform [--stack-base:6rem] sm:[--stack-base:7rem]"
                  style={{ "--stack-top": `calc(var(--stack-base) + ${i * OFFSET}px)`, "--cover": 0, transform: "scale(calc(1 - var(--cover) * 0.06))" }}
                >
                  <button
                    type="button"
                    onClick={() => onShowProgram(program.id)}
                    className={`group relative grid w-full overflow-hidden rounded-[2rem] text-left focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8] sm:grid-cols-[0.9fr_1.1fr] ${card}`}
                  >
                    <span className="relative block aspect-[16/9] overflow-hidden bg-[#E2F0FA] sm:aspect-auto sm:min-h-[300px] dark:bg-[#16364C]">
                      {program.imageUrl && <img src={program.imageUrl} alt="" className="absolute inset-0 h-full w-full object-cover transition-transform duration-700 group-hover:scale-[1.04]" loading="lazy" />}
                    </span>
                    <span className="flex flex-col p-6 sm:p-8">
                      <span className={`w-fit rounded-full px-3 py-1 text-xs font-semibold ${program.open ? "bg-[#E2F0FA] text-[#205E85] dark:bg-[#16364C] dark:text-[#9FD0EE]" : "bg-slate-100 text-slate-600 dark:bg-white/10 dark:text-slate-300"}`}>
                        {program.open ? "Registration open" : "Registration closed"}
                      </span>
                      <span className="mt-4 text-2xl font-semibold tracking-tight text-slate-900 sm:text-3xl dark:text-white" style={{ textWrap: "balance" }}>{program.name}</span>
                      {program.description && <span className={`mt-3 line-clamp-3 leading-relaxed ${muted}`}>{summary(program.description, 170)}</span>}
                      <span className={`mt-5 flex flex-wrap gap-x-4 gap-y-1.5 text-sm font-medium ${muted}`}>
                        {program.time && <span className="flex items-center gap-1.5"><FiClock className="h-4 w-4" aria-hidden="true" />{program.time}</span>}
                        {range && <span>{range}</span>}
                      </span>
                      <span className="mt-auto inline-flex items-center gap-1.5 pt-6 text-sm font-semibold text-[#2A7AB0] dark:text-[#7DBBE0]">View details <FiArrowUpRight className="h-4 w-4" aria-hidden="true" /></span>
                    </span>
                    <span aria-hidden="true" className="pointer-events-none absolute inset-0 bg-[#0F3A5A]" style={{ opacity: "calc(var(--cover) * 0.25)" }} />
                  </button>
                </div>
              );
            })}
          </div>
        )}
      </div>
    </section>
  );
};

export default ProgramsSection;
