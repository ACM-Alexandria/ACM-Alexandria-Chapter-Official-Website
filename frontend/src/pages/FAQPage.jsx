import { useEffect, useMemo, useState } from "react";
import { FiArrowRight, FiSearch, FiX } from "react-icons/fi";
import SiteLayout from "../components/Layout/SiteLayout";
import PageHero from "../components/Layout/PageHero";
import Reveal from "../components/ui/Reveal";
import FaqItem from "../components/faq/FaqItem";
import FaqNav from "../components/faq/FaqNav";
import { contactEmail as fallbackEmail } from "../components/Home/homeContent";
import { errorBox, field, focusRing, muted, primaryButton, secondaryButton, skeleton } from "../components/ui/uiStyles";
import { fetchFAQs } from "../services/faqService";
import { getEnv } from "../utils/env";

// "Contact us" opens a new email to the chapter, like the footer's email link.
const contactHref = `mailto:${getEnv("VITE_CONTACT_EMAIL") || fallbackEmail}`;

// Topics in the order a newcomer needs them; a category the list doesn't know yet is shown after these.
const TOPICS = [
  ["Joining", "Joining"],
  ["Events", "Events"],
  ["Committees", "Committees"],
  ["Club", "Clubs"],
  ["Radio", "Radio"],
  ["Accounts", "Accounts"],
];
const PAGE_SIZE = 100; // the most the API returns in one request

// Every question, fetched once; searching and grouping then happen here, so typing never waits on the network.
const loadAll = async () => {
  const first = await fetchFAQs({ page: 0, size: PAGE_SIZE });
  const rest = await Promise.all(Array.from({ length: Math.max(0, (first.totalPages || 1) - 1) }, (_, i) => fetchFAQs({ page: i + 1, size: PAGE_SIZE })));
  return [first, ...rest].flatMap((page) => page.content || []);
};

const matches = (text, needle) => text.toLowerCase().includes(needle);

const SkeletonList = () => (
  <div aria-hidden="true" className="space-y-12">
    {[3, 2].map((rows, g) => (
      <div key={g}>
        <div className={`h-7 w-40 rounded-lg ${skeleton}`} />
        <div className="mt-4 space-y-5">
          {Array.from({ length: rows }, (_, i) => <div key={i} className={`h-6 rounded-lg ${skeleton}`} style={{ width: `${88 - i * 14}%` }} />)}
        </div>
      </div>
    ))}
  </div>
);

