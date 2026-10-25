import { FiBell, FiBellOff } from "react-icons/fi";
import ConfirmDialog from "./ui/ConfirmDialog";

// Asks before following or unfollowing a committee's calls or the chapter newsletter.
const UnsubscribeConfirmModal = ({ open, onClose, onConfirm, topicName, loading, isSubscribe = false }) => (
  <ConfirmDialog
    open={open}
    onClose={onClose}
    onConfirm={onConfirm}
    icon={isSubscribe ? FiBell : FiBellOff}
    tone={isSubscribe ? "primary" : "danger"}
    title={isSubscribe ? "Turn on updates?" : "Turn off updates?"}
    confirmLabel={isSubscribe ? "Yes, notify me" : "Yes, turn off"}
    loading={loading}
    loadingLabel={isSubscribe ? "Turning on…" : "Turning off…"}
  >
    {isSubscribe
      ? <>We'll email you when <span className="font-semibold text-slate-900 dark:text-white">{topicName}</span> opens a call.</>
      : <>You'll stop getting emails from <span className="font-semibold text-slate-900 dark:text-white">{topicName}</span>.</>}
  </ConfirmDialog>
);

export default UnsubscribeConfirmModal;
