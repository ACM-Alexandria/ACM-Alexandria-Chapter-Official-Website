import React, { useState, useEffect, useRef, useCallback } from "react";
import { FiSearch, FiChevronLeft, FiChevronRight, FiShield, FiCheck, FiUser, FiFileText, FiLoader, FiPlus } from "react-icons/fi";
import { HiStar } from "react-icons/hi";
import ModalShell from "../SharedComponents/ModalShell";
import { labelClass, hintClass, primaryButtonClass, secondaryButtonClass, noticeClass, tableClass, tableFrameClass, tableHeadRowClass, rowBorderClass } from "../SharedComponents/adminStyles";
import { searchUsers, assignUser } from "../../../services/adminService";
import { addUserPointsLog } from "../../../services/pointsLogService";
import { fetchCommittee, fetchClubs } from "../../../services/homePageService";
import { useAuth } from "../../../contexts/useAuth";
import { ROLE_OPTIONS, ROLE_LABELS } from "../../../constants/roles";
import { canShowPhoto } from "../../UserProfile/profileUtils";
import MembersExportModal from "./MembersExportModal";
import PointsLogModal from "../../UserProfile/PointsLogModal";
import MemberProfilePanel from "../../UserProfile/MemberProfilePanel";
import RowActions from "../SharedComponents/RowActions";

const UserAvatar = ({ item }) => {
  const [imgError, setImgError] = useState(false);

  const name = item.name || "Unknown User";
  const url = item.profile_image_url;

  if (url && !imgError && canShowPhoto(item.role)) {
    return (
      <img
        src={url}
        alt=""
        onError={() => setImgError(true)}
        className="w-10 h-10 rounded-full object-cover border border-slate-200 dark:border-ocean-745 shrink-0"
      />
    );
  }

  return (
    <div
      aria-hidden="true"
      className="w-10 h-10 rounded-full bg-ocean-57 dark:bg-ocean-808 text-ocean-614 dark:text-ocean-316 text-base font-semibold flex items-center justify-center shrink-0"
    >
      {name[0].toUpperCase()}
    </div>
  );
};

// Brand-tinted tag, same look as the role/committee tags on the profile page
const Tag = ({ children }) => (
  <span className="inline-flex items-center px-2.5 py-1 rounded-lg bg-ocean-67 dark:bg-ocean-808 text-ocean-637 dark:text-ocean-316 text-xs font-semibold whitespace-nowrap">
    {children}
  </span>
);

const RoleTags = ({ user }) => (
  <div className="flex flex-wrap items-center gap-1.5">
    <Tag>{ROLE_LABELS[user.role] || user.role}</Tag>
    {user.association_name && <Tag>{user.association_name}</Tag>}
  </div>
);

const fieldClass =
  "h-10 px-3 bg-white dark:bg-ocean-916 border border-slate-300 dark:border-ocean-745 rounded-lg outline-none text-slate-900 dark:text-slate-100 text-sm placeholder:text-slate-500 transition-colors focus:border-ocean-461 focus:ring-2 focus:ring-ocean-461/25";

const pageButtonClass =
  "h-8 px-2.5 rounded-lg inline-flex items-center gap-1 text-sm font-medium text-slate-700 dark:text-slate-300 hover:bg-slate-100 dark:hover:bg-ocean-845 disabled:opacity-40 disabled:hover:bg-transparent disabled:cursor-not-allowed transition-colors";

