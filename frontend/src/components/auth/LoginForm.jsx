import { useState } from "react";
import { useNavigate, Link, useLocation } from "react-router-dom";
import InputField from "./InputField";
import PasswordInput from "./PasswordInput";
import { useAuth } from "../../contexts/AuthContext";
import { validateEmail, validatePassword } from "../../utils/validation";
import {
  ErrorCircleIcon,
  SuccessCircleIcon,
  SpinnerIcon,
  EnvelopeIcon,
  LockIcon,
} from "../icons";

const LoginForm = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const [formData, setFormData] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({ email: "", password: "", general: "" });
  const [isLoading, setIsLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name] || errors.general) {
      setErrors((prev) => ({ ...prev, [name]: "", general: "" }));
    }
  };

  const handleBlur = (e) => {
    const { name, value } = e.target;
    let fieldError = "";
    if (name === "email") {
      const validation = validateEmail(value);
      if (!validation.isValid) fieldError = validation.message;
    } else if (name === "password") {
      const validation = validatePassword(value);
      if (!validation.isValid) fieldError = validation.message;
    }
    setErrors((prev) => ({ ...prev, [name]: fieldError }));
  };

  const validateForm = () => {
    const emailValidation = validateEmail(formData.email);
    const passwordValidation = validatePassword(formData.password);
    setErrors({
      email: emailValidation.isValid ? "" : emailValidation.message,
      password: passwordValidation.isValid ? "" : passwordValidation.message,
      general: "",
    });
    return emailValidation.isValid && passwordValidation.isValid;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSuccessMessage("");
    setErrors((prev) => ({ ...prev, general: "" }));

    if (!validateForm()) return;

    setIsLoading(true);

    try {
      await login(formData.email, formData.password);
      setSuccessMessage("Logged in successfully!");
      setFormData({ email: "", password: "" });
      const destination = location.state?.from || "/";
      setTimeout(() => navigate(destination), 1500);
    } catch (error) {
      const errorMessage = error.message || "Incorrect email or password";
      setFormData((prev) => ({ ...prev, password: "" }));

      if (errorMessage.toLowerCase().includes("email")) {
        setErrors((prev) => ({ ...prev, email: errorMessage, general: "" }));
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

      <InputField
        label="Email Address"
        type="email"
        name="email"
        value={formData.email}
        onChange={handleChange}
        onBlur={handleBlur}
        error={errors.email}
        placeholder="you@example.com"
        required={true}
        autoComplete="email"
        icon={EnvelopeIcon}
      />

      <PasswordInput
        label="Password"
        name="password"
        value={formData.password}
        onChange={handleChange}
        onBlur={handleBlur}
        error={errors.password}
        placeholder="Your password"
        required={true}
        autoComplete="current-password"
        icon={LockIcon}
        forgotPasswordLink={<Link to="/forgot-password">Forgot Password?</Link>}
      />

      {/* Submit */}
      <button
        type="submit"
        id="login-submit-btn"
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
        {/* shimmer */}
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
            Signing in…
          </>
        ) : (
          "Sign In"
        )}
      </button>

      {/* Footer links */}
      <div className="mt-5 flex flex-col items-center gap-2.5 text-center">
        <p className="text-sm text-gray-500">
          Don't have an account?{" "}
          <Link to="/register" className="font-semibold text-[#205E85] hover:text-[#4B98C8] transition-colors">
            Create one
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

export default LoginForm;
