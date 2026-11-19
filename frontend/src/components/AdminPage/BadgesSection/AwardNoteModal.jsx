import { useState } from "react";
import { FiAlertTriangle } from "react-icons/fi";
import { messageOf, updateAwardNote } from "../../../services/badgesAdminService";
import ModalShell from "../SharedComponents/ModalShell";
import { hintClass, labelClass, noticeClass, primaryButtonClass, secondaryButtonClass, textareaClass } from "../SharedComponents/adminStyles";
import SpinnerIcon from "../../icons/SpinnerIcon";

const MAX = 300;

// Writes or edits the description on one member's badge: why they earned it. It shows on their profile and
// on the badge's public page. Mount it while open, keyed by the award, so it starts from the saved text.
const AwardNoteModal = ({ award, onClose, onSaved }) => {
  const [note, setNote] = useState(award.note || "");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");

  const save = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError("");
    try {
      onSaved(await updateAwardNote(award.id, note.trim()));
      onClose();
    } catch (err) {
      setError(messageOf(err));
      setSaving(false);
    }
  };

  return (
    <ModalShell
      open
      onClose={onClose}
      onSubmit={save}
      busy={saving}
      title={award.note ? "Edit description" : "Add a description"}
      subtitle={`${award.member.name} · ${award.name}, ${award.periodLabel}`}
      footer={(
        <>
          <button type="button" onClick={onClose} disabled={saving} className={secondaryButtonClass}>Cancel</button>
          <button type="submit" disabled={saving || note.length > MAX} className={primaryButtonClass}>
            {saving && <SpinnerIcon className="w-4 h-4 animate-spin" />}Save
          </button>
        </>
      )}
    >
      <div className="flex flex-col gap-1.5">
        <label htmlFor="award-note" className={labelClass}>Description <span className="font-normal text-slate-500">(optional)</span></label>
        <textarea
          id="award-note"
          rows={4}
          maxLength={MAX}
          value={note}
          onChange={(e) => setNote(e.target.value)}
          placeholder="For example: Led the hackathon team and ran three workshops this month."
          className={textareaClass}
          autoFocus
        />
        <p className={`${hintClass} flex justify-between gap-3`}>
          <span>Shown on their profile and on the badge's public page. Leave it empty to remove it.</span>
          <span className="tabular-nums shrink-0">{note.length}/{MAX}</span>
        </p>
        {error && (
          <p role="alert" className={`mt-2 flex items-start gap-2.5 ${noticeClass.error}`}>
            <FiAlertTriangle className="w-5 h-5 shrink-0 mt-px" aria-hidden="true" />{error}
          </p>
        )}
      </div>
    </ModalShell>
  );
};

export default AwardNoteModal;
