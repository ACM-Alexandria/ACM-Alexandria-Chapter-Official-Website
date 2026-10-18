import { useEffect, useId, useRef, useState } from "react";
import { FiX } from "react-icons/fi";
import { isHttpUrl, isNotEmpty } from "../../../utils/validation";
import { formatBoardSeason } from "../../../utils/boardSeasons";

const inputClass = "mt-1.5 w-full rounded-xl border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/20 disabled:opacity-60 dark:border-slate-600 dark:bg-slate-800 dark:text-slate-100";

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
  const formRef = useRef(null);
  const id = useId();

  useEffect(() => {
    const previous = document.activeElement;
    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";
    formRef.current?.querySelector("input")?.focus();
    return () => {
      document.body.style.overflow = previousOverflow;
      if (previous?.isConnected) previous.focus();
    };
  }, []);

  const change = (key, value) => {
    setForm((previous) => ({ ...previous, [key]: value }));
    setError("");
  };

  const submit = (event) => {
    event.preventDefault();
    if (saving) return;
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
    if (event.key === "Escape" && !saving) {
      event.preventDefault();
      onClose();
    }
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
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/50 p-4 backdrop-blur-sm">
      <form ref={formRef} onSubmit={submit} onKeyDown={keyDown} role="dialog" aria-modal="true" aria-labelledby={`${id}-title`} aria-describedby={`${id}-season`} aria-busy={saving}
        className="max-h-[90vh] w-full max-w-lg overflow-y-auto rounded-2xl border border-slate-200 bg-white p-5 text-slate-800 shadow-2xl dark:border-slate-700 dark:bg-slate-900 dark:text-slate-100 sm:p-6">
        <div className="mb-5 flex items-start justify-between gap-4 border-b border-slate-100 pb-4 dark:border-slate-700">
          <div>
            <h3 id={`${id}-title`} className="text-base font-extrabold">{item ? "Edit" : "Add"} {resource.label.toLowerCase()} record</h3>
            <p id={`${id}-season`} className="mt-1 text-xs text-slate-500 dark:text-slate-300">{formatBoardSeason(season)} · Season stays fixed after saving</p>
          </div>
          <button type="button" disabled={saving} onClick={onClose} aria-label="Close form" className="rounded-lg p-1.5 text-slate-400 transition hover:bg-slate-100 disabled:opacity-40 dark:hover:bg-slate-800"><FiX aria-hidden="true" className="h-5 w-5" /></button>
        </div>
        <fieldset disabled={saving} className="space-y-4">
          {field("Name", "name", "text", true)}
          {resource.member ? (
            <>
              <div className="grid gap-4 sm:grid-cols-2">{field("Role", "role", "text", true)}{field("Display order (optional)", "displayOrder", "number")}</div>
              {field("Photo URL (optional)", "imageUrl", "url")}
              {field("LinkedIn URL (optional)", "linkedinUrl", "url")}
            </>
          ) : (
            <>
              {field("Logo URL (optional)", "logoUrl", "url")}
              <label className="block text-xs font-bold text-slate-600 dark:text-slate-300">Description (optional)
                <textarea className={inputClass} rows={3} value={form.description} onChange={(event) => change("description", event.target.value)} />
              </label>
            </>
          )}
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
        <div className="mt-6 flex justify-end gap-3 border-t border-slate-100 pt-4 dark:border-slate-700">
          <button type="button" disabled={saving} onClick={onClose} className="rounded-xl border border-slate-200 px-4 py-2.5 text-xs font-bold text-slate-500 hover:bg-slate-50 disabled:opacity-50 dark:border-slate-600 dark:text-slate-300 dark:hover:bg-slate-800">Cancel</button>
          <button type="submit" disabled={saving} className="rounded-xl bg-gradient-to-br from-[#4B98C8] to-[#205E85] px-5 py-2.5 text-xs font-bold text-white transition hover:brightness-110 disabled:opacity-50">{saving ? "Saving…" : "Save record"}</button>
        </div>
      </form>
    </div>
  );
};

export default PastResourceForm;
