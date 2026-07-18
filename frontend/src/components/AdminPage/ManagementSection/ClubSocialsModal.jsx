import React, { useState, useEffect } from "react";
import { FiX, FiPlus, FiTrash2 } from "react-icons/fi";
import adminService from "../../../services/adminService";

const BRAND = "#4B98C8";
const BRAND_DARK = "#205E85";

const ClubSocialsModal = ({ open, onClose, club, onSaved }) => {
  const [links, setLinks] = useState([]);
  const [saving, setSaving] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadSocialLinks = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await adminService.fetchClubSocialLinks(club.id);
        setLinks(data || []);
      } catch (err) {
        console.error("Error loading club social links:", err);
        setError(err.message || "Failed to load social links.");
      } finally {
        setLoading(false);
      }
    };

    if (open && club) {
      loadSocialLinks();
    }
  }, [open, club]);

  if (!open || !club) return null;

  const handleAddLink = () => {
    setLinks([...links, ""]);
  };

  const handleLinkChange = (index, value) => {
    const updated = [...links];
    updated[index] = value;
    setLinks(updated);
  };

  const handleRemoveLink = (index) => {
    setLinks(links.filter((_, i) => i !== index));
  };

  const handleSave = async (e) => {
    e.preventDefault();
    setSaving(true);
    setError(null);
    try {
      const cleanLinks = links.map((l) => l.trim()).filter((l) => l !== "");
      await adminService.updateClubSocialLinks(club.id, cleanLinks);
      onSaved();
      onClose();
    } catch (err) {
      console.error("Error updating club socials:", err);
      setError(err.message || "Failed to save social links.");
    } finally {
      setSaving(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl max-w-md w-full max-h-[85vh] overflow-y-auto p-6 animate-[scaleIn_0.25s_ease] flex flex-col">
        {/* Modal Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 shrink-0">
          <div>
            <h3 className="text-base font-extrabold text-slate-800 tracking-tight">
              Manage Club Socials
            </h3>
            <p className="text-[10px] text-slate-400 font-medium mt-0.5 uppercase tracking-wider">
              {club.name}
            </p>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 hover:bg-slate-100 text-slate-400 hover:text-slate-600 rounded-lg transition-colors"
          >
            <FiX className="w-4.5 h-4.5" />
          </button>
        </div>

        {/* Modal Body */}
        <form onSubmit={handleSave} className="flex-1 flex flex-col justify-between mt-4">
          <div className="space-y-4">
            {error && (
              <div className="p-3 bg-rose-50 border border-rose-100 text-rose-600 text-xs font-bold rounded-xl">
                {error}
              </div>
            )}

            {loading ? (
              <div className="flex flex-col items-center justify-center py-12">
                <div className="w-8 h-8 border-4 border-slate-200 border-t-[#4B98C8] rounded-full animate-spin mb-3" />
                <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Loading social links...</p>
              </div>
            ) : (
              <div className="space-y-3">
                <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                  Social Media Links / URLs
                </label>

                {links.length === 0 ? (
                  <div className="text-center py-6 border border-dashed border-slate-200 rounded-xl">
                    <p className="text-slate-400 text-xs font-medium">No social media links added yet.</p>
                  </div>
                ) : (
                  <div className="space-y-3.5 max-h-[40vh] overflow-y-auto pr-1">
                    {links.map((link, index) => (
                      <div key={index} className="flex gap-2 items-center">
                        <input
                          type="url"
                          required
                          value={link}
                          onChange={(e) => handleLinkChange(index, e.target.value)}
                          placeholder="e.g. https://facebook.com/club-page"
                          className="flex-1 px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                        />
                        <button
                          type="button"
                          onClick={() => handleRemoveLink(index)}
                          className="p-2.5 bg-rose-50 text-rose-600 border border-rose-100 hover:bg-rose-100 rounded-xl transition-all active:scale-95"
                          title="Remove Link"
                        >
                          <FiTrash2 className="w-4 h-4" />
                        </button>
                      </div>
                    ))}
                  </div>
                )}

                <button
                  type="button"
                  onClick={handleAddLink}
                  className="px-4 py-2.5 border border-dashed border-slate-300 text-slate-500 hover:text-slate-700 text-xs font-bold rounded-xl w-full flex items-center justify-center gap-1.5 transition-all hover:bg-slate-50 active:scale-95"
                >
                  <FiPlus className="w-4 h-4" /> Add Social URL
                </button>
              </div>
            )}
          </div>

          {/* Modal Footer */}
          <div className="flex justify-end gap-3 pt-6 border-t border-slate-100 mt-6 shrink-0">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 border border-slate-200 text-slate-500 hover:text-slate-800 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={saving}
              className="px-5 py-2.5 text-white text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow disabled:opacity-40"
              style={{ background: `linear-gradient(135deg, ${BRAND}, ${BRAND_DARK})` }}
            >
              {saving ? "Saving..." : "Save Links"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ClubSocialsModal;
