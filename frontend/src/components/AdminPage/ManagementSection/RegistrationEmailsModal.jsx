import React, { useState, useEffect, useCallback, useRef } from "react";
import { FiX, FiSend } from "react-icons/fi";
import adminService from "../../../services/adminService";
import useThemedDialog from "../../../hooks/useThemedDialog";
import { Highlight } from "../../ThemedDialog";

const BRAND = "#4B98C8";
const BRAND_DARK = "#205E85";

const errorMessage = (err, fallback) =>
  err?.message || err?.error || (typeof err === "string" ? err : null) || fallback;

const RESOURCE_LABELS = { events: "Event", clubs: "Club" };

const RegistrationEmailsModal = ({ open, onClose, resource, resourceType }) => {
  const label = RESOURCE_LABELS[resourceType] || "Resource";
  const [announcementSentAt, setAnnouncementSentAt] = useState(null);
  const [loading, setLoading] = useState(false);
  const [busy, setBusy] = useState(null); // 'announce'
  const busyRef = useRef(false);
  const { dialog, confirm, notify } = useThemedDialog();

  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const settings = await adminService.fetchEmailSettings(resourceType, resource.id);
      setAnnouncementSentAt(settings?.announcementSentAt || null);
    } catch (err) {
      console.error("Error loading email data:", err);
      notify({ tone: "danger", title: "Couldn't Load", message: errorMessage(err, "Failed to load email settings.") });
    } finally {
      setLoading(false);
    }
  }, [resource, resourceType, notify]);

  useEffect(() => {
    if (open && resource) {
      loadData();
    }
  }, [open, resource, loadData]);

  if (!open || !resource) return null;

  // Runs one action at a time; the ref blocks a double-click immediately, before React re-renders the disabled buttons
  const runExclusive = async (kind, action) => {
    if (busyRef.current) return;
    busyRef.current = true;
    setBusy(kind);
    try {
      await action();
    } finally {
      busyRef.current = false;
      setBusy(null);
    }
  };

  const handleSendAnnouncement = () =>
    runExclusive("announce", async () => {
      const ok = await confirm({
        tone: "brand",
        title: "Send Announcement?",
        message: <>Send the <Highlight>{resource.name}</Highlight> announcement email to all newsletter subscribers?</>,
        confirmLabel: "Yes, Send",
      });
      if (!ok) return;
      try {
        const sent = await adminService.sendWithResendPrompt(
          (force) => adminService.sendAnnouncement(resourceType, resource.id, force),
          confirm
        );
        if (sent) {
          setAnnouncementSentAt(new Date().toISOString());
          await notify({ tone: "success", title: "Announcement Sent", message: "Announcement emails are being sent to subscribers." });
        }
      } catch (err) {
        console.error("Error sending announcement:", err);
        await notify({ tone: "danger", title: "Couldn't Send", message: errorMessage(err, "Failed to send announcement.") });
      }
    });

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]">
      <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-2xl dark:shadow-slate-950/60 max-w-lg w-full max-h-[85vh] overflow-y-auto p-6 animate-[scaleIn_0.25s_ease] flex flex-col">
        {/* Modal Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-slate-700 shrink-0">
          <div>
            <h3 className="text-base font-extrabold text-slate-800 dark:text-slate-100 tracking-tight">
              {label} Emails
            </h3>
            <p className="text-[10px] text-slate-400 font-medium mt-0.5 uppercase tracking-wider">
              {resource.name}
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400 dark:text-slate-300 hover:text-slate-600 dark:hover:text-slate-100 rounded-lg transition-colors"
          >
            <FiX className="w-4.5 h-4.5" />
          </button>
        </div>

        <div className="space-y-4 mt-4">
          {loading ? (
            <div className="flex flex-col items-center justify-center py-12">
              <div className="w-8 h-8 border-4 border-slate-200 dark:border-slate-700 border-t-[#4B98C8] rounded-full animate-spin mb-3" />
              <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Loading email settings...</p>
            </div>
          ) : (
            <>
              {/* Manual announcement */}
              <div className="flex items-center justify-between gap-3 p-4 bg-slate-50 dark:bg-slate-800 rounded-xl border border-slate-200 dark:border-slate-600">
                <div>
                  <p className="text-xs font-extrabold text-slate-700 dark:text-slate-200">{label} Announcement</p>
                  <p className="text-[10px] text-slate-400 dark:text-slate-300 font-medium mt-0.5">
                    Email newsletter subscribers about this {label.toLowerCase()}
                  </p>
                  <p className="text-[10px] font-bold mt-1 text-slate-500 dark:text-slate-300">
                    {announcementSentAt
                      ? `Last announced: ${new Date(announcementSentAt).toLocaleString()}`
                      : "Not announced yet"}
                  </p>
                </div>
                <button
                  type="button"
                  onClick={handleSendAnnouncement}
                  disabled={busy !== null}
                  className="shrink-0 px-3 py-2 flex items-center gap-1.5 text-white text-[10px] font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow disabled:opacity-40"
                  style={{ background: `linear-gradient(135deg, ${BRAND}, ${BRAND_DARK})` }}
                >
                  <FiSend className="w-3.5 h-3.5" />
                  {busy === "announce" ? "Sending..." : "Send Now"}
                </button>
              </div>
            </>
          )}
        </div>

        {/* Modal Footer */}
        <div className="flex justify-end gap-3 pt-6 border-t border-slate-100 dark:border-slate-700 mt-6 shrink-0">
          <button
            type="button"
            onClick={onClose}
            className="px-4 py-2.5 border border-slate-200 dark:border-slate-600 text-slate-500 dark:text-slate-300 hover:text-slate-800 dark:hover:text-slate-100 hover:bg-slate-50 dark:hover:bg-slate-800 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all"
          >
            Close
          </button>
        </div>
      </div>
      {dialog}
    </div>
  );
};

export default RegistrationEmailsModal;
