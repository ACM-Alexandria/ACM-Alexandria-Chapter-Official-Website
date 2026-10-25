import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { FiCalendar, FiClock } from "react-icons/fi";
import { useAuth } from "../../contexts/useAuth";
import { fetchProgramById } from "../../services/homePageService";
import { cancelProgramRegistration, checkProgramRegistrationStatus } from "../../services/registrationService";
import useDetails from "../../hooks/useDetails";
import useRegistrationStatus from "../../hooks/useRegistrationStatus";
import { goToLogin } from "../../utils/loginRedirect";
import SidePanel, { InfoTile, PanelSection } from "../ui/SidePanel";
import RegistrationAction from "../ui/RegistrationAction";
import RegistrationModal from "../registration/RegistrationModal";
import RegistrationCancellationModal from "../registration/RegistrationCancellationModal";
import { muted } from "../ui/uiStyles";

const longDate = (value) => new Date(value).toLocaleDateString("en-US", { month: "long", day: "numeric", year: "numeric" });

// A program's details: dates, weekly time, description, and registration.
const ProgramDetailsSidebar = ({ programId, isOpen, onClose }) => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const { data: program, loading, error } = useDetails(isOpen, programId, fetchProgramById, "We couldn't load this program.");
  const [registering, setRegistering] = useState(false);
  const [confirmCancel, setConfirmCancel] = useState(false);
  const status = useRegistrationStatus({ open: isOpen, id: programId, check: checkProgramRegistrationStatus, cancel: cancelProgramRegistration, paused: registering });


  if (!programId) return null;
  const dates = program?.startDate && program?.endDate
    ? `${longDate(program.startDate)} to ${longDate(program.endDate)}`
    : program?.startDate ? longDate(program.startDate) : null;
  const actionStatus = !program?.registrationOpen ? "closed" : status.checking ? "checking" : status.registered ? "registered" : "open";

  return (
    <>
      <SidePanel open={isOpen} onClose={onClose} label="Program details" image={program?.imageUrl} eyebrow={program?.registrationOpen ? "Registration open" : "Program"} title={program?.name} loading={loading} error={error}>
        {program && (
          <>
            {(dates || program.time) && (
              <div className="grid gap-3 sm:grid-cols-2">
                {dates && <InfoTile icon={FiCalendar} label="Dates">{dates}</InfoTile>}
                {program.time && <InfoTile icon={FiClock} label="Time">{program.time}</InfoTile>}
              </div>
            )}
            <PanelSection title="About this program">
              <p className={`whitespace-pre-line text-base leading-relaxed ${muted}`}>{program.description || "Details for this program are coming soon."}</p>
            </PanelSection>
            <RegistrationAction
              status={actionStatus}
              labels={{ open: "Register for this program", registered: "You're registered" }}
              onOpen={() => (isAuthenticated ? setRegistering(true) : goToLogin(navigate, "openProgramId", programId))}
              onCancel={() => { status.setCancelError(""); setConfirmCancel(true); }}
            />
          </>
        )}
      </SidePanel>

      <RegistrationModal isOpen={registering} onClose={() => setRegistering(false)} entityId={programId} type="program" entityName={program?.name} />
      <RegistrationCancellationModal
        open={confirmCancel}
        onClose={() => setConfirmCancel(false)}
        onConfirm={async () => { if (await status.cancelRegistration()) setConfirmCancel(false); }}
        entityName={program?.name}
        loading={status.cancelling}
        error={status.cancelError}
      />
    </>
  );
};

export default ProgramDetailsSidebar;
