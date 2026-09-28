import React, { useEffect, useState } from "react";
import { FiMail } from "react-icons/fi";
import { fetchAllClubs, fetchCommittee } from "../../../services/homePageService";
import MembersFilter, { DEFAULT_MEMBERS_ROLES } from "../SharedComponents/MembersFilter";

const EmailCenterTab = () => {
    const [subject, setSubject] = useState("");
    const [selectedRoles, setSelectedRoles] = useState(DEFAULT_MEMBERS_ROLES);
    const [committees, setCommittees] = useState([]);
    const [clubs, setClubs] = useState([]);
    const [selectedCommitteeIds, setSelectedCommitteeIds] = useState([]);
    const [selectedClubIds, setSelectedClubIds] = useState([]);
    const [targetsError, setTargetsError] = useState("");


    useEffect(() => {
        let active = true;
        Promise.all([fetchCommittee(), fetchAllClubs()])
            .then(([committeeData, clubList]) => {
                if (!active) return;
                setCommittees(Array.isArray(committeeData) ? committeeData : []);
                setClubs(Array.isArray(clubList) ? clubList : []);
            })
            .catch((loadError) => {
                if (active) setTargetsError(loadError?.message || "Unable to load committees and clubs.");
            });
        return () => { active = false; };
    }, []);

    return (
        <section className="animate-[fadeIn_0.4s_ease]">
            <div className="mb-6 flex items-start gap-3 border-b border-slate-200 pb-5 dark:border-slate-700">
                <div className="rounded-md bg-sky-50 p-2.5 text-[#205E85] dark:bg-slate-800 dark:text-sky-300">
                    <FiMail aria-hidden="true" className="h-5 w-5" />
                </div>
                <h2 className="text-lg font-bold text-slate-900 mt-1 dark:text-white">Email Center</h2>
            </div>

            <form className="max-w-4xl space-y-5" onSubmit={(e) => e.preventDefault()}>
                <label className="block">
                    <span className="mb-2 block text-sm font-semibold text-slate-700 dark:text-slate-200">Subject</span>
                    <input
                        autoComplete="off"
                        className="w-full rounded-md border border-slate-300 bg-white px-3 py-2.5 text-sm text-slate-900 outline-none transition focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/20 dark:border-slate-600 dark:bg-slate-800 dark:text-white"
                        maxLength={200}
                        onChange={(event) => setSubject(event.target.value)}
                        placeholder="Write a clear subject"
                        required
                        value={subject}
                    />
                </label>

                <MembersFilter
                    selectedRoles={selectedRoles}
                    setSelectedRoles={setSelectedRoles}
                    committees={committees}
                    clubs={clubs}
                    selectedCommitteeIds={selectedCommitteeIds}
                    setSelectedCommitteeIds={setSelectedCommitteeIds}
                    selectedClubIds={selectedClubIds}
                    setSelectedClubIds={setSelectedClubIds}
                    previewError={targetsError}
                    emptyRoleMessage="Select at least one role to target."
                />
            </form>
        </section>
    );
};

export default EmailCenterTab;