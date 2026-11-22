import { Link } from "react-router-dom";
import { FiAlertCircle, FiArrowLeft } from "react-icons/fi";
import SiteLayout from "../Layout/SiteLayout";
import PageHero from "../Layout/PageHero";
import EmptyState from "../ui/EmptyState";
import { secondaryButton, skeleton } from "../ui/uiStyles";

const backLink = "inline-flex h-11 items-center gap-2 rounded-xl px-4 text-sm font-semibold text-white ring-1 ring-white/25 transition-colors hover:bg-white/10";

// The frame of an event's or a program's own page: the deep blue hero with the name and a link back to the list,
// then the cover and details on the left and the facts and registration on the right (above the details on
// phones, so the register button is close to the top).
const DetailPage = ({ loading, error, eyebrow, title, lead, image, back, aside, children }) => (
  <SiteLayout>
    <PageHero eyebrow={eyebrow} title={loading ? "Loading…" : error ? "Not found" : title} text={loading || error ? undefined : lead}>
      <Link to={back.to} className={backLink}><FiArrowLeft className="h-4 w-4" aria-hidden="true" />{back.label}</Link>
    </PageHero>
    <div className="mx-auto max-w-[1280px] px-5 py-12 sm:px-8 sm:py-16">
      {loading ? (
        <div className="grid gap-8 lg:grid-cols-[minmax(0,1fr)_380px]" aria-busy="true">
          <div className="space-y-6">
            <div className={`aspect-[16/9] rounded-3xl ${skeleton}`} />
            <div className={`h-40 rounded-3xl ${skeleton}`} />
          </div>
          <div className={`h-64 rounded-3xl ${skeleton}`} />
        </div>
      ) : error ? (
        <EmptyState icon={FiAlertCircle} tone="error" title="We couldn't find this page" text={error}>
          <Link to={back.to} className={secondaryButton}>{back.label}</Link>
        </EmptyState>
      ) : (
        <div className="grid items-start gap-8 lg:grid-cols-[minmax(0,1fr)_380px] lg:gap-10">
          <aside className="space-y-4 lg:sticky lg:top-28 lg:order-last">{aside}</aside>
          <div className="min-w-0 space-y-10">
            {image && (
              <div className="overflow-hidden rounded-3xl bg-ocean-67 ring-1 ring-ocean-106 dark:bg-ocean-808 dark:ring-white/10">
                <img src={image} alt={title} className="aspect-[16/9] h-full w-full object-cover" />
              </div>
            )}
            {children}
          </div>
        </div>
      )}
    </div>
  </SiteLayout>
);

// A titled block in the main column.
export const DetailSection = ({ title, children }) => (
  <section>
    <h2 className="text-xl font-semibold tracking-tight text-slate-900 dark:text-white">{title}</h2>
    <div className="mt-4">{children}</div>
  </section>
);

export default DetailPage;
