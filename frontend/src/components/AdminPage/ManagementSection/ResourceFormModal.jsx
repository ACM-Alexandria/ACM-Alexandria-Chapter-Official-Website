import React, { useState, useEffect } from "react";
import { FiX, FiUploadCloud, FiTrash2, FiLoader } from "react-icons/fi";
import { uploadImage } from "../../../services/adminService";

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
  error,
}) => {
  if (!open) return null;

  const [uploading, setUploading] = useState(false);
  const [uploadError, setUploadError] = useState(null);

  useEffect(() => {
    if (!open) {
      setUploading(false);
      setUploadError(null);
    }
  }, [open]);

  const isCommittee = activeTab === "committees";
  const currentValue = isCommittee ? formData.logoUrl : formData.imageUrl;

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
      const data = await uploadImage(file);
      const url = data.url;
      setFormData(
        isCommittee
          ? { ...formData, logoUrl: url }
          : { ...formData, imageUrl: url }
      );
    } catch (err) {
      console.error("Upload failed:", err);
      setUploadError(err.message || "Failed to upload image. Please try again.");
    } finally {
      setUploading(false);
    }
  };

  const handleDragOver = (e) => {
    e.preventDefault();
  };

  const handleDrop = async (e) => {
    e.preventDefault();
    const file = e.dataTransfer.files[0];
    if (!file) return;

    if (file.size > 10 * 1024 * 1024) {
      setUploadError("File size exceeds 10MB limit.");
      return;
    }

    setUploading(true);
    setUploadError(null);

    try {
      const data = await uploadImage(file);
      const url = data.url;
      setFormData(
        isCommittee
          ? { ...formData, logoUrl: url }
          : { ...formData, imageUrl: url }
      );
    } catch (err) {
      console.error("Upload failed:", err);
      setUploadError(err.message || "Failed to upload image. Please try again.");
    } finally {
      setUploading(false);
    }
  };

  const handleRemove = () => {
    setFormData(
      isCommittee
        ? { ...formData, logoUrl: "" }
        : { ...formData, imageUrl: "" }
    );
    setUploadError(null);
  };


  const getTitle = () => {
    const modeStr = formMode === "add" ? "Create New" : "Edit";
    let catStr = "";
    if (activeTab === "highboard") catStr = "High Board Member";
    else if (activeTab === "committeeBoard") catStr = "Committee Board Member";
    else if (activeTab === "radio") catStr = "Radio Season";
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
          {/* Social Links Fields */}
          {activeTab === "socialLinks" && (
            <>
              <div>
                <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                  Platform Name
                </label>
                <input
                  type="text"
                  required
                  value={formData.platform || ""}
                  onChange={(e) => setFormData({ ...formData, platform: e.target.value })}
                  placeholder="e.g. Facebook, Instagram, LinkedIn, etc."
                  className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                />
              </div>
              <div>
                <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                  URL
                </label>
                <input
                  type="url"
                  required
                  value={formData.url || ""}
                  onChange={(e) => setFormData({ ...formData, url: e.target.value })}
                  placeholder="https://example.com/acm-alexandria"
                  className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                />
              </div>
            </>
          )}

          {/* Radio Season Fields */}
          {activeTab === "radio" && (
            <div>
              <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                Season Number
              </label>
              <input
                type="number"
                required
                min="1"
                value={formData.seasonNumber ?? ""}
                onChange={(e) => setFormData({ ...formData, seasonNumber: e.target.value ? parseInt(e.target.value) : "" })}
                placeholder="e.g. 1"
                className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
              />
            </div>
          )}

          {/* Common Fields: Name/Title (for all except socialLinks and radio) */}
          {activeTab !== "socialLinks" && (
            <>
              {activeTab !== "radio" && (
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                    Name / Title
                  </label>
                  <input
                    type="text"
                    required
                    value={activeTab === "exclusiveForms" ? (formData.title || "") : (formData.name || "")}
                    onChange={(e) => {
                      if (activeTab === "exclusiveForms") setFormData({ ...formData, title: e.target.value });
                      else setFormData({ ...formData, name: e.target.value });
                    }}
                    placeholder="e.g. John Doe"
                    className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                  />
                </div>
              )}

              {/* Common Fields: Image/Logo Upload */}
              <div>
                <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                    {isCommittee ? "Logo" : "Image"}
                  </label>

                {currentValue ? (
                  // Preview state
                  <div className="relative flex items-center gap-4 p-3.5 bg-slate-50 border border-slate-200 rounded-xl">
                    <img
                      src={currentValue}
                      alt="Preview"
                      className="w-16 h-16 rounded-lg object-cover border border-slate-200 shadow-sm"
                    />
                    <div className="flex-1 min-w-0">
                      <p className="text-[10px] font-bold text-slate-700 truncate">
                        {currentValue.split("/").pop()}
                      </p>
                      <p className="text-[9px] text-slate-400 font-medium truncate mt-0.5">
                        Uploaded to Cloudinary
                      </p>
                    </div>
                    <button
                      type="button"
                      onClick={handleRemove}
                      className="p-2 bg-red-50 text-red-500 hover:bg-red-100 hover:text-red-600 rounded-lg transition-colors active:scale-95"
                      title="Delete image"
                    >
                      <FiTrash2 className="w-4 h-4" />
                    </button>
                  </div>
                ) : (
                  // Upload drop zone
                  <div
                    onDragOver={handleDragOver}
                    onDrop={handleDrop}
                    className={`relative border-2 border-dashed rounded-xl p-6 transition-all text-center flex flex-col items-center justify-center cursor-pointer ${
                      uploading
                        ? "border-[#4B98C8]/40 bg-[#4B98C8]/5"
                        : "border-slate-200 hover:border-[#4B98C8]/50 hover:bg-slate-50"
                    }`}
                  >
                    <input
                      type="file"
                      id="image-file-input"
                      accept="image/*"
                      onChange={handleFileChange}
                      disabled={uploading}
                      className="absolute inset-0 w-full h-full opacity-0 cursor-pointer"
                    />
                    {uploading ? (
                      <>
                        <FiLoader className="w-8 h-8 text-[#4B98C8] animate-spin mb-2" />
                        <p className="text-[11px] font-bold text-slate-600">
                          Uploading image to Cloudinary...
                        </p>
                        <p className="text-[9px] text-slate-400 mt-1">
                          Please wait, this may take a moment.
                        </p>
                      </>
                    ) : (
                      <>
                        <FiUploadCloud className="w-8 h-8 text-slate-400 mb-2" />
                        <p className="text-[11px] font-bold text-slate-600">
                          Click to upload or drag & drop
                        </p>
                        <p className="text-[9px] text-slate-400 mt-1">
                          PNG, JPG, JPEG up to 10MB
                        </p>
                      </>
                    )}
                  </div>
                )}

                {uploadError && (
                  <p className="text-[10px] text-red-500 font-bold mt-1.5 flex items-center gap-1">
                    <span className="inline-block w-1 h-1 rounded-full bg-red-500"></span>
                    {uploadError}
                  </p>
                )}
              </div>

            </>
          )}

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

          {/* Event specific fields */}
          {activeTab === "events" && (
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
            </div>
          )}

          {/* Program specific fields */}
          {activeTab === "programs" && (
            <>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                    Start Date
                  </label>
                  <input
                    type="datetime-local"
                    value={formData.startDate || ""}
                    onChange={(e) => setFormData({ ...formData, startDate: e.target.value || null })}
                    className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                  />
                </div>
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                    End Date
                  </label>
                  <input
                    type="datetime-local"
                    value={formData.endDate || ""}
                    onChange={(e) => setFormData({ ...formData, endDate: e.target.value || null })}
                    className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                  />
                </div>
              </div>
              <div>
                <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
                  Schedule Time
                </label>
                <input
                  type="text"
                  value={formData.time || ""}
                  onChange={(e) => setFormData({ ...formData, time: e.target.value || "" })}
                  placeholder="e.g. Every Sunday 6:00 PM"
                  className="w-full px-3.5 py-2.5 bg-slate-50 border border-slate-200 text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8] transition-all"
                />
              </div>

              {/* Registration Open Toggle */}
              <div className="flex items-center justify-between p-4 bg-slate-50 rounded-xl border border-slate-200">
                <div>
                  <p className="text-xs font-extrabold text-slate-700">Registration Open</p>
                  <p className="text-[10px] text-slate-400 font-medium mt-0.5">
                    Allow users to register for this program
                  </p>
                </div>
                <button
                  type="button"
                  onClick={() => setFormData({ ...formData, registrationOpen: !formData.registrationOpen })}
                  className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-300 focus:outline-none ${
                    formData.registrationOpen ? "bg-[#4B98C8]" : "bg-slate-300"
                  }`}
                >
                  <span
                    className={`inline-block h-4 w-4 transform rounded-full bg-white shadow-md transition-transform duration-300 ${
                      formData.registrationOpen ? "translate-x-6" : "translate-x-1"
                    }`}
                  />
                </button>
              </div>
            </>
          )}


          {/* Description box (Clubs, Events, Committees, Programs, Exclusive Forms) */}
          {(activeTab === "clubs" ||
            activeTab === "events" ||
            activeTab === "committees" ||
            activeTab === "programs" ||
            activeTab === "exclusiveForms") && (
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

          {/* Exclusive Forms specific fields */}
          {activeTab === "exclusiveForms" && (
            <div className="flex items-center justify-between p-4 bg-slate-50 rounded-xl border border-slate-200 mt-4">
              <div>
                <p className="text-xs font-extrabold text-slate-700">Form Active Status</p>
                <p className="text-[10px] text-slate-400 font-medium mt-0.5">
                  Allow users to see and apply to this form
                </p>
              </div>
              <button
                type="button"
                onClick={() => setFormData({ ...formData, isActive: !formData.isActive })}
                className={`relative inline-flex h-6 w-11 items-center rounded-full transition-colors duration-300 focus:outline-none ${
                  formData.isActive ? "bg-[#4B98C8]" : "bg-slate-300"
                }`}
              >
                <span
                  className={`inline-block h-4 w-4 transform rounded-full bg-white shadow-md transition-transform duration-300 ${
                    formData.isActive ? "translate-x-6" : "translate-x-1"
                  }`}
                />
              </button>
            </div>
          )}

          {/* Error display */}
          {error && (
            <div className="bg-red-50 border border-red-200 text-red-600 rounded-xl p-3 flex items-center gap-2 mb-4">
              <span className="text-xs font-semibold">{error}</span>
            </div>
          )}

          {/* Actions row */}
          <div className="flex justify-end gap-3 pt-4 border-t border-slate-100 mt-6">
            <button
              type="button"
              onClick={onClose}
              disabled={loading || uploading}
              className="px-4 py-2.5 border border-slate-200 text-slate-500 hover:text-slate-800 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all disabled:opacity-40"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={loading || uploading}
              className="px-5 py-2.5 text-white text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow disabled:opacity-40"
              style={{ background: `linear-gradient(135deg, ${BRAND}, ${BRAND_DARK})` }}
            >
              {loading ? "Saving..." : uploading ? "Uploading..." : "Save Record"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};

export default ResourceFormModal;
