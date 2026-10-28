import { Link } from "react-router-dom";
import { FiArrowUpRight } from "react-icons/fi";
import CountUp from "../../ui/CountUp";
import Reveal from "../../ui/Reveal";
import { muted, sectionLead } from "../../ui/uiStyles";

// The intro beside an activity section: label, the total, a line of text, and a link to the full page.
// `dark` is for sections on the night background.
const SectionIntro = ({ icon, label, count, countLabel, text, to, cta, dark = false }) => {
  const Icon = icon;
  return (
    <Reveal>
      <p className={`flex items-center gap-2 text-sm font-semibold ${dark ? "text-ocean-316" : "text-ocean-573 dark:text-ocean-316"}`}><Icon className="h-4 w-4" aria-hidden="true" />{label}</p>
      <p className={`mt-3 text-5xl font-semibold tracking-tight sm:mt-4 sm:text-6xl lg:text-7xl ${dark ? "text-white" : "text-ocean-676 dark:text-white"}`}><CountUp value={count} /></p>
      <p className={`mt-1 font-semibold ${sectionLead} ${dark ? "text-white" : "text-slate-900 dark:text-white"}`}>{countLabel}</p>
      <p className={`mt-3 max-w-sm leading-relaxed sm:mt-4 ${sectionLead} ${dark ? "text-ocean-114" : muted}`}>{text}</p>
      <Link to={to} className={`mt-6 inline-flex h-11 items-center gap-2 rounded-xl px-5 text-sm font-semibold transition-transform hover:-translate-y-px ${dark ? "bg-white text-ocean-676" : "bg-ocean-676 text-white dark:bg-ocean-461 dark:text-ocean-910"}`}>{cta} <FiArrowUpRight className="h-4 w-4" aria-hidden="true" /></Link>
    </Reveal>
  );
};

export default SectionIntro;
