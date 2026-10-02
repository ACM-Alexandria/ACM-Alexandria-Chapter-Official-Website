import React from "react";
import { FiEye } from "react-icons/fi";
import MembersFilter from "../SharedComponents/MembersFilter";
import RichTextEditor from "../SharedComponents/RichTextEditor/RichTextEditor";
import UserRecipientSearch from "./UserRecipientSearch";

const EmailComposerForm = ({ composer, committees, clubs, targetsError }) => (
    <form className="max-w-4xl space-y-5" onSubmit={composer.openPreview}>
        <label className="block">
            <span className="mb-2 block text-sm font-semibold text-slate-700 dark:text-slate-200">Subject</span>
            <input
                autoComplete="off"
                className="w-full rounded-md border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/20 dark:border-slate-600 dark:bg-slate-800 dark:text-white"
                maxLength={200}
                onChange={(event) => composer.setSubject(event.target.value)}
                placeholder="Write a clear subject"
                required
                value={composer.subject}
            />
        </label>

        <div>
            <span className="mb-2 block text-sm font-semibold text-slate-700 dark:text-slate-200">Message</span>
            <RichTextEditor value={composer.body} onChange={composer.setBody} />
        </div>

        <UserRecipientSearch composer={composer} />

        <MembersFilter
            selectedRoles={composer.selectedRoles}
            setSelectedRoles={composer.setSelectedRoles}
            committees={committees}
            clubs={clubs}
            selectedCommitteeIds={composer.selectedCommitteeIds}
            setSelectedCommitteeIds={composer.setSelectedCommitteeIds}
            selectedClubIds={composer.selectedClubIds}
            setSelectedClubIds={composer.setSelectedClubIds}
            previewError={targetsError}
            emptyRoleMessage="Select at least one role to target."
        />

        <div className="flex justify-end">
            <button
                type="submit"
                className="flex items-center gap-2 rounded-md bg-[#4B98C8] px-5 py-2.5 text-xs font-bold uppercase tracking-wider text-white shadow transition hover:bg-[#205E85] active:scale-95"
            >
                <FiEye className="h-4 w-4" /> Preview
            </button>
        </div>
    </form>
);

export default EmailComposerForm;