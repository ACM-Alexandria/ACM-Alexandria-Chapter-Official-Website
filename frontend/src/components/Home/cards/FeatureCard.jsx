import { card, muted } from "../../ui/uiStyles";

// The featured card grows into place while its photo zooms out, like a product shot on apple.com.
// With `onClick` the whole card is a button (it opens the item's details).
const FeatureCard = ({ image, badge, title, meta, text, children, dark = false, onClick }) => {
  const Tag = onClick ? "button" : "div";
  return (
    <Tag
      type={onClick ? "button" : undefined}
      onClick={onClick}
      className={`group block w-full origin-top overflow-hidden rounded-[2rem] text-left will-change-transform ${onClick ? "cursor-pointer focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]" : ""} ${dark ? "bg-white/5 ring-1 ring-white/10" : card}`}
      style={{ transform: "scale(calc(0.84 + 0.16 * var(--sp))) translateY(calc((1 - var(--sp)) * 70px))" }}
    >
      <div className="relative aspect-[16/9] overflow-hidden bg-[#E2F0FA] dark:bg-[#16364C]">
        {image && <img src={image} alt="" className="h-full w-full object-cover" loading="lazy" style={{ transform: "scale(calc(1.4 - 0.4 * var(--sp)))" }} />}
        {badge && <span className="absolute left-5 top-5 rounded-full bg-white px-3 py-1 text-xs font-semibold text-[#205E85] shadow-md">{badge}</span>}
      </div>
      <div className="p-5 sm:p-7">
        {meta && <p className={`text-sm font-medium ${dark ? "text-[#7DBBE0]" : "text-[#2A7AB0] dark:text-[#7DBBE0]"}`}>{meta}</p>}
        <h3 className={`mt-2 text-xl font-semibold tracking-tight sm:text-2xl ${dark ? "text-white" : "text-slate-900 dark:text-white"}`}>{title}</h3>
        {text && <p className={`mt-3 leading-relaxed ${dark ? "text-[#CFE6F5]" : muted}`}>{text}</p>}
        {children}
      </div>
    </Tag>
  );
};

export default FeatureCard;
