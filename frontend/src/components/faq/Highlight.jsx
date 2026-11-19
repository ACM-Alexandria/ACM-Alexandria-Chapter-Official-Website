// Wraps every match of `query` in <mark>, so a search shows why a question matched.
const Highlight = ({ text, query }) => {
  const needle = query.trim();
  if (!needle) return text;
  const parts = text.split(new RegExp(`(${needle.replace(/[.*+?^${}()|[\]\\]/g, "\\$&")})`, "ig"));
  return parts.map((part, i) => (i % 2 === 1
    ? <mark key={i} className="rounded bg-ocean-114 px-0.5 text-slate-900 dark:bg-ocean-461/30 dark:text-white">{part}</mark>
    : part));
};

export default Highlight;
