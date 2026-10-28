import { useCallback, useState } from "react";
import { FiAlertCircle, FiBookOpen } from "react-icons/fi";
import SiteLayout from "../components/Layout/SiteLayout";
import PageHero from "../components/Layout/PageHero";
import MediaCard, { StateTag } from "../components/ui/MediaCard";
import Pagination from "../components/ui/Pagination";
import EmptyState from "../components/ui/EmptyState";
import ProgramDetailsSidebar from "../components/details/ProgramDetailsSidebar";
import { fetchPrograms } from "../services/homePageService";
import usePagedList from "../hooks/usePagedList";
import useOpenFromQuery from "../hooks/useOpenFromQuery";
import { eventDate, summary } from "../utils/format";
import { skeleton } from "../components/ui/uiStyles";

const span = (program) => {
  if (!program.startDate) return program.time || "Schedule to be announced";
  const start = eventDate(program.startDate);
  const end = program.endDate ? eventDate(program.endDate) : null;
  return end ? `${start.day} ${start.month} to ${end.day} ${end.month}` : `Starts ${start.day} ${start.month}`;
};

// The chapter's training programs. A card opens the program's details and registration.
const ProgramsPage = () => {
  const list = usePagedList(fetchPrograms, "We couldn't load the programs. Please try again later.");
  const [programId, setProgramId] = useState(null);
  const [open, setOpen] = useState(false);
  const show = useCallback((id) => { setProgramId(id); setOpen(true); }, []);
  useOpenFromQuery("openProgramId", show);

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
                  onClick={() => show(program.id)}
                />
              ))}
            </div>
            <Pagination page={list.page} totalPages={list.totalPages} onChange={list.setPage} />
          </>
        )}
      </div>
      <ProgramDetailsSidebar programId={programId} isOpen={open} onClose={() => setOpen(false)} />
    </SiteLayout>
  );
};

export default ProgramsPage;
