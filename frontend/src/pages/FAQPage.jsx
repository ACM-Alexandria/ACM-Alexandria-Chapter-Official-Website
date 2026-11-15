import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";
import { FiChevronDown, FiHelpCircle, FiMail, FiSearch } from "react-icons/fi";
import Navbar from "../components/HomePage/Navbar";
import Footer from "../components/HomePage/Footer";
import { fetchFAQs } from "../services/faqService";

const CATEGORY_ORDER = ["Joining", "Events", "Committees", "Radio", "Accounts"];

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
    <div className="min-h-screen flex flex-col bg-white dark:bg-slate-950 text-slate-800 dark:text-slate-100">
      <Navbar activeSection="" />
      <main className="flex-1 pt-[74px]">
        <section className="relative overflow-hidden border-b border-slate-200 dark:border-slate-800 bg-gradient-to-b from-slate-50 via-white to-white px-6 py-16 dark:from-slate-900 dark:via-slate-950 dark:to-slate-950 md:py-20">
          <div className="absolute left-1/2 top-0 h-72 w-[42rem] -translate-x-1/2 rounded-full bg-[#4B98C8]/10 blur-3xl" />
          <div className="relative mx-auto max-w-3xl text-center">
            <div className="mx-auto mb-5 flex h-14 w-14 items-center justify-center rounded-2xl bg-[#4B98C8]/15 text-[#205E85] dark:text-[#7DBBE0]"><FiHelpCircle className="h-7 w-7" /></div>
            <h1 className="text-4xl font-black tracking-tight text-slate-900 dark:text-white sm:text-5xl">Frequently asked <span className="text-[#4B98C8]">questions</span></h1>
            <p className="mx-auto mt-5 max-w-2xl text-base leading-relaxed text-slate-600 dark:text-slate-300 sm:text-lg">Everything you need to know about joining ACM Alexandria, events, committees, radio, and your account.</p>
            <label className="relative mx-auto mt-9 block max-w-xl text-left">
              <span className="sr-only">Search FAQs</span>
              <FiSearch className="pointer-events-none absolute left-5 top-1/2 h-5 w-5 -translate-y-1/2 text-slate-400" />
              <input value={query} onChange={(event) => { setQuery(event.target.value); setPage(0); }} placeholder="Search questions and answers…" className="h-14 w-full rounded-2xl border border-slate-200 bg-white pl-13 pr-5 text-sm font-medium shadow-lg shadow-slate-200/50 outline-none transition focus:border-[#4B98C8] focus:ring-4 focus:ring-[#4B98C8]/15 dark:border-slate-700 dark:bg-slate-900 dark:shadow-none" />
            </label>
          </div>
        </section>

        <section className="mx-auto w-full max-w-4xl px-6 py-14 md:py-18">
          {loading && <p className="py-16 text-center text-slate-500">Loading questions…</p>}
          {error && <div role="alert" className="rounded-2xl border border-rose-200 bg-rose-50 p-5 text-center text-rose-700 dark:border-rose-900 dark:bg-rose-950/30 dark:text-rose-300">{error}</div>}
          {!loading && !error && groupedFAQs.length === 0 && <div className="rounded-2xl border border-dashed border-slate-300 p-10 text-center text-slate-500 dark:border-slate-700 dark:text-slate-400">No questions match your search.</div>}
          <div className="space-y-12">
            {groupedFAQs.map(({ category, items }) => (
              <section key={category} aria-labelledby={`faq-${category}`}>
                <h2 id={`faq-${category}`} className="mb-4 text-xl font-black text-slate-900 dark:text-white">{category}</h2>
                <div className="overflow-hidden rounded-2xl border border-slate-200 dark:border-slate-800">
                  {items.map((faq) => {
                    const open = openId === faq.id;
                    return <article key={faq.id} className="border-b border-slate-200 last:border-b-0 dark:border-slate-800">
                      <button type="button" aria-expanded={open} onClick={() => setOpenId(open ? null : faq.id)} className="flex w-full items-center justify-between gap-4 px-5 py-5 text-left font-bold text-slate-800 transition hover:bg-slate-50 dark:text-slate-100 dark:hover:bg-slate-900 sm:px-6">
                        <span>{faq.question}</span><FiChevronDown className={`h-5 w-5 shrink-0 text-[#4B98C8] transition-transform ${open ? "rotate-180" : ""}`} />
                      </button>
                      {open && <div className="px-5 pb-5 text-sm leading-7 text-slate-600 dark:text-slate-300 sm:px-6">{faq.answer}</div>}
                    </article>;
                  })}
                </div>
              </section>
            ))}
          </div>
          {!loading && !error && pageInfo.totalPages > 1 && <nav aria-label="FAQ pages" className="mt-10 flex items-center justify-center gap-4"><button type="button" disabled={page === 0} onClick={() => setPage((current) => current - 1)} className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-bold disabled:cursor-not-allowed disabled:opacity-40 dark:border-slate-700">Previous</button><span className="text-sm text-slate-500 dark:text-slate-400">Page {page + 1} of {pageInfo.totalPages} · {pageInfo.totalElements} questions</span><button type="button" disabled={page >= pageInfo.totalPages - 1} onClick={() => setPage((current) => current + 1)} className="rounded-xl border border-slate-200 px-4 py-2 text-sm font-bold disabled:cursor-not-allowed disabled:opacity-40 dark:border-slate-700">Next</button></nav>}
        </section>

        <section className="mx-auto mb-16 w-[calc(100%-3rem)] max-w-4xl rounded-3xl bg-gradient-to-r from-[#205E85] to-[#4B98C8] px-7 py-10 text-center text-white shadow-xl shadow-blue-900/15 md:px-12">
          <FiMail className="mx-auto h-7 w-7" />
          <h2 className="mt-4 text-2xl font-black">Still have questions?</h2>
          <p className="mx-auto mt-2 max-w-xl text-sm leading-6 text-blue-50">We’re happy to help. Reach out through our contact section and the team will point you in the right direction.</p>
          <Link to="/#footer" className="mt-6 inline-flex rounded-xl bg-white px-5 py-3 text-sm font-bold text-[#205E85] transition hover:-translate-y-0.5 hover:shadow-lg">Contact us</Link>
        </section>
      </main>
      <Footer />
    </div>
  );
};

export default FAQPage;
