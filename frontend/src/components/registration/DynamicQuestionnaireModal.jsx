import { useEffect, useMemo } from "react";
import { FiAlertCircle, FiArrowRight, FiCheck, FiChevronDown, FiClipboard, FiImage, FiX } from "react-icons/fi";
import { DialogBody, DialogFooter, DialogHeader } from "../ui/Dialog";
import SuccessView from "./SuccessView";
import { registrationCopy } from "./registrationCopy";
import { errorBox, field, fieldLabel, muted, primaryButton, secondaryButton } from "../ui/uiStyles";

const MAX_MB = 2;

// The picked image with its size, or a drop area to pick one (2 MB at most).
const ImageAnswer = ({ file, disabled, onChange }) => {
  const preview = useMemo(() => (file ? URL.createObjectURL(file) : null), [file]);
  useEffect(() => () => { if (preview) URL.revokeObjectURL(preview); }, [preview]);
  const tooLarge = file && file.size > MAX_MB * 1024 * 1024;

  if (file) {
    return (
      <div className={`overflow-hidden rounded-2xl ring-2 ${tooLarge ? "ring-red-400" : "ring-[#4B98C8]/40"}`}>
        <div className="relative bg-[#F3F7FB] dark:bg-white/5">
          <img src={preview} alt="Selected upload" className="max-h-56 w-full object-contain" />
          <button type="button" disabled={disabled} onClick={() => onChange(null)} aria-label="Remove image" className="absolute right-2 top-2 flex h-8 w-8 items-center justify-center rounded-full bg-[#06121D]/60 text-white transition-colors hover:bg-red-600">
            <FiX className="h-4 w-4" aria-hidden="true" />
          </button>
        </div>
        <div className="flex items-center justify-between gap-3 px-4 py-2 text-xs">
          <span className={`truncate font-medium ${muted}`}>{file.name}</span>
          <span className={`shrink-0 font-semibold ${tooLarge ? "text-red-600" : muted}`}>{(file.size / (1024 * 1024)).toFixed(2)} MB</span>
        </div>
        {tooLarge && <p className="px-4 pb-3 text-xs font-semibold text-red-600">This image is larger than {MAX_MB} MB. Please choose a smaller one.</p>}
      </div>
    );
  }
  return (
    <label className={`flex w-full cursor-pointer flex-col items-center justify-center gap-3 rounded-2xl border-2 border-dashed border-[#D6E6F2] bg-[#F3F7FB] py-8 transition-colors hover:border-[#4B98C8] dark:border-white/15 dark:bg-white/5 ${disabled ? "pointer-events-none opacity-60" : ""}`}>
      <span className="flex h-12 w-12 items-center justify-center rounded-xl bg-[#E2F0FA] text-[#205E85] dark:bg-[#16364C] dark:text-[#7DBBE0]"><FiImage className="h-5 w-5" aria-hidden="true" /></span>
      <span className="text-center">
        <span className="block text-sm font-semibold text-slate-800 dark:text-slate-100">Choose an image</span>
        <span className={`mt-0.5 block text-xs ${muted}`}>JPG, PNG, WEBP or GIF, up to {MAX_MB} MB</span>
      </span>
      <input type="file" accept="image/*" className="sr-only" disabled={disabled} onChange={(event) => { onChange(event.target.files?.[0] || null); event.target.value = ""; }} />
    </label>
  );
};

