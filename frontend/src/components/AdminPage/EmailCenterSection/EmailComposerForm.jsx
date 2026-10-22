import React from "react";
import { FiEye } from "react-icons/fi";
import MembersFilter from "../SharedComponents/MembersFilter";
import RichTextEditor from "../SharedComponents/RichTextEditor/RichTextEditor";
import UserRecipientSearch from "./UserRecipientSearch";
import { fieldClass, labelClass, primaryButtonClass } from "../SharedComponents/adminStyles";

const EmailComposerForm = ({ composer, committees, clubs, targetsError }) => (
    <form className="max-w-4xl space-y-5" onSubmit={composer.openPreview}>
        <label className="block">
            <span className={`${labelClass} mb-1.5 block`}>Subject</span>
            <input
                autoComplete="off"
                className={fieldClass}
                maxLength={200}
                onChange={(event) => composer.setSubject(event.target.value)}
                placeholder="Write a clear subject"
                required
                value={composer.subject}
            />
        </label>

        <div>
            <span className={`${labelClass} mb-1.5 block`}>Message</span>
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
            emptyRoleMessage=""
        />

        <div className="flex justify-end">
            <button type="submit" disabled={!composer.canPreview} className={`${primaryButtonClass} w-full sm:w-auto`}>
                <FiEye className="h-4 w-4" aria-hidden="true" /> Preview
            </button>
        </div>
    </form>
);

export default EmailComposerForm;