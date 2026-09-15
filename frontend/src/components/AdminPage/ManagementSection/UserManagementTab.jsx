import React, { useState, useEffect } from "react";
import { FiSearch, FiChevronLeft, FiChevronRight, FiShield, FiX, FiCheck, FiUser } from "react-icons/fi";
import { searchUsers, assignUser } from "../../../services/adminService";
import { fetchCommittee, fetchClubs } from "../../../services/homePageService";
import { useAuth } from "../../../contexts/AuthContext";

const UserMediaCell = ({ item }) => {
  const [imgError, setImgError] = useState(false);
  
  const name = item.name || "Unknown User";
  const url = item.profile_image_url;
  
  return (
    <div className="flex items-center gap-3.5">
      {url && !imgError ? (
        <img
          src={url}
          alt={name}
          onError={() => setImgError(true)}
          className="w-10 h-10 bg-slate-200 rounded-xl object-cover border border-slate-200 dark:border-slate-600 shrink-0"
        />
      ) : (
        <div className="w-10 h-10 rounded-xl flex items-center justify-center bg-slate-50 dark:bg-slate-800 text-slate-400 dark:text-slate-300 border border-slate-200 dark:border-slate-600 shrink-0">
          <FiUser className="w-5 h-5" />
        </div>
      )}
      <div className="min-w-0 max-w-[200px] sm:max-w-[300px]">
        <p className="font-extrabold text-slate-800 dark:text-slate-100 truncate">{name}</p>
        <p className="text-[10px] text-slate-400 truncate mt-0.5">{item.email}</p>
      </div>
    </div>
  );
};

