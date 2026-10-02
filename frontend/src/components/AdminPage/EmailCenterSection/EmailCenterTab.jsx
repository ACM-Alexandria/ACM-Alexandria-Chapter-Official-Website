import { FiMail } from "react-icons/fi";
import { useTargetMembers } from "../../../hooks/useTargetMembers";
import { useEmailComposer } from "../../../hooks/useEmailComposer";
import EmailComposerForm from "./EmailComposerForm";
import EmailPreviewModal from "./EmailPreviewModal";
import StatusAlert from "./StatusAlert";

const EmailCenterTab = () => {
    const { committees, clubs, error: targetsError } = useTargetMembers();
    const composer = useEmailComposer({ committees, clubs });

    return (
        <section className="animate-[fadeIn_0.4s_ease]">
            <div className="mb-6 flex items-start gap-3 border-b border-slate-200 pb-5 dark:border-slate-700">
                <div className="rounded-md bg-sky-50 p-2.5 text-[#205E85] dark:bg-slate-800 dark:text-sky-300">
                    <FiMail aria-hidden="true" className="h-5 w-5" />
                </div>
                <h2 className="mt-1 text-lg font-bold text-slate-900 dark:text-white">Email Center</h2>
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