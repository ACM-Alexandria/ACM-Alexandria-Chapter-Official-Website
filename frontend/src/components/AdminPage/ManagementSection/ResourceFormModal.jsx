import React from "react";
import { FiX } from "react-icons/fi";

const BRAND = "#4B98C8";
const BRAND_DARK = "#205E85";

const ResourceFormModal = ({
  open,
  onClose,
  onSubmit,
  formMode,
  activeTab,
  formData,
  setFormData,
  loading,
}) => {
  if (!open) return null;

  const getTitle = () => {
    const modeStr = formMode === "add" ? "Create New" : "Edit";
    let catStr = "";
    if (activeTab === "highboard") catStr = "High Board Member";
    else if (activeTab === "committeeBoard") catStr = "Committee Board Member";
    else catStr = activeTab.charAt(0).toUpperCase() + activeTab.slice(1, -1);
    return `${modeStr} ${catStr}`;
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xl max-w-lg w-full max-h-[90vh] overflow-y-auto p-6 animate-[scaleIn_0.25s_ease]">
        {/* Modal Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 mb-6">
          <h3 className="text-base font-extrabold text-slate-800 tracking-tight capitalize">
            {getTitle()}
          </h3>
          <button
            onClick={onClose}
            className="p-1.5 hover:bg-slate-100 text-slate-400 hover:text-slate-600 rounded-lg transition-colors"
          >
            <FiX className="w-4.5 h-4.5" />
          </button>
        </div>

        {/* Modal Form */}
        <form onSubmit={onSubmit} className="space-y-4">
          {/* Common Fields: Name/Title */}
          <div>
            <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
              Name / Title
            </label>
            <input
              type="text"
              required
              value={formData.name || ""}
              onChange={(e) => setFormData({ ...formData, name: e.target.value })}
              placeholder="e.g. John Doe"
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
            />
          </div>

          {/* Common Fields: Image/Logo Url */}
          <div>
            <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
              {activeTab === "committees" ? "Logo URL" : "Image URL"}
            </label>
            <input
              type="url"
              value={formData.imageUrl || formData.logoUrl || ""}
              onChange={(e) =>
                setFormData(
                  activeTab === "committees"
                    ? { ...formData, logoUrl: e.target.value }
                    : { ...formData, imageUrl: e.target.value }
                )
              }
              placeholder="https://example.com/image.jpg"
              className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
            />
          </div>

          {/* High Board and Committee Board fields */}
          {(activeTab === "highboard" || activeTab === "committeeBoard") && (
            <>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                    Role
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.role || ""}
                    onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                    placeholder="e.g. Coordinator"
                    className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                    Order Index (Sorting)
                  </label>
                  <input
                    type="number"
                    min="1"
                    value={formData.order ?? ""}
                    onChange={(e) => setFormData({ ...formData, order: e.target.value ? parseInt(e.target.value) : null })}
                    placeholder="e.g. 1 (Optional)"
                    className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                  />
                </div>
              </div>
              <div>
                <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                  LinkedIn URL
                </label>
                <input
                  type="url"
                  value={formData.linkedinUrl || ""}
                  onChange={(e) => setFormData({ ...formData, linkedinUrl: e.target.value })}
                  placeholder="https://linkedin.com/in/username"
                  className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                />
              </div>
            </>
          )}

          {/* Event & Program specific fields */}
          {(activeTab === "events" || activeTab === "programs") && (
            <>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                    Event Date & Time
                  </label>
                  <input
                    type="datetime-local"
                    value={formData.eventTime || ""}
                    onChange={(e) => setFormData({ ...formData, eventTime: e.target.value || null })}
                    className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                  />
                </div>
                {activeTab === "events" && (
                  <div>
                    <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                      Location
                    </label>
                    <input
                      type="text"
                      value={formData.location || ""}
                      onChange={(e) => setFormData({ ...formData, location: e.target.value || null })}
                      placeholder="e.g. Hall A / Zoom (Optional)"
                      className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                    />
                  </div>
                )}
              </div>
            </>
          )}


          {/* Description box (Clubs, Events, Committees, Programs) */}
          {(activeTab === "clubs" ||
            activeTab === "events" ||
            activeTab === "committees" ||
            activeTab === "programs") && (
            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                Description
              </label>
              <textarea
                value={formData.description || ""}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Provide a detailed description..."
                rows="3"
                className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all resize-none"
              />
            </div>
          )}

          {/* Actions row */}
          <div className="flex justify-end gap-3 pt-4 border-t border-slate-100 mt-6">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 border border-slate-200 text-slate-500 hover:text-slate-800 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading}
              className="px-5 py-2.5 text-white text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow disabled:opacity-40"
              style={{ background: `linear-gradient(135deg, ${BRAND}, ${BRAND_DARK})` }}
            >
              {loading ? "Saving..." : "Save Record"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ResourceFormModal;
