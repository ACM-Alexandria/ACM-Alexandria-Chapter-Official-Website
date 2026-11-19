import { FiPlus } from "react-icons/fi";
import { focusRing, muted } from "../ui/uiStyles";
import Highlight from "./Highlight";

// One question: a full-width button that opens its answer below it. The answer's height animates with a
// grid-row transition, so it needs no measuring.
const FaqItem = ({ faq, query, open, onToggle }) => {
  const panelId = `faq-answer-${faq.id}`;
  return (
    <li className="border-t border-ocean-106 first:border-t-0 dark:border-white/10">
      <h3>
        <button
          type="button"
          aria-expanded={open}
          aria-controls={panelId}
          onClick={onToggle}
          className={`group flex w-full items-center justify-between gap-6 rounded-lg py-5 text-left ${focusRing}`}
        >
          <span className={`text-base font-semibold leading-snug transition-colors sm:text-lg ${open ? "text-ocean-676 dark:text-ocean-222" : "text-slate-900 group-hover:text-ocean-676 dark:text-white dark:group-hover:text-ocean-222"}`}>
            <Highlight text={faq.question} query={query} />
          </span>
          <span className={`flex h-8 w-8 shrink-0 items-center justify-center rounded-full ring-1 transition-colors duration-200 ${open ? "bg-ocean-676 text-white ring-ocean-676 dark:bg-ocean-461 dark:text-ocean-910 dark:ring-ocean-461" : "text-ocean-573 ring-ocean-106 group-hover:bg-ocean-67 dark:text-ocean-316 dark:ring-white/15 dark:group-hover:bg-white/5"}`}>
            <FiPlus aria-hidden="true" className={`h-4 w-4 transition-transform duration-200 motion-reduce:transition-none ${open ? "rotate-45" : ""}`} />
          </span>
        </button>
      </h3>
      <div
        id={panelId}
        role="region"
        aria-label={faq.question}
        inert={!open}
        className={`grid transition-[grid-template-rows] duration-200 ease-out motion-reduce:transition-none ${open ? "grid-rows-[1fr]" : "grid-rows-[0fr]"}`}
      >
        <div className="overflow-hidden">
          <p className={`max-w-[65ch] whitespace-pre-line pb-6 pr-14 leading-7 ${muted}`}>
            <Highlight text={faq.answer} query={query} />
          </p>
        </div>
      </div>
    </li>
  );
};

export default FaqItem;
