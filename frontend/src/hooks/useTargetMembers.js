import { useEffect, useState } from "react";
import { fetchAllClubs, fetchCommittee } from "../services/homePageService";

export const useTargetMembers = () => {
    const [committees, setCommittees] = useState([]);
    const [clubs, setClubs] = useState([]);
    const [error, setError] = useState("");

    useEffect(() => {
        let active = true;
        Promise.all([fetchCommittee(), fetchAllClubs()])
            .then(([committeeData, clubList]) => {
                if (!active) return;
                setCommittees(Array.isArray(committeeData) ? committeeData : []);
                setClubs(Array.isArray(clubList) ? clubList : []);
            })
            .catch((loadError) => {
                if (active) setError(loadError?.message || "Unable to load committees and clubs.");
            });
        return () => { active = false; };
    }, []);

    return { committees, clubs, error };
};