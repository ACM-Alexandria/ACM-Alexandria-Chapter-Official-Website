import { useMemo, useState } from "react";
import DOMPurify from "dompurify";
import { previewMembersExport, sendCustomEmail } from "../services/adminService";
import { buildMembersFilters, DEFAULT_MEMBERS_ROLES } from "../components/AdminPage/SharedComponents/MembersFilter";

export const useEmailComposer = ({ committees, clubs }) => {
    const [subject, setSubject] = useState("");
    const [body, setBody] = useState("");
    const [selectedRoles, setSelectedRoles] = useState(DEFAULT_MEMBERS_ROLES);
    const [selectedCommitteeIds, setSelectedCommitteeIds] = useState([]);
    const [selectedClubIds, setSelectedClubIds] = useState([]);

    const [previewOpen, setPreviewOpen] = useState(false);
    const [members, setMembers] = useState(null);
    const [membersLoading, setMembersLoading] = useState(false);
    const [modalError, setModalError] = useState("");
    const [sending, setSending] = useState(false);
    const [formError, setFormError] = useState("");
    const [success, setSuccess] = useState("");

    const filters = useMemo(
        () => buildMembersFilters(selectedRoles, selectedCommitteeIds, selectedClubIds),
        [selectedRoles, selectedCommitteeIds, selectedClubIds]
    );

    const selectedCommitteeNames = committees.filter((committee) => filters.committeeIds?.includes(committee.id)).map((committee) => committee.name);

    const selectedClubNames = clubs.filter((club) => filters.clubIds?.includes(club.id)).map((club) => club.name);

    const cleanBody = useMemo(() => DOMPurify.sanitize(body), [body]);

    const openPreview = async (event) => {
        event.preventDefault();
        setFormError("");
        setSuccess("");
        setModalError("");

        if (selectedRoles.length === 0) {
            return setFormError("Select at least one role to target.");
        }
        if (!cleanBody.trim()) {
            return setFormError("Write the email content before previewing.");
        }

        setPreviewOpen(true);
        setMembers(null);
        setMembersLoading(true);
        try {
            setMembers(await previewMembersExport(filters));
        } catch (err) {
            setModalError(err?.message || err?.error || "Could not load the recipients summary.");
        } finally {
            setMembersLoading(false);
        }
    };

    const closePreview = () => {
        if (!sending) setPreviewOpen(false);
    };

    const handleSend = async () => {
        setSending(true);
        setModalError("");
        try {
            const result = await sendCustomEmail({
                subject: subject.trim(),
                body: cleanBody,
                recipientFilter: "FILTERED_USERS",
                ...filters,
            });
            setPreviewOpen(false);
            setSuccess(result?.message || "Email request accepted for sending.");
            setSubject("");
            setBody("");
        } catch (err) {
            setModalError(err?.message || err?.error || "The email could not be sent. Please try again.");
        } finally {
            setSending(false);
        }
    };

    const total = members?.totalMembers ?? 0;
    const roleCounts = Object.entries(members?.roleCounts || {});
    const canSend = !membersLoading && !sending && total > 0;

    return {
        subject, setSubject,
        body, setBody,
        selectedRoles, setSelectedRoles,
        selectedCommitteeIds, setSelectedCommitteeIds,
        selectedClubIds, setSelectedClubIds,
        formError, success, modalError,
        previewOpen, openPreview, closePreview,
        membersLoading, total, roleCounts,
        selectedCommitteeNames, selectedClubNames, cleanBody,
        sending, canSend, handleSend,
    };
};