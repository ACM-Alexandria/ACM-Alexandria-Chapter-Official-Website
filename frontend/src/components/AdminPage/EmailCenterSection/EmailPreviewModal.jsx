import React, { useRef } from "react";
import { FiSend } from "react-icons/fi";
import useThemedDialog from "../../../hooks/useThemedDialog";
import { Highlight } from "../../ThemedDialog";
import ModalShell from "../SharedComponents/ModalShell";
import { noticeClass, primaryButtonClass, secondaryButtonClass, tagMutedClass } from "../SharedComponents/adminStyles";

const EmailPreviewModal = ({ composer }) => {
    const {
        modalError, closePreview, sending, membersLoading, total, roleCounts,
        selectedCommitteeNames, selectedClubNames, subject, emailHtml, canSend, handleSend,
    } = composer;

    const { dialog, confirm } = useThemedDialog();
    const sendInFlight = useRef(false);

    const onConfirmSend = async () => {
        if (sendInFlight.current || !canSend) return;
        sendInFlight.current = true;

        try {
            const confirmed = await confirm({
                tone: "danger",
                title: "Send Bulk Email?",
                description: (
                    <>
                        You are about to send this email to <Highlight>{total}</Highlight> recipient account(s). This action cannot be recalled once the messages are sent.
                    </>
                ),
                confirmLabel: `Yes, Send to ${total}`,
                cancelLabel: "Cancel",
            });

            if (!confirmed) return;

            await handleSend();
        } finally {
            sendInFlight.current = false;
        }
    };

    return (
        <>
            <ModalShell
                open
                onClose={closePreview}
                title="Review before sending"
                size="xl"
                busy={sending}
                footer={
                    <>
                        <button type="button" onClick={closePreview} disabled={sending} className={secondaryButtonClass}>
                            Back to edit
                        </button>
                        <button type="button" onClick={onConfirmSend} disabled={!canSend} className={primaryButtonClass}>
                            <FiSend className="h-4 w-4" aria-hidden="true" />
                            {sending ? "Sending…" : `Send to ${total}`}
                        </button>
                    </>
                }
            >
                <div className="space-y-5">
                    {modalError && (
                        <div className={noticeClass.error} role="alert">
                            {modalError}
                        </div>
                    )}

                    {/* Recipients */}
                    <div className="rounded-lg border border-slate-200 p-4 dark:border-[#28405A]">
                        <p className="text-sm font-medium text-slate-700 dark:text-slate-300">Recipients</p>
                        {membersLoading ? (
                            <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">Calculating…</p>
                        ) : (
                            <>
                                <p className="mt-1 text-2xl font-semibold text-slate-900 dark:text-slate-100 tabular-nums">{total} account(s)</p>
                                {total === 0 && !modalError && (
                                    <p className="mt-1 text-sm text-amber-700 dark:text-amber-400">
                                        No accounts match these filters.
                                    </p>
                                )}
                            </>
                        )}
                        {roleCounts.length > 0 && (
                            <div className="mt-3 flex flex-wrap gap-1.5">
                                {roleCounts.map(([label, count]) => (
                                    <span key={label} className={tagMutedClass}>
                                        {label} <span className="ml-1 text-[#2C6F99] dark:text-[#7DBBE0] tabular-nums">{count}</span>
                                    </span>
                                ))}
                            </div>
                        )}
                        <p className="mt-3 text-xs text-slate-600 dark:text-slate-400">
                            Scope: {[...selectedCommitteeNames, ...selectedClubNames].join(", ") || "All committees and clubs"}
                        </p>
                    </div>

                    <iframe
                        title={`Email preview: ${subject}`}
                        srcDoc={emailHtml}
                        sandbox=""
                        className="h-[420px] sm:h-[620px] w-full rounded-lg border border-slate-200 bg-white dark:border-[#28405A]"
                    />
                </div>
            </ModalShell>
            {dialog}
        </>
    );
};

export default EmailPreviewModal;
