import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import InputField from "./InputField";
import PasswordInput from "./PasswordInput";
import { useAuth } from "../../contexts/useAuth";
import { getEnv } from "../../utils/env";
import { resendConfirmationEmail } from "../../services/authService";
import {
  validateEmail,
  validatePassword,
  validatePasswordMatch,
} from "../../utils/validation";
import {
  ErrorCircleIcon,
  SuccessCircleIcon,
  SpinnerIcon,
  EnvelopeIcon,
  LockIcon,
} from "../icons/index";

const RegisterForm = () => {
  const navigate = useNavigate();
  const { register, loginWithGoogle } = useAuth();

  // Form state
  const [formData, setFormData] = useState({
    email: "",
    password: "",
    password_confirmation: "",
  });

  // Error state for each field
  const [errors, setErrors] = useState({
    email: "",
    password: "",
    password_confirmation: "",
    general: "",
  });

  // Loading and success state
  const [isLoading, setIsLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [isSuccess, setIsSuccess] = useState(false);
  const [registeredEmail, setRegisteredEmail] = useState("");

  // Resend confirmation email
  const [resendCountdown, setResendCountdown] = useState(60);
  const [isResending, setIsResending] = useState(false);
  const [resendStatusMsg, setResendStatusMsg] = useState("");

  const handleGoogleCallback = async (response) => {
    setSuccessMessage("");
    setErrors((prev) => ({ ...prev, general: "" }));
    setIsLoading(true);

    try {
      await loginWithGoogle(response.credential);
      setSuccessMessage("Registered & Logged in successfully!");
      setTimeout(() => navigate("/"), 500);
    } catch (error) {
      setErrors((prev) => ({ ...prev, general: error.message || "Google Sign-Up failed" }));
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    /* global google */
    if (typeof google !== "undefined") {
      try {
        google.accounts.id.initialize({
          client_id: getEnv("VITE_GOOGLE_CLIENT_ID"),
          callback: handleGoogleCallback,
        });
        google.accounts.id.renderButton(
          document.getElementById("google-signup-btn"),
          { theme: "outline", size: "large", width: "100%", text: "signup_with" }
        );
      } catch (err) {
        console.error("Google Auth initialization failed:", err);
      }
    }
  }, [navigate, loginWithGoogle]);

  /**
   * Handle input change
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));

    // Clear field-specific error when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "", general: "" }));
    }
  };

  /**
   * Validate single field on blur
   */
  const handleBlur = (e) => {
    const { name, value } = e.target;

    let fieldError = "";
    if (name === "email") {
      const validation = validateEmail(value);
      if (!validation.isValid) fieldError = validation.message;
    } else if (name === "password") {
      const validation = validatePassword(value);
      if (!validation.isValid) fieldError = validation.message;
    } else if (name === "password_confirmation") {
      const validation = validatePasswordMatch(formData.password, value);
      if (!validation.isValid) fieldError = validation.message;
    }

    setErrors((prev) => ({ ...prev, [name]: fieldError }));
  };

  /**
   * Validate entire form before submission
   */
  const validateForm = () => {
    const emailValidation = validateEmail(formData.email);
    const passwordValidation = validatePassword(formData.password);
    const passwordMatchValidation = validatePasswordMatch(
      formData.password,
      formData.password_confirmation,
    );

    const newErrors = {
      email: emailValidation.isValid ? "" : emailValidation.message,
      password: passwordValidation.isValid ? "" : passwordValidation.message,
      password_confirmation: passwordMatchValidation.isValid
        ? ""
        : passwordMatchValidation.message,
      general: "",
    };

    setErrors(newErrors);

    return (
      emailValidation.isValid &&
      passwordValidation.isValid &&
      passwordMatchValidation.isValid
    );
  };

  
  useEffect(() => {
    let timer;
    if (isSuccess && resendCountdown > 0) {
      timer = setInterval(() => {
        setResendCountdown((prev) => prev - 1);
      }, 1000);
    }
    return () => clearInterval(timer);
  }, [isSuccess, resendCountdown]);

  const handleResendInRegister = async () => {
    if (resendCountdown > 0 || isResending) return;

    setIsResending(true);
    setResendStatusMsg("");

    try {
      await resendConfirmationEmail(registeredEmail);
      setResendStatusMsg("New confirmation link sent! Please check your inbox.");
      setResendCountdown(60); // Reset countdown back to 60 seconds
    } catch (err) {
      setResendStatusMsg(err.message || "Failed to resend confirmation email.");
    } finally {
      setIsResending(false);
    }
  };

  /**
   * Handle form submission
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Clear previous messages
    setSuccessMessage("");
    setErrors((prev) => ({ ...prev, general: "" }));

    // Validate form
    if (!validateForm()) return;

    setIsLoading(true);

    try {
      // Call register API
      const response = await register({
        email: formData.email,
        password: formData.password,
        password_confirmation: formData.password_confirmation,
      });

      // Handle successful registration (replaced, 
      // user doesn't login rightaway but has to wait for the email to be confirmed)
      if (response.id && response.email) {
        setRegisteredEmail(formData.email);
        setIsSuccess(true);
        setFormData({ email: "", password: "", password_confirmation: "" });
      }
    } catch (error) {
      // Handle backend errors
      const errorMessage = error.message || "An error occurred during registration";

      // Set field-specific error or general error based on error message
      if (errorMessage.toLowerCase().includes("email")) {
        setErrors((prev) => ({ ...prev, email: errorMessage, general: "" }));
      } else if (
        errorMessage.toLowerCase().includes("password") &&
        errorMessage.toLowerCase().includes("match")
      ) {
        setErrors((prev) => ({ ...prev, password_confirmation: errorMessage, general: "" }));
      } else if (errorMessage.toLowerCase().includes("password")) {
        setErrors((prev) => ({ ...prev, password: errorMessage, general: "" }));
      } else {
        setErrors((prev) => ({ ...prev, general: errorMessage }));
      }
    } finally {
      setIsLoading(false);
    }
  };

  // Email sent page
  if (isSuccess) {
    return (
      <div
        className="text-center py-4"
        role="status"
        aria-live="polite"
        style={{ animation: "floatIn 0.5s cubic-bezier(0.22,1,0.36,1) both" }}
      >
        <div
          className="w-16 h-16 rounded-full bg-ocean-676 dark:bg-ocean-461 dark:text-ocean-910 flex items-center justify-center text-white text-2xl mx-auto mb-5 shadow-lg"
          style={{ animation: "successPop 0.5s cubic-bezier(0.22,1,0.36,1) both" }}
        >
          ✓
        </div>
        <h2 className="text-xl font-extrabold text-slate-800 dark:text-slate-100 mb-2 tracking-tight">
          Check your email!
        </h2>
        <p className="text-slate-500 dark:text-slate-300 text-sm leading-relaxed mb-6">
          We sent a verification link to <strong className="text-slate-800 dark:text-slate-100">{registeredEmail}</strong>.
          <br />
          Please click the confirmation link in the email to activate your account before logging in.
        </p>

        {/* Resend Button with Countdown */}
        <div className="mb-6">
          {resendStatusMsg && <p role="status" className="mb-3 text-sm text-slate-600 dark:text-ocean-239">{resendStatusMsg}</p>}
          <button
            type="button"
            onClick={handleResendInRegister}
            disabled={resendCountdown > 0 || isResending}
            className={`
              text-xs font-semibold px-4 py-2 rounded-lg border transition-all duration-200
              ${
                resendCountdown > 0 || isResending
                  ? "border-slate-200 dark:border-slate-700 text-slate-400 dark:text-slate-500 bg-slate-50 dark:bg-slate-800/50 cursor-not-allowed"
                  : "border-ocean-461 text-ocean-676 dark:text-ocean-316 hover:bg-ocean-461/10 cursor-pointer"
              }
            `}
          >
            {isResending ? (
              "Sending..."
            ) : resendCountdown > 0 ? (
              `Resend email in ${resendCountdown}s`
            ) : (
              "Resend confirmation email"
            )}
          </button>
        </div>

        <div className="flex flex-col items-center gap-2.5 text-center">
          <Link
            to="/login"
            className="flex w-full h-12 items-center justify-center px-6 bg-ocean-676 hover:bg-ocean-808 dark:bg-ocean-461 dark:text-ocean-910 dark:hover:bg-ocean-316 text-white font-semibold text-sm rounded-xl   transition-all duration-300"
          >
            Go to Sign In
          </Link>
          <Link
            to="/"
            className="inline-flex items-center gap-1 text-xs text-slate-400 dark:text-slate-300 hover:text-slate-600 dark:hover:text-slate-100 transition-colors"
          >
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
              <polyline points="15 18 9 12 15 6" />
            </svg>
            Back to main page
          </Link>
        </div>
      </div>
    );
  }


  return (
    <form onSubmit={handleSubmit} noValidate>
      {/* General error */}
      {errors.general && (
        <div className="mb-4 flex items-start gap-2.5 p-3.5 bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800 rounded-xl text-sm text-red-700 dark:text-red-200" role="alert">
          <ErrorCircleIcon className="w-5 h-5 shrink-0 mt-0.5 text-red-500" />
          <p>{errors.general}</p>
        </div>
      )}

      {/* Success */}
      {successMessage && (
        <div className="mb-4 flex items-start gap-2.5 p-3.5 bg-green-50 dark:bg-green-950/40 border border-green-200 dark:border-green-800 rounded-xl text-sm text-green-700 dark:text-green-200" role="status">
          <SuccessCircleIcon className="w-5 h-5 shrink-0 mt-0.5 text-green-500" />
          <p>{successMessage}</p>
        </div>
      )}

      {/* Email field */}
      <InputField
        label="Email Address"
        type="email"
        name="email"
        value={formData.email}
        onChange={handleChange}
        onBlur={handleBlur}
        error={errors.email}
        placeholder="you@example.com"
        required={false}
        autoComplete="email"
        icon={EnvelopeIcon}
        maxLength={100}
      />

      {/* Password field */}
      <PasswordInput
        label="Password"
        name="password"
        value={formData.password}
        onChange={handleChange}
        onBlur={handleBlur}
        error={errors.password}
        placeholder="Create a strong password"
        required={false}
        autoComplete="new-password"
        icon={LockIcon}
        maxLength={128}
      />

      {/* Confirm Password field */}
      <PasswordInput
        label="Confirm Password"
        name="password_confirmation"
        value={formData.password_confirmation}
        onChange={handleChange}
        onBlur={handleBlur}
        error={errors.password_confirmation}
        placeholder="Repeat your password"
        required={false}
        autoComplete="new-password"
        icon={LockIcon}
        maxLength={128}
      />

      {/* Submit button */}
      <button
        type="submit"
        id="register-submit-btn"
        disabled={isLoading}
        aria-busy={isLoading}
        className={`
          relative w-full mt-2 h-12 px-6 flex items-center justify-center gap-2
          bg-ocean-676 hover:bg-ocean-808 dark:bg-ocean-461 dark:text-ocean-910 dark:hover:bg-ocean-316
          text-white font-semibold text-sm rounded-xl
           overflow-hidden
          transition-all duration-300
          ${isLoading ? "opacity-70 cursor-not-allowed" : ""}
        `}
      >
        {isLoading ? (
          <>
            <span
              aria-hidden="true"
              className="w-4 h-4 rounded-full border-2 border-white/40 border-t-white shrink-0"
              style={{ animation: "spin 0.7s linear infinite" }}
            />
            Creating account…
          </>
        ) : (
          "Create Account"
        )}
      </button>

      {/* OR Divider */}
      <div className="my-4 flex items-center justify-center gap-3">
        <span className="w-full h-px bg-slate-200 dark:bg-slate-700" />
        <span className="text-[10px] font-bold text-slate-400 dark:text-slate-300 uppercase tracking-widest">or</span>
        <span className="w-full h-px bg-slate-200 dark:bg-slate-700" />
      </div>

      {/* Google Sign In Button Container */}
      <div id="google-signup-btn" className="w-full flex justify-center mt-2" />

      {/* Footer Links */}
      <div className="mt-5 flex flex-col items-center gap-2.5 text-center">
        <p className="text-sm text-slate-500 dark:text-slate-300">
          Already have an account?{" "}
          <Link to="/login" className="font-semibold text-ocean-676 dark:text-ocean-316 hover:text-ocean-461 transition-colors">
            Sign In
          </Link>
        </p>
        <Link
          to="/"
          className="inline-flex items-center gap-1 text-xs text-slate-400 dark:text-slate-300 hover:text-slate-600 dark:hover:text-slate-100 transition-colors"
        >
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" aria-hidden="true">
            <polyline points="15 18 9 12 15 6" />
          </svg>
          Back to main page
        </Link>
      </div>
    </form>
  );
};

export default RegisterForm;
