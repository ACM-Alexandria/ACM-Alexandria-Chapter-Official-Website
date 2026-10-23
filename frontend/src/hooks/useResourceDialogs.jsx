import { useRef, useState } from "react";
import useThemedDialog from "./useThemedDialog";
import { Highlight } from "../components/ThemedDialog";
import {
  changeCallMessage,
  fetchRegistrationAnalysis,
  syncRegistrationSheet,
} from "../services/adminService";

// The dialogs opened from a resource row: registrations, questions, announcement emails and
// the committee call message. Both dashboards use this hook, so each dialog behaves the same.
// onChanged runs after a change so the page can reload its list.
const useResourceDialogs = ({ onChanged } = {}) => {
  const { dialog, confirm } = useThemedDialog();
  const [registration, setRegistration] = useState(null); // { target, analysis, loading, syncing, error }
  const [questions, setQuestions] = useState(null); // { target }
  const [emails, setEmails] = useState(null); // { item, type }
  const [callMessage, setCallMessage] = useState(null); // { committee, subject, body, saving, error }
  const syncInFlight = useRef(false);

  const patchRegistration = (patch) => setRegistration((prev) => (prev ? { ...prev, ...patch } : prev));
  const patchCallMessage = (patch) => setCallMessage((prev) => (prev ? { ...prev, ...patch } : prev));

  // Registrations
  const openRegistration = async (item, type) => {
    const target = { id: item.id, name: item.name || item.title, type };
    // Committees open on their call list, which the panel loads itself
    setRegistration({ target, analysis: null, loading: type !== "committee", syncing: false, error: null });
    if (type === "committee") return;

    try {
      const analysis = await fetchRegistrationAnalysis(type, item.id);
      patchRegistration({ analysis, loading: false });
    } catch (err) {
      patchRegistration({ loading: false, error: err.message || err.error || "Failed to load registration analytics." });
    }
  };

  const closeRegistration = () => setRegistration(null);

  const syncRegistration = async () => {
    if (!registration || syncInFlight.current) return;
    syncInFlight.current = true;

    try {
      const { id, name, type } = registration.target;
      const confirmed = await confirm({
        tone: "danger",
        title: "Overwrite Google Sheet?",
        description: (
          <>
            This will replace the existing spreadsheet contents for <Highlight>{name || "this resource"}</Highlight> with the latest registration data from the website. Any manual notes, formulas, or other changes currently in the Google Sheet may be permanently erased.
          </>
        ),
        confirmLabel: "Yes, Overwrite Sheet",
        cancelLabel: "Cancel",
      });
      if (!confirmed) return;

      patchRegistration({ syncing: true, error: null });
      try {
        const analysis = await syncRegistrationSheet(type, id);
        patchRegistration({ analysis });
        onChanged?.();
      } catch (err) {
        patchRegistration({ error: err.message || err.error || "Failed to synchronize spreadsheet." });
      } finally {
        patchRegistration({ syncing: false });
      }
    } finally {
      syncInFlight.current = false;
    }
  };

  // Questions
  const openQuestions = (item, type) => setQuestions({ target: { id: item.id, name: item.name || item.title, type } });
  const closeQuestions = () => setQuestions(null);

  // Announcement emails
  const openEmails = (item, type) => setEmails({ item, type });
  const closeEmails = () => setEmails(null);

  // Committee call message
  const openCallMessage = (committee) =>
    setCallMessage({
      committee,
      subject: committee.callMessage?.subject || "",
      body: committee.callMessage?.body || "",
      saving: false,
      error: null,
    });
  const closeCallMessage = () => setCallMessage(null);
  const setCallMessageField = (field, value) => patchCallMessage({ [field]: value });

  const submitCallMessage = async (e) => {
    e.preventDefault();
    if (!callMessage) return;
    patchCallMessage({ saving: true, error: null });
    try {
      await changeCallMessage(callMessage.committee.id, { subject: callMessage.subject, body: callMessage.body });
      setCallMessage(null);
      onChanged?.();
    } catch (err) {
      console.error(err);
      patchCallMessage({ error: err.message || err.error || "Failed to update call message." });
    } finally {
      patchCallMessage({ saving: false });
    }
  };

  return {
    dialog,
    registration,
    questions,
    emails,
    callMessage,
    openRegistration,
    closeRegistration,
    syncRegistration,
    openQuestions,
    closeQuestions,
    openEmails,
    closeEmails,
    openCallMessage,
    closeCallMessage,
    setCallMessageField,
    submitCallMessage,
  };
};

export default useResourceDialogs;
