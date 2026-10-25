import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/useAuth";
import { fetchExclusiveFormById } from "../../services/homePageService";
import { cancelExclusiveFormRegistration, checkExclusiveFormRegistrationStatus } from "../../services/registrationService";
import useDetails from "../../hooks/useDetails";
import useRegistrationStatus from "../../hooks/useRegistrationStatus";
import { goToLogin } from "../../utils/loginRedirect";
import SidePanel, { PanelSection } from "../ui/SidePanel";
import RegistrationAction from "../ui/RegistrationAction";
import RegistrationModal from "../registration/RegistrationModal";
import RegistrationCancellationModal from "../registration/RegistrationCancellationModal";
import { muted } from "../ui/uiStyles";

// An exclusive opportunity's details and its application form.
const ExclusiveFormDetailsSidebar = ({ formId, isOpen, onClose }) => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const { data: form, loading, error } = useDetails(isOpen, formId, fetchExclusiveFormById, "We couldn't load this opportunity.");
  const [registering, setRegistering] = useState(false);
  const [confirmCancel, setConfirmCancel] = useState(false);
  const status = useRegistrationStatus({
    open: isOpen,
    id: formId,
    check: checkExclusiveFormRegistrationStatus,
    cancel: cancelExclusiveFormRegistration,
    paused: registering,
    failMessage: "We couldn't cancel your form submission. Please try again.",
  });


  if (!formId) return null;
  const actionStatus = !form?.isActive ? "closed" : status.checking ? "checking" : status.registered ? "registered" : "open";

  return (
    <>
      <SidePanel open={isOpen} onClose={onClose} label="Opportunity details" image={form?.imageUrl} eyebrow="Exclusive opportunity" title={form?.title} loading={loading} error={error}>
        {form && (
          <>
            <PanelSection title="About this opportunity">
              <p className={`whitespace-pre-line text-base leading-relaxed ${muted}`}>{form.description || "Details for this opportunity are coming soon."}</p>
            </PanelSection>
            <RegistrationAction
              status={actionStatus}
              labels={{ closed: "This form is closed", open: "Apply now", registered: "You've applied", cancel: "Cancel application" }}
              onOpen={() => (isAuthenticated ? setRegistering(true) : goToLogin(navigate, "openExclusiveFormId", formId))}
              onCancel={() => { status.setCancelError(""); setConfirmCancel(true); }}
            />
          </>
        )}
      </SidePanel>

      <RegistrationModal isOpen={registering} onClose={() => setRegistering(false)} entityId={formId} type="exclusive-form" entityName={form?.title} />
      <RegistrationCancellationModal
        open={confirmCancel}
        onClose={() => setConfirmCancel(false)}
        onConfirm={async () => { if (await status.cancelRegistration()) setConfirmCancel(false); }}
        entityName={form?.title}
        entityLabel="form submission"
        loading={status.cancelling}
        error={status.cancelError}
      />
    </>
  );
};

export default ExclusiveFormDetailsSidebar;
