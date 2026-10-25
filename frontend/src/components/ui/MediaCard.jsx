import { Link } from "react-router-dom";
import Reveal from "./Reveal";
import { card } from "./uiStyles";

// A card in a list page: a photo, a small meta line, the title, optional text and tag. Opens details with
// `onClick` or links with `to`. Fades in as it scrolls into view, a little after the card before it.
const MediaCard = ({ image, meta, title, text, tag, onClick, to, index = 0, square = false }) => {
  const className = `group flex h-full w-full flex-col overflow-hidden rounded-3xl text-left transition-transform duration-300 hover:-translate-y-1 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8] ${card}`;
  const body = (
    <>
      <div className={`relative overflow-hidden bg-[#E2F0FA] dark:bg-[#16364C] ${square ? "aspect-square" : "aspect-[4/3]"}`}>
        {image && <img src={image} alt="" className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-[1.05]" loading="lazy" />}
        {tag && <span className="absolute left-4 top-4">{tag}</span>}
      </div>
      <div className="flex flex-1 flex-col p-5">
        {meta && <p className="text-xs font-medium text-[#2A7AB0] dark:text-[#7DBBE0]">{meta}</p>}
        <h3 className="mt-1 line-clamp-2 text-base font-semibold tracking-tight text-slate-900 dark:text-white">{title}</h3>
        {text && <p className="mt-2 line-clamp-3 text-sm leading-relaxed text-slate-600 dark:text-[#A9C7DB]">{text}</p>}
      </div>
    </>
  );
  return (
    <Reveal delay={(index % 3) * 80} className="h-full">
      {onClick
        ? <button type="button" onClick={onClick} className={`cursor-pointer ${className}`}>{body}</button>
        : <Link to={to} className={className}>{body}</Link>}
    </Reveal>
  );
};

// Open / closed (or any short state) pill laid over a card photo.
export const StateTag = ({ open, openLabel = "Open", closedLabel = "Closed" }) => (
  <span className={`rounded-full px-3 py-1 text-xs font-semibold shadow-sm ${open ? "bg-emerald-500 text-white" : "bg-white/90 text-slate-600"}`}>{open ? openLabel : closedLabel}</span>
);

export default MediaCard;
