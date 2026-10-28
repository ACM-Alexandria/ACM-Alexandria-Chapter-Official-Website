import { useId, useRef, useState } from "react";
import { FiLoader, FiTrash2, FiUploadCloud } from "react-icons/fi";
import { uploadImage } from "../../../services/adminService";
import { iconButtonDangerClass } from "./adminStyles";

const ImageUploadField = ({ label = "Image", value, onChange, disabled = false, uploading, onUploadingChange }) => {
  const [error, setError] = useState("");
  const inFlight = useRef(false);
  const id = useId();
  const busy = disabled || uploading;

  const upload = async (file) => {
    if (!file || busy || inFlight.current) return;
    setError("");
    if (!file.type.startsWith("image/")) {
      setError("Choose an image file.");
      return;
    }
    if (file.size > 5 * 1024 * 1024) {
      setError("Choose an image no larger than 5 MB.");
      return;
    }
    inFlight.current = true;
    onUploadingChange(true);
    try {
      const data = await uploadImage(file);
      if (!data.url) throw new Error("The upload did not return an image. Please try again.");
      onChange(data.url);
    } catch (failure) {
      setError(failure.message || failure.error || "Failed to upload image. Please try again.");
    } finally {
      inFlight.current = false;
      onUploadingChange(false);
    }
  };

  return (
    <div>
      <label htmlFor={value ? undefined : id} className="mb-1.5 block text-sm font-medium text-slate-700 dark:text-slate-300">{label}</label>
      {value ? (
        <div className="flex items-center gap-4 rounded-lg border border-slate-200 bg-slate-50 p-3.5 dark:border-[#28405A] dark:bg-[#0B1520]/60">
          <img src={value} alt={`${label} preview`} className="h-16 w-16 rounded-lg border border-slate-200 object-cover dark:border-[#28405A]" />
          <p className="min-w-0 flex-1 text-sm text-slate-700 dark:text-slate-300">Image selected</p>
          <button type="button" disabled={busy} onClick={() => { onChange(""); setError(""); }} className={iconButtonDangerClass} aria-label={`Remove ${label.toLowerCase()}`} title="Remove image">
            <FiTrash2 className="h-4 w-4" aria-hidden="true" />
          </button>
        </div>
      ) : (
        <div
          onDragOver={(event) => event.preventDefault()}
          onDrop={(event) => { event.preventDefault(); upload(event.dataTransfer.files[0]); }}
          className={`relative flex flex-col items-center justify-center rounded-lg border-2 border-dashed p-6 text-center transition-colors focus-within:ring-2 focus-within:ring-[#4B98C8]/40 ${busy ? "border-[#4B98C8]/40 bg-[#4B98C8]/5" : "border-slate-300 hover:border-[#4B98C8]/60 hover:bg-slate-50 dark:border-[#28405A] dark:hover:bg-[#15273A]/60"}`}
        >
          <input
            id={id}
            type="file"
            accept="image/*"
            disabled={busy}
            aria-describedby={`${id}-hint${error ? ` ${id}-error` : ""}`}
            onChange={(event) => { const file = event.target.files[0]; event.target.value = ""; upload(file); }}
            className="absolute inset-0 h-full w-full cursor-pointer opacity-0 disabled:cursor-not-allowed"
          />
          {uploading ? <FiLoader className="mb-2 h-8 w-8 animate-spin text-[#4B98C8]" aria-hidden="true" /> : <FiUploadCloud className="mb-2 h-8 w-8 text-slate-400" aria-hidden="true" />}
          <p role={uploading ? "status" : undefined} className="text-sm font-medium text-slate-800 dark:text-slate-200">{uploading ? "Uploading image…" : "Click to upload or drag & drop"}</p>
          <p id={`${id}-hint`} className="mt-1 text-xs text-slate-600 dark:text-slate-400">Image files up to 5 MB</p>
        </div>
      )}
      {error && <p id={`${id}-error`} role="alert" className="mt-1.5 text-xs font-medium text-rose-700 dark:text-rose-400">{error}</p>}
    </div>
  );
};

export default ImageUploadField;
