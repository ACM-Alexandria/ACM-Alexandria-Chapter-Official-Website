import { Link } from "react-router-dom";
import { card } from "../../ui/uiStyles";

// Small cards slide up at different speeds, so they settle one after another as you scroll. On phones they sit
// in a row you swipe through instead, and only fade in (--lift is 0) so the row never clips a card mid-slide.
// The card links to `to`, or opens the item's details with `onClick`.
const SmallCard = ({ image, title, meta, to, onClick, index = 0, dark = false, square = false }) => {
  const className = `group block h-full w-full overflow-hidden rounded-3xl text-left focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8] ${dark ? "bg-white/5 ring-1 ring-white/10" : card}`;
  const body = (
    <>
      <div className={`overflow-hidden bg-[#E2F0FA] dark:bg-[#16364C] ${square ? "aspect-square" : "aspect-[4/3]"}`}>
        {image && <img src={image} alt="" className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-[1.05]" loading="lazy" />}
      </div>
      <div className="p-4">
        <p className={`text-xs font-medium ${dark ? "text-[#7DBBE0]" : "text-[#2A7AB0] dark:text-[#7DBBE0]"}`}>{meta}</p>
        <p className={`mt-1 line-clamp-2 text-sm font-semibold ${dark ? "text-white" : "text-slate-900 dark:text-white"}`}>{title}</p>
      </div>
    </>
  );
  return (
    <div className="w-[72%] shrink-0 snap-start [--lift:0] sm:w-auto sm:[--lift:1]" style={{ transform: `translateY(calc((1 - var(--sp)) * ${110 + index * 70}px * var(--lift)))`, opacity: "calc(var(--sp) * 1.4 - 0.2)" }}>
      {onClick
        ? <button type="button" onClick={onClick} className={`cursor-pointer ${className}`}>{body}</button>
        : <Link to={to} className={className}>{body}</Link>}
    </div>
  );
};

export default SmallCard;
