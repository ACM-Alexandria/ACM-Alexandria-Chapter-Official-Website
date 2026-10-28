import React, { useState } from "react";
import ModalShell from "../SharedComponents/ModalShell";
import { primaryButtonClass, secondaryButtonClass } from "../SharedComponents/adminStyles";

// Mounted only while open, so the checkbox starts checked every time
const OpenCallForm = ({ onClose, onConfirm, committee, loading }) => {
  const [sendAnnouncement, setSendAnnouncement] = useState(true);

  return (
    <ModalShell
      open
      onClose={onClose}
      title="Open call?"
      subtitle={committee.name}
      size="sm"
      busy={loading}
      footer={
        <>
          <button type="button" onClick={onClose} disabled={loading} className={secondaryButtonClass}>
            Cancel
          </button>
          <button type="button" onClick={() => onConfirm(sendAnnouncement)} disabled={loading} className={primaryButtonClass}>
            {loading ? "Opening…" : "Open call"}
          </button>
        </>
      }
    >
      <p className="text-sm text-slate-700 dark:text-slate-300">
        Members will be able to apply to <span className="font-semibold text-slate-900 dark:text-slate-100">{committee.name}</span> until you close the call.
      </p>

      <label className="mt-4 flex items-start gap-3 p-3.5 rounded-lg border border-slate-200 dark:border-[#28405A] bg-slate-50 dark:bg-[#0B1520]/60 cursor-pointer">
        <input
          type="checkbox"
          checked={sendAnnouncement}
          onChange={(e) => setSendAnnouncement(e.target.checked)}
          className="mt-0.5 w-4 h-4 accent-[#4B98C8]"
        />
        <span>
          <span className="block text-sm font-medium text-slate-900 dark:text-slate-100">Email subscribers</span>
          <span className="block mt-0.5 text-xs text-slate-600 dark:text-slate-400">
            Send the call message now. You can resend it later.
          </span>
        </span>
      </label>
    </ModalShell>
  );
};

const OpenCallConfirmModal = ({ open, ...props }) => (open && props.committee ? <OpenCallForm {...props} /> : null);

export default OpenCallConfirmModal;
