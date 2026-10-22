import React, { useState } from "react";
import { ProfileAvatar } from "./ProfileViewCard";
import { canShowPhoto } from "./profileUtils";

const inputClass =
  "w-full h-10 px-3 bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-md outline-none text-slate-900 dark:text-slate-100 text-sm placeholder:text-slate-500 transition-colors focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/25 disabled:opacity-60";

const Field = ({ label, htmlFor, hint, children, className = "" }) => (
  <div className={`flex flex-col gap-1.5 ${className}`}>
    <label htmlFor={htmlFor} className="text-sm font-medium text-slate-700 dark:text-slate-300">
      {label}
      {hint && <span className="ml-1 font-normal text-slate-500 dark:text-slate-400">{hint}</span>}
    </label>
    {children}
  </div>
);

const ProfileEditForm = ({
  user,
  profile,
  handleInputChange,
  handleToggleAlexEng,
  handleCancelEdit,
  handleSubmit,
  submitting,
  onImageUpload
}) => {
  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState("");

  const handleImageChange = async (e) => {
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

  const isStudent = profile.isAlexEngStudent === true || profile.isAlexEngStudent === null;

  const segmentClass = (active) =>
    `px-4 h-9 text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/40 ${
      active
        ? "bg-[#4B98C8] text-white"
        : "bg-white dark:bg-slate-900 text-slate-700 dark:text-slate-300 hover:bg-slate-50 dark:hover:bg-slate-800"
    }`;

  return (
    <form onSubmit={handleSubmit}>
      <div className="divide-y divide-slate-100 dark:divide-slate-800">

        {canShowPhoto(profile.role) && (
          <div className="px-5 py-5 flex items-center gap-4">
            <ProfileAvatar profile={profile} email={user?.email} size="sm" />
            <div className="flex flex-col gap-1">
              <label className="w-fit cursor-pointer inline-flex items-center h-9 px-3 rounded-md border border-slate-300 dark:border-slate-700 bg-white dark:bg-slate-900 text-sm font-medium text-slate-700 dark:text-slate-200 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors focus-within:ring-2 focus-within:ring-[#4B98C8]/40">
                {uploading ? "Uploading…" : "Change photo"}
                <input
                  type="file"
                  className="sr-only"
                  accept="image/*"
                  onChange={handleImageChange}
                  disabled={uploading || submitting}
                />
              </label>
              {uploadError ? (
                <span className="text-xs text-rose-600 dark:text-rose-400">{uploadError}</span>
              ) : (
                <span className="text-xs text-slate-500 dark:text-slate-400">Shown on the board and committee pages.</span>
              )}
            </div>
          </div>
        )}

        <div className="px-5 py-5 grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-5">
          <Field label="Full name" htmlFor="profile-name">
            <input
              id="profile-name"
              type="text"
              name="name"
              value={profile.name}
              onChange={handleInputChange}
              autoComplete="name"
              className={inputClass}
              required
            />
          </Field>

          <Field label="Phone number" htmlFor="profile-phone">
            <input
              id="profile-phone"
              type="tel"
              name="phoneNumber"
              value={profile.phoneNumber}
              onChange={handleInputChange}
              placeholder="01XXXXXXXXX"
              pattern="01[0-9]{9}"
              maxLength="11"
              inputMode="numeric"
              autoComplete="tel"
              title="11 digits starting with 01"
              className={`${inputClass} tabular-nums`}
              required
            />
          </Field>

          <Field label="LinkedIn" htmlFor="profile-linkedin" hint="(optional)" className="md:col-span-2">
            <input
              id="profile-linkedin"
              type="url"
              name="linkedinUrl"
              value={profile.linkedinUrl}
              onChange={handleInputChange}
              placeholder="https://linkedin.com/in/username"
              className={inputClass}
            />
          </Field>
        </div>

        <div className="px-5 py-5 flex flex-col gap-5">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
            <div>
              <p id="alex-eng-label" className="text-sm font-medium text-slate-900 dark:text-slate-100">
                Are you an Alexandria Engineering student?
              </p>
              <p className="text-sm text-slate-600 dark:text-slate-400 mt-0.5">
                We use your batch and department for committee calls and events.
              </p>
            </div>
            <div
              role="radiogroup"
              aria-labelledby="alex-eng-label"
              className="inline-flex w-fit rounded-md border border-slate-300 dark:border-slate-700 overflow-hidden divide-x divide-slate-300 dark:divide-slate-700"
            >
              <button
                type="button"
                role="radio"
                aria-checked={isStudent}
                onClick={() => handleToggleAlexEng(true)}
                className={segmentClass(isStudent)}
              >
                Yes
              </button>
              <button
                type="button"
                role="radio"
                aria-checked={profile.isAlexEngStudent === false}
                onClick={() => handleToggleAlexEng(false)}
                className={segmentClass(profile.isAlexEngStudent === false)}
              >
                No
              </button>
            </div>
          </div>

          {profile.isAlexEngStudent === true && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-x-6 gap-y-5">
              <Field label="Batch" htmlFor="profile-batch">
                <select
                  id="profile-batch"
                  name="batch"
                  value={profile.batch}
                  onChange={handleInputChange}
                  required
                  className={inputClass}
                >
                  <option value="">Select batch</option>
                  <option value="2024">2024</option>
                  <option value="2025">2025</option>
                  <option value="2026">2026</option>
                  <option value="2027">2027</option>
                  <option value="2028">2028</option>
                  <option value="2028++">2028++</option>
                  <option value="2029">2029</option>
                  <option value="2030">2030</option>
                </select>
              </Field>

              <Field label="Department" htmlFor="profile-department">
                <select
                  id="profile-department"
                  name="department"
                  value={profile.department}
                  onChange={handleInputChange}
                  required
                  className={inputClass}
                >
                  <option value="">Select department</option>
                  <option value="CSED">Computer & Systems (CSED)</option>
                  <option value="CCE">Computer & Communications (CCE)</option>
                  <option value="OTHER">Other</option>
                </select>
              </Field>
            </div>
          )}
        </div>
      </div>

      <div className="px-5 py-4 flex flex-col-reverse sm:flex-row sm:justify-end gap-2 border-t border-slate-200 dark:border-slate-800 bg-slate-50 dark:bg-slate-900/60">
        <button
          type="button"
          onClick={handleCancelEdit}
          disabled={submitting}
          className="h-9 px-4 rounded-md text-sm font-medium text-slate-700 dark:text-slate-200 bg-white dark:bg-slate-900 border border-slate-300 dark:border-slate-700 hover:bg-slate-50 dark:hover:bg-slate-800 transition-colors disabled:opacity-50"
        >
          Cancel
        </button>
        <button
          type="submit"
          disabled={submitting}
          className="h-9 px-4 rounded-md text-sm font-medium text-white bg-[#4B98C8] hover:bg-[#3d7da6] transition-colors inline-flex items-center justify-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-[#4B98C8] dark:focus-visible:ring-offset-slate-900"
        >
          {submitting && (
            <span className="w-4 h-4 rounded-full border-2 border-white/30 border-t-white animate-spin" aria-hidden="true" />
          )}
          {submitting ? "Saving…" : "Save changes"}
        </button>
      </div>
    </form>
  );
};

export default ProfileEditForm;
