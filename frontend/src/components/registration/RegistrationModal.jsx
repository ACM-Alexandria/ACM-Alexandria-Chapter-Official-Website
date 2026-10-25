import { useEffect, useState } from "react";
import { useAuth } from "../../contexts/useAuth";
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
import { uploadImage } from "../../services/adminService";

// Sub-views separated into clean individual files for easy developer editing (User Request)
import ProfileGatewayModal from "./ProfileGatewayModal";
import SimpleConfirmModal from "./SimpleConfirmModal";
import DynamicQuestionnaireModal from "./DynamicQuestionnaireModal";
import Dialog from "../ui/Dialog";
import { muted } from "../ui/uiStyles";

const RegistrationModal = ({ isOpen, onClose, entityId, type, entityName }) => {
  const { user } = useAuth();
  
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

  // Start every opening from a clean form. The dialog itself locks page scrolling.
  useEffect(() => {
    if (!isOpen) return;

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
        if (q.is_required) {
          const ans = answers[q.id];
          let isEmpty;
          if (ans instanceof File) {
            isEmpty = false; // a file is present — not empty
          } else if (Array.isArray(ans)) {
            isEmpty = ans.length === 0;
          } else {
            isEmpty = !ans?.trim();
          }
          if (isEmpty) {
            setError(`Question "${q.question_text}" is required.`);
            return;
          }
        }
        // Block submission if image is selected but too large
        if (q.question_type === "IMAGE") {
          const ans = answers[q.id];
          if (ans instanceof File && ans.size > 2 * 1024 * 1024) {
            setError(`The image for "${q.question_text}" exceeds the 2 MB limit. Please choose a smaller file.`);
            return;
          }
        }
      }
    }

    setSubmitting(true);
    setError(null);

    // Upload FILE answers (IMAGE questions) via the image service, replace with returned URL
    // Also serialize CHECKBOX arrays → comma-joined strings for the backend (Map<Long, String>)
    const serializedAnswers = { ...answers };
    for (const [id, val] of Object.entries(serializedAnswers)) {
      if (val instanceof File) {
        try {
          const result = await uploadImage(val);
          // API returns { url: "..." } so response.data is the object
          serializedAnswers[id] = typeof result === "string" ? result : result?.url || result?.imageUrl || String(result);
        } catch {
          setSubmitting(false);
          setError("Failed to upload image. Please try again.");
          return;
        }
      } else if (Array.isArray(val)) {
        serializedAnswers[id] = val.join(", ");
      }
    }

    try {
      if (type === "event") {
        await registerForEvent(entityId, user.id, serializedAnswers);
      } else if (type === "club") {
        await registerForClub(entityId, user.id, serializedAnswers);
      } else if (type === "program") {
        await registerForProgram(entityId, user.id, serializedAnswers);
      } else if (type === "exclusive-form") {
        await registerForExclusiveForm(entityId, user.id, serializedAnswers);
      } else {
        await registerForCommittee(entityId, user.id, serializedAnswers);
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

  if (!isOpen) return null;
  const busy = submitting || profileSubmitting || success;
  const compact = loading || (!needsProfileUpdate && questions.length === 0);

  // One dialog for the whole flow: profile check first if needed, then a plain confirmation or the questions.
  return (
    <Dialog open={isOpen} onClose={onClose} size={compact ? "sm" : "lg"} dismissable={!busy} labelledBy="registration-title" z="z-[80]">
      {loading ? (
        <div className="flex flex-col items-center gap-4 px-8 py-14 text-center">
          <span className="h-10 w-10 animate-spin rounded-full border-4 border-[#E2F0FA] border-t-[#4B98C8] dark:border-[#16364C]" aria-hidden="true" />
          <p id="registration-title" className={`text-sm font-semibold ${muted}`}>Getting your form ready…</p>
        </div>
      ) : needsProfileUpdate ? (
        <ProfileGatewayModal
          onClose={onClose}
          type={type}
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
    </Dialog>
  );
};

export default RegistrationModal;
