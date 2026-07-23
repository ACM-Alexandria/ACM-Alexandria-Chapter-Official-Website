import { useState, useEffect } from "react";
import { useNavigate, Link } from "react-router-dom";
import InputField from "./InputField";
import PasswordInput from "./PasswordInput";
import { useAuth } from "../../contexts/AuthContext";
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
} from "../icons";

const RegisterForm = () => {
  const navigate = useNavigate();
  const { register, login, loginWithGoogle } = useAuth();

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
          client_id: "286108572806-agfr1j9sshfsg5us3irpdll4omsns06o.apps.googleusercontent.com",
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

      // Handle successful registration
      if (response.id && response.email) {
        setSuccessMessage("Account created successfully! Logging you in...");

        // Log the user in automatically using destructured login
        await login(formData.email, formData.password);
        
        setSuccessMessage("Account created & logged in successfully!");
        setFormData({ email: "", password: "", password_confirmation: "" });

        // Redirect to main page after a short delay
        setTimeout(() => navigate("/"), 500);
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

  return (
    <form onSubmit={handleSubmit} noValidate>
      {/* General error */}
      {errors.general && (
        <div className="mb-4 flex items-start gap-2.5 p-3.5 bg-red-50 border border-red-200 rounded-xl text-sm text-red-700" role="alert">
          <ErrorCircleIcon className="w-5 h-5 shrink-0 mt-0.5 text-red-500" />
          <p>{errors.general}</p>
        </div>
      )}

      {/* Success */}
      {successMessage && (
        <div className="mb-4 flex items-start gap-2.5 p-3.5 bg-green-50 border border-green-200 rounded-xl text-sm text-green-700" role="status">
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
          relative w-full mt-2 py-3 px-6 flex items-center justify-center gap-2
          bg-gradient-to-r from-[#4B98C8] to-[#205E85]
          text-white font-bold text-sm rounded-xl
          shadow-md overflow-hidden
          transition-all duration-300
          ${isLoading ? "opacity-70 cursor-not-allowed" : "hover:-translate-y-0.5 hover:shadow-lg hover:from-[#5aa3d0] hover:to-[#256b96]"}
        `}
      >
        <span
          aria-hidden="true"
          className="absolute inset-0 pointer-events-none"
          style={{
            background: "linear-gradient(105deg,transparent 40%,rgba(255,255,255,0.18) 50%,transparent 60%)",
            backgroundSize: "200% 100%",
            animation: "shimmer 2.8s linear infinite",
          }}
        />
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
        <span className="w-full h-px bg-gray-200" />
        <span className="text-[10px] font-bold text-gray-400 uppercase tracking-widest">or</span>
        <span className="w-full h-px bg-gray-200" />
      </div>

      {/* Google Sign In Button Container */}
      <div id="google-signup-btn" className="w-full flex justify-center mt-2" />

      {/* Footer Links */}
      <div className="mt-5 flex flex-col items-center gap-2.5 text-center">
        <p className="text-sm text-gray-500">
          Already have an account?{" "}
          <Link to="/login" className="font-semibold text-[#205E85] hover:text-[#4B98C8] transition-colors">
            Sign In
          </Link>
        </p>
        <Link
          to="/"
          className="inline-flex items-center gap-1 text-xs text-gray-400 hover:text-gray-600 transition-colors"
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
