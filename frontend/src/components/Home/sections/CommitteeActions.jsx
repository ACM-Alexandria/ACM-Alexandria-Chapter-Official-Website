import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { FiArrowRight, FiBell, FiCheck, FiStar } from "react-icons/fi";
import { useAuth } from "../../../contexts/useAuth";
import UnsubscribeConfirmModal from "../../UnsubscribeConfirmModal";
import RegistrationCancellationModal from "../../registration/RegistrationCancellationModal";
import { fetchCommitteeSubscriptionStatus, subscribeToCommittee, unsubscribeFromCommittee } from "../../../services/homePageService";
import { cancelCommitteeRegistration, checkCommitteeRegistrationStatus } from "../../../services/registrationService";
import { primaryButton, secondaryButton } from "../../ui/uiStyles";

// What a visitor can do with a committee: see it is theirs, follow its announcements, and apply (or cancel an
// application) while its call is open. Signed-out visitors are sent to log in and brought back to the application.
const CommitteeActions = ({ committee, isMyCommittee = false, onApply, isRegistrationModalOpen = false }) => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const [subscribed, setSubscribed] = useState(false);
  const [subscribing, setSubscribing] = useState(false);
  const [subscribeOpen, setSubscribeOpen] = useState(false);
  const [registered, setRegistered] = useState(false);
  const [checking, setChecking] = useState(false);
  const [cancelOpen, setCancelOpen] = useState(false);
  const [cancelling, setCancelling] = useState(false);
  const [cancelError, setCancelError] = useState("");
  const open = committee.open;

  useEffect(() => {
    let active = true;
    if (!isAuthenticated || !open || !user?.id || isRegistrationModalOpen) {
      if (!isRegistrationModalOpen) setRegistered(false);
      return undefined;
    }
    setChecking(true);
    checkCommitteeRegistrationStatus(committee.id, user.id)
      .then((value) => { if (active) setRegistered(value); })
      .catch((err) => console.error("Error checking committee registration:", err))
      .finally(() => { if (active) setChecking(false); });
    return () => { active = false; };
  }, [isAuthenticated, open, committee.id, user?.id, isRegistrationModalOpen]);

  useEffect(() => {
    let active = true;
    if (!isAuthenticated) {
      setSubscribed(false);
      return undefined;
    }
    fetchCommitteeSubscriptionStatus(committee.id)
      .then((data) => { if (active) setSubscribed(Boolean(data?.subscribed)); })
      .catch((err) => console.error("Error fetching committee subscription status:", err));
    return () => { active = false; };
  }, [isAuthenticated, committee.id]);

  const askToSubscribe = () => {
    if (!isAuthenticated) {
      navigate("/login");
      return;
    }
    setSubscribeOpen(true);
  };

  const toggleSubscription = async () => {
    setSubscribing(true);
    try {
      if (subscribed) await unsubscribeFromCommittee(committee.id);
      else await subscribeToCommittee(committee.id);
      setSubscribed(!subscribed);
      setSubscribeOpen(false);
    } catch (err) {
      console.error("Error toggling committee subscription:", err);
    } finally {
      setSubscribing(false);
    }
  };

  const apply = () => {
    if (isAuthenticated) {
      onApply?.(committee.id);
      return;
    }
    const path = window.location.pathname + window.location.search;
    const from = `${path}${path.includes("?") ? "&" : "?"}openCommitteeId=${committee.id}`;
    navigate("/login", { state: { from } });
  };

  const cancelApplication = async () => {
    if (!user?.id) return;
    setCancelling(true);
    setCancelError("");
    try {
      await cancelCommitteeRegistration(committee.id, user.id);
      setRegistered(false);
      setCancelOpen(false);
    } catch (err) {
      setCancelError(typeof err === "string" ? err : "We couldn't cancel your application. Please try again.");
    } finally {
      setCancelling(false);
    }
  };

  return (
    <>
      <div className="mt-6 flex flex-wrap items-center gap-3">
        {isMyCommittee && (
          <span className="inline-flex h-11 items-center gap-1.5 rounded-xl bg-[#E2F0FA] px-4 text-sm font-semibold text-[#1D6A9C] dark:bg-[#16364C] dark:text-[#9FD0EE]">
            <FiStar className="h-4 w-4 fill-current" aria-hidden="true" />My committee
          </span>
        )}
        {open && (checking ? (
          <span className="inline-flex h-11 items-center gap-2 rounded-xl px-4 text-sm font-semibold text-slate-500 ring-1 ring-[#D6E6F2] dark:text-[#A9C7DB] dark:ring-white/15">
            <span className="h-3.5 w-3.5 animate-spin rounded-full border-2 border-[#D6E6F2] border-t-[#4B98C8]" aria-hidden="true" />Checking
          </span>
        ) : registered ? (
          <>
            <span className="inline-flex h-11 items-center gap-1.5 rounded-xl bg-emerald-50 px-4 text-sm font-semibold text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-300">
              <FiCheck className="h-4 w-4" aria-hidden="true" />Applied
            </span>
            <button type="button" onClick={() => { setCancelError(""); setCancelOpen(true); }} className="h-11 rounded-xl px-3 text-sm font-semibold text-red-600 transition-colors hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-950/40">
              Cancel application
            </button>
          </>
        ) : !isMyCommittee && (
          <button type="button" onClick={apply} className={primaryButton}>
            Apply now <FiArrowRight className="h-4 w-4" aria-hidden="true" />
          </button>
        ))}
        <button type="button" onClick={askToSubscribe} disabled={subscribing} className={secondaryButton} aria-pressed={subscribed}>
          <FiBell className="h-4 w-4" aria-hidden="true" />{subscribed ? "Following" : "Follow updates"}
        </button>
      </div>

      <UnsubscribeConfirmModal open={subscribeOpen} onClose={() => setSubscribeOpen(false)} onConfirm={toggleSubscription} topicName={committee.name} loading={subscribing} isSubscribe={!subscribed} />
      <RegistrationCancellationModal open={cancelOpen} onClose={() => setCancelOpen(false)} onConfirm={cancelApplication} entityName={committee.name} entityLabel="application" loading={cancelling} error={cancelError} />
    </>
  );
};

export default CommitteeActions;