const FAQPage = () => {
  const [faqs, setFaqs] = useState([]);
  const [status, setStatus] = useState("loading"); // loading | ready | error
  const [attempt, setAttempt] = useState(0);
  const [query, setQuery] = useState("");
  const [open, setOpen] = useState(() => new Set());
  const [activeId, setActiveId] = useState("");

  useEffect(() => {
    let cancelled = false;
    loadAll()
      .then((all) => { if (!cancelled) { setFaqs(all); setStatus("ready"); } })
      .catch(() => { if (!cancelled) setStatus("error"); });
    return () => { cancelled = true; };
  }, [attempt]);

  const needle = query.trim().toLowerCase();

  const groups = useMemo(() => {
    const known = new Set(TOPICS.map(([category]) => category));
    const topics = [...TOPICS, ...[...new Set(faqs.map((faq) => faq.category))].filter((category) => !known.has(category)).map((category) => [category, category])];
    return topics
      .map(([category, label]) => ({
        id: `faq-${category.toLowerCase()}`,
        label,
        items: faqs
          .filter((faq) => faq.category === category && (!needle || matches(`${faq.question} ${faq.answer}`, needle)))
          .sort((a, b) => a.order - b.order || a.id - b.id),
      }))
      .filter((group) => group.items.length > 0);
  }, [faqs, needle]);

  const shown = groups.reduce((sum, group) => sum + group.items.length, 0);

  // The topic whose heading is nearest the top of the screen is highlighted in the index.
  useEffect(() => {
    const sections = [...document.querySelectorAll("[data-faq-topic]")];
    if (sections.length === 0 || typeof IntersectionObserver === "undefined") return undefined;
    const observer = new IntersectionObserver((entries) => {
      const visible = entries.filter((entry) => entry.isIntersecting).sort((a, b) => a.boundingClientRect.top - b.boundingClientRect.top);
      if (visible.length) setActiveId(visible[0].target.id);
    }, { rootMargin: "-20% 0px -65% 0px" });
    sections.forEach((section) => observer.observe(section));
    return () => observer.disconnect();
  }, [groups]);

  const toggle = (id) => setOpen((current) => {
    const next = new Set(current);
    if (!next.delete(id)) next.add(id);
    return next;
  });

  // A question that matches only in its answer opens by itself, so the highlighted words are visible.
  const search = (value) => {
    setQuery(value);
    const text = value.trim().toLowerCase();
    setOpen(new Set(text ? faqs.filter((faq) => !matches(faq.question, text) && matches(faq.answer, text)).map((faq) => faq.id) : []));
  };

  return (
    <SiteLayout>
      <PageHero eyebrow="FAQ" title="Frequently asked questions" text="Everything you need to know about joining ACM Alexandria, events, committees, clubs, radio, and your account.">
        <div className="relative w-full max-w-xl">
          <label htmlFor="faq-search" className="sr-only">Search questions and answers</label>
          <FiSearch aria-hidden="true" className="pointer-events-none absolute left-4 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-500" />
          <input
            id="faq-search"
            type="text"
            value={query}
            onChange={(event) => search(event.target.value)}
            placeholder="Search questions and answers"
            autoComplete="off"
            className={`${field} h-12 pl-12 pr-12`}
          />
          {query && (
            <button type="button" onClick={() => search("")} aria-label="Clear search" className={`absolute right-2 top-1/2 flex h-8 w-8 -translate-y-1/2 items-center justify-center rounded-lg text-slate-500 transition-colors hover:bg-ocean-31 hover:text-slate-900 dark:hover:bg-white/10 dark:hover:text-white ${focusRing}`}>
              <FiX aria-hidden="true" className="h-4 w-4" />
            </button>
          )}
        </div>
      </PageHero>

      <div className="mx-auto max-w-[1080px] px-5 py-12 sm:px-8 sm:py-16">
        <div className="grid gap-8 lg:grid-cols-[13rem_minmax(0,1fr)] lg:gap-16">
          {status === "ready" && groups.length > 0 && <FaqNav groups={groups} activeId={activeId || groups[0].id} />}

          <div className={`min-w-0 ${status === "ready" && groups.length > 0 ? "" : "lg:col-span-2"}`}>
            <p role="status" aria-live="polite" className={`mb-8 min-h-5 text-sm ${muted}`}>
              {status === "ready" && needle && (shown > 0 ? `${shown} ${shown === 1 ? "result" : "results"} for “${query.trim()}”` : "")}
            </p>

            {status === "loading" && <SkeletonList />}

            {status === "error" && (
              <div role="alert" className={`${errorBox} items-center justify-between gap-4`}>
                <span>We couldn’t load the questions right now.</span>
                <button type="button" onClick={() => { setStatus("loading"); setAttempt((n) => n + 1); }} className={`${secondaryButton} h-9 shrink-0 bg-white px-4 dark:bg-transparent`}>Try again</button>
              </div>
            )}

            {status === "ready" && groups.length === 0 && (
              <div className="py-10 text-center">
                <p className="text-lg font-semibold text-slate-900 dark:text-white">{needle ? `Nothing matches “${query.trim()}”` : "No questions yet"}</p>
                <p className={`mx-auto mt-2 max-w-md ${muted}`}>{needle ? "Try a shorter word, or ask the team directly." : "Check back soon, or ask the team directly."}</p>
                <div className="mt-6 flex flex-wrap justify-center gap-3">
                  {needle && <button type="button" onClick={() => search("")} className={secondaryButton}>Clear search</button>}
                  <a href={contactHref} className={primaryButton}>Contact us</a>
                </div>
              </div>
            )}

            {status === "ready" && (
              <div className="space-y-14">
                {groups.map((group) => (
                  <section key={group.id} id={group.id} data-faq-topic aria-labelledby={`${group.id}-title`} className="scroll-mt-28">
                    <h2 id={`${group.id}-title`} className="text-2xl font-semibold tracking-tight text-slate-900 dark:text-white">{group.label}</h2>
                    <ul className="mt-3">
                      {group.items.map((faq) => <FaqItem key={faq.id} faq={faq} query={query} open={open.has(faq.id)} onToggle={() => toggle(faq.id)} />)}
                    </ul>
                  </section>
                ))}
              </div>
            )}
          </div>
        </div>

        <Reveal className="mt-20 flex flex-col items-start justify-between gap-6 rounded-[2rem] bg-ocean-794 p-8 text-white sm:flex-row sm:items-center sm:p-10 dark:bg-ocean-820">
          <div>
            <h2 className="text-xl font-semibold tracking-tight">Didn’t find your answer?</h2>
            <p className="mt-1 max-w-xl text-ocean-114">Send us an email and the team will point you in the right direction.</p>
          </div>
          <a href={contactHref} className={`inline-flex h-11 shrink-0 items-center gap-2 rounded-xl bg-white px-5 text-sm font-semibold text-ocean-676 transition-colors hover:bg-ocean-67 ${focusRing}`}>Contact us <FiArrowRight className="h-4 w-4" aria-hidden="true" /></a>
        </Reveal>
      </div>
    </SiteLayout>
  );
};

export default FAQPage;
