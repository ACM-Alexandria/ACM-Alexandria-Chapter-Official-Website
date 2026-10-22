import React from "react";
import ModalShell from "../SharedComponents/ModalShell";
import {
  fieldClass,
  hintClass,
  labelClass,
  primaryButtonClass,
  secondaryButtonClass,
  textareaClass,
} from "../SharedComponents/adminStyles";

const CallMessageModal = ({
  open,
  onClose,
  onSubmit,
  selectedCommittee,
  messageSubject,
  setMessageSubject,
  messageBody,
  setMessageBody,
  loading,
}) => (
  <ModalShell
    open={open}
    onClose={onClose}
    onSubmit={onSubmit}
    title="Edit call email message"
    subtitle={selectedCommittee?.name}
    busy={loading}
    footer={
      <>
        <button type="button" onClick={onClose} disabled={loading} className={secondaryButtonClass}>
          Cancel
        </button>
        <button type="submit" disabled={loading} className={primaryButtonClass}>
          {loading ? "Saving…" : "Save message"}
        </button>
      </>
    }
  >
    <div className="flex flex-col gap-5">
      <p className={hintClass}>
        This email goes to active subscribers when a call for {selectedCommittee?.name || "this committee"} opens.
      </p>

      <div className="flex flex-col gap-1.5">
        <label htmlFor="call-subject" className={labelClass}>Subject</label>
        <input
          id="call-subject"
          type="text"
          required
          value={messageSubject}
          onChange={(e) => setMessageSubject(e.target.value)}
          placeholder="e.g. Call for members is open"
          className={fieldClass}
        />
      </div>

      <div className="flex flex-col gap-1.5">
        <label htmlFor="call-body" className={labelClass}>Message</label>
        <textarea
          id="call-body"
          required
          value={messageBody}
          onChange={(e) => setMessageBody(e.target.value)}
          placeholder="Write the email content here"
          rows="7"
          className={`${textareaClass} resize-y`}
        />
      </div>
    </div>
  </ModalShell>
);

export default CallMessageModal;
