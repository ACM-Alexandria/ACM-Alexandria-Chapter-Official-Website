import React from "react";
import { FiSearch, FiX } from "react-icons/fi";

const UserRecipientSearch = ({ composer }) => (
    <div className="space-y-3 rounded-xl border border-slate-200 bg-slate-50 p-4 dark:border-slate-700 dark:bg-slate-800/50">
        <div className="flex items-center justify-between gap-2">
            <span className="text-sm font-semibold text-slate-700 dark:text-slate-200">Specific users</span>
            {composer.selectedUsers.length > 0 && (
                <span className="rounded-full bg-[#4B98C8]/10 px-2 py-1 text-[10px] font-bold uppercase tracking-wider text-[#205E85]">
                    {composer.selectedUsers.length} selected
                </span>
            )}
        </div>

        <div className="relative">
            <FiSearch className="pointer-events-none absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-slate-400" />
            <input
                value={composer.userSearch}
                onChange={(event) => composer.setUserSearch(event.target.value)}
                placeholder="Search by name or email"
                className="w-full rounded-lg border border-slate-300 bg-white py-2.5 pl-9 pr-3 text-sm text-slate-900 outline-none transition focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/20 dark:border-slate-600 dark:bg-slate-900 dark:text-white"
            />
        </div>

        {composer.userSearchLoading && <p className="text-xs text-slate-500">Searching users...</p>}

        {composer.userResults.length > 0 && (
            <div className="space-y-2 rounded-lg border border-slate-200 bg-white p-2 dark:border-slate-700 dark:bg-slate-900">
                {composer.userResults.map((user) => (
                    <button
                        key={user.id}
                        type="button"
                        onClick={() => composer.toggleSelectedUser(user)}
                        className="flex w-full items-center justify-between rounded-md px-2 py-2 text-left transition hover:bg-slate-100 dark:hover:bg-slate-800"
                    >
                        <div className="min-w-0">
                            <p className="truncate text-sm font-semibold text-slate-800 dark:text-slate-100">{user.name || "Unnamed user"}</p>
                            <p className="truncate text-[11px] text-slate-500">{user.email}</p>
                        </div>
                        <span className="text-[10px] font-bold uppercase tracking-wider text-[#4B98C8]">Select</span>
                    </button>
                ))}
            </div>
        )}

        {composer.userHasMore && (
            <button
                type="button"
                onClick={composer.loadMoreUsers}
                disabled={composer.userSearchLoading}
                className="w-full rounded-md border border-slate-300 px-3 py-2 text-xs font-semibold text-slate-600 transition hover:bg-slate-100 disabled:cursor-wait disabled:opacity-60 dark:border-slate-600 dark:text-slate-300 dark:hover:bg-slate-800"
            >
                {composer.userSearchLoading ? "Loading users..." : "Load more users"}
            </button>
        )}

        {composer.selectedUsers.length > 0 && (
            <div className="flex flex-wrap gap-2">
                {composer.selectedUsers.map((user) => (
                    <span
                        key={user.email}
                        className="inline-flex items-center gap-2 rounded-full border border-[#4B98C8]/30 bg-[#4B98C8]/10 px-2.5 py-1 text-[11px] font-semibold text-slate-700 dark:text-slate-100"
                    >
                        {user.name || user.email}
                        <button
                            type="button"
                            onClick={() => composer.toggleSelectedUser(user)}
                            className="rounded-full p-0.5 text-slate-500 hover:text-slate-700 dark:hover:text-slate-200"
                            aria-label={`Remove ${user.name || user.email}`}
                        >
                            <FiX className="h-3 w-3" />
                        </button>
                    </span>
                ))}
            </div>
        )}
    </div>
);

export default UserRecipientSearch;