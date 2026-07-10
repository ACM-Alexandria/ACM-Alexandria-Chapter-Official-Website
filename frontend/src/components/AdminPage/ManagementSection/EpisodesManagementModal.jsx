import React, { useState, useEffect } from "react";
import { FiX, FiPlus, FiTrash2, FiEdit2, FiArrowLeft, FiUploadCloud, FiLoader, FiExternalLink, FiChevronLeft, FiChevronRight } from "react-icons/fi";
import adminService from "../../../services/adminService";

const BRAND = "#4B98C8";
const BRAND_DARK = "#205E85";

const EpisodesManagementModal = ({ open, onClose, seasonId, seasonNumber }) => {
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

  const handleDeleteClick = async (id) => {
    if (!window.confirm("Are you sure you want to delete this episode? This action is permanent.")) {
      return;
    }
    setLoading(true);
    setError(null);
    try {
      await adminService.deleteEpisode(id);
      // Reload current page, or previous if current became empty
      const targetPage = episodesPage.content.length === 1 && episodesPage.number > 0 
        ? episodesPage.number - 1 
        : episodesPage.number;
      loadEpisodes(targetPage);
    } catch (err) {
      console.error("Error deleting episode:", err);
      setError(err.message || "Failed to delete episode.");
      setLoading(false);
    }
  };

  const handlePageChange = (newPage) => {
    loadEpisodes(newPage);
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-xl max-w-3xl w-full max-h-[90vh] overflow-y-auto p-6 animate-[scaleIn_0.25s_ease] flex flex-col justify-between">
        
        {/* Modal Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 mb-6 shrink-0">
          <div className="flex items-center gap-3">
            {mode !== "list" && (
              <button
                onClick={() => setMode("list")}
                className="p-1.5 hover:bg-slate-100 text-slate-500 rounded-lg transition-colors"
                title="Back to list"
              >
                <FiArrowLeft className="w-4 h-4" />
              </button>
            )}
            <h3 className="text-base font-extrabold text-slate-800 tracking-tight">
              {mode === "list" ? `Manage Season ${seasonNumber} Episodes` : mode === "add" ? "Add Episode" : "Edit Episode"}
            </h3>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 hover:bg-slate-100 text-slate-400 hover:text-slate-600 rounded-lg transition-colors"
          >
            <FiX className="w-4.5 h-4.5" />
          </button>
        </div>

        {/* Modal Content */}
        <div className="flex-1 overflow-y-auto min-h-[300px]">
          {error && (
            <div className="mb-4 bg-red-50 border border-red-200 text-red-600 rounded-xl p-3 text-xs font-bold">
              {error}
            </div>
          )}

          {mode === "list" ? (
            <>
              <div className="flex justify-between items-center mb-4">
                <p className="text-xs text-slate-400 font-semibold">
                  Seasons list all episodes sorted by episode number.
                </p>
                <button
                  type="button"
                  onClick={handleAddClick}
                  className="flex items-center gap-1.5 px-3 py-1.5 bg-[#4B98C8] hover:bg-[#205E85] text-white rounded-xl text-xs font-bold uppercase tracking-wider transition-colors active:scale-95"
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
                <div className="text-center py-16 border border-dashed border-slate-200 rounded-xl">
                  <p className="text-slate-400 text-xs font-bold">No episodes found in this season.</p>
                </div>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full text-left border-collapse">
                    <thead>
                      <tr className="border-b border-slate-100 text-slate-400 text-[10px] font-bold uppercase tracking-widest">
                        <th className="pb-2.5 pl-2 w-16">Number</th>
                        <th className="pb-2.5">Title</th>
                        <th className="pb-2.5">Host & Guest</th>
                        <th className="pb-2.5">Link</th>
                        <th className="pb-2.5 pr-2 text-right">Actions</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-50 text-xs font-semibold text-slate-700">
                      {episodesPage.content.map((ep) => (
                        <tr key={ep.id} className="hover:bg-slate-50/50 transition-colors">
                          <td className="py-2.5 pl-2 text-slate-500 font-bold">Ep. {ep.episodeNumber}</td>
                          <td className="py-2.5">
                            <div className="flex items-center gap-2">
                              <img src={ep.imageUrl} alt={ep.title} className="w-10 h-7 object-cover rounded border border-slate-200 shrink-0" />
                              <span className="font-extrabold text-slate-800 truncate max-w-[150px]">{ep.title}</span>
                            </div>
                          </td>
                          <td className="py-2.5 text-slate-500">
                            <div>Host: <span className="text-slate-700 font-bold">{ep.host || "N/A"}</span></div>
                            <div className="text-[10px] text-slate-400 mt-0.5">Guest: <span className="font-bold">{ep.guest || "N/A"}</span></div>
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
                                className="p-1.5 bg-slate-50 hover:bg-sky-50 text-slate-500 hover:text-sky-600 rounded-lg transition-colors"
                              >
                                <FiEdit2 className="w-3.5 h-3.5" />
                              </button>
                              <button
                                onClick={() => handleDeleteClick(ep.id)}
                                className="p-1.5 bg-slate-50 hover:bg-red-50 text-slate-500 hover:text-red-500 rounded-lg transition-colors"
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
                <div className="flex items-center justify-between border-t border-slate-100 pt-4 mt-4">
                  <span className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">
                    Page {episodesPage.number + 1} of {episodesPage.totalPages}
                  </span>
                  <div className="flex gap-2">
                    <button
                      disabled={episodesPage.number === 0}
                      onClick={() => handlePageChange(episodesPage.number - 1)}
                      className="p-1.5 border border-slate-200 text-slate-500 hover:text-slate-800 disabled:opacity-40 rounded-lg active:scale-95 transition-all"
                    >
                      <FiChevronLeft className="w-4 h-4" />
                    </button>
                    <button
                      disabled={episodesPage.number >= episodesPage.totalPages - 1}
                      onClick={() => handlePageChange(episodesPage.number + 1)}
                      className="p-1.5 border border-slate-200 text-slate-500 hover:text-slate-800 disabled:opacity-40 rounded-lg active:scale-95 transition-all"
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
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                    Episode Number *
                  </label>
                  <input
                    type="number"
                    required
                    min="1"
                    value={episodeNumber}
                    onChange={(e) => setEpisodeNumber(e.target.value)}
                    placeholder="e.g. 1"
                    className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                  />
                </div>

                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                    Episode Title *
                  </label>
                  <input
                    type="text"
                    required
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    placeholder="e.g. Intro to Web Development"
                    className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                  />
                </div>
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                    Host Name
                  </label>
                  <input
                    type="text"
                    value={host}
                    onChange={(e) => setHost(e.target.value)}
                    placeholder="e.g. John"
                    className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                  />
                </div>

                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                    Guest Name
                  </label>
                  <input
                    type="text"
                    value={guest}
                    onChange={(e) => setGuest(e.target.value)}
                    placeholder="e.g. Dr. Ahmed"
                    className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                  />
                </div>
              </div>

              <div>
                <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                  Episode Link / URL *
                </label>
                <input
                  type="url"
                  required
                  value={url}
                  onChange={(e) => setUrl(e.target.value)}
                  placeholder="https://www.youtube.com/watch?v=..."
                  className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                />
              </div>

              <div>
                <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                  Episode Cover Image *
                </label>

                {imageUrl ? (
                  <div className="relative flex items-center gap-4 p-3 bg-slate-50 border border-slate-200 rounded-xl">
                    <img src={imageUrl} alt="Episode Preview" className="w-16 h-12 rounded object-cover border border-slate-200" />
                    <div className="flex-1 min-w-0">
                      <p className="text-[10px] font-bold text-slate-700 truncate">{imageUrl.split("/").pop()}</p>
                      <p className="text-[9px] text-slate-400 font-medium truncate mt-0.5">Cloudinary Asset</p>
                    </div>
                    <button
                      type="button"
                      onClick={handleRemoveImage}
                      className="p-2 bg-red-50 text-red-500 hover:bg-red-100 hover:text-red-600 rounded-lg transition-colors active:scale-95"
                      title="Remove image"
                    >
                      <FiTrash2 className="w-4 h-4" />
                    </button>
                  </div>
                ) : (
                  <div className="relative border-2 border-dashed border-slate-200 hover:border-[#4B98C8]/50 hover:bg-slate-50 rounded-xl p-5 text-center flex flex-col items-center justify-center cursor-pointer">
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
                        <p className="text-[10px] font-bold text-slate-600">Uploading to Cloudinary...</p>
                      </>
                    ) : (
                      <>
                        <FiUploadCloud className="w-6 h-6 text-slate-400 mb-1" />
                        <p className="text-[10px] font-bold text-slate-600">Click to upload cover image</p>
                      </>
                    )}
                  </div>
                )}
                {uploadError && (
                  <p className="text-[10px] text-red-500 font-bold mt-1.5">{uploadError}</p>
                )}
              </div>

              <div className="flex gap-3 justify-end pt-4 border-t border-slate-100">
                <button
                  type="button"
                  onClick={() => setMode("list")}
                  className="px-4 py-2.5 border border-slate-200 text-slate-500 hover:text-slate-800 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={saving || uploading}
                  className="px-4 py-2.5 bg-sky-500 hover:bg-sky-600 text-white text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow disabled:opacity-40 flex items-center gap-1.5"
                  style={{ backgroundColor: BRAND }}
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
      </div>
    </div>
  );
};

export default EpisodesManagementModal;
