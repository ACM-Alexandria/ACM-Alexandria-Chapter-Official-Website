import { useCallback, useEffect, useState } from "react";
import { useLocation, useNavigate, useParams } from "react-router-dom";
import { FiCalendar, FiClock } from "react-icons/fi";
import { useAuth } from "../contexts/useAuth";
import { fetchProgramById } from "../services/homePageService";
import { cancelProgramRegistration, checkProgramRegistrationStatus } from "../services/registrationService";
import useDetails from "../hooks/useDetails";
import usePageMeta from "../hooks/usePageMeta";
import useStructuredData from "../hooks/useStructuredData";
import useOpenFromQuery from "../hooks/useOpenFromQuery";
import useRegistrationStatus from "../hooks/useRegistrationStatus";
import { goToLogin } from "../utils/loginRedirect";
import { idFromSlug, programPath } from "../utils/slug";
import { summary } from "../utils/format";
import { organizationRef, siteUrl, venue } from "../seo/pageMeta";
import DetailPage, { DetailSection } from "../components/details/DetailPage";
import { InfoTile } from "../components/ui/SidePanel";
import RegistrationAction from "../components/ui/RegistrationAction";
import RegistrationModal from "../components/registration/RegistrationModal";
import RegistrationCancellationModal from "../components/registration/RegistrationCancellationModal";
import { muted } from "../components/ui/uiStyles";

const longDate = (value) => new Date(value).toLocaleDateString("en-US", { month: "long", day: "numeric", year: "numeric" });

// schema.org Course, so search engines can list the program as a course the chapter offers.
const courseSchema = (program, url) => ({
  "@context": "https://schema.org",
  "@type": "Course",
  name: program.name,
  description: summary(program.description || `${program.name}, a training program by the ${organizationRef.name}.`, 300),
  url,
  image: program.imageUrl || undefined,
  provider: organizationRef,
  isAccessibleForFree: true,
  hasCourseInstance: program.startDate ? {
    "@type": "CourseInstance",
    courseMode: "Onsite",
    startDate: program.startDate,
    endDate: program.endDate || undefined,
    courseSchedule: program.time ? { "@type": "Schedule", description: program.time, repeatFrequency: "P1W" } : undefined,
    location: { "@type": "Place", name: venue.name, address: venue.address },
  } : undefined,
});

// One program's own page: its dates and weekly time, what it covers, and registration.
// /programs/7-backend-track; an address with the wrong words after the number is corrected in place.
const ProgramPage = () => {
  const { slug } = useParams();
  const id = idFromSlug(slug);
  const navigate = useNavigate();
  const { pathname } = useLocation();
  const { isAuthenticated } = useAuth();
  const { data: program, loading, error } = useDetails(Boolean(id), id, fetchProgramById, "This program doesn't exist or was removed.");
  const [registering, setRegistering] = useState(false);
  const [registerAfterLogin, setRegisterAfterLogin] = useState(false);
  const [confirmCancel, setConfirmCancel] = useState(false);
  const status = useRegistrationStatus({ open: Boolean(program), id, check: checkProgramRegistrationStatus, cancel: cancelProgramRegistration, paused: registering });
  useOpenFromQuery("register", useCallback(() => setRegisterAfterLogin(true), []));

  const ready = Boolean(program) && !loading;
  const canonical = ready ? programPath(program) : null;
  useEffect(() => {
    if (canonical && canonical !== pathname) navigate(canonical, { replace: true });
  }, [canonical, pathname, navigate]);

  const actionStatus = !program?.registrationOpen ? "closed" : status.checking ? "checking" : status.registered ? "registered" : "open";

  // Back from signing in to register (?register=1): open the form once we know they aren't registered yet and
  // registration is still open. Set while rendering, so the form opens in the same pass the answer arrives.
  if (registerAfterLogin && ready && (status.checked || !isAuthenticated)) {
    setRegisterAfterLogin(false);
    if (isAuthenticated && actionStatus === "open") setRegistering(true);
  }

  usePageMeta({ title: "Program", noindex: !id || Boolean(error) }, ready ? {
    title: program.name,
    description: summary(program.description || `${program.name}, a training program by the ACM Alexandria Student Chapter.`, 160),
    path: canonical,
    image: program.imageUrl,
  } : undefined);
  useStructuredData(ready ? courseSchema(program, `${siteUrl}${canonical}`) : null);

  const dates = program?.startDate && program?.endDate
    ? `${longDate(program.startDate)} to ${longDate(program.endDate)}`
    : program?.startDate ? `Starts ${longDate(program.startDate)}` : null;
  const lead = [program?.time, dates].filter(Boolean).join(" · ") || "A training program by the ACM Alexandria Student Chapter";

  return (
    <>
      <DetailPage
        loading={Boolean(id) && loading}
        error={!id ? "This program doesn't exist or was removed." : error}
        eyebrow={program?.registrationOpen ? "Program · Registration open" : "Program"}
        title={program?.name}
        lead={lead}
        image={program?.imageUrl}
        back={{ to: "/programs", label: "All programs" }}
        aside={ready && (
          <>
            {dates && <InfoTile icon={FiCalendar} label="Dates">{dates}</InfoTile>}
            {program.time && <InfoTile icon={FiClock} label="Weekly time">{program.time}</InfoTile>}
            <div className="pt-2">
              <RegistrationAction
                status={actionStatus}
                labels={{ open: "Register for this program", registered: "You're registered" }}
                onOpen={() => (isAuthenticated ? setRegistering(true) : goToLogin(navigate, "register", 1))}
                onCancel={() => { status.setCancelError(""); setConfirmCancel(true); }}
              />
            </div>
          </>
        )}
      >
        {ready && (
          <DetailSection title="About this program">
            <p className={`whitespace-pre-line text-base leading-relaxed sm:text-lg ${muted}`}>{program.description || "Details for this program are coming soon."}</p>
          </DetailSection>
        )}
      </DetailPage>

      {ready && (
        <>
          <RegistrationModal isOpen={registering} onClose={() => setRegistering(false)} entityId={id} type="program" entityName={program.name} />
          <RegistrationCancellationModal
            open={confirmCancel}
            onClose={() => setConfirmCancel(false)}
            onConfirm={async () => { if (await status.cancelRegistration()) setConfirmCancel(false); }}
            entityName={program.name}
            loading={status.cancelling}
            error={status.cancelError}
          />
        </>
      )}
    </>
  );
};

export default ProgramPage;