const UserManagementTab = () => {
  const { user } = useAuth();
  const [users, setUsers] = useState([]);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [query, setQuery] = useState("");
  const [roleFilter, setRoleFilter] = useState("");
  const [committeeFilter, setCommitteeFilter] = useState("");
  const [clubFilter, setClubFilter] = useState("");
  const [loading, setLoading] = useState(false);
  
  // Modals
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
      <ul className="list-disc pl-5 space-y-1 text-sm text-slate-600 dark:text-slate-300 font-medium">
        {items.map((item, i) => (
          <li key={i} className={item.includes("WARNING") ? "text-rose-600 dark:text-rose-400 font-bold" : ""}>
            {item}
          </li>
        ))}
      </ul>
    );
  };

  return (
    <div className="flex flex-col h-full animate-[fadeIn_0.3s_ease]">
      {/* Header controls */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
        <div>
          <h2 className="text-lg font-extrabold text-slate-800 dark:text-slate-100 tracking-tight">User Management</h2>
          <p className="text-xs text-slate-400 dark:text-slate-300 font-medium">Manage user roles and board assignments.</p>
        </div>
      </div>

      {/* Filters */}
      <div className="flex flex-col sm:flex-row gap-4 mb-6">
        <div className="relative flex-1">
          <span className="absolute inset-y-0 left-0 pl-3.5 flex items-center pointer-events-none text-slate-400">
            <FiSearch className="w-4 h-4" />
          </span>
          <input
            type="text"
            placeholder="Search by name or email..."
            value={query}
            onChange={(e) => { setQuery(e.target.value); setPage(0); }}
            className="w-full pl-10 pr-4 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 rounded-xl text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
          />
        </div>
        <select
          value={roleFilter}
          onChange={(e) => { 
            setRoleFilter(e.target.value);
            setCommitteeFilter("");
            setClubFilter("");
            setPage(0); 
          }}
          className="px-4 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 rounded-xl text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
        >
          <option value="">All Roles</option>
          <option value="SUPER_ADMIN">Super Admin</option>
          <option value="ACM_HIGH_BOARD">High Board</option>
          <option value="ACM_COMMITTEE_BOARD">Committee Board</option>
          <option value="ACM_CLUB_BOARD">Club Board</option>
          <option value="ACM_MEMBER">ACM Member</option>
          <option value="USER">Standard User</option>
        </select>

        {["ACM_COMMITTEE_BOARD", "ACM_MEMBER"].includes(roleFilter) && (
          <select
            value={committeeFilter}
            onChange={(e) => { setCommitteeFilter(e.target.value); setPage(0); }}
            className="px-4 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 rounded-xl text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
          >
            <option value="">All Committees</option>
            {committeesList.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        )}

        {roleFilter === "ACM_CLUB_BOARD" && (
          <select
            value={clubFilter}
            onChange={(e) => { setClubFilter(e.target.value); setPage(0); }}
            className="px-4 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 rounded-xl text-xs font-semibold focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
          >
            <option value="">All Clubs</option>
            {clubsList.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        )}
      </div>

      {/* Table */}
      <div className="flex-1 overflow-auto rounded-xl border border-slate-200 dark:border-slate-700">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse min-w-[700px]">
            <thead>
              <tr className="border-b border-slate-200 dark:border-slate-700 text-slate-400 text-[10px] font-bold uppercase tracking-widest">
                <th className="pb-3 pl-4 pt-3">User</th>
                <th className="pb-3 pt-3">Role</th>
                <th className="pb-3 pr-4 pt-3 text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 dark:divide-slate-700 text-xs font-semibold text-slate-700 dark:text-slate-200">
              {loading ? (
                <tr>
                  <td colSpan="3" className="p-8">
                    <div className="flex items-center justify-center">
                      <div className="w-8 h-8 border-4 border-slate-200 dark:border-slate-700 border-t-[#4B98C8] rounded-full animate-spin" />
                    </div>
                  </td>
                </tr>
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan="3" className="p-8 text-center text-slate-400 dark:text-slate-300 font-medium">
                    No users found.
                  </td>
                </tr>
              ) : (
                users.map(u => (
                  <tr key={u.id} className="hover:bg-slate-50/55 dark:hover:bg-slate-800/60 transition-colors">
                    <td className="py-3.5 pl-4">
                      <UserMediaCell item={u} />
                    </td>
                    <td className="py-3.5">
                      <div className="flex flex-col items-start gap-1">
                        <span className="inline-flex items-center px-2.5 py-1 bg-slate-100 dark:bg-slate-800/80 border border-slate-200 dark:border-slate-700/80 rounded-lg text-[10px] font-extrabold uppercase tracking-wide text-slate-600 dark:text-slate-300">
                          {u.role}
                        </span>
                        {u.association_name && (
                          <span className="text-[11px] font-bold text-[#4B98C8] dark:text-[#60a5fa] pl-0.5">
                            {u.association_name}
                          </span>
                        )}
                      </div>
                    </td>
                    <td className="py-3.5 pr-4 text-right">
                      {canManageUser(u) && (
                        <button
                          onClick={() => handleOpenModal(u)}
                          className="inline-flex items-center justify-center gap-2 px-4 py-2 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 text-slate-600 dark:text-slate-300 hover:text-slate-900 dark:hover:text-slate-100 hover:border-slate-300 dark:hover:border-slate-500 rounded-xl text-xs font-bold tracking-wide active:scale-95 transition-all"
                        >
                          <FiShield className="w-3.5 h-3.5" /> Manage Access
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Pagination */}
      {totalPages > 1 && (
        <div className="flex justify-between items-center mt-6">
          <span className="text-xs font-medium text-slate-500">
            Page {page + 1} of {totalPages}
          </span>
          <div className="flex gap-2">
            <button
              onClick={() => setPage(p => Math.max(0, p - 1))}
              disabled={page === 0}
              className="p-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg hover:bg-slate-50 disabled:opacity-50 transition-colors"
            >
              <FiChevronLeft className="w-4 h-4" />
            </button>
            <button
              onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
              disabled={page === totalPages - 1}
              className="p-2 bg-white dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg hover:bg-slate-50 disabled:opacity-50 transition-colors"
            >
              <FiChevronRight className="w-4 h-4" />
            </button>
          </div>
        </div>
      )}

      {/* Dynamic Assign Modal */}
      {assignModalOpen && selectedUser && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]">
          <div className="bg-white dark:bg-slate-900 rounded-2xl border border-slate-200 dark:border-slate-700 shadow-xl dark:shadow-slate-950/60 max-w-lg w-full max-h-[90vh] overflow-y-auto p-6 animate-[scaleIn_0.25s_ease]">
            {/* Modal Header */}
            <div className="flex items-center justify-between pb-4 border-b border-slate-100 dark:border-slate-700 mb-6">
              <h3 className="text-base font-extrabold text-slate-800 dark:text-slate-100 tracking-tight capitalize">
                {modalStep === 'form' ? "Manage Access" : "Confirm Changes"}
              </h3>
              <button
                onClick={() => setAssignModalOpen(false)}
                className="p-1.5 hover:bg-slate-100 dark:hover:bg-slate-800 text-slate-400 dark:text-slate-300 hover:text-slate-600 dark:hover:text-slate-100 rounded-lg transition-colors"
              >
                <FiX className="w-4.5 h-4.5" />
              </button>
            </div>
            
            {modalError && (
              <div className="mb-4 p-3 rounded-xl bg-red-50 dark:bg-red-900/30 border border-red-200 dark:border-red-800 text-xs font-bold text-red-600 dark:text-red-400">
                {modalError}
              </div>
            )}
            <div className="mb-6 bg-slate-50 dark:bg-slate-800 p-4 rounded-xl border border-slate-200 dark:border-slate-700">
              <p className="text-xs font-bold text-slate-400 uppercase tracking-wider mb-1">Target User</p>
              <p className="text-sm font-extrabold text-slate-800 dark:text-slate-100">{selectedUser.name}</p>
              <p className="text-xs text-slate-500 mt-0.5">{selectedUser.email}</p>
            </div>

            {modalStep === 'form' ? (
              <div className="space-y-4">
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">Target Role</label>
                  <select
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
                    className="w-full px-3.5 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 text-slate-800 dark:text-slate-100 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
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
                  <div>
                    <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">Committee *</label>
                    <select
                      value={formData.committeeId}
                      onChange={(e) => setFormData(prev => ({ ...prev, committeeId: e.target.value }))}
                      className="w-full px-3.5 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 text-slate-800 dark:text-slate-100 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                    >
                      <option value="">Select Committee</option>
                      {committeesList.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                    </select>
                  </div>
                )}

                {formData.targetRole === "ACM_CLUB_BOARD" && (
                  <div>
                    <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">Club *</label>
                    <select
                      value={formData.clubId}
                      onChange={(e) => setFormData(prev => ({ ...prev, clubId: e.target.value }))}
                      className="w-full px-3.5 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 text-slate-800 dark:text-slate-100 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                    >
                      <option value="">Select Club</option>
                      {clubsList.map(c => <option key={c.id} value={c.id}>{c.name}</option>)}
                    </select>
                  </div>
                )}

                {["ACM_HIGH_BOARD", "ACM_COMMITTEE_BOARD", "ACM_CLUB_BOARD"].includes(formData.targetRole) && (
                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">Board Role/Title *</label>
                      <input
                        type="text"
                        placeholder="e.g. Head of PR"
                        value={formData.boardRole}
                        onChange={(e) => setFormData(prev => ({ ...prev, boardRole: e.target.value }))}
                        className="w-full px-3.5 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 text-slate-800 dark:text-slate-100 placeholder:text-slate-400 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                      />
                    </div>
                    <div>
                      <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">Hierarchy Order *</label>
                      <input
                        type="number"
                        placeholder="e.g. 1"
                        value={formData.boardOrder}
                        onChange={(e) => setFormData(prev => ({ ...prev, boardOrder: e.target.value }))}
                        className="w-full px-3.5 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-600 text-slate-800 dark:text-slate-100 placeholder:text-slate-400 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                      />
                    </div>
                  </div>
                )}



                <div className="flex justify-end gap-3 pt-6">
                  <button
                    type="button"
                    onClick={() => setAssignModalOpen(false)}
                    className="px-5 py-2.5 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 hover:border-slate-300 dark:hover:border-slate-600 text-slate-600 dark:text-slate-300 rounded-xl text-xs font-bold tracking-wide transition-all active:scale-95"
                  >
                    Cancel
                  </button>
                  <button
                    onClick={handleNextStep}
                    className="px-5 py-2.5 bg-[#4B98C8] hover:bg-[#3d83b0] text-white rounded-xl text-xs font-bold tracking-wide shadow-md hover:shadow-lg transition-all active:scale-95 flex items-center justify-center gap-2"
                  >
                    Review Changes
                  </button>
                </div>
              </div>
            ) : (
              <div className="space-y-6">
                <div className="bg-sky-50 dark:bg-sky-900/20 p-5 rounded-xl border border-sky-100 dark:border-sky-800/50">
                  {renderConfirmationSummary()}
                </div>
                
                <div className="flex justify-end gap-3">
                  <button
                    onClick={() => setModalStep('form')}
                    className="px-5 py-2.5 bg-white dark:bg-slate-900 border border-slate-200 dark:border-slate-700 hover:border-slate-300 dark:hover:border-slate-600 text-slate-600 dark:text-slate-300 rounded-xl text-xs font-bold tracking-wide transition-all active:scale-95"
                  >
                    Back
                  </button>
                  <button
                    onClick={handleConfirmSave}
                    className="px-5 py-2.5 bg-emerald-500 hover:bg-emerald-600 text-white rounded-xl text-xs font-bold tracking-wide shadow-md hover:shadow-lg transition-all active:scale-95 flex items-center justify-center gap-2"
                  >
                    <FiCheck className="w-4 h-4" /> Confirm & Apply
                  </button>
                </div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default UserManagementTab;
