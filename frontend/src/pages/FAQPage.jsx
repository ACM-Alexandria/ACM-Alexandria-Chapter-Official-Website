import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { FiChevronDown, FiMail, FiSearch } from "react-icons/fi";
import SiteLayout from "../components/Layout/SiteLayout";
import PageHero from "../components/Layout/PageHero";
import Reveal from "../components/ui/Reveal";
import { card, field, focusRing, muted, secondaryButton } from "../components/ui/uiStyles";
import { fetchFAQs } from "../services/faqService";

const CATEGORY_ORDER = ["Joining", "Events", "Committees", "Club", "Radio", "Accounts"];

const FAQPage = () => {
  const [faqs, setFaqs] = useState([]);
  const [query, setQuery] = useState("");
  const [page, setPage] = useState(0);
  const [pageInfo, setPageInfo] = useState({ totalPages: 0, totalElements: 0 });
  const [openId, setOpenId] = useState(null);
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchFAQs({ page, query })
      .then((result) => {
        setFaqs(result.content || []);
        setPageInfo({ totalPages: result.totalPages || 0, totalElements: result.totalElements || 0 });
      })
      .catch((loadError) => setError(loadError.message))
      .finally(() => setLoading(false));
  }, [page, query]);

  const groupedFAQs = useMemo(() => {
    const normalizedQuery = query.trim().toLowerCase();
    return CATEGORY_ORDER.map((category) => ({
      category,
      items: faqs.filter((faq) =>
        faq.category === category &&
        (!normalizedQuery || `${faq.question} ${faq.answer}`.toLowerCase().includes(normalizedQuery)),
      ),
    })).filter((group) => group.items.length > 0);
  }, [faqs, query]);

  return (
    <SiteLayout>
      <PageHero eyebrow="FAQ" title="Frequently asked questions" text="Everything you need to know about joining ACM Alexandria, events, committees, clubs, radio, and your account.">
        <label className="relative block w-full max-w-xl">
          <span className="sr-only">Search FAQs</span>
          <FiSearch aria-hidden="true" className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
          <input
            value={query}
            onChange={(event) => { setQuery(event.target.value); setPage(0); }}
            placeholder="Search questions and answers…"
            className={`${field} h-12 pl-12`}
          />
        </label>
      </PageHero>

      <div className="mx-auto max-w-4xl space-y-12 px-5 py-16 sm:px-8 sm:py-20">
        {loading && <p className={`py-10 text-center ${muted}`}>Loading questions…</p>}
        {error && <div role="alert" className="rounded-xl bg-red-50 px-4 py-3 text-center text-sm font-medium text-red-700 dark:bg-red-950/40 dark:text-red-200">{error}</div>}
        {!loading && !error && groupedFAQs.length === 0 && <div className={`rounded-2xl border border-dashed border-ocean-106 p-10 text-center dark:border-white/15 ${muted}`}>No questions match your search.</div>}

        {groupedFAQs.map(({ category, items }) => (
          <section key={category} aria-labelledby={`faq-${category}`}>
            <h2 id={`faq-${category}`} className="mb-4 text-xl font-semibold tracking-tight text-slate-900 dark:text-white">{category}</h2>
            <div className={`overflow-hidden rounded-2xl ${card}`}>
              {items.map((faq) => {
                const open = openId === faq.id;
                return (
                  <article key={faq.id} className="border-b border-ocean-106 last:border-b-0 dark:border-white/10">
                    <button
                      type="button"
                      aria-expanded={open}
                      onClick={() => setOpenId(open ? null : faq.id)}
                      className={`flex w-full items-center justify-between gap-4 px-5 py-5 text-left font-semibold text-slate-800 transition-colors hover:bg-ocean-31 dark:text-slate-100 dark:hover:bg-white/5 sm:px-6 ${focusRing}`}
                    >
                      <span>{faq.question}</span>
                      <FiChevronDown aria-hidden="true" className={`h-5 w-5 shrink-0 text-ocean-573 transition-transform dark:text-ocean-316 ${open ? "rotate-180" : ""}`} />
                    </button>
                    {open && <div className={`px-5 pb-5 text-sm leading-7 sm:px-6 ${muted}`}>{faq.answer}</div>}
                  </article>
                );
              })}
            </div>
          </section>
        ))}

        {!loading && !error && pageInfo.totalPages > 1 && (
          <nav aria-label="FAQ pages" className="flex flex-wrap items-center justify-center gap-4">
            <button type="button" disabled={page === 0} onClick={() => setPage((current) => current - 1)} className={secondaryButton}>Previous</button>
            <span className={`text-sm ${muted}`}>Page {page + 1} of {pageInfo.totalPages} · {pageInfo.totalElements} questions</span>
            <button type="button" disabled={page >= pageInfo.totalPages - 1} onClick={() => setPage((current) => current + 1)} className={secondaryButton}>Next</button>
          </nav>
        )}

        <Reveal className="flex flex-col items-start justify-between gap-6 rounded-[2rem] bg-ocean-794 p-8 text-white sm:flex-row sm:items-center sm:p-10 dark:bg-ocean-820">
          <div>
            <FiMail aria-hidden="true" className="h-7 w-7 text-ocean-316" />
            <h2 className="mt-4 text-2xl font-semibold tracking-tight">Still have questions?</h2>
            <p className="mt-2 max-w-xl text-sm leading-6 text-ocean-114">We’re happy to help. Reach out through our contact section and the team will point you in the right direction.</p>
          </div>
          <Link to="/#footer" className={`inline-flex h-11 shrink-0 items-center rounded-xl bg-white px-5 text-sm font-semibold text-ocean-794 transition-colors hover:bg-ocean-31 ${focusRing}`}>Contact us</Link>
        </Reveal>
      </div>
    </SiteLayout>
  );
};

export default FAQPage;
