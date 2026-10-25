import { FiArrowUpRight, FiClock, FiFileText } from "react-icons/fi";
import Reveal from "../../ui/Reveal";
import { card, muted, skeleton } from "../../ui/uiStyles";

// Limited-time registrations, right under the gallery. Each card opens the form's details.
const ExclusiveFormsSection = ({ loading, forms, onShowDetails }) => {
  if (!loading && forms.length === 0) return null;
  return (
    <section id="exclusive-forms" className="mx-auto max-w-[1280px] scroll-mt-20 px-5 pt-16 sm:px-8 sm:pt-24">
      <Reveal className="mb-8 sm:mb-10">
        <p className="flex items-center gap-2 text-sm font-semibold text-[#2A7AB0] dark:text-[#7DBBE0]"><FiClock className="h-4 w-4" aria-hidden="true" />Open for a limited time</p>
        <h2 className="mt-3 text-4xl font-semibold tracking-tight text-slate-900 sm:text-5xl dark:text-white" style={{ textWrap: "balance" }}>Exclusive opportunities</h2>
        <p className={`mt-4 max-w-2xl text-lg ${muted}`}>Registrations with limited seats. Secure your spot before they close.</p>
      </Reveal>
      <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
        {loading
          ? [0, 1, 2].map((i) => <div key={i} className={`aspect-[4/3] rounded-[2rem] ${skeleton}`} />)
          : forms.map((form, i) => (
            <Reveal key={form.id} delay={i * 80}>
              <button type="button" onClick={() => onShowDetails(form.id)} className={`group flex h-full w-full flex-col overflow-hidden rounded-[2rem] text-left transition-transform hover:-translate-y-1 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8] ${card}`}>
                <div className="aspect-[16/9] overflow-hidden bg-[#E2F0FA] dark:bg-[#16364C]">
                  {form.imageUrl
                    ? <img src={form.imageUrl} alt="" className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-[1.05]" loading="lazy" />
                    : <span className="flex h-full w-full items-center justify-center"><FiFileText className="h-12 w-12 text-[#4B98C8]/50" aria-hidden="true" /></span>}
                </div>
                <div className="flex flex-1 flex-col p-6">
                  <h3 className="line-clamp-2 text-xl font-semibold tracking-tight text-slate-900 dark:text-white">{form.title}</h3>
                  {form.description && <p className={`mt-2 line-clamp-3 leading-relaxed ${muted}`}>{form.description}</p>}
                  <span className="mt-auto inline-flex items-center gap-1.5 pt-5 text-sm font-semibold text-[#2A7AB0] dark:text-[#7DBBE0]">View details <FiArrowUpRight className="h-4 w-4" aria-hidden="true" /></span>
                </div>
              </button>
            </Reveal>
          ))}
      </div>
    </section>
  );
};

export default ExclusiveFormsSection;
