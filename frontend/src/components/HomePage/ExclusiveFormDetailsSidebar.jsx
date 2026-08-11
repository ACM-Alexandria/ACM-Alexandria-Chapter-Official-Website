import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";
import { fetchExclusiveFormById } from "../../services/homePageService";
import { HiOutlineX } from "react-icons/hi";
import { FiFileText } from "react-icons/fi";
import RegistrationModal from "../registration/RegistrationModal";
import { checkExclusiveFormRegistrationStatus } from "../../services/registrationService";

const ExclusiveFormDetailsSidebar = ({ formId, isOpen, onClose }) => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const [form, setForm] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [isVisible, setIsVisible] = useState(false);
  const [isRegistrationOpen, setIsRegistrationOpen] = useState(false);

  // Registration presence trackers
  const [isRegistered, setIsRegistered] = useState(false);
  const [checkingStatus, setCheckingStatus] = useState(false);

  // Monitor actual user registration status dynamically
  useEffect(() => {
    if (!isOpen || !formId || !isAuthenticated || !user?.id) {
      setIsRegistered(false);
      return;
    }

    const checkStatus = async () => {
      setCheckingStatus(true);
      try {
        const registered = await checkExclusiveFormRegistrationStatus(formId, user.id);
        setIsRegistered(registered);
      } catch (err) {
        console.error("Error checking exclusive form registration:", err);
      } finally {
        setCheckingStatus(false);
      }
    };

    if (!isRegistrationOpen) {
      checkStatus();
    }
  }, [isOpen, formId, isAuthenticated, user?.id, isRegistrationOpen]);

  useEffect(() => {
    if (!isOpen || !formId) return;

    const loadForm = async () => {
      setLoading(true);
      setError(null);
      try {
        const data = await fetchExclusiveFormById(formId);
        setForm(data);
      } catch (err) {
        console.error("Failed to load exclusive form details:", err);
        setError("Unable to load form details right now.");
      } finally {
        setLoading(false);
      }
    };

    loadForm();
  }, [formId, isOpen]);

  useEffect(() => {
    if (!isOpen) {
      setIsVisible(false);
      return;
    }

    const frame = requestAnimationFrame(() => {
      setIsVisible(true);
    });

    const originalOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    const handleKeyDown = (e) => {
      if (e.key === "Escape") onClose();
    };

    window.addEventListener("keydown", handleKeyDown);

    return () => {
      document.body.style.overflow = originalOverflow;
      cancelAnimationFrame(frame);
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [isOpen, onClose]);

  if (!formId) return null;

  return (
    <div
      className={`fixed inset-0 z-50 overflow-hidden ${isVisible ? "pointer-events-auto" : "pointer-events-none"}`}
      aria-hidden={!isVisible}
    >
      {/* Backdrop */}
      <div
        className={`absolute inset-0 bg-slate-900/40 backdrop-blur-md transition-opacity duration-300 ${isVisible ? "opacity-100" : "opacity-0"}`}
        onClick={onClose}
      />

      <aside
        className={`absolute top-0 right-0 h-full w-full md:w-[600px] lg:w-[700px] overflow-hidden bg-white shadow-2xl transition-transform duration-500 ease-[cubic-bezier(0.22,1,0.36,1)] ${isVisible ? "translate-x-0" : "translate-x-full"}`}
      >
        <div className="h-full flex flex-col">
          {/* Header Image Section (Generic for forms) */}
          <div className="relative h-64 shrink-0 overflow-hidden bg-slate-100">
            {/* Close Button */}
            <button
              type="button"
              onClick={onClose}
              className="absolute right-6 top-6 z-30 w-12 h-12 flex items-center justify-center rounded-2xl bg-slate-900/80 backdrop-blur-md text-white border border-slate-700/50 shadow-xl transition-all duration-300 hover:bg-slate-900 hover:scale-105 active:scale-95"
              aria-label="Close"
            >
              <HiOutlineX className="w-6 h-6" />
            </button>

            <div className="w-full h-full flex items-center justify-center bg-gradient-to-br from-[#4B98C8] to-[#205E85]">
              {form?.imageUrl ? (
                <img 
                  src={form.imageUrl} 
                  alt={form.title} 
                  className="w-full h-full object-cover"
                />
              ) : (
                <div className="w-full h-full flex items-center justify-center opacity-20">
                  <FiFileText className="w-32 h-32 text-white" />
                </div>
              )}
            </div>

            {/* Gradient Overlay */}
            <div className="absolute inset-0 bg-gradient-to-t from-slate-950 via-slate-950/20 to-transparent opacity-80" />

            <div className="absolute bottom-10 left-10 right-10 z-20">
              {!loading && form && (
                <div style={{ animation: "slideLeft 0.8s cubic-bezier(0.22,1,0.36,1) both" }}>
                  <div className="inline-flex rounded-full bg-[#4B98C8] px-4 py-1.5 text-[10px] font-black uppercase tracking-[0.2em] text-white mb-4">
                    Special Opportunity
                  </div>
                  <h2 className="text-3xl md:text-4xl font-extrabold text-white tracking-tight leading-tight">
                    {form.title}
                  </h2>
                </div>
              )}
            </div>
          </div>

          {/* Content Area */}
          <div className="flex-1 overflow-y-auto px-10 py-12 scrollbar-hide">
            {loading ? (
              <div className="space-y-8 animate-pulse">
                <div className="h-64 bg-slate-50 rounded-[2.5rem]" />
              </div>
            ) : error ? (
              <div className="flex flex-col items-center justify-center h-full text-center space-y-4">
                <div className="w-20 h-20 bg-red-50 rounded-full flex items-center justify-center text-red-500 mb-2">
                  <HiOutlineX className="w-10 h-10" />
                </div>
                <p className="text-xl font-extrabold text-slate-900">{error}</p>
                <button onClick={onClose} className="px-6 py-2 bg-slate-900 text-white rounded-xl font-bold">Close</button>
              </div>
            ) : form && (
              <div className="space-y-10" style={{ animation: "fadeIn 1s ease 0.3s both" }}>
                
                {/* About Section */}
                <div className="space-y-4">
                  <h4 className="text-sm font-black text-slate-900 uppercase tracking-[0.15em] flex items-center gap-3">
                    Description
                    <div className="h-px flex-1 bg-slate-100" />
                  </h4>
                  <p className="text-slate-600 text-lg leading-relaxed font-medium whitespace-pre-line">
                    {form.description || "No description provided for this opportunity."}
                  </p>
                </div>

                {/* Action Section */}
                <div className="pt-6">
                  {!form.isActive ? (
                    <button
                      disabled
                      className="group relative flex w-full items-center justify-center gap-3 overflow-hidden rounded-[2rem] bg-slate-200 px-10 py-6 text-lg font-black text-slate-400 cursor-not-allowed border border-slate-200"
                    >
                      <span className="relative z-10 uppercase tracking-widest text-sm">Form Closed</span>
                    </button>
                  ) : checkingStatus ? (
                    <button
                      disabled
                      className="flex w-full items-center justify-center gap-3 rounded-[2rem] bg-slate-50 px-10 py-6 text-slate-400 border border-slate-100 cursor-not-allowed"
                    >
                      <div className="w-5 h-5 border-3 border-slate-200 border-t-[#4B98C8] rounded-full animate-spin" />
                      <span className="uppercase tracking-widest text-sm font-extrabold animate-pulse">Verifying Status...</span>
                    </button>
                  ) : isRegistered ? (
                    <button
                      disabled
                      className="group relative flex w-full items-center justify-center gap-3 overflow-hidden rounded-[2rem] bg-emerald-500 px-10 py-6 text-lg font-black text-white border border-emerald-600/20 shadow-2xl shadow-emerald-100 cursor-default"
                    >
                      <span className="relative z-10 uppercase tracking-widest text-sm flex items-center gap-2">
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 animate-[bounce_1s_ease_infinite_alternate]" viewBox="0 0 20 20" fill="currentColor">
                          <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                        </svg>
                        Already Registered
                      </span>
                    </button>
                  ) : (
                    <button
                      onClick={() => {
                        if (!isAuthenticated) {
                          const path = window.location.pathname + window.location.search;
                          const delimiter = path.includes("?") ? "&" : "?";
                          const from = `${path}${delimiter}openExclusiveFormId=${formId}`;
                          navigate("/login", { state: { from } });
                        } else {
                          setIsRegistrationOpen(true);
                        }
                      }}
                      className="group relative flex w-full items-center justify-center gap-3 overflow-hidden rounded-[2rem] bg-gradient-to-r from-[#4B98C8] to-[#205E85] px-10 py-6 text-lg font-black text-white shadow-2xl shadow-blue-200 transition-all duration-300 hover:-translate-y-1 hover:shadow-blue-300 active:scale-[0.98]"
                    >
                      <span className="relative z-10 uppercase tracking-widest text-sm">Apply Now</span>
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5 relative z-10 group-hover:translate-x-1 transition-transform" viewBox="0 0 20 20" fill="currentColor">
                        <path fillRule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clipRule="evenodd" />
                      </svg>
                      <div className="absolute inset-0 bg-gradient-to-r from-transparent via-white/10 to-transparent -translate-x-full group-hover:animate-[shimmer_2s_infinite]" />
                    </button>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>
      </aside>

      <RegistrationModal
        isOpen={isRegistrationOpen}
        onClose={() => setIsRegistrationOpen(false)}
        entityId={formId}
        type="exclusive-form"
        entityName={form?.title}
      />
    </div>
  );
};

export default ExclusiveFormDetailsSidebar;
