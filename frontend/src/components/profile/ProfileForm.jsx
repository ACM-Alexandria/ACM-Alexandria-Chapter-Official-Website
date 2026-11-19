import { useState } from "react";
import { FiCamera } from "react-icons/fi";
import Avatar from "../ui/Avatar";
import { field, fieldLabel, muted, primaryButton, secondaryButton } from "../ui/uiStyles";
import { canShowPhoto } from "../UserProfile/profileUtils";

const BATCHES = ["2024", "2025", "2026", "2027", "2028", "2028++", "2029", "2030"];

const Field = ({ label, htmlFor, hint, className = "", children }) => (
  <div className={`flex flex-col gap-1.5 ${className}`}>
    <label htmlFor={htmlFor} className={fieldLabel}>{label}{hint && <span className={`ml-1 font-normal ${muted}`}>{hint}</span>}</label>
    {children}
  </div>
);

const choice = (active) => `h-10 px-5 text-sm font-semibold transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-inset focus-visible:ring-ocean-461 ${active ? "bg-ocean-676 text-white dark:bg-ocean-461 dark:text-ocean-910" : "bg-white text-slate-700 hover:bg-ocean-31 dark:bg-transparent dark:text-slate-200 dark:hover:bg-white/5"}`;

// The edit form for your own details. Board members and admins can also change their photo, which uploads
// straight away through `onImageUpload`; everything else is saved with the form.
const ProfileForm = ({ email, profile, onChange, onToggleStudent, onCancel, onSubmit, submitting, onImageUpload }) => {
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState("");
  const student = profile.isAlexEngStudent === true || profile.isAlexEngStudent === null;

  const changePhoto = async (e) => {
    const file = e.target.files?.[0];
    if (!file) return;
    try {
      setUploading(true);
      setUploadError("");
      await onImageUpload(file);
    } catch {
      setUploadError("Couldn't upload that image. Try a smaller JPG or PNG.");
    } finally {
      setUploading(false);
      e.target.value = "";
    }
  };

  return (
    <form onSubmit={onSubmit} className="flex flex-col gap-8">
      {canShowPhoto(profile.role) && (
        <div className="flex items-center gap-5">
          <Avatar name={profile.name || email} src={profile.profileImageUrl} className="h-20 w-20 text-2xl" />
          <div className="flex flex-col gap-1.5">
            <label className={`w-fit cursor-pointer focus-within:ring-2 focus-within:ring-ocean-461 ${secondaryButton}`}>
              <FiCamera className="h-4 w-4" aria-hidden="true" />{uploading ? "Uploading…" : "Change photo"}
              <input type="file" accept="image/*" onChange={changePhoto} disabled={uploading || submitting} className="sr-only" />
            </label>
            <span className={`text-xs ${uploadError ? "text-red-600 dark:text-red-300" : muted}`}>{uploadError || "Shown on the board and committee pages."}</span>
          </div>
        </div>
      )}

      <div className="grid gap-5 sm:grid-cols-2">
        <Field label="Full name" htmlFor="profile-name">
          <input id="profile-name" type="text" name="name" value={profile.name} onChange={onChange} autoComplete="name" required className={`h-12 ${field}`} />
        </Field>
        <Field label="Phone number" htmlFor="profile-phone">
          <input id="profile-phone" type="tel" name="phoneNumber" value={profile.phoneNumber} onChange={onChange} placeholder="01XXXXXXXXX" pattern="01[0-9]{9}" maxLength="11" inputMode="numeric" autoComplete="tel" title="11 digits starting with 01" required className={`h-12 tabular-nums ${field}`} />
        </Field>
        <Field label="LinkedIn" htmlFor="profile-linkedin" hint="(optional)" className="sm:col-span-2">
          <input id="profile-linkedin" type="url" name="linkedinUrl" value={profile.linkedinUrl} onChange={onChange} placeholder="https://linkedin.com/in/username" className={`h-12 ${field}`} />
        </Field>
      </div>

      <div className="flex flex-col gap-5 border-t border-ocean-69 pt-8 dark:border-white/10">
        <div className="flex flex-col justify-between gap-3 sm:flex-row sm:items-center">
          <div>
            <p id="alex-eng-label" className="text-sm font-semibold text-slate-900 dark:text-white">Are you an Alexandria Engineering student?</p>
            <p className={`mt-0.5 text-sm ${muted}`}>We use your batch and department for committee calls and events.</p>
          </div>
          <div role="radiogroup" aria-labelledby="alex-eng-label" className="inline-flex w-fit shrink-0 divide-x divide-ocean-106 overflow-hidden rounded-xl ring-1 ring-ocean-106 dark:divide-white/15 dark:ring-white/15">
            <button type="button" role="radio" aria-checked={student} onClick={() => onToggleStudent(true)} className={choice(student)}>Yes</button>
            <button type="button" role="radio" aria-checked={profile.isAlexEngStudent === false} onClick={() => onToggleStudent(false)} className={choice(profile.isAlexEngStudent === false)}>No</button>
          </div>
        </div>
        {profile.isAlexEngStudent === true && (
          <div className="grid gap-5 sm:grid-cols-2">
            <Field label="Batch" htmlFor="profile-batch">
              <select id="profile-batch" name="batch" value={profile.batch} onChange={onChange} required className={`h-12 ${field}`}>
                <option value="">Select batch</option>
                {BATCHES.map((batch) => <option key={batch} value={batch}>{batch}</option>)}
              </select>
            </Field>
            <Field label="Department" htmlFor="profile-department">
              <select id="profile-department" name="department" value={profile.department} onChange={onChange} required className={`h-12 ${field}`}>
                <option value="">Select department</option>
                <option value="CSED">Computer & Systems (CSED)</option>
                <option value="CCE">Computer & Communications (CCE)</option>
                <option value="OTHER">Other</option>
              </select>
            </Field>
          </div>
        )}
      </div>

      <div className="flex flex-col-reverse gap-3 sm:flex-row sm:justify-end">
        <button type="button" onClick={onCancel} disabled={submitting} className={secondaryButton}>Cancel</button>
        <button type="submit" disabled={submitting} className={primaryButton}>
          {submitting && <span className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white" aria-hidden="true" />}
          {submitting ? "Saving…" : "Save changes"}
        </button>
      </div>
    </form>
  );
};

export default ProfileForm;
