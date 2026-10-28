import { FiAlertCircle } from "react-icons/fi";
import ConfirmDialog from "../ui/ConfirmDialog";

// Asks before cancelling a registration, application or form submission.
const RegistrationCancellationModal = ({ open, onClose, onConfirm, entityName, entityLabel = "registration", loading, error }) => (
  <ConfirmDialog
    open={open}
    onClose={onClose}
    onConfirm={onConfirm}
    icon={FiAlertCircle}
    tone="danger"
    title={`Cancel your ${entityLabel}?`}
    confirmLabel="Yes, cancel"
    cancelLabel="Keep it"
    loading={loading}
    loadingLabel="Cancelling…"
    error={error}
  >
    Your {entityLabel} for <span className="font-semibold text-slate-900 dark:text-white">{entityName}</span> will be cancelled. You can register again later.
  </ConfirmDialog>
);

export default RegistrationCancellationModal;
