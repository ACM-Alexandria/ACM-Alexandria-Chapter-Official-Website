import { Navigate, useSearchParams } from "react-router-dom";
import { FiAlertCircle, FiBookOpen } from "react-icons/fi";
import SiteLayout from "../components/Layout/SiteLayout";
import PageHero from "../components/Layout/PageHero";
import MediaCard, { StateTag } from "../components/ui/MediaCard";
import Pagination from "../components/ui/Pagination";
import EmptyState from "../components/ui/EmptyState";
import { fetchPrograms } from "../services/homePageService";
import usePagedList from "../hooks/usePagedList";
import { eventDate, summary } from "../utils/format";
import { skeleton } from "../components/ui/uiStyles";
import { programPath } from "../utils/slug";
import usePageMeta from "../hooks/usePageMeta";

const span = (program) => {
  if (!program.startDate) return program.time || "Schedule to be announced";
  const start = eventDate(program.startDate);
  const end = program.endDate ? eventDate(program.endDate) : null;
  return end ? `${start.day} ${start.month} to ${end.day} ${end.month}` : `Starts ${start.day} ${start.month}`;
};

// The chapter's training programs. A card links to the program's own page.
const ProgramsPage = () => {
  usePageMeta("programs");
  const list = usePagedList(fetchPrograms, "We couldn't load the programs. Please try again later.");
  const [params] = useSearchParams();
  // Links from before programs had their own pages (/programs?openProgramId=7) go to the program's page.
  const legacyId = params.get("openProgramId");
  if (legacyId) return <Navigate to={`/programs/${legacyId}`} replace />;

  return (
    <SiteLayout>
      <PageHero eyebrow="Programs" title="Longer tracks that build real skills" text="Programs meet every week and go deep on one topic. Register while a program is open." />
      <div className="mx-auto max-w-[1280px] px-5 py-16 sm:px-8 sm:py-20">
        {list.loading ? (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {Array.from({ length: 3 }, (_, i) => <div key={i} className={`aspect-[4/5] rounded-3xl ${skeleton}`} />)}
          </div>
        ) : list.error ? (
          <EmptyState icon={FiAlertCircle} tone="error" title="Something went wrong" text={list.error} />
        ) : list.items.length === 0 ? (
          <EmptyState icon={FiBookOpen} title="No programs right now" text="New programs are announced at the start of each term." />
        ) : (
          <>
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {list.items.map((program, i) => (
                <MediaCard
                  key={program.id}
                  index={i}
                  image={program.imageUrl}
                  meta={[span(program), program.startDate && program.time].filter(Boolean).join(" · ")}
                  title={program.name}
                  text={summary(program.description, 120)}
                  tag={<StateTag open={program.registrationOpen} openLabel="Registration open" closedLabel="Registration closed" />}
                  to={programPath(program)}
                />
              ))}
            </div>
            <Pagination page={list.page} totalPages={list.totalPages} onChange={list.setPage} />
          </>
        )}
      </div>
    </SiteLayout>
  );
};

export default ProgramsPage;
