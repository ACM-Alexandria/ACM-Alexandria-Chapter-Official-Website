import { 
  HiOutlineX, 
  HiOutlineExclamationCircle, 
  HiOutlineQuestionMarkCircle,
  HiOutlineCheck
} from "react-icons/hi";

const SimpleConfirmModal = ({ isVisible, onClose, type, entityName, onSubmit, submitting, error, success }) => {
  
  // Receives visual transition tracker from parent Orchestrator
  return (
    <div className={`relative w-full max-w-md bg-white rounded-[2.5rem] shadow-2xl border border-slate-100 overflow-hidden transition-all duration-500 transform ease-[cubic-bezier(0.34,1.56,0.64,1)] ${
      isVisible ? "translate-y-0 scale-100 opacity-100" : "translate-y-12 scale-95 opacity-0"
    }`}>
      
      {/* Close Button */}
      {!submitting && !success && (
        <button
          onClick={onClose}
          className="absolute right-6 top-6 w-10 h-10 rounded-full bg-slate-50 text-slate-400 flex items-center justify-center transition-all hover:bg-slate-100 hover:text-slate-700 active:scale-95 z-10"
        >
          <HiOutlineX className="w-5 h-5" />
        </button>
      )}

      <div className="p-8 md:p-10 flex flex-col items-center text-center">
        {success ? (
          /* Success View */
          <div className="space-y-6 py-4 animate-[fadeInUp_0.4s_ease] w-full">
            <div className="w-20 h-20 rounded-full bg-emerald-50 flex items-center justify-center text-emerald-500 mx-auto shadow-inner animate-[bounce_1s_ease_infinite_alternate]">
              <HiOutlineCheck className="w-10 h-10" strokeWidth={3} />
            </div>
            <div className="space-y-2">
              <h4 className="text-xl font-black text-slate-900">Registration Saved!</h4>
              <p className="text-slate-500 font-medium text-sm leading-relaxed">
                You have successfully registered for <span className="text-slate-800 font-bold">"{entityName}"</span>. 
                Please check your inbox for a <span className="text-[#4B98C8] font-bold">confirmation email</span> soon!
              </p>
            </div>
            <button
              onClick={onClose}
              className="w-full mt-4 px-6 py-3.5 bg-slate-900 text-white font-bold rounded-2xl text-sm transition-all hover:bg-slate-800 hover:shadow-lg active:scale-95"
            >
              Done
            </button>
          </div>
        ) : (
          /* Active Question Prompt View */
          <form onSubmit={onSubmit} className="w-full space-y-6 pt-4">
            <div className="w-16 h-16 rounded-2xl bg-blue-50 text-[#4B98C8] flex items-center justify-center mx-auto mb-2">
              <HiOutlineQuestionMarkCircle className="w-10 h-10" />
            </div>

            <div className="space-y-2">
              <h3 className="text-lg font-black text-slate-400 uppercase tracking-widest leading-none">
                {type === "event" ? "Event Signup" : "Club Application"}
              </h3>
              <h2 className="text-xl font-extrabold text-slate-800 leading-snug px-2">
                Are you sure you want to {type === "event" ? "register to" : "join"} "{entityName}"?
              </h2>
            </div>

            {error && (
              <div className="flex items-center gap-3 bg-rose-50 border border-rose-200 text-rose-800 rounded-2xl px-4 py-3.5 text-left text-xs font-bold animate-[fadeIn_0.3s_ease]">
                <HiOutlineExclamationCircle className="w-5 h-5 text-rose-500 shrink-0" />
                <span>{error}</span>
              </div>
            )}

            <div className="flex gap-3 w-full pt-2">
              <button
                type="button"
                onClick={onClose}
                disabled={submitting}
                className="flex-1 py-4 bg-slate-50 border border-slate-200 text-slate-600 rounded-2xl font-bold text-[15px] hover:bg-white hover:text-slate-900 transition-all disabled:opacity-50"
              >
                No, Cancel
              </button>
              <button
                type="submit"
                disabled={submitting}
                className="flex-1 py-4 bg-gradient-to-r from-[#4B98C8] to-[#205E85] text-white rounded-2xl font-bold text-[15px] shadow-lg shadow-blue-100 hover:shadow-xl hover:-translate-y-0.5 transition-all active:scale-95 disabled:opacity-70"
              >
                {submitting ? "Processing..." : "Yes, Confirm"}
              </button>
            </div>
          </form>
        )}
      </div>
    </div>
  );
};

export default SimpleConfirmModal;
