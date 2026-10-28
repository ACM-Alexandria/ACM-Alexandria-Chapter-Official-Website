import { FiGlobe } from "react-icons/fi";
import Reveal from "../../ui/Reveal";
import { muted, panelRadius, sectionLead, sectionTitle, sectionY, skeleton } from "../../ui/uiStyles";

// Cells draw their own hairline edges, so a short last row leaves no coloured gap.
const cell = "group relative flex h-28 items-center justify-center bg-white p-5 sm:p-6 shadow-[inset_0_0_0_0.5px_var(--color-ocean-106)] sm:h-36 dark:bg-ocean-871 dark:shadow-[inset_0_0_0_0.5px_color-mix(in_srgb,var(--color-white)_10%,transparent)]";

const Logo = ({ partner }) => {
  const content = partner.imageUrl
    ? <img src={partner.imageUrl} alt={partner.name} className="max-h-full max-w-full object-contain grayscale transition duration-300 group-hover:scale-105 group-hover:grayscale-0 group-focus-visible:grayscale-0 dark:opacity-80 dark:group-hover:opacity-100" loading="lazy" />
    : <span className="flex flex-col items-center gap-1 text-sm font-semibold text-slate-500"><FiGlobe className="h-6 w-6" aria-hidden="true" />{partner.name}</span>;
  return partner.website
    ? <a href={partner.website} target="_blank" rel="noopener noreferrer" title={partner.name} className={`${cell} focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-ocean-461`}>{content}</a>
    : <div className={cell}>{content}</div>;
};

// The chapter's partners as a still wall of logos on a white band, before the footer. The beam passes over it once
// when it scrolls into view. A logo is grey until you point at it and links to the partner's site.
const PartnersSection = ({ loading, partners }) => (
  <section id="partners" className={`scroll-mt-20 bg-white dark:bg-ocean-871 ${sectionY}`}>
    <div className="mx-auto max-w-[1280px] px-5 sm:px-8">
      <Reveal className="mb-8 max-w-2xl sm:mb-10">
        <h2 className={sectionTitle}>Our partners</h2>
        <p className={`mt-3 ${sectionLead} ${muted}`}>Organisations that share our commitment to technology, education, and community.</p>
      </Reveal>
      {loading ? (
        <div className={`h-56 ${panelRadius} ${skeleton}`} />
      ) : (
        <Reveal>
          <div className={`relative overflow-hidden ring-1 ring-ocean-106 dark:ring-white/10 ${panelRadius}`}>
            <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4">
              {partners.map((partner) => <Logo key={partner.id} partner={partner} />)}
            </div>
            <span aria-hidden="true" className="pointer-events-none absolute inset-y-0 left-0 w-1/5 bg-ocean-461/15" style={{ animation: "hp-sweep 5s ease-in-out 0.6s 1 both" }} />
          </div>
        </Reveal>
      )}
    </div>
  </section>
);

export default PartnersSection;
