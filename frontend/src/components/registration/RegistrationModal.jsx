import { useEffect, useState } from "react";
import { useAuth } from "../../contexts/AuthContext";
import { 
  fetchEventQuestions, 
  fetchClubQuestions, 
  fetchCommitteeQuestions,
  fetchProgramQuestions,
  registerForEvent, 
  registerForClub,
  registerForCommittee,
  registerForProgram,
  fetchExclusiveFormQuestions,
  registerForExclusiveForm,
} from "../../services/registrationService";
import { 
  fetchUserProfile, 
  updateUserProfile 
} from "../../services/userService";

// Sub-views separated into clean individual files for easy developer editing (User Request)
import ProfileGatewayModal from "./ProfileGatewayModal";
import SimpleConfirmModal from "./SimpleConfirmModal";
import DynamicQuestionnaireModal from "./DynamicQuestionnaireModal";

const RegistrationModal = ({ isOpen, onClose, entityId, type, entityName }) => {
  const { user } = useAuth();
  
  // Global overlay active & entry tracking (Fixes backdrop blur lag)
  const [isVisible, setIsVisible] = useState(false);

  // Controller Loading / State flags
  const [loading, setLoading] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  // Orchestrator Flow states
  const [needsProfileUpdate, setNeedsProfileUpdate] = useState(false);
  const [questions, setQuestions] = useState([]);
  const [answers, setAnswers] = useState({});

  // Inline Profile Verification states
  const [profileForm, setProfileForm] = useState({
    name: "",
    phoneNumber: "",
    isAlexEngStudent: true,
    batch: "",
    department: ""
  });
  const [profileSubmitting, setProfileSubmitting] = useState(false);
  const [profileError, setProfileError] = useState(null);

  // Manage opening effects, global scrolling freeze & visibility entries
  useEffect(() => {
    if (!isOpen) {
      setIsVisible(false);
      return;
    }

    setError(null);
    setSuccess(false);
    setNeedsProfileUpdate(false);
    setProfileError(null);
    setAnswers({});
    setProfileForm({
      name: "",
      phoneNumber: "",
      isAlexEngStudent: true,
      batch: "",
      department: ""
    });

    const frame = requestAnimationFrame(() => {
      setIsVisible(true);
    });
    
    // Block external layout scrolling while modal dashboard is visible
    const originalStyle = window.getComputedStyle(document.body).overflow;
    document.body.style.overflow = "hidden";

    return () => {
      cancelAnimationFrame(frame);
      document.body.style.overflow = originalStyle;
      setIsVisible(false);
    };
  }, [isOpen]);

  // Orchestrator Data Loading Sequence
  useEffect(() => {
    if (!isOpen || !entityId || !type || !user?.id) return;

    const initializeFlow = async () => {
      setLoading(true);
      setError(null);
      try {
        // 1. Fetch and audit detailed user profile data
        const fullProfile = await fetchUserProfile(user.id);
        
        const currentName = fullProfile.name || "";
        const currentPhone = fullProfile.phone_number || "";
        const serverIsStudent = fullProfile.is_alex_eng_student;
        const isStudent = serverIsStudent ?? true; // default to true if null in DB
        const currentBatch = fullProfile.batch || "";
        const currentDept = fullProfile.department || "";

        const isMissingBasic = !currentName.trim() || !currentPhone.trim();
        const isMissingAcademic = serverIsStudent === true && (!currentBatch.trim() || !currentDept.trim());

        setProfileForm({
          name: currentName,
          phoneNumber: currentPhone,
          isAlexEngStudent: isStudent,
          batch: currentBatch,
          department: currentDept
        });

        if (isMissingBasic || isMissingAcademic || serverIsStudent === null) {
          setNeedsProfileUpdate(true);
        }

        // 2. Pre-load custom database questions for dynamic rendering
        const data = type === "event" 
          ? await fetchEventQuestions(entityId)
          : type === "club"
            ? await fetchClubQuestions(entityId)
            : type === "program"
              ? await fetchProgramQuestions(entityId)
              : type === "exclusive-form"
                ? await fetchExclusiveFormQuestions(entityId)
                : await fetchCommitteeQuestions(entityId);
        
        setQuestions(data || []);
        
        const initialAnswers = {};
        data.forEach(q => {
          initialAnswers[q.id] = "";
        });
        setAnswers(initialAnswers);

      } catch (err) {
        console.error("[Registration Orchestrator] Init failure:", err);
        setError("Initialization failed. Please refresh and try again.");
      } finally {
        setLoading(false);
      }
    };

    initializeFlow();
  }, [isOpen, entityId, type, user?.id]);

  // ================= HANDLERS: Step 1 (Profile Verification) =================
  const handleProfileInputChange = (e) => {
    const { name, value } = e.target;
    if (name === "phoneNumber") {
      const digits = value.replace(/\D/g, "").slice(0, 11);
      setProfileForm(prev => ({ ...prev, phoneNumber: digits }));
      return;
    }
    setProfileForm(prev => ({ ...prev, [name]: value }));
  };

  const handleToggleAlexEng = (val) => {
    setProfileForm(prev => ({
      ...prev,
      isAlexEngStudent: val,
      batch: val === true ? prev.batch : "",
      department: val === true ? prev.department : ""
    }));
  };

  const handleProfileSubmit = async (e) => {
    e.preventDefault();
    setProfileError(null);

    const phoneRegex = /^01[0-9]{9}$/;
    if (!phoneRegex.test(profileForm.phoneNumber)) {
      setProfileError("Phone number must be 11 digits starting with '01'.");
      return;
    }

    if (profileForm.isAlexEngStudent === true && (!profileForm.batch || !profileForm.department)) {
      setProfileError("Academic batch and department are mandatory.");
      return;
    }

    setProfileSubmitting(true);
    try {
      await updateUserProfile(user.id, {
        name: profileForm.name.trim(),
        phone_number: profileForm.phoneNumber,
        is_alex_eng_student: profileForm.isAlexEngStudent,
        batch: profileForm.isAlexEngStudent === true ? profileForm.batch : null,
        department: profileForm.isAlexEngStudent === true ? profileForm.department : null
      });
      setNeedsProfileUpdate(false);
    } catch (err) {
      console.error("[Profile Gateway] Update failed:", err);
      let errorMessage = "Save failed. Recheck inputs.";
      if (typeof err === "string") {
        errorMessage = err;
      } else if (err && typeof err === "object") {
        errorMessage = err.message || err.error || err.errorMessage || errorMessage;
      }
      setProfileError(errorMessage);
    } finally {
      setProfileSubmitting(false);
    }
  };

  // ================= HANDLERS: Step 2 (Registration Routing) =================
  const handleInputChange = (questionId, value) => {
    setAnswers((prev) => ({
      ...prev,
      [questionId]: value
    }));
  };

  const handleRegistrationSubmit = async (e) => {
    if (e) e.preventDefault();
    if (!user?.id) {
      setError("Invalid authentication session.");
      return;
    }

    if (questions.length > 0) {
      for (const q of questions) {
        if (q.is_required && !answers[q.id]?.trim()) {
          setError(`Question "${q.question_text}" is required.`);
          return;
        }
      }
    }

    setSubmitting(true);
    setError(null);

    try {
      if (type === "event") {
        await registerForEvent(entityId, user.id, answers);
      } else if (type === "club") {
        await registerForClub(entityId, user.id, answers);
      } else if (type === "program") {
        await registerForProgram(entityId, user.id, answers);
      } else if (type === "exclusive-form") {
        await registerForExclusiveForm(entityId, user.id, answers);
      } else {
        await registerForCommittee(entityId, user.id, answers);
      }
      setSuccess(true);
    } catch (err) {
      console.error("[Registration Submission] API failure:", err);
      let errorMessage = "An unexpected server error occurred. Please try again.";
      if (typeof err === "string") {
        errorMessage = err;
      } else if (err && typeof err === "object") {
        errorMessage = err.message || err.error || err.errorMessage || errorMessage;
      }
      setError(errorMessage);
    } finally {
      setSubmitting(false);
    }
  };

  // Do not render anything if state is closed
  if (!isOpen) return null;

  // ================= DUAL ROUTING UI RENDER =================
  return (
    <div className={`fixed inset-0 z-[100] flex items-center justify-center px-4 sm:px-6 transition-all duration-300 ${
      isVisible ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none"
    }`}>
      
      {/* Singular, Continuous Global Backdrop: Stays stable across state changes (NO MORE LAG/BLINK!) */}
      <div 
        className="absolute inset-0 bg-slate-900/60 transition-opacity" 
        onClick={submitting || profileSubmitting || success ? undefined : onClose}
      />

      {loading ? (
        /* Standard loading skeleton card (inherits parent visibility transitions) */
        <div className={`relative w-full max-w-md bg-white rounded-[2.5rem] p-10 flex flex-col items-center space-y-4 shadow-2xl border border-slate-100 transform transition-all duration-500 ${
          isVisible ? "translate-y-0 scale-100" : "translate-y-12 scale-95 opacity-0"
        }`}>
          <div className="w-12 h-12 border-4 border-blue-100 border-t-[#4B98C8] rounded-full animate-spin" />
          <span className="text-sm font-bold text-slate-600 tracking-wide animate-pulse">Initializing Session...</span>
        </div>
      ) : needsProfileUpdate ? (
        <ProfileGatewayModal
          isVisible={isVisible}
          onClose={onClose}
          entityName={entityName}
          profileForm={profileForm}
          onInputChange={handleProfileInputChange}
          onToggle={handleToggleAlexEng}
          onSubmit={handleProfileSubmit}
          submitting={profileSubmitting}
          error={profileError}
        />
      ) : questions.length === 0 ? (
        <SimpleConfirmModal
          isVisible={isVisible}
          onClose={onClose}
          type={type}
          entityName={entityName}
          onSubmit={handleRegistrationSubmit}
          submitting={submitting}
          error={error}
          success={success}
        />
      ) : (
        <DynamicQuestionnaireModal
          isVisible={isVisible}
          onClose={onClose}
          type={type}
          entityName={entityName}
          questions={questions}
          answers={answers}
          onInputChange={handleInputChange}
          onSubmit={handleRegistrationSubmit}
          submitting={submitting}
          error={error}
          success={success}
        />
      )}
    </div>
  );
};

export default RegistrationModal;