const UserManagementTab = ({ hideManageAccess = false }) => {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [query, setQuery] = useState("");
  const [roleFilter, setRoleFilter] = useState("");
  const [committeeFilter, setCommitteeFilter] = useState("");
  const [clubFilter, setClubFilter] = useState("");
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const assignInFlight = useRef(false);

  // Member profile side panel
  const [profileUserId, setProfileUserId] = useState(null);
  const closeProfilePanel = useCallback(() => setProfileUserId(null), []);

  // Add points (from the table row)
  const [pointsTarget, setPointsTarget] = useState(null);
  const [notice, setNotice] = useState(null); // { type: "success" | "error", text }

  // Modals
  const [exportModalOpen, setExportModalOpen] = useState(false);
  const [assignModalOpen, setAssignModalOpen] = useState(false);
  const [selectedUser, setSelectedUser] = useState(null);
  const [modalStep, setModalStep] = useState('form'); // 'form' or 'confirm'
  const [modalError, setModalError] = useState(null);

  // Modal Data
  const [formData, setFormData] = useState({
    targetRole: "",
    boardRole: "",
    boardOrder: "",
    committeeId: "",
    clubId: ""
  });
  const [committeesList, setCommitteesList] = useState([]);
  const [clubsList, setClubsList] = useState([]);

  useEffect(() => {
    loadUsers();
  }, [page, query, roleFilter, committeeFilter, clubFilter]);

  useEffect(() => {
    fetchCommittee().then(setCommitteesList).catch(console.error);
    fetchClubs(0, 100).then(data => setClubsList(data.content || [])).catch(console.error);
  }, []);

  const loadUsers = async () => {
    setLoading(true);
    try {
      const data = await searchUsers(query, roleFilter, page, 10, committeeFilter, clubFilter);
      setUsers(data.content || []);
      setTotalPages(data.totalPages || 1);
    } catch (err) {
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (!notice) return;
    const timer = setTimeout(() => setNotice(null), 4000);
    return () => clearTimeout(timer);
  }, [notice]);

  const handleAddPoints = async ({ reason, points }) => {
    const target = pointsTarget;
    try {
      await addUserPointsLog({ member: target.id, assigner: user.id, reason, points });
      setPointsTarget(null);
      setNotice({ type: "success", text: `${points > 0 ? "+" : ""}${points} points given to ${target.name || "the member"}.` });
      loadUsers();
    } catch (err) {
      console.error("Failed to add points:", err);
      setPointsTarget(null);
      setNotice({ type: "error", text: "Couldn't add the points. Please try again." });
    }
  };

  const viewProfile = (u) => setProfileUserId(u.id);

  const canManageUser = (targetUser) => {
    if (!user || !user.role) return false;
    const myRole = user.role;
    const targetRole = targetUser.role;

    if (myRole === 'SUPER_ADMIN') return true;
    if (myRole === 'ACM_HIGH_BOARD') {
      return targetRole !== 'SUPER_ADMIN';
    }
    if (myRole === 'ACM_COMMITTEE_BOARD' || myRole === 'ACM_CLUB_BOARD') {
      return targetRole !== 'SUPER_ADMIN' && targetRole !== 'ACM_HIGH_BOARD';
    }
    return false;
  };

  const getSelectableRoles = () => {
    if (!user || !user.role) return [];
    const myRole = user.role;
    if (myRole === 'SUPER_ADMIN') {
      return ["SUPER_ADMIN", "ACM_HIGH_BOARD", "ACM_COMMITTEE_BOARD", "ACM_CLUB_BOARD", "ACM_MEMBER", "USER"];
    }
    if (myRole === 'ACM_HIGH_BOARD') {
      return ["ACM_HIGH_BOARD", "ACM_COMMITTEE_BOARD", "ACM_CLUB_BOARD", "ACM_MEMBER", "USER"];
    }
    if (myRole === 'ACM_COMMITTEE_BOARD' || myRole === 'ACM_CLUB_BOARD') {
      return ["ACM_COMMITTEE_BOARD", "ACM_CLUB_BOARD", "ACM_MEMBER", "USER"];
    }
    return [];
  };

  const handleOpenModal = (u) => {
    setSelectedUser(u);
    setModalError(null);
    setFormData({
      targetRole: u.role || "USER",
      boardRole: u.board_role || "",
      boardOrder: u.board_order !== undefined && u.board_order !== null ? u.board_order.toString() : "",
      committeeId: u.committee_id !== undefined && u.committee_id !== null ? u.committee_id.toString() : "",
      clubId: u.club_id !== undefined && u.club_id !== null ? u.club_id.toString() : ""
    });
    setModalStep('form');
    setAssignModalOpen(true);
  };

  const handleNextStep = () => {
    setModalError(null);
    // Validate required fields based on role
    const r = formData.targetRole;
    if (["ACM_HIGH_BOARD", "ACM_COMMITTEE_BOARD", "ACM_CLUB_BOARD"].includes(r)) {
      if (!formData.boardRole) return setModalError("Board Role/Title is required.");
      if (formData.boardOrder === "" || isNaN(formData.boardOrder)) return setModalError("Board Order is required.");
    }
    if (["ACM_COMMITTEE_BOARD", "ACM_MEMBER"].includes(r) && !formData.committeeId) {
      return setModalError("Committee is required.");
    }
    if (r === "ACM_CLUB_BOARD" && !formData.clubId) {
      return setModalError("Club is required.");
    }

    setModalStep('confirm');
  };

  const handleConfirmSave = async () => {
    if (assignInFlight.current || saving) return;
    assignInFlight.current = true;
    setSaving(true);
    setModalError(null);
    try {
      const payload = { ...formData };
      if (payload.boardOrder !== "") {
        payload.boardOrder = parseInt(payload.boardOrder);
      }
      if (payload.committeeId === "") {
        payload.committeeId = null;
      } else {
        payload.committeeId = parseInt(payload.committeeId);
      }
      if (payload.clubId === "") {
        payload.clubId = null;
      } else {
        payload.clubId = parseInt(payload.clubId);
      }
      await assignUser(selectedUser.id, payload);
      setAssignModalOpen(false);
      loadUsers();
    } catch (err) {
      setModalError("Failed to assign user: " + err.message);
    } finally {
      setSaving(false);
      assignInFlight.current = false;
    }
  };

  const renderConfirmationSummary = () => {
    const role = formData.targetRole;
    const items = [];
    items.push(`Role will be set to: ${role}`);

    if (["ACM_HIGH_BOARD", "ACM_COMMITTEE_BOARD", "ACM_CLUB_BOARD"].includes(role)) {
      items.push(`Title: ${formData.boardRole}`);
      items.push(`Order: ${formData.boardOrder}`);
    }

    if (["ACM_COMMITTEE_BOARD", "ACM_MEMBER"].includes(role)) {
      const comm = committeesList.find(c => c.id.toString() === formData.committeeId.toString());
      items.push(`Committee: ${comm ? comm.name : "None"}`);
    }

    if (role === "ACM_CLUB_BOARD" && formData.clubId) {
      const club = clubsList.find(c => c.id.toString() === formData.clubId.toString());
      items.push(`Club: ${club ? club.name : "None"}`);
    }



    if (role === "SUPER_ADMIN") {
      items.push("WARNING: This user will have full unrestricted access to the entire platform.");
    }

    if (role === "USER") {
      items.push("This user will lose all admin and board privileges.");
    }

    return (
      <ul className="list-disc pl-5 space-y-1 text-sm text-slate-700 dark:text-slate-300">
        {items.map((item, i) => (
          <li key={i} className={item.includes("WARNING") ? "font-medium text-rose-700 dark:text-rose-400" : ""}>
            {item}
          </li>
        ))}
      </ul>
    );
  };

  return (
    <div className="flex flex-col h-full animate-[fadeIn_0.3s_ease]">
      {/* Header controls */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-5">
        <div>
          <h2 className="text-lg font-semibold text-slate-900 dark:text-slate-100 tracking-tight">User Management</h2>
          <p className="mt-0.5 text-sm text-slate-600 dark:text-slate-400">Manage user roles and board assignments.</p>
        </div>
        <button
          onClick={() => setExportModalOpen(true)}
          className="h-9 px-3.5 inline-flex items-center justify-center gap-1.5 rounded-lg bg-ocean-573 hover:bg-ocean-629 text-white text-sm font-medium transition-colors focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-offset-2 focus-visible:ring-ocean-461 dark:focus-visible:ring-offset-ocean-890"
        >
          <FiFileText className="w-4 h-4" aria-hidden="true" /> Export
        </button>
      </div>

      {notice && (
        <div
          role={notice.type === "success" ? "status" : "alert"}
          className={`mb-5 rounded-lg border px-4 py-3 text-sm ${notice.type === "success"
            ? "bg-emerald-50 dark:bg-emerald-950/40 border-emerald-200 dark:border-emerald-900 text-emerald-800 dark:text-emerald-200"
            : "bg-rose-50 dark:bg-rose-950/40 border-rose-200 dark:border-rose-900 text-rose-800 dark:text-rose-200"}`}
        >
          {notice.text}
        </div>
      )}

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-3 mb-5">
        <div className="relative flex-1">
          <span className="absolute inset-y-0 left-0 pl-3 flex items-center pointer-events-none text-slate-600 dark:text-slate-400">
            <FiSearch className="w-4 h-4" aria-hidden="true" />
          </span>
          <input
            type="text"
            aria-label="Search users"
            placeholder="Search by name or email"
            value={query}
            onChange={(e) => { setQuery(e.target.value); setPage(0); }}
            className={`${fieldClass} w-full pl-9`}
          />
        </div>
        <select
          aria-label="Filter by role"
          value={roleFilter}
          onChange={(e) => {
            setRoleFilter(e.target.value);
            setCommitteeFilter("");
            setClubFilter("");
            setPage(0);
          }}
          className={fieldClass}
        >
          <option value="">All roles</option>
          {ROLE_OPTIONS.map(r => <option key={r.value} value={r.value}>{r.label}</option>)}
        </select>

        {["ACM_COMMITTEE_BOARD", "ACM_MEMBER"].includes(roleFilter) && (
          <select
            aria-label="Filter by committee"
            value={committeeFilter}
            onChange={(e) => { setCommitteeFilter(e.target.value); setPage(0); }}
            className={fieldClass}
          >
            <option value="">All committees</option>
            {committeesList.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        )}

        {roleFilter === "ACM_CLUB_BOARD" && (
          <select
            aria-label="Filter by club"
            value={clubFilter}
            onChange={(e) => { setClubFilter(e.target.value); setPage(0); }}
            className={fieldClass}
          >
            <option value="">All clubs</option>
            {clubsList.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        )}
      </div>

      {/* Table */}
      <div className={tableFrameClass}>
        <div className="overflow-x-auto">
          <table className={tableClass}>
            <thead>
              <tr className={tableHeadRowClass}>
                <th scope="col" className="px-4 sm:px-5 py-3 font-medium">User</th>
                <th scope="col" className="hidden @min-[900px]:table-cell px-5 py-3 font-medium">Role</th>
                <th scope="col" className="hidden @min-[560px]:table-cell w-24 px-5 py-3 font-medium text-right">Points</th>
                <th scope="col" className="px-3 sm:px-5 py-3"><span className="sr-only">Actions</span></th>
              </tr>
            </thead>
            <tbody>
              {loading ? (
                [0, 1, 2, 3, 4].map((i) => (
                  <tr key={i} className="group animate-pulse motion-reduce:animate-none" aria-hidden="true">
                    <td className={`px-4 sm:px-5 py-4 ${rowBorderClass}`}>
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-full bg-slate-200 dark:bg-ocean-845" />
                        <div className="space-y-2">
                          <div className="h-3.5 w-32 rounded bg-slate-200 dark:bg-ocean-845" />
                          <div className="h-3 w-44 rounded bg-slate-200 dark:bg-ocean-845" />
                        </div>
                      </div>
                    </td>
                    <td className={`hidden @min-[900px]:table-cell px-5 py-4 ${rowBorderClass}`}>
                      <div className="h-5 w-28 rounded-full bg-slate-200 dark:bg-ocean-845" />
                    </td>
                    <td className={`hidden @min-[560px]:table-cell px-5 py-4 ${rowBorderClass}`}>
                      <div className="h-4 w-8 ml-auto rounded bg-slate-200 dark:bg-ocean-845" />
                    </td>
                    <td className={`px-3 sm:px-5 py-4 ${rowBorderClass}`} />
                  </tr>
                ))
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan="4" className="px-5 py-12 text-center">
                    <p className="text-sm font-medium text-slate-900 dark:text-slate-100">No users found</p>
                    <p className="mt-1 text-sm text-slate-600 dark:text-slate-400">Try a different search or role filter.</p>
                  </td>
                </tr>
              ) : (
                users.map(u => (
                  <tr
                    key={u.id}
                    onClick={() => viewProfile(u)}
                    className="group cursor-pointer"
                  >
                    <td className={`px-4 sm:px-5 py-4 w-full max-w-0 @min-[900px]:w-auto @min-[900px]:max-w-none ${rowBorderClass}`}>
                      <div className="flex items-center gap-3 min-w-0">
                        <UserAvatar item={u} />
                        <div className="min-w-0">
                          <button
                            type="button"
                            onClick={(e) => { e.stopPropagation(); viewProfile(u); }}
                            className="block max-w-full text-left text-sm font-medium text-slate-900 dark:text-slate-100 truncate @min-[900px]:max-w-[14rem] @min-[1200px]:max-w-xs rounded hover:underline underline-offset-2 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ocean-461/40"
                            title={`Open ${u.name || "user"}'s profile`}
                          >
                            {u.name || "Unknown User"}
                          </button>
                          <p className="text-xs text-slate-600 dark:text-slate-400 truncate @min-[900px]:max-w-[14rem] @min-[1200px]:max-w-xs" title={u.email}>
                            {u.email}
                          </p>
                          {/* Phones: role tags and points move under the name */}
                          <div className="@min-[900px]:hidden mt-1.5">
                            <RoleTags user={u} />
                          </div>
                          <p className="@min-[560px]:hidden mt-1.5 inline-flex items-center gap-1 text-xs text-slate-600 dark:text-slate-400">
                            <HiStar className="w-4 h-4 text-yellow-400" aria-hidden="true" />
                            <span className="font-semibold tabular-nums text-slate-900 dark:text-slate-100">{u.season_points ?? u.seasonPoints ?? u.points ?? 0}</span>
                            season points
                          </p>
                        </div>
                      </div>
                    </td>
                    <td className={`hidden @min-[900px]:table-cell px-5 py-4 ${rowBorderClass}`}>
                      <RoleTags user={u} />
                    </td>
                    <td className={`hidden @min-[560px]:table-cell px-5 py-4 text-right whitespace-nowrap ${rowBorderClass}`}>
                      <span className="inline-flex items-center justify-end gap-1 text-sm font-semibold tabular-nums text-slate-900 dark:text-slate-100">
                        <HiStar className="w-4 h-4 text-yellow-400" aria-hidden="true" />
                        {u.season_points ?? u.seasonPoints ?? u.points ?? 0}
                      </span>
                    </td>
                    <td className={`px-3 sm:px-5 py-4 text-right whitespace-nowrap ${rowBorderClass}`}>
                      <RowActions
                        from="compact"
                        name={u.name || "user"}
                        actions={[
                          { key: "profile", label: "View profile", icon: FiUser, onClick: () => viewProfile(u), sheetOnly: true },
                          { key: "points", label: "Add points", icon: FiPlus, onClick: () => setPointsTarget(u), hidden: u.id === user?.id, keepSpace: true },
                          { key: "manage", label: "Manage access", icon: FiShield, onClick: () => handleOpenModal(u), hidden: hideManageAccess || !canManageUser(u), keepSpace: true },
                        ]}
                      />
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Pagination */}
        {totalPages > 1 && (
          <div className="px-3 py-2.5 flex items-center justify-between border-t border-slate-200 dark:border-ocean-800">
            <button onClick={() => setPage(p => Math.max(0, p - 1))} disabled={page === 0} className={pageButtonClass}>
              <FiChevronLeft className="w-4 h-4" aria-hidden="true" /> Previous
            </button>
            <span className="text-sm text-slate-600 dark:text-slate-400 tabular-nums">
              Page {page + 1} of {totalPages}
            </span>
            <button onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))} disabled={page === totalPages - 1} className={pageButtonClass}>
              Next <FiChevronRight className="w-4 h-4" aria-hidden="true" />
            </button>
          </div>
        )}
      </div>

      {profileUserId && (
        <MemberProfilePanel userId={profileUserId} onClose={closeProfilePanel} onChanged={loadUsers} />
      )}

      <PointsLogModal
        isOpen={!!pointsTarget}
        onClose={() => setPointsTarget(null)}
        onSubmit={handleAddPoints}
        editingLog={null}
        memberName={pointsTarget?.name}
      />

      {exportModalOpen && (
        <MembersExportModal
          onClose={() => setExportModalOpen(false)}
          committees={committeesList}
          clubs={clubsList}
          initialFilters={{ role: roleFilter, committeeId: committeeFilter, clubId: clubFilter }}
        />
      )}

      {/* Manage access dialog: two steps, form then confirmation */}
      <ModalShell
        open={assignModalOpen && !!selectedUser}
        onClose={() => setAssignModalOpen(false)}
        title={modalStep === "form" ? "Manage access" : "Confirm changes"}
        subtitle={selectedUser?.name}
        size="md"
        busy={saving}
        onSubmit={(e) => {
          e.preventDefault();
          if (modalStep === "form") handleNextStep();
          else handleConfirmSave();
        }}
        footer={
          modalStep === "form" ? (
            <>
              <button type="button" onClick={() => setAssignModalOpen(false)} className={secondaryButtonClass}>
                Cancel
              </button>
              <button type="submit" className={primaryButtonClass}>
                Review changes
              </button>
            </>
          ) : (
            <>
              <button type="button" onClick={() => setModalStep("form")} disabled={saving} className={secondaryButtonClass}>
                Back
              </button>
              <button type="submit" disabled={saving} className={primaryButtonClass}>
                {saving ? (
                  <>
                    <FiLoader className="w-4 h-4 animate-spin" aria-hidden="true" /> Saving…
                  </>
                ) : (
                  <>
                    <FiCheck className="w-4 h-4" aria-hidden="true" /> Confirm and apply
                  </>
                )}
              </button>
            </>
          )
        }
      >
        {modalError && (
          <div role="alert" className={`${noticeClass.error} mb-5`}>
            {modalError}
          </div>
        )}

        <div className="mb-5 rounded-2xl border border-slate-200 dark:border-ocean-800 bg-slate-50 dark:bg-ocean-916/60 p-4">
          <p className={hintClass}>Target user</p>
          <p className="mt-0.5 text-sm font-medium text-slate-900 dark:text-slate-100 break-words">{selectedUser?.name}</p>
          <p className={`${hintClass} break-all`}>{selectedUser?.email}</p>
        </div>

        {modalStep === "form" ? (
          <div className="flex flex-col gap-5">
            <div className="flex flex-col gap-1.5">
              <label htmlFor="assign-role" className={labelClass}>Role</label>
              <select
                id="assign-role"
                value={formData.targetRole}
                onChange={(e) => {
                  const newRole = e.target.value;
                  if (newRole === selectedUser?.role) {
                    setFormData({
                      targetRole: newRole,
                      boardRole: selectedUser.board_role || "",
                      boardOrder: selectedUser.board_order !== undefined && selectedUser.board_order !== null ? selectedUser.board_order.toString() : "",
                      committeeId: selectedUser.committee_id !== undefined && selectedUser.committee_id !== null ? selectedUser.committee_id.toString() : "",
                      clubId: selectedUser.club_id !== undefined && selectedUser.club_id !== null ? selectedUser.club_id.toString() : ""
                    });
                  } else {
                    setFormData({
                      targetRole: newRole,
                      boardRole: "",
                      boardOrder: "",
                      committeeId: "",
                      clubId: ""
                    });
                  }
                }}
                className={fieldClass}
              >
                {getSelectableRoles().includes("SUPER_ADMIN") && <option value="SUPER_ADMIN">Super Admin</option>}
                {getSelectableRoles().includes("ACM_HIGH_BOARD") && <option value="ACM_HIGH_BOARD">High Board</option>}
                {getSelectableRoles().includes("ACM_COMMITTEE_BOARD") && <option value="ACM_COMMITTEE_BOARD">Committee Board</option>}
                {getSelectableRoles().includes("ACM_CLUB_BOARD") && <option value="ACM_CLUB_BOARD">Club Board</option>}
                {getSelectableRoles().includes("ACM_MEMBER") && <option value="ACM_MEMBER">ACM Member</option>}
                {getSelectableRoles().includes("USER") && <option value="USER">Standard User</option>}
              </select>
            </div>

            {["ACM_COMMITTEE_BOARD", "ACM_MEMBER"].includes(formData.targetRole) && (
              <div className="flex flex-col gap-1.5">
                <label htmlFor="assign-committee" className={labelClass}>Committee</label>
                <select
                  id="assign-committee"
                  required
                  value={formData.committeeId}
                  onChange={(e) => setFormData(prev => ({ ...prev, committeeId: e.target.value }))}
                  className={fieldClass}
                >
                  <option value="">Select committee</option>
                  {committeesList.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
            )}

            {formData.targetRole === "ACM_CLUB_BOARD" && (
              <div className="flex flex-col gap-1.5">
                <label htmlFor="assign-club" className={labelClass}>Club</label>
                <select
                  id="assign-club"
                  required
                  value={formData.clubId}
                  onChange={(e) => setFormData(prev => ({ ...prev, clubId: e.target.value }))}
                  className={fieldClass}
                >
                  <option value="">Select club</option>
                  {clubsList.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                </select>
              </div>
            )}

            {["ACM_HIGH_BOARD", "ACM_COMMITTEE_BOARD", "ACM_CLUB_BOARD"].includes(formData.targetRole) && (
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-5">
                <div className="flex flex-col gap-1.5">
                  <label htmlFor="assign-board-role" className={labelClass}>Board role or title</label>
                  <input
                    id="assign-board-role"
                    type="text"
                    required
                    placeholder="e.g. Head of PR"
                    value={formData.boardRole}
                    onChange={(e) => setFormData(prev => ({ ...prev, boardRole: e.target.value }))}
                    className={fieldClass}
                  />
                </div>
                <div className="flex flex-col gap-1.5">
                  <label htmlFor="assign-board-order" className={labelClass}>Hierarchy order</label>
                  <input
                    id="assign-board-order"
                    type="number"
                    required
                    placeholder="e.g. 1"
                    value={formData.boardOrder}
                    onChange={(e) => setFormData(prev => ({ ...prev, boardOrder: e.target.value }))}
                    className={`${fieldClass} tabular-nums`}
                  />
                </div>
              </div>
            )}
          </div>
        ) : (
          <div className="rounded-2xl border border-slate-200 dark:border-ocean-800 bg-slate-50 dark:bg-ocean-916/60 p-4">
            {renderConfirmationSummary()}
          </div>
        )}
      </ModalShell>
    </div>
  );
};

export default UserManagementTab;
