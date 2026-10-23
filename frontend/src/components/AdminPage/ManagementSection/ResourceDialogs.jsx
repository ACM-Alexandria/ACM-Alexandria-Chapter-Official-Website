import React from "react";
import RegistrationPanelModal from "./RegistrationPanelModal";
import QuestionsManagementModal from "./QuestionsManagementModal";
import RegistrationEmailsModal from "./RegistrationEmailsModal";
import CallMessageModal from "./CallMessageModal";

// Renders the dialogs opened from a resource row. The state and handlers come from
// useResourceDialogs, which both dashboards use.
const ResourceDialogs = ({ dialogs }) => {
  const { registration, questions, emails, callMessage } = dialogs;

  return (
    <>
      <RegistrationPanelModal
        open={Boolean(registration)}
        onClose={dialogs.closeRegistration}
        resourceId={registration?.target.id}
        resourceName={registration?.target.name || ""}
        resourceType={registration?.target.type || ""}
        analysis={registration?.analysis}
        loading={registration?.loading}
        syncLoading={registration?.syncing}
        onSyncSheet={dialogs.syncRegistration}
        error={registration?.error}
      />

      <QuestionsManagementModal
        open={Boolean(questions)}
        onClose={dialogs.closeQuestions}
        resourceId={questions?.target.id}
        resourceName={questions?.target.name}
        resourceType={questions?.target.type}
      />

      <RegistrationEmailsModal
        open={Boolean(emails)}
        onClose={dialogs.closeEmails}
        resource={emails?.item}
        resourceType={emails?.type}
      />

      <CallMessageModal
        open={Boolean(callMessage)}
        onClose={dialogs.closeCallMessage}
        onSubmit={dialogs.submitCallMessage}
        selectedCommittee={callMessage?.committee}
        messageSubject={callMessage?.subject ?? ""}
        setMessageSubject={(value) => dialogs.setCallMessageField("subject", value)}
        messageBody={callMessage?.body ?? ""}
        setMessageBody={(value) => dialogs.setCallMessageField("body", value)}
        loading={callMessage?.saving}
        error={callMessage?.error}
      />

      {dialogs.dialog}
    </>
  );
};

export default ResourceDialogs;
