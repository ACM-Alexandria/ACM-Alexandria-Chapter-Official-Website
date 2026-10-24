import { useState } from "react";
import ModalShell from "../SharedComponents/ModalShell";
import { fieldClass, labelClass, noticeClass, primaryButtonClass, secondaryButtonClass } from "../SharedComponents/adminStyles";
import { isPastBoardSeason } from "../../../utils/validation";

// Changes the year of a season. The backend moves every record and rejects a year that already exists.
const RenameSeasonModal = ({ season, currentSeason, saving, serverError, onSave, onClose }) => {
  const [year, setYear] = useState(String(season));
  const [error, setError] = useState("");

  const submit = (event) => {
    event.preventDefault();
    if (saving) return;
    if (!isPastBoardSeason(year, currentSeason)) {
      setError(`Enter a season between 2000 and ${currentSeason - 1}.`);
      return;
    }
    if (Number(year) === season) {
      onClose();
      return;
    }
    setError("");
    onSave(Number(year));
  };

  return (
    <ModalShell
      open
      size="sm"
      onClose={onClose}
      onSubmit={submit}
      busy={saving}
      title="Change season year"
      subtitle={`Currently ${season}. Every record of this season moves to the new year.`}
      footer={(
        <>
          <button type="button" disabled={saving} onClick={onClose} className={secondaryButtonClass}>Cancel</button>
          <button type="submit" disabled={saving} className={primaryButtonClass}>{saving ? "Saving…" : "Save year"}</button>
        </>
      )}
    >
      <label className={`block ${labelClass}`}>
        New season
        <input
          className={`mt-1.5 ${fieldClass}`}
          inputMode="numeric"
          maxLength={4}
          autoComplete="off"
          value={year}
          autoFocus
          onChange={(event) => { setYear(event.target.value); setError(""); }}
        />
      </label>
      {(error || serverError) && <p role="alert" className={`mt-4 ${noticeClass.error}`}>{error || serverError}</p>}
    </ModalShell>
  );
};

export default RenameSeasonModal;
