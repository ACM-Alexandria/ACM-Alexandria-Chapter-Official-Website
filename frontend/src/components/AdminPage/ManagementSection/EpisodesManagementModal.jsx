import React, { useState, useEffect, useRef } from "react";
import { FiPlus, FiTrash2, FiEdit2, FiArrowLeft, FiUploadCloud, FiLoader, FiExternalLink, FiChevronLeft, FiChevronRight } from "react-icons/fi";
import adminService from "../../../services/adminService";
import useThemedDialog from "../../../hooks/useThemedDialog";
import { Highlight } from "../../ThemedDialog";
import ModalShell from "../SharedComponents/ModalShell";
import {
  fieldClass,
  noticeClass,
  primaryButtonClass,
  secondaryButtonClass,
} from "../SharedComponents/adminStyles";


const EpisodesManagementModal = ({ open, onClose, seasonId, seasonNumber }) => {
  const { dialog, confirm } = useThemedDialog();
  const deleteEpisodeInFlight = useRef(false);

  const [episodesPage, setEpisodesPage] = useState({ content: [], number: 0, totalPages: 1 });
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [uploadError, setUploadError] = useState(null);
  const [uploading, setUploading] = useState(false);

  // Form mode: 'list', 'add', 'edit'
  const [mode, setMode] = useState("list");
  const [editingEpisode, setEditingEpisode] = useState(null);

  // Form states
  const [episodeNumber, setEpisodeNumber] = useState("");
  const [title, setTitle] = useState("");
  const [imageUrl, setImageUrl] = useState("");
  const [url, setUrl] = useState("");
  const [guest, setGuest] = useState("");
  const [host, setHost] = useState("");

  const loadEpisodes = async (page = 0) => {
    if (!seasonId) return;
    setLoading(true);
    setError(null);
    try {
      const data = await adminService.fetchEpisodesBySeason(seasonId, page);
      setEpisodesPage(data);
    } catch (err) {
      console.error("Error loading episodes:", err);
      setError(err.message || "Failed to load episodes.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open && seasonId) {
      loadEpisodes(0);
      setMode("list");
      setEditingEpisode(null);
      setError(null);
      setUploadError(null);
    }
  }, [open, seasonId]);

  if (!open || !seasonId) return null;

  const handleEditClick = (ep) => {
    setEditingEpisode(ep);
    setEpisodeNumber(ep.episodeNumber || "");
    setTitle(ep.title || "");
    setImageUrl(ep.imageUrl || "");
    setUrl(ep.url || "");
    setGuest(ep.guest || "");
    setHost(ep.host || "");
    setError(null);
    setUploadError(null);
    setMode("edit");
  };

  const handleAddClick = () => {
    setEditingEpisode(null);
    setEpisodeNumber("");
    setTitle("");
    setImageUrl("");
    setUrl("");
    setGuest("");
    setHost("");
    setError(null);
    setUploadError(null);
    setMode("add");
  };

  const handleFileChange = async (e) => {
    const file = e.target.files[0];
    if (!file) return;

    if (file.size > 10 * 1024 * 1024) {
      setUploadError("File size exceeds 10MB limit.");
      return;
    }

    setUploading(true);
    setUploadError(null);

    try {
      const data = await adminService.uploadImage(file);
      setImageUrl(data.url);
    } catch (err) {
      console.error("Upload failed:", err);
      setUploadError(err.message || "Failed to upload image. Please try again.");
    } finally {
      setUploading(false);
    }
  };

  const handleRemoveImage = () => {
    setImageUrl("");
    setUploadError(null);
  };

  const handleFormSubmit = async (e) => {
    e.preventDefault();
    if (!episodeNumber || !title || !imageUrl || !url) {
      setError("Please fill in all required fields.");
      return;
    }

    setSaving(true);
    setError(null);

    const payload = {
      episodeNumber: parseInt(episodeNumber),
      title,
      imageUrl,
      url,
      guest,
      host,
      radioSeasonId: seasonId
    };

    try {
      if (mode === "add") {
        await adminService.createEpisode(payload);
      } else {
        await adminService.updateEpisode(editingEpisode.id, payload);
      }
      setMode("list");
      loadEpisodes(episodesPage.number || 0);
    } catch (err) {
      console.error("Error saving episode:", err);
      setError(err.message || "Failed to save episode.");
    } finally {
      setSaving(false);
    }
  };

  const handleDeleteClick = async (ep) => {
    if (deleteEpisodeInFlight.current) return;
    deleteEpisodeInFlight.current = true;

    try {
      const episodeTitle = typeof ep === "object" ? ep.title : null;
      const episodeId = typeof ep === "object" ? ep.id : ep;

      const confirmed = await confirm({
        tone: "danger",
        title: "Delete Episode?",
        description: episodeTitle ? (
          <>
            Are you sure you want to delete <Highlight>{episodeTitle}</Highlight>? This episode will be permanently deleted. This action cannot be undone.
          </>
        ) : (
          "This episode will be permanently deleted. This action cannot be undone."
        ),
        confirmLabel: "Yes, Delete",
        cancelLabel: "Cancel",
      });

      if (!confirmed) return;

      setLoading(true);
      setError(null);
      try {
        await adminService.deleteEpisode(episodeId);
        // Reload current page, or previous if current became empty
        const targetPage = episodesPage.content.length === 1 && episodesPage.number > 0 
          ? episodesPage.number - 1 
          : episodesPage.number;
        await loadEpisodes(targetPage);
      } catch (err) {
        console.error("Error deleting episode:", err);
        setError(err.message || "Failed to delete episode.");
        setLoading(false);
      }
    } finally {
      deleteEpisodeInFlight.current = false;
    }
  };

  const handlePageChange = (newPage) => {
    loadEpisodes(newPage);
  };

  return (
    <>
      <ModalShell
        open
        onClose={onClose}
        title={<>{mode === "list" ? `Manage Season ${seasonNumber} Episodes` : mode === "add" ? "Add Episode" : "Edit Episode"}</>}
        size="xl" busy={saving || uploading}
      >


        {mode !== "list" && (
          <button
            type="button"
            onClick={() => setMode("list")}
            className="mb-4 inline-flex items-center gap-1.5 text-sm font-medium text-slate-600 dark:text-slate-400 hover:text-slate-900 dark:hover:text-slate-100 rounded focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/40"
          >
            <FiArrowLeft className="w-4 h-4" aria-hidden="true" /> Back to list
          </button>
        )}

        {/* Modal Content */}
        <div className="flex-1 overflow-y-auto min-h-[300px]">
          {error && (
            <div role="alert" className={`mb-4 ${noticeClass.error}`}>
              {error}
            </div>
          )}

          {mode === "list" ? (
            <>
              <div className="flex justify-between items-center mb-4">
                <p className="text-xs text-slate-600 dark:text-slate-400 font-semibold">
                  Seasons list all episodes sorted by episode number.
                </p>
                <button
                  type="button"
                  onClick={handleAddClick}
                  className="flex items-center gap-1.5 px-3 py-1.5 bg-[#4B98C8] hover:bg-[#205E85] text-white rounded-lg text-xs font-medium transition-colors"
                >
                  <FiPlus className="w-3.5 h-3.5" />
                  Add Episode
                </button>
              </div>

              {loading ? (
                <div className="flex items-center justify-center py-16">
                  <FiLoader className="w-8 h-8 text-[#4B98C8] animate-spin" />
                </div>
              ) : episodesPage.content.length === 0 ? (
                <div className="text-center py-16 border border-dashed border-slate-200 dark:border-slate-700 rounded-lg">
                  <p className="text-slate-600 dark:text-slate-400 text-xs font-medium">No episodes found in this season.</p>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="border-b border-slate-100 dark:border-slate-700 text-slate-600 dark:text-slate-400 text-xs font-medium">
                        <th className="pb-2.5 pl-2 w-16">Number</th>
                        <th className="pb-2.5">Title</th>
                        <th className="pb-2.5">Host & Guest</th>
                        <th className="pb-2.5">Link</th>
                        <th className="pb-2.5 pr-2 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-50 dark:divide-slate-700 text-xs font-semibold text-slate-700 dark:text-slate-200">
                      {episodesPage.content.map((ep) => (
                        <tr key={ep.id} className="hover:bg-slate-50/50 dark:hover:bg-slate-800/60 transition-colors">
                          <td className="py-2.5 pl-2 text-slate-500 font-medium">Ep. {ep.episodeNumber}</td>
                          <td className="py-2.5">
                            <div className="flex items-center gap-2">
                              <img src={ep.imageUrl} alt={ep.title} className="w-10 h-7 object-cover rounded border border-slate-200 dark:border-slate-700 shrink-0" />
                              <span className="font-semibold text-slate-800 dark:text-slate-100 truncate max-w-[150px]">{ep.title}</span>
                            </div>
                          </td>
                          <td className="py-2.5 text-slate-500">
                            <div>Host: <span className="text-slate-700 dark:text-slate-200 font-medium">{ep.host || "N/A"}</span></div>
                            <div className="text-xs text-slate-600 dark:text-slate-400 mt-0.5">Guest: <span className="font-medium">{ep.guest || "N/A"}</span></div>
                          </td>
                          <td className="py-2.5">
                            <a href={ep.url} target="_blank" rel="noopener noreferrer" className="text-[#4B98C8] hover:underline inline-flex items-center gap-0.5">
                              Watch <FiExternalLink className="w-3 h-3" />
                            </a>
                          </td>
                          <td className="py-2.5 pr-2 text-right">
                            <div className="flex justify-end gap-2">
                              <button
                                onClick={() => handleEditClick(ep)}
                                className="p-1.5 bg-slate-50 dark:bg-slate-800 hover:bg-sky-50 dark:hover:bg-sky-950/40 text-slate-500 dark:text-slate-300 hover:text-sky-600 rounded-lg transition-colors"
                              >
                                <FiEdit2 className="w-3.5 h-3.5" />
                              </button>
                              <button
                                onClick={() => handleDeleteClick(ep)}
                                disabled={loading}
                                className="p-1.5 bg-slate-50 dark:bg-slate-800 hover:bg-red-50 dark:hover:bg-red-950/40 text-slate-500 dark:text-slate-300 hover:text-red-500 rounded-lg transition-colors disabled:opacity-50 disabled:cursor-not-allowed disabled:pointer-events-none"
                              >
                                <FiTrash2 className="w-3.5 h-3.5" />
                              </button>
                            </div>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}

              {/* Pagination */}
              {episodesPage.totalPages > 1 && !loading && (
                <div className="flex items-center justify-between border-t border-slate-100 dark:border-slate-700 pt-4 mt-4">
                  <span className="text-xs text-slate-600 dark:text-slate-400 font-medium">
                    Page {episodesPage.number + 1} of {episodesPage.totalPages}
                  </span>
                  <div className="flex gap-2">
                    <button
                      disabled={episodesPage.number === 0}
                      onClick={() => handlePageChange(episodesPage.number - 1)}
                      className={secondaryButtonClass}
                    >
                      <FiChevronLeft className="w-4 h-4" />
                    </button>
                    <button
                      disabled={episodesPage.number >= episodesPage.totalPages - 1}
                      onClick={() => handlePageChange(episodesPage.number + 1)}
                      className={secondaryButtonClass}
                    >
                      <FiChevronRight className="w-4 h-4" />
                    </button>
                  </div>
                </div>
              )}
            </>
          ) : (
            <form onSubmit={handleFormSubmit} className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                    Episode Number *
                  </label>
                  <input
                    type="number"
                    required
                    min="1"
                    value={episodeNumber}
                    onChange={(e) => setEpisodeNumber(e.target.value)}
                    placeholder="e.g. 1"
                    className="w-full px-3.5 py-2.5 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 text-slate-800 dark:text-slate-100 text-xs font-semibold rounded-lg focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                  />
                </div>

                <div>
                  <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                    Episode Title *
                  </label>
                  <input
                    type="text"
                    required
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    placeholder="e.g. Intro to Web Development"
                    className={fieldClass}
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                    Host Name
                  </label>
                  <input
                    type="text"
                    value={host}
                    onChange={(e) => setHost(e.target.value)}
                    placeholder="e.g. John"
                    className={fieldClass}
                  />
                </div>

                <div>
                  <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                    Guest Name
                  </label>
                  <input
                    type="text"
                    value={guest}
                    onChange={(e) => setGuest(e.target.value)}
                    placeholder="e.g. Dr. Ahmed"
                    className={fieldClass}
                  />
                </div>
              </div>

              <div>
                <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                  Episode Link / URL *
                </label>
                <input
                  type="url"
                  required
                  value={url}
                  onChange={(e) => setUrl(e.target.value)}
                  placeholder="https://www.youtube.com/watch?v=..."
                  className={fieldClass}
                />
              </div>

              <div>
                <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                  Episode Cover Image *
                </label>

                {imageUrl ? (
                  <div className="relative flex items-center gap-4 p-3 bg-slate-50 dark:bg-slate-800 border border-slate-200 dark:border-slate-700 rounded-lg">
                    <img src={imageUrl} alt="Episode Preview" className="w-16 h-12 rounded object-cover border border-slate-200 dark:border-slate-700" />
                    <div className="flex-1 min-w-0">
                      <p className="text-sm font-medium text-slate-900 dark:text-slate-100 truncate">{imageUrl.split("/").pop()}</p>
                      <p className="text-xs text-slate-600 dark:text-slate-400 truncate mt-0.5">Cloudinary Asset</p>
                    </div>
                    <button
                      type="button"
                      onClick={handleRemoveImage}
                      className="p-1.5 bg-slate-50 dark:bg-slate-800 hover:bg-red-50 dark:hover:bg-red-950/40 text-slate-500 dark:text-slate-300 hover:text-red-500 rounded-lg transition-colors"
                      title="Remove image"
                    >
                      <FiTrash2 className="w-4 h-4" />
                    </button>
                  </div>
                ) : (
                  <div className="relative border-2 border-dashed border-slate-300 dark:border-slate-700 hover:border-[#4B98C8]/60 hover:bg-slate-50 dark:hover:bg-slate-800/60 rounded-lg p-5 text-center flex flex-col items-center justify-center cursor-pointer">
                    <input
                      type="file"
                      accept="image/*"
                      onChange={handleFileChange}
                      disabled={uploading}
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                    />
                    {uploading ? (
                      <>
                        <FiLoader className="w-6 h-6 text-[#4B98C8] animate-spin mb-1" />
                        <p className="text-xs font-medium text-slate-600 dark:text-slate-200">Uploading to Cloudinary...</p>
                      </>
                    ) : (
                      <>
                        <FiUploadCloud className="w-6 h-6 text-slate-600 dark:text-slate-400 mb-1" />
                        <p className="text-xs font-medium text-slate-600 dark:text-slate-200">Click to upload cover image</p>
                      </>
                    )}
                  </div>
                )}
                {uploadError && (
                  <p className="text-xs text-red-500 font-medium mt-1.5">{uploadError}</p>
                )}
              </div>

              <div className="flex gap-3 justify-end pt-4 border-t border-slate-100 dark:border-slate-700">
                <button
                  type="button"
                  onClick={() => setMode("list")}
                  className={secondaryButtonClass}
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving || uploading}
                  className={primaryButtonClass}
                >
                  {saving ? (
                    <>
                      <FiLoader className="w-3.5 h-3.5 animate-spin" /> Saving...
                    </>
                  ) : "Save Episode"}
                </button>
              </div>
            </form>
          )}
        </div>
      </ModalShell>
      {dialog}
    </>
  );
};

export default EpisodesManagementModal;
