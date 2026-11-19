import { useEffect, useState } from "react";
import { useAuth } from "../contexts/useAuth";

// Whether the signed-in user is registered for an event, club, program or form, rechecked whenever the panel
// opens or the registration dialog closes, plus cancelling that registration.
const useRegistrationStatus = ({ open, id, check, cancel, paused = false, failMessage = "We couldn't cancel your registration. Please try again." }) => {
  const { isAuthenticated, user } = useAuth();
  const [registered, setRegistered] = useState(false);
  const [checking, setChecking] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [cancelError, setCancelError] = useState("");

  useEffect(() => {
    let active = true;
    if (!open || !id || !isAuthenticated || !user?.id) {
      setRegistered(false);
      return undefined;
    }
    if (paused) return undefined;
    setChecking(true);
    check(id, user.id)
      .then((value) => { if (active) setRegistered(Boolean(value)); })
      .catch((err) => console.error("Error checking registration:", err))
      .finally(() => { if (active) setChecking(false); });
    return () => { active = false; };
  }, [open, id, isAuthenticated, user?.id, paused, check]);

  const cancelRegistration = async () => {
    if (!user?.id) return false;
    setCancelling(true);
    setCancelError("");
    try {
      await cancel(id, user.id);
      setRegistered(false);
      return true;
    } catch (err) {
      setCancelError(typeof err === "string" ? err : failMessage);
      return false;
    } finally {
      setCancelling(false);
    }
  };

  return { registered, checking, cancelling, cancelError, setCancelError, cancelRegistration };
};

export default useRegistrationStatus;
