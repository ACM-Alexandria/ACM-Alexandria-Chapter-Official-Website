import React, { useState, useEffect, useRef } from "react";
import { FiPlus, FiTrash2, FiEdit2, FiArrowLeft, FiCheckSquare, FiList, FiType, FiImage } from "react-icons/fi";
import adminService from "../../../services/adminService";
import useThemedDialog from "../../../hooks/useThemedDialog";
import { Highlight } from "../../ThemedDialog";
import ModalShell from "../SharedComponents/ModalShell";
import {
  fieldClass,
  iconButtonClass,
  iconButtonDangerClass,
  noticeClass,
  primaryButtonClass,
  secondaryButtonClass,
  tagDangerClass,
  tagMutedClass,
} from "../SharedComponents/adminStyles";

const QuestionsManagementModal = ({ open, onClose, resourceId, resourceName, resourceType }) => {
  const { dialog, confirm } = useThemedDialog();
  const deleteQuestionInFlight = useRef(false);

  const [questions, setQuestions] = useState([]);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState(null);
  const [fieldErrors, setFieldErrors] = useState({});

  // Form mode: 'list', 'add', 'edit'
  const [mode, setMode] = useState("list");
  const [editingQuestion, setEditingQuestion] = useState(null);
  const [questionText, setQuestionText] = useState("");
  const [questionType, setQuestionType] = useState("TEXT");
  const [isRequired, setIsRequired] = useState(false);
  const [options, setOptions] = useState([]);

  const validateForm = () => {
    const errors = {};
    if (!questionText.trim()) {
      errors.questionText = "Question text is required and cannot be empty.";
    }
    if (!questionType) {
      errors.questionType = "Question type is required.";
    }
    if (questionType === "MULTIPLE_CHOICE" || questionType === "CHECKBOX") {
      if (options.length === 0) {
        errors.options = "At least one option must be added.";
      } else {
        const emptyIndices = [];
        options.forEach((opt, idx) => {
          if (!opt.trim()) {
            emptyIndices.push(idx);
          }
        });
        if (emptyIndices.length > 0) {
          errors.options = "Option fields cannot be empty. Please fill in or remove them.";
          errors.emptyOptionIndices = emptyIndices;
        }
      }
    }
    setFieldErrors(errors);
    return Object.keys(errors).length === 0;
  };

  const loadQuestions = async () => {
    setLoading(true);
    setError(null);
    try {
      const data = await adminService.fetchQuestions(resourceType, resourceId);
      const mapped = (data || []).map((q) => ({
        id: q.id,
        questionText: q.question_text || q.questionText || "",
        questionType: q.question_type || q.questionType || "TEXT",
        isRequired: q.is_required ?? q.isRequired ?? false,
        options: q.options || [],
      }));
      setQuestions(mapped);
    } catch (err) {
      console.error("Error loading questions:", err);
      setError(err.message || "Failed to load form questions.");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (open && resourceId) {
      loadQuestions();
      setMode("list");
      setEditingQuestion(null);
      setError(null);
    }
  }, [open, resourceId, resourceType]);

  if (!open || !resourceId) return null;

  const handleEditClick = (q) => {
    setEditingQuestion(q);
    setQuestionText(q.questionText || "");
    setQuestionType(q.questionType || "TEXT");
    setIsRequired(q.isRequired || false);
    setOptions(q.options || []);
    setError(null);
    setFieldErrors({});
    setMode("edit");
  };

  const handleAddClick = () => {
    setEditingQuestion(null);
    setQuestionText("");
    setQuestionType("TEXT");
    setIsRequired(false);
    setOptions([]);
    setError(null);
    setFieldErrors({});
    setMode("add");
  };

  const handleDeleteClick = async (q) => {
    if (deleteQuestionInFlight.current) return;
    deleteQuestionInFlight.current = true;

    try {
      const questionId = typeof q === "object" ? q.id : q;
      const questionName = typeof q === "object" ? q.questionText : null;

      const confirmed = await confirm({
        tone: "danger",
        title: "Delete Form Question?",
        description: questionName ? (
          <>
            Are you sure you want to delete <Highlight>{questionName}</Highlight>? This will permanently delete the question and all historical submitted answers for it.
          </>
        ) : (
          "Are you sure you want to delete this question? This will permanently delete the question and all historical submitted answers for it."
        ),
        confirmLabel: "Yes, Delete",
        cancelLabel: "Cancel",
      });

      if (!confirmed) return;

      setLoading(true);
      setError(null);
      try {
        await adminService.deleteQuestion(resourceType, resourceId, questionId);
        await loadQuestions();
      } catch (err) {
        console.error("Error deleting question:", err);
        setError(err.message || "Failed to delete question.");
        setLoading(false);
      }
    } finally {
      deleteQuestionInFlight.current = false;
    }
  };

  const handleAddOption = () => {
    setOptions([...options, ""]);
    if (fieldErrors.options || fieldErrors.emptyOptionIndices) {
      setFieldErrors((prev) => ({ 
        ...prev, 
        options: null, 
        emptyOptionIndices: null 
      }));
    }
  };

  const handleOptionChange = (idx, value) => {
    const updated = [...options];
    updated[idx] = value;
    setOptions(updated);
    if (fieldErrors.options || fieldErrors.emptyOptionIndices) {
      setFieldErrors((prev) => {
        const next = { ...prev };
        if (next.emptyOptionIndices) {
          next.emptyOptionIndices = next.emptyOptionIndices.filter((i) => i !== idx);
          if (next.emptyOptionIndices.length === 0) {
            next.emptyOptionIndices = null;
            next.options = null;
          }
        } else {
          next.options = null;
        }
        return next;
      });
    }
  };

  const handleRemoveOption = (idx) => {
    const updated = options.filter((_, i) => i !== idx);
    setOptions(updated);
    if (fieldErrors.options || fieldErrors.emptyOptionIndices) {
      setFieldErrors((prev) => {
        const next = { ...prev };
        if (next.emptyOptionIndices) {
          const filtered = next.emptyOptionIndices
            .filter((i) => i !== idx)
            .map((i) => (i > idx ? i - 1 : i));
          next.emptyOptionIndices = filtered.length > 0 ? filtered : null;
          if (!next.emptyOptionIndices) {
            next.options = null;
          }
        } else {
          next.options = null;
        }
        return next;
      });
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (!validateForm()) {
      setError("Please correct the errors in the form before saving.");
      return;
    }

    setSaving(true);
    setError(null);
    try {
      const cleanOptions = (questionType === "MULTIPLE_CHOICE" || questionType === "CHECKBOX")
        ? options.map((opt) => opt.trim())
        : [];

      const payload = {
        question_text: questionText.trim(),
        question_type: questionType,
        is_required: isRequired,
        options: cleanOptions,
      };

      if (mode === "add") {
        await adminService.createQuestion(resourceType, resourceId, payload);
      } else {
        await adminService.updateQuestion(resourceType, resourceId, editingQuestion.id, payload);
      }

      setMode("list");
      loadQuestions();
    } catch (err) {
      console.error("Error saving question:", err);
      setError(err.message || "Failed to save question.");
    } finally {
      setSaving(false);
    }
  };

  const getTypeIcon = (type) => {
    if (type === "MULTIPLE_CHOICE") return <FiList className="w-3.5 h-3.5" />;
    if (type === "CHECKBOX") return <FiCheckSquare className="w-3.5 h-3.5" />;
    if (type === "IMAGE") return <FiImage className="w-3.5 h-3.5" />;
    return <FiType className="w-3.5 h-3.5" />;
  };

  return (
    <>
      <ModalShell
        open
        onClose={onClose}
        title={mode === "list" ? "Registration form questions" : mode === "add" ? "Add question" : "Edit question"}
        subtitle={resourceName}
        size="lg"
        busy={saving}
        footer={
          mode === "list" ? (
            <button type="button" onClick={onClose} className={secondaryButtonClass}>
              Close
            </button>
          ) : null
        }
      >
        <div className="space-y-4">
          {error && (
            <div role="alert" className={noticeClass.error}>
              {error}
            </div>
          )}

          {mode === "list" ? (
            <>
              {/* List View Actions */}
              <div className="flex justify-between items-center mb-1 shrink-0">
                <span className="text-sm font-medium text-slate-700 dark:text-slate-300">
                  Form structure ({questions.length} questions)
                </span>
                <button
                  type="button"
                  onClick={handleAddClick}
                  className={primaryButtonClass}
                >
                  <FiPlus className="w-4 h-4" aria-hidden="true" /> Add question
                </button>
              </div>

              {/* Questions List */}
              {loading ? (
                <div className="flex flex-col items-center justify-center py-12">
                  <div className="w-8 h-8 border-4 border-slate-200 dark:border-ocean-745 border-t-ocean-461 rounded-full animate-spin mb-3" />
                  <p className="text-sm text-slate-600 dark:text-slate-400">Loading questions…</p>
                </div>
              ) : questions.length === 0 ? (
                <div className="text-center py-10 px-4 border border-dashed border-slate-300 dark:border-ocean-745 rounded-lg">
                  <p className="text-sm text-slate-600 dark:text-slate-400">No custom questions defined yet. Only standard registration fields (Name, Email, Phone, Batch, Dept) are requested.</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {questions.map((q, index) => (
                    <div key={q.id || index} className="bg-slate-50 dark:bg-ocean-916/60 border border-slate-200 dark:border-ocean-745 rounded-lg p-4 flex justify-between gap-4">
                      <div className="space-y-1.5 min-w-0">
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className={`${tagMutedClass} gap-1`}>
                            {getTypeIcon(q.questionType)}
                            {q.questionType?.replace("_"," ")}
                          </span>
                          <span className={q.isRequired ? tagDangerClass : tagMutedClass}>
                            {q.isRequired ? "Required" : "Optional"}
                          </span>
                        </div>
                        <h4 className="text-sm font-medium text-slate-900 dark:text-slate-100 break-words">
                          {q.questionText}
                        </h4>
                        {(q.questionType === "MULTIPLE_CHOICE" || q.questionType === "CHECKBOX") && q.options && q.options.length > 0 && (
                          <div className="flex flex-wrap gap-1.5 pt-1">
                            {q.options.map((opt, oIdx) => (
                              <span key={oIdx} className="bg-white dark:bg-ocean-890 border border-slate-200 dark:border-ocean-745 text-slate-700 dark:text-slate-300 text-xs px-2 py-0.5 rounded-lg">
                                {opt}
                              </span>
                            ))}
                          </div>
                        )}
                      </div>
                      <div className="flex gap-1.5 self-start shrink-0">
                        <button
                          type="button"
                          onClick={() => handleEditClick(q)}
                          className={iconButtonClass}
                          title="Edit question"
                          aria-label="Edit question"
                        >
                          <FiEdit2 className="w-4 h-4" aria-hidden="true" />
                        </button>
                        <button
                          type="button"
                          onClick={() => handleDeleteClick(q)}
                          disabled={loading}
                          className={iconButtonDangerClass}
                          title="Delete question"
                          aria-label="Delete question"
                        >
                          <FiTrash2 className="w-4 h-4" aria-hidden="true" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </>
          ) : (
            /* Add/Edit Form View */
            <form onSubmit={handleSubmit} noValidate className="space-y-5">
              <div>
                <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                  Question text / Label
                </label>
                <input
                  type="text"
                  required
                  value={questionText}
                  onChange={(e) => {
                    setQuestionText(e.target.value);
                    if (fieldErrors.questionText) {
                      setFieldErrors((prev) => ({ ...prev, questionText: null }));
                    }
                  }}
                  placeholder="e.g. What is your GitHub profile link?"
                          className={`${fieldClass} ${fieldErrors.questionText ? "border-rose-500! focus:ring-rose-500/25!" : ""}`}
                />
                {fieldErrors.questionText && (
                  <p className="mt-1.5 text-xs font-medium text-rose-700 dark:text-rose-400">
                    {fieldErrors.questionText}
                  </p>
                )}
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block mb-1.5 text-sm font-medium text-slate-700 dark:text-slate-300">
                    Input Type
                  </label>
                  <select
                    value={questionType}
                    onChange={(e) => {
                      setQuestionType(e.target.value);
                      if (fieldErrors.questionType || fieldErrors.options || fieldErrors.emptyOptionIndices) {
                        setFieldErrors((prev) => ({ ...prev, questionType: null, options: null, emptyOptionIndices: null }));
                      }
                    }}
                    className={`${fieldClass} cursor-pointer ${fieldErrors.questionType ? "border-rose-500! focus:ring-rose-500/25!" : ""}`}
                  >
                    <option value="TEXT">Short Text Answer</option>
                    <option value="MULTIPLE_CHOICE">Multiple Choice (Dropdown)</option>
                    <option value="CHECKBOX">Checkbox Selection</option>
                    <option value="IMAGE">Image Upload</option>
                  </select>
                  {fieldErrors.questionType && (
                    <p className="mt-1.5 text-xs font-medium text-rose-700 dark:text-rose-400">
                      {fieldErrors.questionType}
                    </p>
                  )}
                </div>

                <div className="flex items-center pt-5">
                  <label className="flex items-center gap-2.5 cursor-pointer select-none">
                    <input
                      type="checkbox"
                      checked={isRequired}
                      onChange={(e) => setIsRequired(e.target.checked)}
                      className="w-4 h-4 rounded text-ocean-461 focus:ring-ocean-461/30 border-slate-300 dark:border-ocean-745"
                    />
                    <span className="text-sm font-medium text-slate-800 dark:text-slate-200">
                      Mark as required
                    </span>
                  </label>
                </div>
              </div>

              {/* Info note for IMAGE type */}
              {questionType === "IMAGE" && (
                <div className="flex items-start gap-2.5 bg-ocean-461/10 border border-ocean-461/30 rounded-lg px-4 py-3">
                  <FiImage className="w-4 h-4 text-ocean-461 shrink-0 mt-0.5" />
                  <p className="text-sm text-slate-800 dark:text-slate-200">
                    Users will be prompted to upload an image file.<br />
                    <span className="font-semibold">Maximum size: 2 MB.</span> Accepted formats: JPG, PNG, WEBP, GIF, etc.
                  </p>
                </div>
              )}

              {/* Options Editor (Only if MULTIPLE_CHOICE or CHECKBOX) */}
              {(questionType === "MULTIPLE_CHOICE" || questionType === "CHECKBOX") && (
                <div className="space-y-3 border-t border-slate-200 dark:border-ocean-800 pt-4">
                  <div className="flex justify-between items-center">
                    <label className="block text-xs font-medium text-slate-600 dark:text-slate-400">
                      {questionType === "MULTIPLE_CHOICE" ? "Dropdown Options" : "Checkbox Options"}
                    </label>
                    <button
                      type="button"
                      onClick={handleAddOption}
                      className="text-sm font-medium text-ocean-614 dark:text-ocean-316 hover:underline flex items-center gap-1"
                    >
                      <FiPlus className="w-4 h-4" aria-hidden="true" /> Add option
                    </button>
                  </div>

                  {options.length === 0 ? (
                    <p className="text-sm text-slate-600 dark:text-slate-400">
                      Please add at least one option for the {questionType === "MULTIPLE_CHOICE" ? "dropdown" : "checkbox"} selection.
                    </p>
                  ) : (
                    <div className="space-y-2">
                      {options.map((opt, optIdx) => {
                        const isOptionEmpty = fieldErrors.emptyOptionIndices?.includes(optIdx);
                        return (
                          <div key={optIdx} className="flex gap-2 items-center">
                            <input
                              type="text"
                              value={opt}
                              onChange={(e) => handleOptionChange(optIdx, e.target.value)}
                              placeholder={`Option ${optIdx + 1}`}
                              className={`${fieldClass} flex-1 min-w-0 ${isOptionEmpty || fieldErrors.options ? "border-rose-500! focus:ring-rose-500/25!" : ""}`}
                            />
                            <button
                              type="button"
                              onClick={() => handleRemoveOption(optIdx)}
                              className={`${iconButtonDangerClass} shrink-0`}
                              title="Remove option"
                              aria-label={`Remove option ${optIdx + 1}`}
                            >
                              <FiTrash2 className="w-4 h-4" aria-hidden="true" />
                            </button>
                          </div>
                        );
                      })}
                    </div>
                  )}
                  {fieldErrors.options && (
                    <p className="mt-1.5 text-xs font-medium text-rose-700 dark:text-rose-400">
                      {fieldErrors.options}
                    </p>
                  )}
                </div>
              )}

              {/* Form Footer Buttons */}
              <div className="flex flex-col-reverse sm:flex-row sm:justify-between gap-2 pt-4 border-t border-slate-200 dark:border-ocean-800 mt-6">
                <button
                  type="button"
                  onClick={() => setMode("list")}
                  className={secondaryButtonClass}
                >
                  <FiArrowLeft className="w-4 h-4" aria-hidden="true" /> Back to list
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className={primaryButtonClass}
                >
                  {saving ? "Saving…" : "Save question"}
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

export default QuestionsManagementModal;
