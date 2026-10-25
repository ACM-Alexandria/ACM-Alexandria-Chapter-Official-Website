import { FiLogOut } from "react-icons/fi";
import ConfirmDialog from "../ui/ConfirmDialog";

// Asks before signing out.
const LogoutConfirmModal = ({ open, onCancel, onConfirm }) => (
  <ConfirmDialog
    open={open}
    onClose={onCancel}
    onConfirm={onConfirm}
    icon={FiLogOut}
    tone="danger"
    title="Log out?"
    confirmLabel="Log out"
    cancelLabel="Stay signed in"
  >
    You'll need to sign in again to register or see your profile.
  </ConfirmDialog>
);

export default LogoutConfirmModal;
