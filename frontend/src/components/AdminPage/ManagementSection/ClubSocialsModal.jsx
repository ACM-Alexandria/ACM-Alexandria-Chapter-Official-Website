import React, { useState, useEffect } from "react";
import { FiPlus, FiTrash2 } from "react-icons/fi";
import adminService from "../../../services/adminService";
import ModalShell from "../SharedComponents/ModalShell";
import {
  fieldClass,
  iconButtonDangerClass,
  labelClass,
  noticeClass,
  primaryButtonClass,
  secondaryButtonClass,
} from "../SharedComponents/adminStyles";

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
    <ModalShell
      open
      onClose={onClose}
      onSubmit={handleSave}
      title="Club social links"
      subtitle={club.name}
      busy={saving}
      footer={
        <>
          <button type="button" onClick={onClose} disabled={saving} className={secondaryButtonClass}>
            Cancel
          </button>
          <button type="submit" disabled={saving || loading} className={primaryButtonClass}>
            {saving ? "Saving…" : "Save links"}
          </button>
        </>
      }
    >
      <div className="flex flex-col gap-4">
        {error && (
          <div role="alert" className={noticeClass.error}>
            {error}
          </div>
        )}

        {loading ? (
          <div className="flex flex-col items-center justify-center py-10" role="status">
            <div className="w-8 h-8 border-4 border-slate-200 dark:border-slate-700 border-t-[#4B98C8] rounded-full animate-spin motion-reduce:animate-none mb-3" />
            <p className="text-sm text-slate-600 dark:text-slate-400">Loading social links…</p>
          </div>
        ) : (
          <>
            <p className={labelClass}>Social media links</p>

            {links.length === 0 ? (
              <div className="py-6 text-center rounded-lg border border-dashed border-slate-300 dark:border-slate-700">
                <p className="text-sm text-slate-600 dark:text-slate-400">No social media links added yet.</p>
              </div>
            ) : (
              <ul className="flex flex-col gap-3">
                {links.map((link, index) => (
                  <li key={index} className="flex gap-2 items-center">
                    <input
                      type="url"
                      required
                      aria-label={`Link ${index + 1}`}
                      value={link}
                      onChange={(e) => handleLinkChange(index, e.target.value)}
                      placeholder="https://facebook.com/club-page"
                      className={`${fieldClass} flex-1 min-w-0`}
                    />
                    <button
                      type="button"
                      onClick={() => handleRemoveLink(index)}
                      className={`${iconButtonDangerClass} shrink-0`}
                      title="Remove link"
                      aria-label={`Remove link ${index + 1}`}
                    >
                      <FiTrash2 className="w-4 h-4" aria-hidden="true" />
                    </button>
                  </li>
                ))}
              </ul>
            )}

            <button type="button" onClick={handleAddLink} className={`${secondaryButtonClass} w-full border-dashed`}>
              <FiPlus className="w-4 h-4" aria-hidden="true" /> Add link
            </button>
          </>
        )}
      </div>
    </ModalShell>
  );
};

export default ClubSocialsModal;
