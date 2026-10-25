import { useState } from "react";
import { createPortal } from "react-dom";
import { FiX } from "react-icons/fi";
import useOverlay from "../../hooks/useOverlay";
import { initials } from "../../utils/format";
import { muted, primaryButton, skeleton } from "./uiStyles";

// A details panel that slides in from the right: a cover image with the eyebrow and title laid over it, then
// the content. The whole panel scrolls (never the page behind it). Stays mounted while closed so it can
// slide out.
const SidePanel = ({ open, onClose, label, image, eyebrow, title, loading = false, error = null, children }) => {
  const visible = useOverlay(open, onClose);
  const [failed, setFailed] = useState(null);
  const showImage = image && failed !== image;

  // Rendered into <body> so no transformed ancestor can trap the fixed overlay.
  return createPortal(
    <div
      data-lenis-prevent
      className={`fixed inset-0 z-50 ${visible ? "pointer-events-auto" : "pointer-events-none"}`}
      aria-hidden={!visible}
      inert={!open ? true : undefined}
    >
      <div className={`absolute inset-0 bg-[#06121D]/55 backdrop-blur-sm transition-opacity duration-300 ${visible ? "opacity-100" : "opacity-0"}`} onClick={onClose} />
      <aside
        role="dialog"
        aria-modal="true"
        aria-label={label || title}
        className={`absolute inset-y-0 right-0 w-full overflow-y-auto overscroll-contain bg-[#F3F7FB] shadow-[0_0_80px_-20px_rgba(4,18,30,0.6)] transition-transform duration-500 ease-[cubic-bezier(0.22,1,0.36,1)] sm:w-[560px] sm:rounded-l-[2rem] lg:w-[640px] dark:bg-[#0A1724] ${visible ? "translate-x-0" : "translate-x-full"}`}
      >
        <div className="relative aspect-[16/10] w-full overflow-hidden bg-[#0F3A5A] sm:aspect-[16/9]">
          {loading ? (
            <div className={`h-full w-full ${skeleton}`} />
          ) : showImage ? (
            <img src={image} alt="" className="h-full w-full object-cover" onError={() => setFailed(image)} />
          ) : (
            <div className="flex h-full w-full items-center justify-center bg-[radial-gradient(circle_at_30%_20%,#1D5A85,#0F3A5A_60%,#081826)]">
              <span className="text-8xl font-semibold text-white/10">{initials(title || "")}</span>
            </div>
          )}
          <div className="absolute inset-0 bg-gradient-to-t from-[#06121D]/90 via-[#06121D]/25 to-transparent" />
          <button type="button" onClick={onClose} aria-label="Close" className="absolute right-4 top-4 flex h-11 w-11 items-center justify-center rounded-full bg-white/90 text-slate-700 shadow-lg transition-colors hover:bg-white">
            <FiX className="h-5 w-5" aria-hidden="true" />
          </button>
          {!loading && title && (
            <div className="absolute inset-x-6 bottom-6 sm:inset-x-8" style={{ animation: "hp-rise 0.6s cubic-bezier(0.22,1,0.36,1) both" }}>
              {eyebrow && <span className="inline-flex rounded-full bg-white px-3 py-1 text-xs font-semibold text-[#205E85]">{eyebrow}</span>}
              <h2 className="mt-3 text-2xl font-semibold tracking-tight text-white sm:text-3xl" style={{ textWrap: "balance" }}>{title}</h2>
            </div>
          )}
        </div>

        <div className="p-6 sm:p-8">
          {loading ? (
            <div className="space-y-5">
              <div className="grid gap-4 sm:grid-cols-2">
                <div className={`h-20 rounded-2xl ${skeleton}`} />
                <div className={`h-20 rounded-2xl ${skeleton}`} />
              </div>
              <div className={`h-40 rounded-3xl ${skeleton}`} />
            </div>
          ) : error ? (
            <div className="flex flex-col items-center py-12 text-center">
              <p className="text-lg font-semibold text-slate-900 dark:text-white">{error}</p>
              <p className={`mt-1 text-sm ${muted}`}>Please try again in a moment.</p>
              <button type="button" onClick={onClose} className={`mt-6 ${primaryButton}`}>Close</button>
            </div>
          ) : (
            <div className="space-y-8" style={{ animation: "hp-fade 0.5s ease 0.15s both" }}>{children}</div>
          )}
        </div>
      </aside>
    </div>,
    document.body,
  );
};

// A titled block inside a panel.
export const PanelSection = ({ title, children }) => (
  <section>
    <h3 className="text-sm font-semibold text-slate-900 dark:text-white">{title}</h3>
    <div className="mt-3">{children}</div>
  </section>
);

// A small fact with an icon: date, place, time.
export const InfoTile = ({ icon, label, children }) => {
  const Icon = icon;
  return (
    <div className="flex items-center gap-4 rounded-2xl bg-white p-4 ring-1 ring-[#D6E6F2] dark:bg-[#0F2233] dark:ring-white/10">
      <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-[#E2F0FA] text-[#205E85] dark:bg-[#16364C] dark:text-[#7DBBE0]"><Icon className="h-5 w-5" aria-hidden="true" /></span>
      <div className="min-w-0">
        <p className={`text-xs font-medium ${muted}`}>{label}</p>
        <p className="mt-0.5 text-sm font-semibold text-slate-900 dark:text-white">{children}</p>
      </div>
    </div>
  );
};

export default SidePanel;
