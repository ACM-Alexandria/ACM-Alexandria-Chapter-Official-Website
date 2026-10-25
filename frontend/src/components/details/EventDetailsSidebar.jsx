import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { FiCalendar, FiMapPin, FiMaximize2 } from "react-icons/fi";
import { useAuth } from "../../contexts/useAuth";
import { fetchEventById } from "../../services/homePageService";
import { cancelEventRegistration, checkEventRegistrationStatus } from "../../services/registrationService";
import useDetails from "../../hooks/useDetails";
import useRegistrationStatus from "../../hooks/useRegistrationStatus";
import { goToLogin } from "../../utils/loginRedirect";
import SidePanel, { InfoTile, PanelSection } from "../ui/SidePanel";
import RegistrationAction from "../ui/RegistrationAction";
import Lightbox from "../ui/Lightbox";
import RegistrationModal from "../registration/RegistrationModal";
import RegistrationCancellationModal from "../registration/RegistrationCancellationModal";
import { muted } from "../ui/uiStyles";

const formatDateTime = (value) => {
  if (!value) return "To be announced";
  const date = new Date(value);
  return Number.isNaN(date.getTime()) ? value : date.toLocaleString("en-US", { month: "long", day: "numeric", year: "numeric", hour: "numeric", minute: "2-digit", hour12: true });
};

// An event's details: date, place, description, photos, and registration.
const EventDetailsSidebar = ({ eventId, isOpen, onClose }) => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const { data: event, loading, error } = useDetails(isOpen, eventId, fetchEventById, "We couldn't load this event.");
  const [registering, setRegistering] = useState(false);
  const [confirmCancel, setConfirmCancel] = useState(false);
  const [photo, setPhoto] = useState(null);
  const status = useRegistrationStatus({ open: isOpen, id: eventId, check: checkEventRegistrationStatus, cancel: cancelEventRegistration, paused: registering });


  if (!eventId) return null;
  const isPast = event?.eventTime ? new Date(event.eventTime) < new Date() : false;
  const photos = event?.attachedImages || [];
  const actionStatus = isPast ? "ended" : !event?.registrationOpen ? "closed" : status.checking ? "checking" : status.registered ? "registered" : "open";

  return (
    <>
      <SidePanel open={isOpen} onClose={() => { setPhoto(null); onClose(); }} label="Event details" image={event?.imageUrl} eyebrow={isPast ? "Past event" : "Upcoming event"} title={event?.name} loading={loading} error={error}>
        {event && (
          <>
            <div className="grid gap-3 sm:grid-cols-2">
              <InfoTile icon={FiCalendar} label="Date and time">{formatDateTime(event.eventTime)}</InfoTile>
              <InfoTile icon={FiMapPin} label="Location">{event.location || "To be announced"}</InfoTile>
            </div>
            <PanelSection title="About this event">
              <p className={`whitespace-pre-line text-base leading-relaxed ${muted}`}>{event.description || "Details for this event are coming soon."}</p>
            </PanelSection>
            {photos.length > 0 && (
              <PanelSection title="Photos">
                <div className="grid grid-cols-2 gap-3 sm:grid-cols-3">
                  {photos.map((src, i) => (
                    <button key={src} type="button" onClick={() => setPhoto(i)} className="group relative aspect-video overflow-hidden rounded-2xl bg-[#E2F0FA] dark:bg-[#16364C]" aria-label={`Open photo ${i + 1}`}>
                      <img src={src} alt="" className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105" loading="lazy" />
                      <span className="absolute inset-0 flex items-center justify-center bg-[#06121D]/30 opacity-0 transition-opacity group-hover:opacity-100"><FiMaximize2 className="h-5 w-5 text-white" aria-hidden="true" /></span>
                    </button>
                  ))}
                </div>
              </PanelSection>
            )}
            <RegistrationAction
              status={actionStatus}
              labels={{ ended: "This event has ended", open: "Register for this event", registered: "You're registered" }}
              onOpen={() => (isAuthenticated ? setRegistering(true) : goToLogin(navigate, "openEventId", eventId))}
              onCancel={() => { status.setCancelError(""); setConfirmCancel(true); }}
            />
          </>
        )}
      </SidePanel>

      <RegistrationModal isOpen={registering} onClose={() => setRegistering(false)} entityId={eventId} type="event" entityName={event?.name} />
      <RegistrationCancellationModal
        open={confirmCancel}
        onClose={() => setConfirmCancel(false)}
        onConfirm={async () => { if (await status.cancelRegistration()) setConfirmCancel(false); }}
        entityName={event?.name}
        loading={status.cancelling}
        error={status.cancelError}
      />
      {isOpen && photo !== null && photos.length > 0 && <Lightbox images={photos} startIndex={photo} onClose={() => setPhoto(null)} />}
    </>
  );
};

export default EventDetailsSidebar;
