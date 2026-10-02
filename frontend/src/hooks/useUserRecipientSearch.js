import { useEffect, useMemo, useRef, useState } from "react";
import { searchUsers } from "../services/adminService";

const PAGE_SIZE = 15;

export const useUserRecipientSearch = () => {
    const [selectedUsers, setSelectedUsers] = useState([]);
    const [userSearch, setUserSearch] = useState("");
    const [userResults, setUserResults] = useState([]);
    const [userSearchLoading, setUserSearchLoading] = useState(false);
    const [userNextPage, setUserNextPage] = useState(1);
    const [userHasMore, setUserHasMore] = useState(false);
    const userSearchRequestId = useRef(0);

    useEffect(() => {
        const query = userSearch.trim();
        const requestId = ++userSearchRequestId.current;
        setUserSearchLoading(false);
        if (!query) {
            setUserResults([]);
            setUserHasMore(false);
            setUserNextPage(1);
            return undefined;
        }

        setUserResults([]);
        setUserHasMore(false);
        const timer = setTimeout(async () => {
            setUserSearchLoading(true);
            try {
                const response = await searchUsers(query, "", 0, PAGE_SIZE);
                if (requestId !== userSearchRequestId.current) return;

                const selectedEmails = new Set(selectedUsers.map((user) => user.email?.toLowerCase()));
                setUserResults((response?.content || []).filter(
                    (user) => !selectedEmails.has(user.email?.toLowerCase())
                ));
                setUserNextPage(1);
                setUserHasMore(response?.last === false);
            } catch {
                if (requestId === userSearchRequestId.current) {
                    setUserResults([]);
                }
            } finally {
                if (requestId === userSearchRequestId.current) {
                    setUserSearchLoading(false);
                }
            }
        }, 250);

        return () => clearTimeout(timer);
    }, [selectedUsers, userSearch]);

    const loadMoreUsers = async () => {
        const query = userSearch.trim();
        if (!query || !userHasMore || userSearchLoading) return;

        const requestId = userSearchRequestId.current;
        setUserSearchLoading(true);
        try {
            const response = await searchUsers(query, "", userNextPage, PAGE_SIZE);
            if (requestId !== userSearchRequestId.current) return;

            const selectedEmails = new Set(selectedUsers.map((user) => user.email?.toLowerCase()));
            setUserResults((current) => {
                const existingEmails = new Set(current.map((user) => user.email?.toLowerCase()));
                const nextResults = (response?.content || []).filter((user) => {
                    const email = user.email?.toLowerCase();
                    return email && !selectedEmails.has(email) && !existingEmails.has(email);
                });
                return [...current, ...nextResults];
            });
            setUserNextPage((page) => page + 1);
            setUserHasMore(response?.last === false);
        } catch {
            setUserHasMore(true);
        } finally {
            if (requestId === userSearchRequestId.current) {
                setUserSearchLoading(false);
            }
        }
    };

    const selectedUserEmails = useMemo(
        () => selectedUsers.map((user) => user.email).filter(Boolean),
        [selectedUsers]
    );

    const toggleSelectedUser = (user) => {
        if (!user?.email) return;
        setSelectedUsers((current) => {
            const exists = current.some((item) => item.email === user.email);
            if (exists) {
                return current.filter((item) => item.email !== user.email);
            }
            return [...current, { id: user.id, email: user.email, name: user.name || user.email }];
        });
        setUserSearch("");
    };

    const clearSelectedUsers = () => {
        setSelectedUsers([]);
        setUserSearch("");
    };

    return {
        selectedUsers,
        selectedUserEmails,
        userSearch,
        setUserSearch,
        userResults,
        userSearchLoading,
        userHasMore,
        loadMoreUsers,
        toggleSelectedUser,
        clearSelectedUsers,
    };
};