import React, { useState, useEffect, useCallback, useRef } from "react";
import { FiSend } from "react-icons/fi";
import adminService from "../../../services/adminService";
import useThemedDialog from "../../../hooks/useThemedDialog";
import { Highlight } from "../../ThemedDialog";
import ModalShell from "../SharedComponents/ModalShell";
import { primaryButtonClass, secondaryButtonClass } from "../SharedComponents/adminStyles";
import { RESOURCE_TYPES } from "../../../constants/resourceTypes";

const errorMessage = (err, fallback) =>
  err?.message || err?.error || (typeof err === "string" ? err : null) || fallback;

const RegistrationEmailsModal = ({ open, onClose, resource, resourceType }) => {
  const label = RESOURCE_TYPES[resourceType]?.label || "Resource";
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
    <>
      <ModalShell
        open
        onClose={onClose}
        title={`${label} emails`}
        subtitle={resource.name}
        busy={busy !== null}
        footer={
          <button type="button" onClick={onClose} className={secondaryButtonClass}>
            Close
          </button>
        }
      >
        {loading ? (
          <div className="flex flex-col items-center justify-center py-12" role="status">
            <div className="w-8 h-8 border-4 border-slate-200 dark:border-ocean-745 border-t-ocean-461 rounded-full animate-spin motion-reduce:animate-none mb-3" />
            <p className="text-sm text-slate-600 dark:text-slate-400">Loading email settings…</p>
          </div>
        ) : (
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 p-4 rounded-lg border border-slate-200 dark:border-ocean-745 bg-slate-50 dark:bg-ocean-916/60">
            <div className="min-w-0">
              <p className="text-sm font-medium text-slate-900 dark:text-slate-100">{label} announcement</p>
              <p className="mt-0.5 text-sm text-slate-600 dark:text-slate-400">
                Email newsletter subscribers about this {label.toLowerCase()}.
              </p>
              <p className="mt-1.5 text-xs text-slate-600 dark:text-slate-400">
                {announcementSentAt
                  ? `Last announced: ${new Date(announcementSentAt).toLocaleString()}`
                  : "Not announced yet"}
              </p>
            </div>
            <button
              type="button"
              onClick={handleSendAnnouncement}
              disabled={busy !== null}
              className={`${primaryButtonClass} shrink-0`}
            >
              <FiSend className="w-4 h-4" aria-hidden="true" />
              {busy === "announce" ? "Sending…" : "Send now"}
            </button>
          </div>
        )}
      </ModalShell>
      {dialog}
    </>
  );
};

export default RegistrationEmailsModal;
