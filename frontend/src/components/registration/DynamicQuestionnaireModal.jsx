import { 
  HiOutlineX, 
  HiOutlineCheckCircle, 
  HiOutlineExclamationCircle, 
  HiOutlineArrowRight,
  HiOutlineClipboardList,
  HiOutlineSparkles,
  HiOutlinePhotograph
} from "react-icons/hi";

const DynamicQuestionnaireModal = ({ 
  isVisible, 
  onClose, 
  type, 
  entityName, 
  questions, 
  answers, 
  onInputChange, 
  onSubmit, 
  submitting, 
  error, 
  success 
}) => {
  
  // Card inner view inheriting orchestration visibility flags
  return (
    <div className={`relative w-full max-w-2xl bg-white rounded-[2.5rem] shadow-2xl border border-slate-100 overflow-hidden transition-all duration-500 transform ease-[cubic-bezier(0.34,1.56,0.64,1)] flex flex-col max-h-[85vh] ${
      isVisible ? "translate-y-0 scale-100 opacity-100" : "translate-y-16 scale-95 opacity-0"
    }`}>
      
      {/* Header Panel */}
      <div className="bg-slate-50/50 px-8 py-6 border-b border-slate-100 flex items-center justify-between shrink-0">
        <div className="flex items-center gap-3.5">
          <div className="w-12 h-12 rounded-2xl bg-gradient-to-br from-[#4B98C8] to-[#205E85] shadow-md flex items-center justify-center text-white shrink-0">
            {type === "event" ? <HiOutlineSparkles className="w-6 h-6" /> : <HiOutlineClipboardList className="w-6 h-6" />}
          </div>
          <div className="overflow-hidden">
            <span className="text-[10px] font-black text-[#4B98C8] uppercase tracking-[0.15em] block">
              {type === "event" ? "Event Questionnaire" : "Club Questionnaire"}
            </span>
            <h3 className="text-lg font-black text-slate-900 truncate leading-tight mt-0.5">
              {entityName}
            </h3>
          </div>
        </div>

        {!submitting && !success && (
          <button onClick={onClose} className="w-10 h-10 rounded-xl bg-white border border-slate-200 text-slate-500 flex items-center justify-center transition-all hover:bg-slate-50 hover:text-slate-900 active:scale-95">
            <HiOutlineX className="w-5 h-5" />
          </button>
        )}
      </div>

      {/* Main Scroll Area */}
      <div className="flex-1 overflow-y-auto p-8 md:p-10 scrollbar-hide">
        {success ? (
          /* Completed Success Panel */
          <div className="flex flex-col items-center text-center py-6 space-y-5 animate-[fadeInUp_0.5s_ease]">
            <div className="w-24 h-24 rounded-full bg-emerald-50 flex items-center justify-center text-emerald-500 relative shadow-inner animate-[bounce_1s_ease_infinite_alternate]">
              <HiOutlineCheckCircle className="w-16 h-16" />
            </div>
            <div className="space-y-2">
              <h4 className="text-2xl font-black text-slate-900">Successfully Registered!</h4>
              <p className="text-slate-500 font-medium max-w-md text-sm leading-relaxed">
                Your answers for <strong className="text-slate-800">{entityName}</strong> have been submitted successfully! 
                Please keep an eye on your inbox for a <strong className="text-[#4B98C8]">confirmation email</strong> shortly.
              </p>
            </div>
            <button onClick={onClose} className="mt-6 px-10 py-4 rounded-2xl bg-gradient-to-r from-slate-800 to-slate-900 text-white text-sm font-black shadow-lg hover:-translate-y-0.5 active:scale-95 transition-all uppercase tracking-wider">
              Close Panel
            </button>
          </div>
        ) : (
          /* Reactive Input Fields Loop */
          <form id="registration-questionnaire-form" onSubmit={onSubmit} className="space-y-7">
            {error && (
              <div className="flex items-center gap-3 bg-rose-50 border border-rose-200 text-rose-800 rounded-2xl px-5 py-4 animate-[fadeIn_0.3s_ease]">
                <HiOutlineExclamationCircle className="w-6 h-6 text-rose-500 shrink-0" />
                <p className="font-bold text-sm leading-snug">{error}</p>
              </div>
            )}

            <div className="space-y-7">
              {questions.map((question) => {
                const isText = question.question_type === "TEXT";
                const isCheckbox = question.question_type === "CHECKBOX";
                const isImage = question.question_type === "IMAGE";
                return (
                  <div key={question.id} className="space-y-2.5 group">
                    <label className="text-[13px] font-bold text-slate-800 flex items-center gap-1.5 ml-1 leading-tight">
                      {question.question_text}
                      {question.is_required && (
                        <span className="text-rose-500 text-base font-black leading-none">*</span>
                      )}
                    </label>
                    <div className="relative">
                      {isText ? (
                        <textarea
                          rows={3}
                          value={answers[question.id] || ""}
                          onChange={(e) => onInputChange(question.id, e.target.value)}
                          required={question.is_required}
                          disabled={submitting}
                          placeholder="Type your answer here..."
                          className="w-full px-5 py-4 bg-slate-50 border border-slate-100 rounded-2xl outline-none text-slate-800 font-semibold text-[15px] transition-all focus:border-[#4B98C8]/30 focus:bg-white focus:ring-4 focus:ring-blue-50 resize-none disabled:opacity-60"
                        />
                      ) : isCheckbox ? (
                        <div className="w-full px-5 py-4 bg-slate-50 border border-slate-100 rounded-2xl space-y-3 disabled:opacity-60">
                          {question.options && question.options.map((opt, idx) => {
                            const selectedValues = Array.isArray(answers[question.id]) ? answers[question.id] : [];
                            const isChecked = selectedValues.includes(opt);
                            const handleCheckboxChange = () => {
                              const updated = isChecked
                                ? selectedValues.filter((v) => v !== opt)
                                : [...selectedValues, opt];
                              onInputChange(question.id, updated);
                            };
                            return (
                              <label
                                key={idx}
                                className={`flex items-center gap-3 cursor-pointer group/opt select-none ${submitting ? "opacity-60 cursor-not-allowed" : ""}`}
                              >
                                <div
                                  className={`w-5 h-5 rounded-md border-2 flex items-center justify-center shrink-0 transition-all ${
                                    isChecked
                                      ? "bg-[#4B98C8] border-[#4B98C8]"
                                      : "bg-white border-slate-300 group-hover/opt:border-[#4B98C8]/60"
                                  }`}
                                >
                                  {isChecked && (
                                    <svg className="w-3 h-3 text-white" viewBox="0 0 12 12" fill="none" xmlns="http://www.w3.org/2000/svg">
                                      <path d="M2 6L5 9L10 3" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
                                    </svg>
                                  )}
                                </div>
                                <input
                                  type="checkbox"
                                  className="sr-only"
                                  checked={isChecked}
                                  onChange={handleCheckboxChange}
                                  disabled={submitting}
                                />
                                <span className="text-slate-700 font-semibold text-[15px]">{opt}</span>
                              </label>
                            );
                          })}
                        </div>
                      ) : isImage ? (() => {
                          const MAX_MB = 2;
                          const file = answers[question.id] instanceof File ? answers[question.id] : null;
                          const previewUrl = file ? URL.createObjectURL(file) : null;
                          const isTooLarge = file && file.size > MAX_MB * 1024 * 1024;
                          return (
                            <div className="space-y-3">
                              {file ? (
                                <div className={`relative rounded-2xl overflow-hidden border-2 ${isTooLarge ? "border-rose-400" : "border-[#4B98C8]/40"} bg-slate-50`}>
                                  <img
                                    src={previewUrl}
                                    alt="Preview"
                                    className="w-full max-h-56 object-contain"
                                  />
                                  <button
                                    type="button"
                                    disabled={submitting}
                                    onClick={() => onInputChange(question.id, null)}
                                    className="absolute top-2 right-2 w-8 h-8 rounded-full bg-slate-900/60 text-white flex items-center justify-center hover:bg-rose-600/80 transition-all"
                                    title="Remove image"
                                  >
                                    <HiOutlineX className="w-4 h-4" />
                                  </button>
                                  <div className="px-4 py-2 border-t border-slate-100 bg-white/80 flex items-center justify-between">
                                    <span className="text-[12px] font-semibold text-slate-600 truncate max-w-[70%]">{file.name}</span>
                                    <span className={`text-[11px] font-black ${isTooLarge ? "text-rose-500" : "text-slate-400"}`}>
                                      {(file.size / (1024 * 1024)).toFixed(2)} MB
                                    </span>
                                  </div>
                                </div>
                              ) : (
                                <label
                                  className={`flex flex-col items-center justify-center gap-3 w-full py-10 border-2 border-dashed rounded-2xl cursor-pointer transition-all ${submitting ? "opacity-60 cursor-not-allowed" : "border-slate-200 bg-slate-50 hover:border-[#4B98C8]/50 hover:bg-blue-50/30"}`}
                                >
                                  <div className="w-14 h-14 rounded-2xl bg-gradient-to-br from-[#4B98C8]/10 to-[#205E85]/10 flex items-center justify-center text-[#4B98C8]">
                                    <HiOutlinePhotograph className="w-7 h-7" />
                                  </div>
                                  <div className="text-center">
                                    <p className="text-sm font-bold text-slate-700">Click to upload image</p>
                                    <p className="text-[11px] text-slate-400 font-medium mt-0.5">JPG, PNG, WEBP, GIF · Max 2 MB</p>
                                  </div>
                                  <input
                                    type="file"
                                    accept="image/*"
                                    className="sr-only"
                                    disabled={submitting}
                                    onChange={(e) => {
                                      const picked = e.target.files?.[0] || null;
                                      onInputChange(question.id, picked);
                                      e.target.value = "";
                                    }}
                                  />
                                </label>
                              )}
                              {isTooLarge && (
                                <p className="text-[11px] text-rose-500 font-extrabold uppercase tracking-wide ml-1">
                                  ✕ File exceeds 2 MB limit. Please choose a smaller image.
                                </p>
                              )}
                            </div>
                          );
                        })() : (
                        <>
                          <select
                            value={answers[question.id] || ""}
                            onChange={(e) => onInputChange(question.id, e.target.value)}
                            required={question.is_required}
                            disabled={submitting}
                            className="w-full h-14 px-5 bg-slate-50 border border-slate-100 rounded-2xl outline-none text-slate-800 font-semibold text-[15px] transition-all focus:border-[#4B98C8]/30 focus:bg-white focus:ring-4 focus:ring-blue-50 appearance-none cursor-pointer disabled:opacity-60"
                          >
                            <option value="">-- Select an Option --</option>
                            {question.options && question.options.map((opt, idx) => (
                              <option key={idx} value={opt}>{opt}</option>
                            ))}
                          </select>
                          <div className="absolute inset-y-0 right-5 flex items-center pointer-events-none text-slate-400">
                            <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 20 20" fill="currentColor">
                              <path fillRule="evenodd" d="M5.293 7.293a1 1 0 011.414 0L10 10.586l3.293-3.293a1 1 0 111.414 1.414l-4 4a1 1 0 01-1.414 0l-4-4a1 1 0 010-1.414z" clipRule="evenodd" />
                            </svg>
                          </div>
                        </>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </form>
        )}
      </div>

      {/* Action Bar */}
      {!success && (
        <div className="bg-slate-50/30 px-8 py-6 border-t border-slate-100 flex flex-col sm:flex-row sm:items-center justify-end gap-3 shrink-0">
          <button type="button" onClick={onClose} disabled={submitting} className="px-7 py-3.5 rounded-xl border border-slate-200 text-slate-600 text-sm font-bold transition-all hover:bg-white hover:text-slate-800 hover:shadow-sm disabled:opacity-50">
            Discard
          </button>
          <button
            type="submit"
            form="registration-questionnaire-form"
            disabled={submitting}
            className="group px-8 py-3.5 flex items-center justify-center gap-2 bg-gradient-to-r from-[#4B98C8] to-[#205E85] text-white text-sm font-bold rounded-xl shadow-md shadow-blue-200 hover:shadow-lg hover:-translate-y-0.5 transition-all active:scale-95 disabled:opacity-70 disabled:cursor-not-allowed"
          >
            {submitting ? (
              <>
                <div className="w-4 h-4 border-2 border-white/30 border-t-white rounded-full animate-spin" />
                <span>Submitting App...</span>
              </>
            ) : (
              <>
                <span>Submit Application</span>
                <HiOutlineArrowRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
              </>
            )}
          </button>
        </div>
      )}
    </div>
  );
};

export default DynamicQuestionnaireModal;
