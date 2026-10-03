import React, { useRef } from "react";
import { FiSend, FiX } from "react-icons/fi";
import useThemedDialog from "../../../hooks/useThemedDialog";
import { Highlight } from "../../ThemedDialog";

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
        <div
            className="fixed inset-0 z-50 flex items-center justify-center bg-slate-900/40 p-4 backdrop-blur-sm"
            role="dialog"
            aria-modal="true"
            aria-label="Email preview"
        >
            <div className="flex max-h-[90vh] w-full max-w-3xl flex-col rounded-2xl border border-slate-200 bg-white p-6 shadow-2xl dark:border-slate-700 dark:bg-slate-900">
                <div className="flex shrink-0 items-center justify-between border-b border-slate-100 pb-4 dark:border-slate-700">
                    <h3 className="text-base font-extrabold text-slate-800 dark:text-slate-100">Review before sending</h3>
                    <button
                        type="button"
                        onClick={closePreview}
                        disabled={sending}
                        aria-label="Close preview"
                        className="rounded-lg p-1.5 text-slate-400 hover:bg-slate-100 dark:hover:bg-slate-800"
                    >
                        <FiX className="h-5 w-5" />
                    </button>
                </div>

                <div className="flex-1 space-y-5 overflow-y-auto py-5 pr-1">
                    {modalError && (
                        <div className="rounded-xl border border-rose-100 bg-rose-50 p-3.5 text-xs font-bold text-rose-600" role="alert">
                            {modalError}
                        </div>
                    )}

                    {/* Recipients */}
                    <div className="rounded-xl border border-slate-200 p-4 dark:border-slate-700">
                        <p className="text-[10px] font-bold uppercase tracking-wider text-slate-400">Recipients</p>
                        {membersLoading ? (
                            <p className="mt-1 text-sm text-slate-500">Calculating…</p>
                        ) : (
                            <>
                                <p className="mt-1 text-2xl font-black text-slate-800 dark:text-slate-100">{total} account(s)</p>
                                {total === 0 && !modalError && (
                                    <p className="mt-1 text-xs font-semibold text-amber-600">
                                        No accounts match these filters.
                                    </p>
                                )}
                            </>
                        )}
                        {roleCounts.length > 0 && (
                            <div className="mt-3 flex flex-wrap gap-1.5">
                                {roleCounts.map(([label, count]) => (
                                    <span
                                        key={label}
                                        className="rounded-lg border border-slate-200 bg-slate-100 px-2.5 py-1 text-[10px] font-extrabold uppercase text-slate-600 dark:border-slate-700 dark:bg-slate-800 dark:text-slate-300"
                                    >
                                        {label} <span className="text-[#4B98C8]">{count}</span>
                                    </span>
                                ))}
                            </div>
                        )}
                        <p className="mt-3 text-xs text-slate-500 dark:text-slate-400">
                            Scope: {[...selectedCommitteeNames, ...selectedClubNames].join(", ") || "All committees and clubs"}
                        </p>
                    </div>

                    <iframe
                        title={`Email preview: ${subject}`}
                        srcDoc={emailHtml}
                        sandbox=""
                        className="h-[680px] w-full rounded-md border border-slate-200 bg-white dark:border-slate-700"
                    />
                </div>

                <div className="flex shrink-0 justify-end gap-2 border-t border-slate-100 pt-4 dark:border-slate-700">
                    <button
                        type="button"
                        onClick={closePreview}
                        disabled={sending}
                        className="rounded-xl border border-slate-200 px-5 py-2.5 text-xs font-bold uppercase tracking-wider text-slate-500 hover:bg-slate-50 dark:border-slate-600 dark:text-slate-300 dark:hover:bg-slate-800"
                    >
                        Back to edit
                    </button>
                    <button
                        type="button"
                        onClick={onConfirmSend}
                        disabled={!canSend}
                        className="flex items-center gap-2 rounded-xl bg-emerald-600 px-5 py-2.5 text-xs font-bold uppercase tracking-wider text-white shadow transition hover:bg-emerald-700 active:scale-95 disabled:opacity-50"
                    >
                        <FiSend className="h-4 w-4" />
                        {sending ? "Sending..." : `Send to ${total}`}
                    </button>
                </div>
            </div>
            {dialog}
        </div>
    );
};

export default EmailPreviewModal;