import { useCallback, useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { FiCalendar, FiClock, FiMapPin, FiMaximize2 } from "react-icons/fi";
import { useAuth } from "../contexts/useAuth";
import { fetchEventById } from "../services/homePageService";
import { cancelEventRegistration, checkEventRegistrationStatus } from "../services/registrationService";
import useDetails from "../hooks/useDetails";
import usePageMeta from "../hooks/usePageMeta";
import useStructuredData from "../hooks/useStructuredData";
import useOpenFromQuery from "../hooks/useOpenFromQuery";
import useRegistrationStatus from "../hooks/useRegistrationStatus";
import { goToLogin } from "../utils/loginRedirect";
import { eventPath, idFromSlug } from "../utils/slug";
import { isEventPast, summary } from "../utils/format";
import { organizationRef, siteUrl, venue } from "../seo/pageMeta";
import DetailPage, { DetailSection } from "../components/details/DetailPage";
import EventReviews from "../components/details/EventReviews";
import { InfoTile } from "../components/ui/SidePanel";
import RegistrationAction from "../components/ui/RegistrationAction";
import Lightbox from "../components/ui/Lightbox";
import RegistrationModal from "../components/registration/RegistrationModal";
import RegistrationCancellationModal from "../components/registration/RegistrationCancellationModal";
import { muted } from "../components/ui/uiStyles";

const dateFormat = { weekday: "long", month: "long", day: "numeric", year: "numeric" };
const timeFormat = { hour: "numeric", minute: "2-digit", hour12: true };
const valid = (value) => value && !Number.isNaN(new Date(value).getTime());

// "6:00 PM to 9:00 PM", or one time when the event has no end (or ends on another day, shown under the date).
const timeRange = (start, end) => {
  const from = new Date(start).toLocaleTimeString("en-US", timeFormat);
  if (!valid(end) || new Date(end).toDateString() !== new Date(start).toDateString()) return from;
  return `${from} to ${new Date(end).toLocaleTimeString("en-US", timeFormat)}`;
};

// schema.org Event, for Google's event results. Times are the chapter's local (Cairo) time, as the admin entered them.
const eventSchema = (event, url) => ({
  "@context": "https://schema.org",
  "@type": "Event",
  name: event.name,
  description: summary(event.description || "", 300) || undefined,
  url,
  image: [event.imageUrl, ...(event.attachedImages || [])].filter(Boolean),
  startDate: event.eventTime,
  endDate: event.endTime || undefined,
  eventStatus: "https://schema.org/EventScheduled",
  eventAttendanceMode: "https://schema.org/OfflineEventAttendanceMode",
  isAccessibleForFree: true,
  // Events are held on campus or at outside venues, so the place is the event's own location; only the city is certain.
  location: { "@type": "Place", name: event.location || venue.name, address: venue.address },
  organizer: organizationRef,
});

// One event's own page: when and where, what it is, its photos, registration, and once it's over, reviews.
// /events/12-icpc-training-camp; an address with the wrong words after the number is corrected in place.
const EventPage = () => {
  const { slug } = useParams();
  const id = idFromSlug(slug);
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { isAuthenticated } = useAuth();
  const { data: event, loading, error } = useDetails(Boolean(id), id, fetchEventById, "This event doesn't exist or was removed.");
  const [registering, setRegistering] = useState(false);
  const [registerAfterLogin, setRegisterAfterLogin] = useState(false);
  const [confirmCancel, setConfirmCancel] = useState(false);
  const [photo, setPhoto] = useState(null);
  const status = useRegistrationStatus({ open: Boolean(event), id, check: checkEventRegistrationStatus, cancel: cancelEventRegistration, paused: registering });
  useOpenFromQuery("register", useCallback(() => setRegisterAfterLogin(true), []));

  const ready = Boolean(event) && !loading;
  const canonical = ready ? eventPath(event) : null;
  useEffect(() => {
    if (canonical && canonical !== pathname) navigate(canonical, { replace: true });
  }, [canonical, pathname, navigate]);

  const isPast = ready && isEventPast(event);
  const actionStatus = isPast ? "ended" : !event?.registrationOpen ? "closed" : status.checking ? "checking" : status.registered ? "registered" : "open";

  // Back from signing in to register (?register=1): open the form once we know they aren't registered yet and
  // registration is still open. Set while rendering, so the form opens in the same pass the answer arrives.
  if (registerAfterLogin && ready && (status.checked || !isAuthenticated)) {
    setRegisterAfterLogin(false);
    if (isAuthenticated && actionStatus === "open") setRegistering(true);
  }

  const start = ready && valid(event.eventTime) ? new Date(event.eventTime) : null;
  const endsOtherDay = start && valid(event.endTime) && new Date(event.endTime).toDateString() !== start.toDateString();
  usePageMeta({ title: "Event", noindex: !id || Boolean(error) }, ready ? {
    title: event.name,
    description: summary(event.description || `${event.name}, an event by the ACM Alexandria Student Chapter.`, 160),
    path: canonical,
    image: event.imageUrl,
  } : undefined);
  useStructuredData(ready && start ? eventSchema(event, `${siteUrl}${canonical}`) : null);

  const photos = event?.attachedImages || [];
  const lead = start ? start.toLocaleDateString("en-US", dateFormat) + (event.location ? ` · ${event.location}` : "") : "Date to be announced";
  const toLogin = () => navigate("/login", { state: { from: pathname } });

  return (
    <>
      <DetailPage
        loading={Boolean(id) && loading}
        error={!id ? "This event doesn't exist or was removed." : error}
        eyebrow={isPast ? "Past event" : "Event"}
        title={event?.name}
        lead={lead}
        image={event?.imageUrl}
        back={{ to: "/events", label: "All events" }}
        aside={ready && (
          <>
            <InfoTile icon={FiCalendar} label="Date">
              {start ? start.toLocaleDateString("en-US", dateFormat) : "To be announced"}
              {endsOtherDay && <> to {new Date(event.endTime).toLocaleDateString("en-US", dateFormat)}</>}
            </InfoTile>
            {start && <InfoTile icon={FiClock} label="Time">{timeRange(event.eventTime, event.endTime)}</InfoTile>}
            <InfoTile icon={FiMapPin} label="Location">{event.location || "To be announced"}</InfoTile>
            <div className="pt-2">
              <RegistrationAction
                status={actionStatus}
                labels={{ ended: "This event has ended", open: "Register for this event", registered: "You're registered" }}
                onOpen={() => (isAuthenticated ? setRegistering(true) : goToLogin(navigate, "register", 1))}
                onCancel={() => { status.setCancelError(""); setConfirmCancel(true); }}
              />
            </div>
          </>
        )}
      >
        {ready && (
          <>
            <DetailSection title="About this event">
              <p className={`whitespace-pre-line text-base leading-relaxed sm:text-lg ${muted}`}>{event.description || "Details for this event are coming soon."}</p>
            </DetailSection>
            {photos.length > 0 && (
              <DetailSection title="Photos">
                <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
                  {photos.map((src, i) => (
                    <button key={src} type="button" onClick={() => setPhoto(i)} className="group relative aspect-video overflow-hidden rounded-2xl bg-ocean-67 dark:bg-ocean-808" aria-label={`Open photo ${i + 1} of ${event.name}`}>
                      <img src={src} alt="" className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105" loading="lazy" />
                      <span className="absolute inset-0 flex items-center justify-center bg-ocean-931/30 opacity-0 transition-opacity group-hover:opacity-100"><FiMaximize2 className="h-5 w-5 text-white" aria-hidden="true" /></span>
                    </button>
                  ))}
                </div>
              </DetailSection>
            )}
            <EventReviews eventId={id} isPast={isPast} isLoggedIn={isAuthenticated} isRegistered={status.registered} onLoginRequired={toLogin} />
          </>
        )}
      </DetailPage>

      {ready && (
        <>
          <RegistrationModal isOpen={registering} onClose={() => setRegistering(false)} entityId={id} type="event" entityName={event.name} />
          <RegistrationCancellationModal
            open={confirmCancel}
            onClose={() => setConfirmCancel(false)}
            onConfirm={async () => { if (await status.cancelRegistration()) setConfirmCancel(false); }}
            entityName={event.name}
            loading={status.cancelling}
            error={status.cancelError}
          />
        </>
      )}
      {photo !== null && photos.length > 0 && <Lightbox images={photos} startIndex={photo} onClose={() => setPhoto(null)} />}
    </>
  );
};

export default EventPage;
