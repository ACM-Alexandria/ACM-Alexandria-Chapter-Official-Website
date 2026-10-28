import NightSky from "../ui/NightSky";

// The deep blue band at the top of a page: eyebrow, title, a line of text, optional actions, and an optional
// `media` block (a cover image) beside the text from `md` up. In dark mode it carries the home hero's night sky.
const PageHero = ({ eyebrow, title, text, children, media, align = "left" }) => (
  <section className="relative overflow-hidden bg-ocean-794 text-white dark:bg-ocean-910">
    <div aria-hidden="true" className="pointer-events-none absolute inset-0 opacity-[0.08] dark:hidden" style={{ backgroundImage: "radial-gradient(var(--color-white) 1px, transparent 1px)", backgroundSize: "28px 28px" }} />
    <NightSky />
    <div aria-hidden="true" className="pointer-events-none absolute -right-40 -top-40 h-[28rem] w-[28rem] rounded-full bg-ocean-461/20 blur-3xl" />
    <div className={`relative mx-auto max-w-[1280px] px-5 pb-16 pt-32 sm:px-8 sm:pb-20 sm:pt-40 ${align === "center" ? "text-center" : ""} ${media ? "grid items-center gap-10 md:grid-cols-[auto_1fr]" : ""}`}>
      {media && <div className="md:order-first" style={{ animation: "hp-pop 0.7s cubic-bezier(0.22,1,0.36,1) both" }}>{media}</div>}
      <div>
      {eyebrow && <p className="text-sm font-semibold text-ocean-316" style={{ animation: "hp-rise 0.6s ease-out both" }}>{eyebrow}</p>}
      <h1 className={`mt-3 text-4xl font-semibold leading-[1.05] tracking-[-0.03em] sm:text-6xl ${align === "center" ? "mx-auto" : ""} max-w-3xl`} style={{ textWrap: "balance", animation: "hp-rise 0.7s cubic-bezier(0.22,1,0.36,1) 0.08s both" }}>{title}</h1>
      {text && <p className={`mt-5 max-w-2xl text-lg leading-relaxed text-ocean-114 ${align === "center" ? "mx-auto" : ""}`} style={{ animation: "hp-rise 0.7s ease-out 0.16s both" }}>{text}</p>}
      {children && <div className={`mt-8 flex flex-wrap items-center gap-3 ${align === "center" ? "justify-center" : ""}`} style={{ animation: "hp-rise 0.7s ease-out 0.24s both" }}>{children}</div>}
      </div>
    </div>
  </section>
);

export default PageHero;
