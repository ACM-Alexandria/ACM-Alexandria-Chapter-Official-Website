import React, { useEffect, useRef } from "react";
import { HiX } from "react-icons/hi";
import MemberProfileView from "./MemberProfileView";

// Slide-over panel with a member's profile, so admins don't have to leave the list.
const MemberProfilePanel = ({ userId, onClose, onChanged }) => {
  const closeButtonRef = useRef(null);

  useEffect(() => {
    const previouslyFocused = document.activeElement;
    // Lock the page behind the panel. Locking <body> alone isn't enough here: the site sets
    // overflow-x: clip on <html>, so the body's overflow never reaches the real scroll area.
    const html = document.documentElement;
    const previousHtmlOverflow = html.style.overflow;
    const previousBodyOverflow = document.body.style.overflow;
    html.style.overflow = "hidden";
    document.body.style.overflow = "hidden";
    closeButtonRef.current?.focus();

    const handleKeyDown = (e) => {
      if (e.key !== "Escape") return;
      // Escape belongs to a dialog opened on top of the panel (add points, delete) first
      if (document.querySelector("[data-modal-overlay]")) return;
      onClose();
    };
    document.addEventListener("keydown", handleKeyDown);

    return () => {
      document.removeEventListener("keydown", handleKeyDown);
      html.style.overflow = previousHtmlOverflow;
      document.body.style.overflow = previousBodyOverflow;
      if (previouslyFocused instanceof HTMLElement) previouslyFocused.focus();
    };
  }, [onClose]);

  return (
    <div className="fixed inset-0 z-[60] flex justify-end">
      <div
        className="absolute inset-0 bg-slate-900/40 animate-[fadeIn_0.2s_ease-out] motion-reduce:animate-none"
        onClick={onClose}
        aria-hidden="true"
      />

      <aside
        role="dialog"
        aria-modal="true"
        aria-label="Member profile"
        className="relative h-full w-full md:w-[min(56rem,90vw)] bg-slate-50 dark:bg-slate-950 border-l border-slate-200 dark:border-slate-800 shadow-2xl overflow-y-auto overscroll-contain animate-[slideRight_0.25s_ease-out] motion-reduce:animate-none"
      >
        <div className="sticky top-0 z-10 flex items-center justify-between gap-4 px-4 sm:px-6 py-3 bg-slate-50 dark:bg-slate-950 border-b border-slate-200 dark:border-slate-800">
          <h2 className="text-base font-semibold text-slate-900 dark:text-slate-100">Member profile</h2>
          <button
            ref={closeButtonRef}
            type="button"
            onClick={onClose}
            aria-label="Close"
            title="Close"
            className="w-9 h-9 inline-flex items-center justify-center rounded-md text-slate-600 dark:text-slate-300 hover:bg-slate-200/70 dark:hover:bg-slate-800 transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/50"
          >
            <HiX className="w-5 h-5" aria-hidden="true" />
          </button>
        </div>

        <div className="p-4 sm:p-6 pb-16">
          <MemberProfileView userId={userId} onChanged={onChanged} />
        </div>
      </aside>
    </div>
  );
};

export default MemberProfilePanel;
