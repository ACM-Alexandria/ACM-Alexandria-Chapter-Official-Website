import { FiGlobe } from "react-icons/fi";
import Marquee from "../../ui/Marquee";
import Reveal from "../../ui/Reveal";
import { muted, skeleton } from "../../ui/uiStyles";

const Logo = ({ partner }) => {
  const box = "mr-4 flex h-24 w-44 shrink-0 items-center justify-center rounded-2xl bg-white p-4 ring-1 ring-[#D6E6F2] dark:ring-white/10";
  const content = partner.imageUrl
    ? <img src={partner.imageUrl} alt={partner.name} className="h-full w-full object-contain" loading="lazy" />
    : <span className="flex flex-col items-center gap-1 text-xs font-semibold text-slate-500"><FiGlobe className="h-6 w-6" aria-hidden="true" />{partner.name}</span>;
  return partner.website
    ? <a href={partner.website} target="_blank" rel="noopener noreferrer" className={`${box} transition-transform hover:-translate-y-0.5`} title={partner.name}>{content}</a>
    : <span className={box}>{content}</span>;
};

// The chapter's partners as a sliding row of logos on a white band, before the footer.
const PartnersSection = ({ loading, partners }) => (
  <section id="partners" className="scroll-mt-20 bg-white py-16 sm:py-20 dark:bg-[#0F2233]">
    <Reveal className="mx-auto mb-10 max-w-[1280px] px-5 sm:px-8">
      <h2 className="text-3xl font-semibold tracking-tight text-slate-900 sm:text-4xl dark:text-white">Our partners</h2>
      <p className={`mt-3 max-w-2xl text-lg ${muted}`}>Organisations that share our commitment to technology, education, and community.</p>
    </Reveal>
    {loading ? (
      <div className="mx-auto flex max-w-[1280px] gap-4 overflow-hidden px-5 sm:px-8">
        {[0, 1, 2, 3, 4].map((i) => <div key={i} className={`h-24 w-44 shrink-0 rounded-2xl ${skeleton}`} />)}
      </div>
    ) : (
      <Marquee label="Chapter partners" speed={70}>
        {partners.map((partner) => <Logo key={partner.id} partner={partner} />)}
      </Marquee>
    )}
  </section>
);

export default PartnersSection;
