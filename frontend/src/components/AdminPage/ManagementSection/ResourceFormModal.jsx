import React, { useState } from "react";
import { FiX, FiTrash2, FiPlus, FiMinus, FiType, FiCheckSquare, FiList, FiImage } from "react-icons/fi";
import ImageUploadField from "../SharedComponents/ImageUploadField";
import ModalShell from "../SharedComponents/ModalShell";
import ToggleRow from "../SharedComponents/ToggleRow";
import {
  fieldClass,
  noticeClass,
  primaryButtonClass,
  secondaryButtonClass,
  textareaClass,
} from "../SharedComponents/adminStyles";

const ResourceForm = ({
  onClose,
  onSubmit,
  formMode,
  activeTab,
  formData,
  setFormData,
  loading,
  error,
}) => {
  const [uploading, setUploading] = useState(false);
  const isCommittee = activeTab === "committees";
  const imageKey = isCommittee ? "logoUrl" : "imageUrl";

  const getTitle = () => {
    const modeStr = formMode === "add" ? "Create New" : "Edit";
    let catStr = "";
    if (activeTab === "highboard") catStr = "High Board Member";
    else if (activeTab === "committeeBoard") catStr = "Committee Board Member";
    else if (activeTab === "radio") catStr = "Radio Season";
    else if (activeTab === "partners") catStr = "Partner";
    else catStr = activeTab.charAt(0).toUpperCase() + activeTab.slice(1, -1);
    return `${modeStr} ${catStr}`;
  };

  return (
    <ModalShell
      open
      onClose={onClose}
      onSubmit={onSubmit}
      title={getTitle()}
      busy={loading || uploading}
      footer={
        <>
          <button type="button" onClick={onClose} disabled={loading || uploading} className={secondaryButtonClass}>
            Cancel
          </button>
          <button type="submit" disabled={loading || uploading} className={primaryButtonClass}>
            {loading ? "Saving…" : uploading ? "Uploading…" : "Save"}
          </button>
        </>
      }
    >
      <div className="space-y-5">
          {/* Social Links Fields */}
          {activeTab === "socialLinks" && (
            <>
              <div>
                <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                  Platform Name
                </label>
                <input
                  type="text"
                  required
                  value={formData.platform || ""}
                  onChange={(e) => setFormData({ ...formData, platform: e.target.value })}
                  placeholder="e.g. Facebook, Instagram, LinkedIn, etc."
                  className={fieldClass}
                />
              </div>
              <div>
                <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                  URL
                </label>
                <input
                  type="url"
                  required
                  value={formData.url || ""}
                  onChange={(e) => setFormData({ ...formData, url: e.target.value })}
                  placeholder="https://example.com/acm-alexandria"
                  className={fieldClass}
                />
              </div>
            </>
          )}

          {/* Radio Season Fields */}
          {activeTab === "radio" && (
            <div>
              <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                Season Number
              </label>
              <input
                type="number"
                required
                min="1"
                value={formData.seasonNumber ?? ""}
                onChange={(e) => setFormData({ ...formData, seasonNumber: e.target.value ? parseInt(e.target.value) : "" })}
                placeholder="e.g. 1"
                className={fieldClass}
              />
            </div>
          )}

          {/* Common Fields: Name/Title (for all except socialLinks and radio) */}
          {activeTab !== "socialLinks" && (
            <>
              {activeTab !== "radio" && (
                <div>
                  <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
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
                    className={fieldClass}
                  />
                </div>
              )}

              <ImageUploadField
                label={isCommittee ? "Logo" : "Image"}
                value={formData[imageKey]}
                onChange={(url) => setFormData((previous) => ({ ...previous, [imageKey]: url }))}
                disabled={loading}
                uploading={uploading}
                onUploadingChange={setUploading}
              />

            </>
          )}

          {/* High Board and Committee Board fields */}
          {(activeTab === "highboard" || activeTab === "committeeBoard") && (
            <>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                    Role
                  </label>
                  <input
                    type="text"
                    required
                    value={formData.role || ""}
                    onChange={(e) => setFormData({ ...formData, role: e.target.value })}
                    placeholder="e.g. Coordinator"
                    className={fieldClass}
                  />
                </div>
                <div>
                  <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                    Order Index (Sorting)
                  </label>
                  <input
                    type="number"
                    min="1"
                    value={formData.order ?? ""}
                    onChange={(e) => setFormData({ ...formData, order: e.target.value ? parseInt(e.target.value) : null })}
                    placeholder="e.g. 1 (Optional)"
                    className={fieldClass}
                  />
                </div>
              </div>
              <div>
                <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                  LinkedIn URL
                </label>
                <input
                  type="url"
                  value={formData.linkedinUrl || ""}
                  onChange={(e) => setFormData({ ...formData, linkedinUrl: e.target.value })}
                  placeholder="https://linkedin.com/in/username"
                  className={fieldClass}
                />
              </div>
            </>
          )}


          {/* Event specific fields */}
          {activeTab === "events" && (
            <div className="space-y-4">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                    Event Date & Time
                  </label>
                  <input
                    type="datetime-local"
                    value={formData.eventTime || ""}
                    onChange={(e) => setFormData({ ...formData, eventTime: e.target.value || null })}
                    className={fieldClass}
                  />
                </div>

                <div>
                  <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                    Event End Date & Time
                  </label>
                  <input
                    type="datetime-local"
                    value={formData.endTime || ""}
                    onChange={(e) => setFormData({ ...formData, endTime: e.target.value || null })}
                    className={fieldClass}
                  />
                </div>
              </div>

              <div>
                <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                  Location
                </label>
                <input
                  type="text"
                  value={formData.location || ""}
                  onChange={(e) => setFormData({ ...formData, location: e.target.value || null })}
                  placeholder="e.g. Hall A / Zoom (Optional)"
                  className={fieldClass}
                />
              </div>
            </div>
          )}



          {/* Program specific fields */}
          {activeTab === "programs" && (
            <>
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                    Start Date
                  </label>
                  <input
                    type="datetime-local"
                    value={formData.startDate || ""}
                    onChange={(e) => setFormData({ ...formData, startDate: e.target.value || null })}
                    className={fieldClass}
                  />
                </div>
                <div>
                  <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                    End Date
                  </label>
                  <input
                    type="datetime-local"
                    value={formData.endDate || ""}
                    onChange={(e) => setFormData({ ...formData, endDate: e.target.value || null })}
                    className={fieldClass}
                  />
                </div>
              </div>
              <div>
                <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                  Schedule Time
                </label>
                <input
                  type="text"
                  value={formData.time || ""}
                  onChange={(e) => setFormData({ ...formData, time: e.target.value || "" })}
                  placeholder="e.g. Every Sunday 6:00 PM"
                  className={fieldClass}
                />
              </div>

              {/* Registration Open Toggle */}
              <ToggleRow
  title="Registration Open"
  description="Allow users to register for this program"
  checked={formData.registrationOpen}
  onChange={() => setFormData({ ...formData, registrationOpen: !formData.registrationOpen })}
/>
            </>
          )}


          {/* External Club & Registration Open Toggles */}
          {activeTab === "clubs" && (
            <div className="space-y-4">
              <ToggleRow
  title="External Club"
  description="Allow user registrations (Internal clubs are restricted)"
  checked={formData.isExternal}
  onChange={() => setFormData({ ...formData, isExternal: !formData.isExternal })}
/>

              <ToggleRow
  title="Registration Open"
  description="Allow users to register for this club"
  checked={formData.registrationOpen}
  onChange={() => setFormData({ ...formData, registrationOpen: !formData.registrationOpen })}
/>
            </div>
          )}

          {activeTab === "events" && (
            <ToggleRow
  title="Registration Open"
  description="Allow users to register for this event"
  checked={formData.registrationOpen}
  onChange={() => setFormData({ ...formData, registrationOpen: !formData.registrationOpen })}
/>
          )}
          {(activeTab === "clubs" ||
            activeTab === "events" ||
            activeTab === "committees" ||
            activeTab === "programs" ||
            activeTab === "exclusiveForms") && (
            <div>
              <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                Description
              </label>
              <textarea
                value={formData.description || ""}
                onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                placeholder="Provide a detailed description…"
                rows="3"
                className={`${textareaClass} resize-none`}
              />
            </div>
          )}

          {/* Inline Form Questions (Only on Add Mode for Events) */}
          {activeTab === "events" && formMode === "add" && (
            <div className="p-4 border border-slate-200 dark:border-[#28405A] rounded-lg bg-slate-50 dark:bg-[#0B1520]/60">
              <div className="flex items-center justify-between mb-3">
                <div>
                  <h4 className="text-sm font-medium text-slate-900 dark:text-slate-100">Form Questions (Optional)</h4>
                  <p className="text-xs text-slate-600 dark:text-slate-400">Add questions now to prevent an empty registration form.</p>
                </div>
                <button
                  type="button"
                  onClick={() => {
                    const newQuestions = formData.questions || [];
                    setFormData({ ...formData, questions: [...newQuestions, { questionText: "", questionType: "TEXT", isRequired: false, options: [] }] });
                  }}
                  className={secondaryButtonClass}
                >
                  <FiPlus className="w-3.5 h-3.5" />
                  Add Question
                </button>
              </div>
              
              <div className="space-y-3">
                {(formData.questions || []).length === 0 ? (
                  <div className="text-center py-6 px-4 text-sm text-slate-600 dark:text-slate-400 border border-dashed border-slate-300 dark:border-[#28405A] rounded-lg bg-white dark:bg-[#0F1C29]">
                    No questions added yet. The form will only collect default fields (Name, Email, etc.).
                  </div>
                ) : (
                  (formData.questions || []).map((q, idx) => (
                    <div key={idx} className="p-3 bg-white dark:bg-[#0F1C29] border border-slate-200 dark:border-[#28405A] rounded-lg relative group">
                      <button
                        type="button"
                        onClick={() => {
                          const newQ = [...formData.questions];
                          newQ.splice(idx, 1);
                          setFormData({ ...formData, questions: newQ });
                        }}
                        className="absolute top-2 right-2 p-1.5 rounded-lg text-slate-600 dark:text-slate-400 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/30 transition-colors"
                      >
                        <FiTrash2 className="w-3.5 h-3.5" />
                      </button>
                      
                      <div className="grid grid-cols-1 sm:grid-cols-12 gap-3 mb-3 pr-8">
                        <div className="sm:col-span-8">
                          <input
                            type="text"
                            placeholder="Question text…"
                            required
                            value={q.questionText}
                            onChange={(e) => {
                              const newQ = [...formData.questions];
                              newQ[idx].questionText = e.target.value;
                              setFormData({ ...formData, questions: newQ });
                            }}
                            className="w-full h-9 px-2.5 bg-white dark:bg-[#0B1520] border border-slate-300 dark:border-[#28405A] text-slate-900 dark:text-slate-100 placeholder:text-slate-500 text-sm rounded-lg focus:outline-none focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/25 transition-colors"
                          />
                        </div>
                        <div className="sm:col-span-4">
                          <select
                            value={q.questionType}
                            onChange={(e) => {
                              const newQ = [...formData.questions];
                              newQ[idx].questionType = e.target.value;
                              if (e.target.value === "MULTIPLE_CHOICE" || e.target.value === "CHECKBOX") {
                                if (newQ[idx].options.length === 0) newQ[idx].options = [""];
                              }
                              setFormData({ ...formData, questions: newQ });
                            }}
                            className="w-full h-9 px-2.5 bg-white dark:bg-[#0B1520] border border-slate-300 dark:border-[#28405A] text-slate-900 dark:text-slate-100 placeholder:text-slate-500 text-sm rounded-lg focus:outline-none focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/25 transition-colors cursor-pointer"
                          >
                            <option value="TEXT">Text</option>
                            <option value="MULTIPLE_CHOICE">Multiple Choice</option>
                            <option value="CHECKBOX">Checkboxes</option>
                            <option value="IMAGE">Image</option>
                          </select>
                        </div>
                      </div>
                      
                      <label className="flex items-center gap-2 text-sm text-slate-700 dark:text-slate-300 cursor-pointer w-max mb-2">
                        <input
                          type="checkbox"
                          checked={q.isRequired}
                          onChange={(e) => {
                            const newQ = [...formData.questions];
                            newQ[idx].isRequired = e.target.checked;
                            setFormData({ ...formData, questions: newQ });
                          }}
                          className="rounded border-slate-300 text-[#4B98C8] focus:ring-[#4B98C8]"
                        />
                        Required Field
                      </label>

                      {(q.questionType === "MULTIPLE_CHOICE" || q.questionType === "CHECKBOX") && (
                        <div className="pl-2 border-l-2 border-[#4B98C8]/30 space-y-2 mt-2">
                          {q.options.map((opt, optIdx) => (
                            <div key={optIdx} className="flex items-center gap-2">
                              <div className="w-1.5 h-1.5 rounded-full bg-slate-300"></div>
                              <input
                                type="text"
                                placeholder={`Option ${optIdx + 1}`}
                                required
                                value={opt}
                                onChange={(e) => {
                                  const newQ = [...formData.questions];
                                  newQ[idx].options[optIdx] = e.target.value;
                                  setFormData({ ...formData, questions: newQ });
                                }}
                                className="flex-1 min-w-0 h-9 px-2.5 bg-white dark:bg-[#0B1520] border border-slate-300 dark:border-[#28405A] text-slate-900 dark:text-slate-100 placeholder:text-slate-500 text-sm rounded-lg focus:outline-none focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/25 transition-colors"
                              />
                              <button
                                type="button"
                                onClick={() => {
                                  const newQ = [...formData.questions];
                                  newQ[idx].options.splice(optIdx, 1);
                                  setFormData({ ...formData, questions: newQ });
                                }}
                                className="p-1.5 rounded-lg text-slate-600 dark:text-slate-400 hover:text-rose-600 hover:bg-rose-50 dark:hover:bg-rose-950/30 transition-colors"
                              >
                                <FiX className="w-3.5 h-3.5" />
                              </button>
                            </div>
                          ))}
                          <button
                            type="button"
                            onClick={() => {
                              const newQ = [...formData.questions];
                              newQ[idx].options.push("");
                              setFormData({ ...formData, questions: newQ });
                            }}
                            className="text-sm font-medium text-[#2C6F99] dark:text-[#7DBBE0] hover:underline flex items-center gap-1"
                          >
                            <FiPlus className="w-3 h-3" /> Add Option
                          </button>
                        </div>
                      )}
                    </div>
                  ))
                )}
              </div>
            </div>
          )}

          {/* Exclusive Forms specific fields */}
          {activeTab === "exclusiveForms" && (
            <ToggleRow
  title="Form Active Status"
  description="Allow users to see and apply to this form"
  checked={formData.isActive}
  onChange={() => setFormData({ ...formData, isActive: !formData.isActive })}
/>
          )}

          {/* Partners specific fields — website URL */}
          {activeTab === "partners" && (
            <div>
              <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                Website URL
              </label>
              <input
                type="url"
                value={formData.website || ""}
                onChange={(e) => setFormData({ ...formData, website: e.target.value })}
                placeholder="https://partner-website.com (Optional)"
                className={fieldClass}
              />
            </div>
          )}

          {/* Announcement toggle (Only on Add Mode for Events, Clubs and Programs) */}
          {(activeTab === "events" || activeTab === "clubs" || activeTab === "programs") && formMode === "add" && (
            <ToggleRow
  title="Email Subscribers"
  description="Send an announcement email to newsletter subscribers (you can send it later instead)"
  checked={formData.sendAnnouncement !== false}
  onChange={() => setFormData({ ...formData, sendAnnouncement: formData.sendAnnouncement === false })}
/>
          )}

          {/* Error display */}
          {error && (
            <div role="alert" className={noticeClass.error}>
              {error}
            </div>
          )}
      </div>
    </ModalShell>
  );
};

const ResourceFormModal = ({ open, ...props }) => (open ? <ResourceForm {...props} /> : null);

export default ResourceFormModal;
