import { useTargetMembers } from "../../../hooks/useTargetMembers";
import { useEmailComposer } from "../../../hooks/useEmailComposer";
import EmailComposerForm from "./EmailComposerForm";
import EmailPreviewModal from "./EmailPreviewModal";
import StatusAlert from "./StatusAlert";

const EmailCenterTab = () => {
    const { committees, clubs, error: targetsError } = useTargetMembers();
    const composer = useEmailComposer({ committees, clubs });

    return (
        <section className="rounded-2xl border border-slate-200 bg-white p-4 dark:border-ocean-800 dark:bg-ocean-890 sm:p-6">
            <div className="mb-6">
                <h2 className="text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-100">Email center</h2>
                <p className="mt-0.5 text-sm text-slate-600 dark:text-slate-400">
                    Write a message and send it to members, committees or clubs.
                </p>
            </div>

            <StatusAlert type="error" message={composer.formError} />
            <StatusAlert type="success" message={composer.success} />

            <EmailComposerForm
                composer={composer}
                committees={committees}
                clubs={clubs}
                targetsError={targetsError}
            />

            {composer.previewOpen && <EmailPreviewModal composer={composer} />}
        </section>
    );
};

export default EmailCenterTab;