import { useCallback, useState } from "react";
import { FiAlertCircle, FiCalendar } from "react-icons/fi";
import SiteLayout from "../components/Layout/SiteLayout";
import PageHero from "../components/Layout/PageHero";
import MediaCard, { StateTag } from "../components/ui/MediaCard";
import Pagination from "../components/ui/Pagination";
import EmptyState from "../components/ui/EmptyState";
import EventDetailsSidebar from "../components/details/EventDetailsSidebar";
import { fetchEvents } from "../services/homePageService";
import usePagedList from "../hooks/usePagedList";
import useOpenFromQuery from "../hooks/useOpenFromQuery";
import { eventDate, summary } from "../utils/format";
import { skeleton } from "../components/ui/uiStyles";

// Every chapter event, newest first, twelve to a page. A card opens the event's details and registration.
const EventsPage = () => {
  const list = usePagedList(fetchEvents, "We couldn't load the events. Please try again later.");
  const [eventId, setEventId] = useState(null);
  const [open, setOpen] = useState(false);
  const show = useCallback((id) => { setEventId(id); setOpen(true); }, []);
  useOpenFromQuery("openEventId", show);
  const now = new Date();

  return (
    <SiteLayout>
      <PageHero eyebrow="Events" title="Workshops, talks, and competitions" text="Everything the chapter runs during the year, open to every student. Pick an event to see the details and register." />
      <div className="mx-auto max-w-[1280px] px-5 py-16 sm:px-8 sm:py-20">
        {list.loading ? (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
            {Array.from({ length: 6 }, (_, i) => <div key={i} className={`aspect-[4/5] rounded-3xl ${skeleton}`} />)}
          </div>
        ) : list.error ? (
          <EmptyState icon={FiAlertCircle} tone="error" title="Something went wrong" text={list.error} />
        ) : list.items.length === 0 ? (
          <EmptyState icon={FiCalendar} title="No events yet" text="New events will show up here as soon as they're announced." />
        ) : (
          <>
            <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-3">
              {list.items.map((event, i) => {
                const past = event.eventTime && new Date(event.eventTime) < now;
                return (
                  <MediaCard
                    key={event.id}
                    index={i}
                    image={event.imageUrl}
                    meta={event.eventTime ? eventDate(event.eventTime).full : "Date to be announced"}
                    title={event.name}
                    text={summary(event.description, 120)}
                    tag={past ? <span className="rounded-full bg-white/90 px-3 py-1 text-xs font-semibold text-slate-600 shadow-sm">Ended</span> : <StateTag open={event.registrationOpen} openLabel="Registration open" closedLabel="Registration closed" />}
                    onClick={() => show(event.id)}
                  />
                );
              })}
            </div>
            <Pagination page={list.page} totalPages={list.totalPages} onChange={list.setPage} />
          </>
        )}
      </div>
      <EventDetailsSidebar eventId={eventId} isOpen={open} onClose={() => setOpen(false)} />
    </SiteLayout>
  );
};

export default EventsPage;
