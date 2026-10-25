import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { FaFacebook, FaInstagram, FaLink, FaLinkedin, FaWhatsapp } from "react-icons/fa";
import { useAuth } from "../../contexts/useAuth";
import { fetchClubById } from "../../services/homePageService";
import { cancelClubRegistration, checkClubRegistrationStatus } from "../../services/registrationService";
import useDetails from "../../hooks/useDetails";
import useRegistrationStatus from "../../hooks/useRegistrationStatus";
import { goToLogin } from "../../utils/loginRedirect";
import SidePanel, { PanelSection } from "../ui/SidePanel";
import RegistrationAction from "../ui/RegistrationAction";
import RegistrationModal from "../registration/RegistrationModal";
import RegistrationCancellationModal from "../registration/RegistrationCancellationModal";
import { muted } from "../ui/uiStyles";

const socialIcon = (url) => {
  if (url.includes("facebook.com")) return FaFacebook;
  if (url.includes("instagram.com")) return FaInstagram;
  if (url.includes("linkedin.com")) return FaLinkedin;
  if (url.includes("whatsapp.com")) return FaWhatsapp;
  return FaLink;
};

// A club's details: description, joining (internal clubs are for chapter members only), and its links.
const ClubDetailsSidebar = ({ clubId, isOpen, onClose }) => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const { data: club, loading, error } = useDetails(isOpen, clubId, fetchClubById, "We couldn't load this club.");
  const [registering, setRegistering] = useState(false);
  const [confirmCancel, setConfirmCancel] = useState(false);
  const status = useRegistrationStatus({ open: isOpen, id: clubId, check: checkClubRegistrationStatus, cancel: cancelClubRegistration, paused: registering });


  if (!clubId) return null;
  const membersOnly = !club?.isExternal && (!isAuthenticated || user?.role === "USER");
  const actionStatus = !club?.registrationOpen ? "closed" : membersOnly ? "locked" : status.checking ? "checking" : status.registered ? "registered" : "open";
  const links = club?.socialMediaLinks || [];

  return (
    <>
      <SidePanel open={isOpen} onClose={onClose} label="Club details" image={club?.imageUrl} eyebrow={club?.registrationOpen ? "Open to join" : "Club"} title={club?.name} loading={loading} error={error}>
        {club && (
          <>
            <PanelSection title="About this club">
              <p className={`whitespace-pre-line text-base leading-relaxed ${muted}`}>{club.description || "Details for this club are coming soon."}</p>
            </PanelSection>
            <RegistrationAction
              status={actionStatus}
              labels={{ open: "Join this club", registered: "You're a member", locked: "Internal club, for chapter members only" }}
              onOpen={() => (isAuthenticated ? setRegistering(true) : goToLogin(navigate, "openClubId", clubId))}
              onCancel={() => { status.setCancelError(""); setConfirmCancel(true); }}
            />
            {links.length > 0 && (
              <PanelSection title="Find the club online">
                <div className="flex flex-wrap gap-3">
                  {links.map((url) => {
                    const Icon = socialIcon(url);
                    return (
                      <a key={url} href={url} target="_blank" rel="noopener noreferrer" aria-label={url} className="flex h-11 w-11 items-center justify-center rounded-full bg-white text-[#205E85] ring-1 ring-[#D6E6F2] transition-colors hover:bg-[#205E85] hover:text-white dark:bg-[#0F2233] dark:text-[#9FD0EE] dark:ring-white/10 dark:hover:bg-[#4B98C8] dark:hover:text-[#081826]">
                        <Icon className="h-5 w-5" aria-hidden="true" />
                      </a>
                    );
                  })}
                </div>
              </PanelSection>
            )}
          </>
        )}
      </SidePanel>

      <RegistrationModal isOpen={registering} onClose={() => setRegistering(false)} entityId={clubId} type="club" entityName={club?.name} />
      <RegistrationCancellationModal
        open={confirmCancel}
        onClose={() => setConfirmCancel(false)}
        onConfirm={async () => { if (await status.cancelRegistration()) setConfirmCancel(false); }}
        entityName={club?.name}
        loading={status.cancelling}
        error={status.cancelError}
      />
    </>
  );
};

export default ClubDetailsSidebar;
