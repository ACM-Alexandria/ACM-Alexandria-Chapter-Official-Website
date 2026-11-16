import { FiGlobe } from "react-icons/fi";
import Reveal from "../../ui/Reveal";
import { muted, sectionLead, sectionTitle, sectionY, skeleton } from "../../ui/uiStyles";

const item = "flex h-20 w-44 shrink-0 items-center justify-center px-6 sm:h-24 sm:w-56 sm:px-10";

const Logo = ({ partner, copy = false }) => {
  const content = partner.imageUrl
    ? <img src={partner.imageUrl} alt={partner.name} className="max-h-full max-w-full object-contain transition duration-300 group-hover/logo:scale-105" loading="lazy" />
    : <span className="flex items-center gap-2 text-sm font-semibold text-slate-500 dark:text-ocean-239"><FiGlobe className="h-5 w-5" aria-hidden="true" />{partner.name}</span>;
  return partner.website
    ? <a href={partner.website} target="_blank" rel="noopener noreferrer" title={partner.name} tabIndex={copy ? -1 : undefined} className={`group/logo ${item} focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ocean-461`}>{content}</a>
    : <div className={item}>{content}</div>;
};

// The chapter's partners as a slow, endless slide of logos before the footer. The row is two identical halves,
// each long enough to fill the screen, so sliding by half of it loops without a jump. It pauses when you point at
// it, and with reduced motion it stands still and wraps. A logo links to the partner's site.
const PartnersSection = ({ loading, partners }) => {
  const copies = Math.max(1, Math.ceil(8 / Math.max(partners.length, 1)));
  const half = Array.from({ length: copies }, () => partners).flat();
  const still = partners.length <= 1;

  return (
    <section id="partners" className={`scroll-mt-20 bg-white dark:bg-ocean-871 ${sectionY}`}>
      <div className="mx-auto max-w-[1280px] px-5 sm:px-8">
        <Reveal className="mb-8 max-w-2xl sm:mb-10">
          <h2 className={sectionTitle}>Our partners</h2>
          <p className={`mt-3 ${sectionLead} ${muted}`}>Organisations that share our commitment to technology, education, and community.</p>
        </Reveal>
      </div>
      {loading ? (
        <div className={`mx-auto h-24 max-w-[1280px] ${skeleton}`} />
      ) : (
        <Reveal>
          <div className="overflow-hidden [mask-image:linear-gradient(to_right,transparent,#000_8%,#000_92%,transparent)]">
            <ul className="flex w-max items-center hover:[animation-play-state:paused] motion-reduce:!animate-none" style={still ? undefined : { animation: `hp-marquee ${half.length * 5}s linear infinite` }}>
              {(still ? [0] : [0, 1]).flatMap((n) => half.map((partner, i) => (
                <li key={`${n}-${i}`} aria-hidden={n === 1 ? "true" : undefined}><Logo partner={partner} copy={n === 1} /></li>
              )))}
            </ul>
          </div>
        </Reveal>
      )}
    </section>
  );
};

export default PartnersSection;
