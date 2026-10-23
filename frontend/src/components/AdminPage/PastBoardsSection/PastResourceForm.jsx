import { useEffect, useRef, useState } from "react";
import ImageUploadField from "../SharedComponents/ImageUploadField";
import ModalShell from "../SharedComponents/ModalShell";
import { fieldClass, textareaClass, primaryButtonClass, secondaryButtonClass } from "../SharedComponents/adminStyles";
import { isHttpUrl, isNotEmpty } from "../../../utils/validation";
import { formatBoardSeason } from "../../../utils/boardSeasons";

const inputClass = `mt-1.5 ${fieldClass}`;

const PastResourceForm = ({ resource, item, parents, season, saving, onSave, onClose, serverError }) => {
  const [form, setForm] = useState(() => ({
    name: item?.userName || item?.name || "",
    role: item?.role || "",
    imageUrl: item?.profileImageUrl || "",
    linkedinUrl: item?.linkedinUrl || "",
    displayOrder: item?.order ?? "",
    logoUrl: item?.logoUrl || "",
    description: item?.description || "",
    ...(resource.relation ? { [resource.relation]: item?.[resource.relation] || "" } : {}),
  }));
  const [error, setError] = useState("");
  const [uploading, setUploading] = useState(false);
  const busy = saving || uploading;
  const formRef = useRef(null);

  useEffect(() => {
    const previous = document.activeElement;
    formRef.current?.querySelector("input")?.focus();
    return () => {
      if (previous?.isConnected) previous.focus();
    };
  }, []);

  const change = (key, value) => {
    setForm((previous) => ({ ...previous, [key]: value }));
    setError("");
  };

  const submit = (event) => {
    event.preventDefault();
    if (busy) return;
    if (!isNotEmpty(form.name) || (resource.member && !isNotEmpty(form.role))) {
      setError(resource.member ? "Name and role must contain text." : "Name must contain text.");
      return;
    }
    const urls = resource.member ? [form.imageUrl, form.linkedinUrl] : [form.logoUrl];
    if (!urls.every(isHttpUrl)) {
      setError("Use an absolute HTTP or HTTPS URL.");
      return;
    }
    const order = form.displayOrder === "" ? null : Number(form.displayOrder);
    if (resource.member && order !== null && (!Number.isInteger(order) || order < -2147483648 || order > 2147483647)) {
      setError("Display order must be a whole number between -2147483648 and 2147483647.");
      return;
    }
    if (resource.relation && !parents.some((parent) => parent.id === Number(form[resource.relation]))) {
      setError("Choose a committee or club from this season.");
      return;
    }
    const payload = resource.member ? {
      season,
      name: form.name.trim(),
      role: form.role.trim(),
      imageUrl: form.imageUrl.trim() || null,
      linkedinUrl: form.linkedinUrl.trim() || null,
      displayOrder: order,
    } : {
      season,
      name: form.name.trim(),
      description: form.description,
      logoUrl: form.logoUrl.trim() || null,
    };
    if (resource.relation) payload[resource.relation] = Number(form[resource.relation]);
    setError("");
    onSave(payload);
  };

  const field = (label, key, type = "text", required = false) => (
    <label className="block text-xs font-bold text-slate-600 dark:text-slate-300" key={key}>
      {label}
      <input
        className={inputClass}
        type={type}
        value={form[key]}
        required={required}
        maxLength={type === "url" ? 2048 : type === "text" ? 255 : undefined}
        step={type === "number" ? 1 : undefined}
        min={type === "number" ? -2147483648 : undefined}
        max={type === "number" ? 2147483647 : undefined}
        placeholder={type === "url" ? "https://…" : undefined}
        onChange={(event) => change(key, event.target.value)}
      />
    </label>
  );

  const keyDown = (event) => {
    if (event.key !== "Tab") return;
    const elements = [...formRef.current.querySelectorAll("input:not(:disabled), select:not(:disabled), textarea:not(:disabled), button:not(:disabled)")];
    if (!elements.length) {
      event.preventDefault();
      return;
    }
    const first = elements[0];
    const last = elements.at(-1);
    if (event.shiftKey && document.activeElement === first) {
      event.preventDefault();
      last.focus();
    }
    if (!event.shiftKey && document.activeElement === last) {
      event.preventDefault();
      first.focus();
    }
  };

  return (
    <div ref={formRef} onKeyDown={keyDown} aria-busy={busy}>
      <ModalShell
        open
        onClose={onClose}
        onSubmit={submit}
        busy={busy}
        title={`${item ? "Edit" : "Add"} ${resource.label.toLowerCase()} record`}
        subtitle={`${formatBoardSeason(season)} · Season stays fixed after saving`}
        footer={(
          <>
            <button type="button" disabled={busy} onClick={onClose} className={secondaryButtonClass}>Cancel</button>
            <button type="submit" disabled={busy} className={primaryButtonClass}>{saving ? "Saving…" : uploading ? "Uploading…" : "Save record"}</button>
          </>
        )}
      >
        <fieldset disabled={busy} className="space-y-4">
          {field("Name", "name", "text", true)}
          {resource.member ? (
            <>
              <div className="grid gap-4 sm:grid-cols-2">{field("Role", "role", "text", true)}{field("Display order (optional)", "displayOrder", "number")}</div>
              {field("LinkedIn URL (optional)", "linkedinUrl", "url")}
            </>
          ) : (
            <>
              <label className="block text-xs font-bold text-slate-600 dark:text-slate-300">Description (optional)
                <textarea className={`mt-1.5 ${textareaClass}`} rows={3} value={form.description} onChange={(event) => change("description", event.target.value)} />
              </label>
            </>
          )}
          <ImageUploadField
            label={resource.member ? "Photo (optional)" : "Logo (optional)"}
            value={resource.member ? form.imageUrl : form.logoUrl}
            onChange={(url) => change(resource.member ? "imageUrl" : "logoUrl", url)}
            disabled={saving}
            uploading={uploading}
            onUploadingChange={setUploading}
          />
          {resource.parent && (
            <label className="block text-xs font-bold text-slate-600 dark:text-slate-300">{resource.parent === "clubs" ? "Club" : "Committee"}
              <select required className={inputClass} value={form[resource.relation]} onChange={(event) => change(resource.relation, event.target.value)}>
                <option value="">Select a parent from this season</option>
                {parents.map((parent) => <option key={parent.id} value={parent.id}>{parent.name}</option>)}
              </select>
            </label>
          )}
        </fieldset>
        {(error || serverError) && <p role="alert" className="mt-4 rounded-xl bg-red-50 px-3 py-2.5 text-xs font-semibold text-red-600 dark:bg-red-950/30 dark:text-red-300">{error || serverError}</p>}
      </ModalShell>
    </div>
  );
};

export default PastResourceForm;
