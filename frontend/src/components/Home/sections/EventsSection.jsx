import { useCallback, useRef, useState } from "react";
import { FiCalendar, FiMapPin } from "react-icons/fi";
import SectionIntro from "./SectionIntro";
import Reveal from "../../ui/Reveal";
import { useScrollFrame, clamp01, prefersReducedMotion } from "../../../hooks/useScrollFrame";
import { card, muted, skeleton } from "../../ui/uiStyles";
import { eventDate, summary } from "../../../utils/format";

const SHOWN = 5;

// The latest events on a date rail: big dates down the side, a line that the beam fills as you scroll past, and a
// card per event. Each dot lights up as the beam reaches it. A card opens the event's details.
const EventsSection = ({ loading, events, count, onShowEvent }) => {
  const railRef = useRef(null);
  const [now] = useState(() => Date.now());
  // 0 when the rail's top is at 60% of the screen height, 1 once its bottom gets there.
  const onFrame = useCallback(() => {
    const node = railRef.current;
    if (!node) return;
    const box = node.getBoundingClientRect();
    const progress = prefersReducedMotion() ? 1 : clamp01((window.innerHeight * 0.6 - box.top) / box.height);
    node.style.setProperty("--rail", progress.toFixed(4));
  }, []);
  useScrollFrame(onFrame);

  const shown = events.slice(0, SHOWN);

  return (
    <section id="events" className="scroll-mt-20 bg-white py-16 sm:py-24 dark:bg-[#0F2233]">
      <div className="mx-auto grid max-w-[1280px] gap-10 px-5 sm:gap-12 sm:px-8 lg:grid-cols-[0.7fr_1.3fr] lg:items-start">
        <div className="lg:sticky lg:top-28">
          <SectionIntro icon={FiCalendar} label="Events" count={count} countLabel={count === 1 ? "event this year" : "events this year"} text="Workshops, talks, and competitions open to every student." to="/events" cta={`See all ${count} events`} />
        </div>

        {loading ? (
          <div className="flex flex-col gap-4">
            {[0, 1, 2, 3].map((i) => <div key={i} className={`h-28 rounded-3xl ${skeleton}`} />)}
          </div>
        ) : (
          <ol ref={railRef} style={{ "--rail": 0 }} className="relative flex flex-col gap-4 sm:gap-5">
            <span aria-hidden="true" className="absolute bottom-10 left-[67px] top-10 w-0.5 rounded-full bg-[#D6E6F2] sm:left-[87px] dark:bg-white/10">
              <span className="block h-full w-full origin-top rounded-full bg-[#4B98C8]" style={{ transform: "scaleY(var(--rail))" }} />
            </span>
            {shown.map((event, i) => {
              const date = eventDate(event.time);
              const upcoming = new Date(event.time).getTime() > now;
              // Where this dot sits along the rail, so it lights once --rail passes it.
              const at = shown.length > 1 ? i / (shown.length - 1) : 0;
              return (
                <Reveal as="li" key={event.id} delay={i * 70} className="grid grid-cols-[56px_24px_minmax(0,1fr)] items-center sm:grid-cols-[72px_32px_minmax(0,1fr)]">
                  <div className="text-center leading-none">
                    <p className="text-3xl font-semibold tracking-tight text-[#205E85] sm:text-4xl dark:text-white">{date.day}</p>
                    <p className={`mt-1.5 text-sm font-medium ${muted}`}>{date.month} {new Date(event.time).getFullYear()}</p>
                  </div>
                  <span aria-hidden="true" className="relative mx-auto h-3 w-3 rounded-full bg-[#D6E6F2] dark:bg-white/15">
                    <span className="absolute inset-0 rounded-full bg-[#4B98C8] ring-4 ring-[#4B98C8]/20" style={{ opacity: `clamp(0, calc((var(--rail) - ${(at * 0.92).toFixed(3)}) * 25), 1)` }} />
                  </span>
                  <button
                    type="button"
                    onClick={() => onShowEvent(event.id)}
                    className={`group flex w-full items-center gap-4 rounded-3xl p-3 text-left transition-transform hover:-translate-y-0.5 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8] sm:p-4 ${card}`}
                  >
                    <span className="h-20 w-24 shrink-0 overflow-hidden rounded-2xl bg-[#E2F0FA] sm:h-24 sm:w-36 dark:bg-[#16364C]">
                      {event.imageUrl && <img src={event.imageUrl} alt="" className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-[1.05]" loading="lazy" />}
                    </span>
                    <span className="min-w-0 flex-1">
                      <span className="line-clamp-2 font-semibold tracking-tight text-slate-900 sm:text-lg dark:text-white">{event.name}</span>
                      {event.description && <span className={`mt-1 line-clamp-1 hidden text-sm sm:block ${muted}`}>{summary(event.description, 120)}</span>}
                      <span className="mt-2 flex flex-wrap items-center gap-x-3 gap-y-1.5 text-sm">
                        {event.location && <span className={`flex items-center gap-1 font-medium ${muted}`}><FiMapPin className="h-3.5 w-3.5" aria-hidden="true" />Room {event.location}</span>}
                        {event.open && <span className="rounded-full bg-[#E2F0FA] px-2.5 py-0.5 text-xs font-semibold text-[#205E85] dark:bg-[#16364C] dark:text-[#9FD0EE]">Registration open</span>}
                        {!event.open && upcoming && <span className="rounded-full bg-[#E2F0FA] px-2.5 py-0.5 text-xs font-semibold text-[#205E85] dark:bg-[#16364C] dark:text-[#9FD0EE]">Upcoming</span>}
                      </span>
                    </span>
                  </button>
                </Reveal>
              );
            })}
          </ol>
        )}
      </div>
    </section>
  );
};

export default EventsSection;
