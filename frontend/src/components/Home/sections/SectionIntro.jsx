import { Link } from "react-router-dom";
import { FiArrowUpRight } from "react-icons/fi";
import CountUp from "../../ui/CountUp";
import Reveal from "../../ui/Reveal";
import { muted } from "../../ui/uiStyles";

// The intro beside an activity section: label, the total, a line of text, and a link to the full page.
// `dark` is for sections on the night background.
const SectionIntro = ({ icon, label, count, countLabel, text, to, cta, dark = false }) => {
  const Icon = icon;
  return (
    <Reveal>
      <p className={`flex items-center gap-2 text-sm font-semibold ${dark ? "text-[#7DBBE0]" : "text-[#2A7AB0] dark:text-[#7DBBE0]"}`}><Icon className="h-4 w-4" aria-hidden="true" />{label}</p>
      <p className={`mt-4 text-6xl font-semibold tracking-tight sm:text-7xl ${dark ? "text-white" : "text-[#205E85] dark:text-white"}`}><CountUp value={count} /></p>
      <p className={`mt-1 text-lg font-semibold ${dark ? "text-white" : "text-slate-900 dark:text-white"}`}>{countLabel}</p>
      <p className={`mt-4 max-w-sm text-lg leading-relaxed ${dark ? "text-[#CFE6F5]" : muted}`}>{text}</p>
      <Link to={to} className={`mt-7 inline-flex h-11 items-center gap-2 rounded-xl px-5 text-sm font-semibold transition-transform hover:-translate-y-px ${dark ? "bg-white text-[#205E85]" : "bg-[#205E85] text-white dark:bg-[#4B98C8] dark:text-[#081826]"}`}>{cta} <FiArrowUpRight className="h-4 w-4" aria-hidden="true" /></Link>
    </Reveal>
  );
};

export default SectionIntro;