// Registration with custom questions: text, single choice, multiple choice, and image answers.
const DynamicQuestionnaireModal = ({ onClose, type, entityName, questions, answers, onInputChange, onSubmit, submitting, error, success }) => {
  const words = registrationCopy(type);
  if (success) return <SuccessView title={words.done} entityName={entityName} onClose={onClose} />;

  return (
    <>
      <DialogHeader icon={FiClipboard} eyebrow={words.eyebrow} title={entityName} id="registration-title" onClose={submitting ? undefined : onClose} />
      <DialogBody>
        <form id="registration-questionnaire-form" onSubmit={onSubmit} className="space-y-6">
          {error && <p className={errorBox}><FiAlertCircle className="mt-0.5 h-4 w-4 shrink-0" aria-hidden="true" />{error}</p>}
          {questions.map((question) => {
            const inputId = `question-${question.id}`;
            const value = answers[question.id];
            return (
              <div key={question.id} className="space-y-2">
                <label htmlFor={question.question_type === "IMAGE" || question.question_type === "CHECKBOX" ? undefined : inputId} className={`block ${fieldLabel}`}>
                  {question.question_text}{question.is_required && <span className="ml-1 text-red-500" aria-hidden="true">*</span>}
                </label>
                {question.question_type === "TEXT" ? (
                  <textarea id={inputId} rows={3} value={value || ""} onChange={(event) => onInputChange(question.id, event.target.value)} required={question.is_required} disabled={submitting} placeholder="Your answer" className={`${field} resize-none py-3`} />
                ) : question.question_type === "CHECKBOX" ? (
                  <div role="group" aria-label={question.question_text} className="grid gap-2">
                    {(question.options || []).map((option) => {
                      const selected = Array.isArray(value) ? value : [];
                      const checked = selected.includes(option);
                      return (
                        <label key={option} className={`flex cursor-pointer items-center gap-3 rounded-xl px-4 py-3 ring-1 transition-colors ${checked ? "bg-[#E2F0FA] ring-[#4B98C8] dark:bg-[#16364C]" : "bg-white ring-[#D6E6F2] hover:ring-[#4B98C8] dark:bg-[#0A1724] dark:ring-white/15"} ${submitting ? "pointer-events-none opacity-60" : ""}`}>
                          <span className={`flex h-5 w-5 shrink-0 items-center justify-center rounded-md ring-1 ${checked ? "bg-[#205E85] text-white ring-[#205E85] dark:bg-[#4B98C8] dark:text-[#081826] dark:ring-[#4B98C8]" : "ring-slate-300 dark:ring-white/25"}`}>
                            {checked && <FiCheck className="h-3.5 w-3.5" aria-hidden="true" />}
                          </span>
                          <input type="checkbox" className="sr-only" checked={checked} disabled={submitting} onChange={() => onInputChange(question.id, checked ? selected.filter((v) => v !== option) : [...selected, option])} />
                          <span className="text-[15px] font-medium text-slate-800 dark:text-slate-100">{option}</span>
                        </label>
                      );
                    })}
                  </div>
                ) : question.question_type === "IMAGE" ? (
                  <ImageAnswer file={value instanceof File ? value : null} disabled={submitting} onChange={(file) => onInputChange(question.id, file)} />
                ) : (
                  <div className="relative">
                    <select id={inputId} value={value || ""} onChange={(event) => onInputChange(question.id, event.target.value)} required={question.is_required} disabled={submitting} className={`${field} h-12 cursor-pointer appearance-none pr-10`}>
                      <option value="">Choose an option</option>
                      {(question.options || []).map((option) => <option key={option} value={option}>{option}</option>)}
                    </select>
                    <FiChevronDown className="pointer-events-none absolute right-4 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" aria-hidden="true" />
                  </div>
                )}
              </div>
            );
          })}
        </form>
      </DialogBody>
      <DialogFooter>
        <button type="button" onClick={onClose} disabled={submitting} className={secondaryButton}>Cancel</button>
        <button type="submit" form="registration-questionnaire-form" disabled={submitting} className={primaryButton}>
          {submitting ? <><span className="h-4 w-4 animate-spin rounded-full border-2 border-white/30 border-t-white" aria-hidden="true" />Sending…</> : <>Submit <FiArrowRight className="h-4 w-4" aria-hidden="true" /></>}
        </button>
      </DialogFooter>
    </>
  );
};

export default DynamicQuestionnaireModal;
