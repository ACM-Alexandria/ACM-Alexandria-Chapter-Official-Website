import { FiAlertCircle, FiArrowRight, FiCheck, FiChevronDown, FiUser } from "react-icons/fi";
import { DialogBody, DialogFooter, DialogHeader } from "../ui/Dialog";
import { registrationCopy } from "./registrationCopy";
import { errorBox, field, fieldLabel, muted, primaryButton, secondaryButton } from "../ui/uiStyles";

const batches = ["2024", "2025", "2026", "2027", "2028", "2028++", "2029", "2030"];
const departments = [["CSED", "Computer and Systems (CSED)"], ["CCE", "Computer and Communications (CCE)"], ["OTHER", "Other"]];

const Select = ({ id, name, value, onChange, required, disabled, placeholder, options }) => (
  <div className="relative">
    <select id={id} name={name} value={value} onChange={onChange} required={required} disabled={disabled} className={`${field} h-12 cursor-pointer appearance-none pr-10`}>
      <option value="">{placeholder}</option>
      {options.map(([optionValue, label]) => <option key={optionValue} value={optionValue}>{label}</option>)}
    </select>
    <FiChevronDown className="pointer-events-none absolute right-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" aria-hidden="true" />
  </div>
);

// Step one when the profile is incomplete: name, phone, and (for Alexandria Engineering students) batch and
// department, saved to the profile before registering.
const ProfileGatewayModal = ({ onClose, type, entityName, profileForm, onInputChange, onToggle, onSubmit, submitting, error }) => {
  const words = registrationCopy(type);
  const student = profileForm.isAlexEngStudent;
  const choice = (active) => `inline-flex h-10 items-center gap-1.5 rounded-lg px-4 text-sm font-semibold transition-colors ${active ? "bg-white text-[#205E85] shadow-sm dark:bg-[#4B98C8] dark:text-[#081826]" : "text-slate-600 hover:text-slate-900 dark:text-[#A9C7DB] dark:hover:text-white"}`;

  return (
    <>
      <DialogHeader icon={FiUser} eyebrow={`${words.eyebrow} · Step 1 of 2`} title="Complete your profile" id="registration-title" onClose={submitting ? undefined : onClose} />
      <DialogBody>
        <form id="profile-gateway-form" onSubmit={onSubmit} className="space-y-6">
          <p className={`text-sm leading-relaxed ${muted}`}>Before you {words.verb} <span className="font-semibold text-slate-900 dark:text-white">{entityName}</span>, add the details the chapter needs to process your registration.</p>
          {error && <p className={errorBox}><FiAlertCircle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />{error}</p>}
          <div className="grid gap-5 sm:grid-cols-2">
            <div className="space-y-2">
              <label htmlFor="profile-name" className={`block ${fieldLabel}`}>Full name <span className="text-red-500" aria-hidden="true">*</span></label>
              <input id="profile-name" type="text" name="name" value={profileForm.name} onChange={onInputChange} required disabled={submitting} placeholder="Your full name" autoComplete="name" className={`${field} h-12`} />
            </div>
            <div className="space-y-2">
              <label htmlFor="profile-phone" className={`block ${fieldLabel}`}>Phone number <span className="text-red-500" aria-hidden="true">*</span></label>
              <input id="profile-phone" type="tel" name="phoneNumber" value={profileForm.phoneNumber} onChange={onInputChange} required disabled={submitting} placeholder="01XXXXXXXXX" maxLength={11} inputMode="numeric" autoComplete="tel" className={`${field} h-12`} />
            </div>
          </div>

          <div className="rounded-2xl bg-[#F3F7FB] p-5 dark:bg-white/5">
            <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
              <p className={fieldLabel}>Are you a student at Alexandria University's Faculty of Engineering? <span className="text-red-500" aria-hidden="true">*</span></p>
              <div role="radiogroup" aria-label="Faculty of Engineering student" className="inline-flex w-fit shrink-0 rounded-xl bg-white p-1 ring-1 ring-[#D6E6F2] dark:bg-[#0A1724] dark:ring-white/15">
                <button type="button" role="radio" aria-checked={student === true} onClick={() => onToggle(true)} disabled={submitting} className={choice(student === true)}>{student === true && <FiCheck className="h-4 w-4" aria-hidden="true" />}Yes</button>
                <button type="button" role="radio" aria-checked={student === false} onClick={() => onToggle(false)} disabled={submitting} className={choice(student === false)}>{student === false && <FiCheck className="h-4 w-4" aria-hidden="true" />}No</button>
              </div>
            </div>
            {student === true && (
              <div className="mt-5 grid gap-5 border-t border-[#D6E6F2] pt-5 sm:grid-cols-2 dark:border-white/10" style={{ animation: "hp-fade 0.3s ease both" }}>
                <div className="space-y-2">
                  <label htmlFor="profile-batch" className={`block ${fieldLabel}`}>Batch <span className="text-red-500" aria-hidden="true">*</span></label>
                  <Select id="profile-batch" name="batch" value={profileForm.batch} onChange={onInputChange} required disabled={submitting} placeholder="Choose your batch" options={batches.map((b) => [b, b])} />
                </div>
                <div className="space-y-2">
                  <label htmlFor="profile-department" className={`block ${fieldLabel}`}>Department <span className="text-red-500" aria-hidden="true">*</span></label>
                  <Select id="profile-department" name="department" value={profileForm.department} onChange={onInputChange} required disabled={submitting} placeholder="Choose your department" options={departments} />
                </div>
              </div>
            )}
          </div>
        </form>
      </DialogBody>
      <DialogFooter>
        <button type="button" onClick={onClose} disabled={submitting} className={secondaryButton}>Cancel</button>
        <button type="submit" form="profile-gateway-form" disabled={submitting} className={primaryButton}>{submitting ? "Saving…" : <>Save and continue <FiArrowRight className="h-4 w-4" aria-hidden="true" /></>}</button>
      </DialogFooter>
    </>
  );
};

export default ProfileGatewayModal;
