import React, { useState, useEffect } from "react";
import { FiX, FiPlus, FiTrash2, FiEdit2, FiArrowLeft, FiCheckSquare, FiList, FiType } from "react-icons/fi";
import adminService from "../../../services/adminService";

const BRAND = "#4B98C8";
const BRAND_DARK = "#205E85";

const QuestionsManagementModal = ({ open, onClose, resourceId, resourceName, resourceType }) => {
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

  const handleDeleteClick = async (questionId) => {
    if (!window.confirm("Are you sure you want to delete this question? This will also remove all submitted answers for it.")) return;
    setLoading(true);
    setError(null);
    try {
      await adminService.deleteQuestion(resourceType, resourceId, questionId);
      loadQuestions();
    } catch (err) {
      console.error("Error deleting question:", err);
      setError(err.message || "Failed to delete question.");
      setLoading(false);
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
    return <FiType className="w-3.5 h-3.5" />;
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-900/40 backdrop-blur-sm animate-[fadeIn_0.2s_ease]">
      <div className="bg-white rounded-2xl border border-slate-200 shadow-2xl max-w-xl w-full max-h-[85vh] overflow-y-auto p-6 animate-[scaleIn_0.25s_ease] flex flex-col">
        {/* Modal Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-100 shrink-0">
          <div>
            <h3 className="text-base font-extrabold text-slate-800 tracking-tight">
              {mode === "list" ? "Registration Form Questions" : mode === "add" ? "Add Question" : "Edit Question"}
            </h3>
            <p className="text-[10px] text-slate-400 font-medium mt-0.5 uppercase tracking-wider">
              {resourceName}
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
        <div className="flex-1 overflow-y-auto py-4 space-y-4">
          {error && (
            <div className="p-3.5 bg-rose-50 border border-rose-100 rounded-xl text-rose-600 text-xs font-bold">
              {error}
            </div>
          )}

          {mode === "list" ? (
            <>
              {/* List View Actions */}
              <div className="flex justify-between items-center mb-1 shrink-0">
                <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                  Form Structure ({questions.length} questions)
                </span>
                <button
                  type="button"
                  onClick={handleAddClick}
                  className="px-3.5 py-1.5 text-white text-[11px] font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow flex items-center gap-1.5"
                  style={{ background: `linear-gradient(135deg, ${BRAND}, ${BRAND_DARK})` }}
                >
                  <FiPlus className="w-3.5 h-3.5" /> Add Question
                </button>
              </div>

              {/* Questions List */}
              {loading ? (
                <div className="flex flex-col items-center justify-center py-12">
                  <div className="w-8 h-8 border-4 border-slate-200 border-t-[#4B98C8] rounded-full animate-spin mb-3" />
                  <p className="text-[10px] text-slate-400 font-bold uppercase tracking-wider">Loading questions...</p>
                </div>
              ) : questions.length === 0 ? (
                <div className="text-center py-12 border border-dashed border-slate-200 rounded-xl">
                  <p className="text-slate-400 text-xs font-medium">No custom questions defined yet. Only standard registration fields (Name, Email, Phone, Batch, Dept) are requested.</p>
                </div>
              ) : (
                <div className="space-y-3.5 max-h-[50vh] overflow-y-auto pr-1">
                  {questions.map((q, index) => (
                    <div key={q.id || index} className="bg-slate-50/50 hover:bg-slate-50 border border-slate-200/80 rounded-2xl p-4 flex justify-between gap-4 transition-all duration-300">
                      <div className="space-y-1.5 min-w-0">
                        <div className="flex items-center gap-2 flex-wrap">
                          <span className={`px-2 py-0.5 rounded-full text-[9px] font-extrabold uppercase tracking-wide border flex items-center gap-1 bg-white text-slate-600 border-slate-200`}>
                            {getTypeIcon(q.questionType)}
                            {q.questionType?.replace("_", " ")}
                          </span>
                          <span className={`px-2 py-0.5 rounded-full text-[9px] font-extrabold uppercase tracking-wide border ${q.isRequired ? 'bg-rose-50/50 text-rose-600 border-rose-100' : 'bg-slate-50 text-slate-500 border-slate-200/50'}`}>
                            {q.isRequired ? "Required" : "Optional"}
                          </span>
                        </div>
                        <h4 className="text-xs font-extrabold text-slate-800 break-words leading-snug">
                          {q.questionText}
                        </h4>
                        {(q.questionType === "MULTIPLE_CHOICE" || q.questionType === "CHECKBOX") && q.options && q.options.length > 0 && (
                          <div className="flex flex-wrap gap-1.5 pt-1">
                            {q.options.map((opt, oIdx) => (
                              <span key={oIdx} className="bg-white border border-slate-200 text-slate-600 text-[9px] font-bold px-2 py-0.5 rounded-lg">
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
                          className="p-2 bg-white hover:bg-sky-50 text-slate-400 hover:text-[#4B98C8] border border-slate-200 rounded-xl transition-all active:scale-95"
                          title="Edit Question"
                        >
                          <FiEdit2 className="w-3.5 h-3.5" />
                        </button>
                        <button
                          type="button"
                          onClick={() => handleDeleteClick(q.id)}
                          className="p-2 bg-white hover:bg-rose-50 text-slate-400 hover:text-rose-600 border border-slate-200 rounded-xl transition-all active:scale-95"
                          title="Delete Question"
                        >
                          <FiTrash2 className="w-3.5 h-3.5" />
                        </button>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </>
          ) : (
            /* Add/Edit Form View */
            <form onSubmit={handleSubmit} noValidate className="space-y-4">
              <div>
                <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
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
                  className={`w-full px-3.5 py-2.5 bg-slate-50 border ${fieldErrors.questionText ? 'border-rose-400 focus:ring-rose-400/25 focus:border-rose-500' : 'border-slate-200 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8]'} text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 transition-all`}
                />
                {fieldErrors.questionText && (
                  <p className="mt-1.5 text-[9px] text-rose-500 font-extrabold uppercase tracking-wide">
                    {fieldErrors.questionText}
                  </p>
                )}
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <div>
                  <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider mb-1.5">
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
                    className={`w-full px-3.5 py-2.5 bg-slate-50 border ${fieldErrors.questionType ? 'border-rose-400 focus:ring-rose-400/25 focus:border-rose-500' : 'border-slate-200 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8]'} text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 transition-all cursor-pointer`}
                  >
                    <option value="TEXT">Short Text Answer</option>
                    <option value="MULTIPLE_CHOICE">Multiple Choice (Dropdown)</option>
                    <option value="CHECKBOX">Checkbox Selection</option>
                  </select>
                  {fieldErrors.questionType && (
                    <p className="mt-1.5 text-[9px] text-rose-500 font-extrabold uppercase tracking-wide">
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
                      className="w-4 h-4 rounded text-[#4B98C8] focus:ring-[#4B98C8]/30 border-slate-300"
                    />
                    <span className="text-xs font-extrabold text-slate-700 uppercase tracking-wide">
                      Mark as Required
                    </span>
                  </label>
                </div>
              </div>

              {/* Options Editor (Only if MULTIPLE_CHOICE or CHECKBOX) */}
              {(questionType === "MULTIPLE_CHOICE" || questionType === "CHECKBOX") && (
                <div className="space-y-2 border-t border-slate-100 pt-3.5">
                  <div className="flex justify-between items-center">
                    <label className="block text-[10px] font-bold text-slate-400 uppercase tracking-wider">
                      {questionType === "MULTIPLE_CHOICE" ? "Dropdown Options" : "Checkbox Options"}
                    </label>
                    <button
                      type="button"
                      onClick={handleAddOption}
                      className="text-[10px] font-black text-[#4B98C8] hover:text-[#205E85] flex items-center gap-1 active:scale-95 transition-all"
                    >
                      <FiPlus className="w-3 h-3" /> Add Option
                    </button>
                  </div>

                  {options.length === 0 ? (
                    <p className="text-[11px] text-slate-400 italic">
                      Please add at least one option for the {questionType === "MULTIPLE_CHOICE" ? "dropdown" : "checkbox"} selection.
                    </p>
                  ) : (
                    <div className="space-y-2 max-h-[30vh] overflow-y-auto pr-1">
                      {options.map((opt, optIdx) => {
                        const isOptionEmpty = fieldErrors.emptyOptionIndices?.includes(optIdx);
                        return (
                          <div key={optIdx} className="flex gap-2 items-center">
                            <input
                              type="text"
                              value={opt}
                              onChange={(e) => handleOptionChange(optIdx, e.target.value)}
                              placeholder={`Option ${optIdx + 1}`}
                              className={`flex-1 px-3 py-2 bg-slate-50 border ${
                                isOptionEmpty || fieldErrors.options 
                                  ? 'border-rose-400 focus:ring-rose-400/25 focus:border-rose-500' 
                                  : 'border-slate-200 focus:ring-[#4B98C8]/25 focus:border-[#4B98C8]'
                              } text-slate-800 text-xs font-semibold rounded-xl focus:outline-none focus:ring-2 transition-all`}
                            />
                            <button
                              type="button"
                              onClick={() => handleRemoveOption(optIdx)}
                              className="p-2 bg-rose-50 text-rose-600 border border-rose-100 hover:bg-rose-100 rounded-xl transition-all"
                              title="Remove Option"
                            >
                              <FiTrash2 className="w-3.5 h-3.5" />
                            </button>
                          </div>
                        );
                      })}
                    </div>
                  )}
                  {fieldErrors.options && (
                    <p className="mt-1.5 text-[9px] text-rose-500 font-extrabold uppercase tracking-wide">
                      {fieldErrors.options}
                    </p>
                  )}
                </div>
              )}

              {/* Form Footer Buttons */}
              <div className="flex justify-between items-center pt-4 border-t border-slate-100 mt-6 shrink-0">
                <button
                  type="button"
                  onClick={() => setMode("list")}
                  className="px-4 py-2 bg-slate-100 hover:bg-slate-200 text-slate-600 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all flex items-center gap-1.5"
                >
                  <FiArrowLeft className="w-4.5 h-4.5" /> Back to list
                </button>
                <button
                  type="submit"
                  disabled={saving}
                  className="px-5 py-2 text-white text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all shadow disabled:opacity-40"
                  style={{ background: `linear-gradient(135deg, ${BRAND}, ${BRAND_DARK})` }}
                >
                  {saving ? "Saving..." : "Save Question"}
                </button>
              </div>
            </form>
          )}
        </div>

        {/* Footer (Only shown in list view) */}
        {mode === "list" && (
          <div className="flex justify-end pt-4 border-t border-slate-100 mt-2 shrink-0">
            <button
              type="button"
              onClick={onClose}
              className="px-5 py-2.5 border border-slate-200 text-slate-500 hover:text-slate-800 text-xs font-bold uppercase tracking-wider rounded-xl active:scale-95 transition-all"
            >
              Close
            </button>
          </div>
        )}
      </div>
    </div>
  );
};

export default QuestionsManagementModal;
