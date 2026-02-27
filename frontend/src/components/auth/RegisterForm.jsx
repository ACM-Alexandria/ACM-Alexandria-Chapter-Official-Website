import { useState } from "react";
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
  const { register } = useAuth();

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

  /**
   * Handle input change
   */
  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
    }));

    // Clear field-specific error when user starts typing
    if (errors[name]) {
      setErrors((prev) => ({
        ...prev,
        [name]: "",
        general: "",
      }));
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
      if (!validation.isValid) {
        fieldError = validation.message;
      }
    } else if (name === "password") {
      const validation = validatePassword(value);
      if (!validation.isValid) {
        fieldError = validation.message;
      }
    } else if (name === "password_confirmation") {
      const validation = validatePasswordMatch(formData.password, value);
      if (!validation.isValid) {
        fieldError = validation.message;
      }
    }

    setErrors((prev) => ({
      ...prev,
      [name]: fieldError,
    }));
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
    if (!validateForm()) {
      return;
    }

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
        setSuccessMessage(
          "Account created successfully! Redirecting to login...",
        );

        // Clear password fields only (keep email for convenience)
        setFormData((prev) => ({
          ...prev,
          password: "",
          password_confirmation: "",
        }));

        // Redirect to login after a short delay
        setTimeout(() => {
          navigate("/login");
        }, 2000);
      }
    } catch (error) {
      // Handle backend errors
      const errorMessage =
        error.message || "An error occurred during registration";

      // Clear password fields on error (as per requirements)
      setFormData((prev) => ({
        ...prev,
        password: "",
        password_confirmation: "",
      }));

      // Set field-specific error or general error based on error message
      if (errorMessage.toLowerCase().includes("email")) {
        setErrors((prev) => ({
          ...prev,
          email: errorMessage,
          general: "",
        }));
      } else if (
        errorMessage.toLowerCase().includes("password") &&
        errorMessage.toLowerCase().includes("match")
      ) {
        setErrors((prev) => ({
          ...prev,
          password_confirmation: errorMessage,
          general: "",
        }));
      } else if (errorMessage.toLowerCase().includes("password")) {
        setErrors((prev) => ({
          ...prev,
          password: errorMessage,
          general: "",
        }));
      } else {
        setErrors((prev) => ({
          ...prev,
          general: errorMessage,
        }));
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} noValidate>
      {/* General error message */}
      {errors.general && (
        <div
          className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3"
          role="alert">
          <ErrorCircleIcon className="w-5 h-5 text-red-600 mt-0.5" />
          <p className="text-sm text-red-800">{errors.general}</p>
        </div>
      )}

      {/* Success message */}
      {successMessage && (
        <div
          className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg flex items-start gap-3"
          role="alert">
          <SuccessCircleIcon className="w-5 h-5 text-green-600 mt-0.5" />
          <p className="text-sm text-green-800">{successMessage}</p>
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
        placeholder="Enter your email"
        required={false}
        autoComplete="email"
        icon={EnvelopeIcon}
      />

      {/* Password field */}
      <PasswordInput
        label="Password"
        name="password"
        value={formData.password}
        onChange={handleChange}
        onBlur={handleBlur}
        error={errors.password}
        placeholder="Enter your password"
        required={false}
        autoComplete="new-password"
        icon={LockIcon}
      />

      {/* Confirm Password field */}
      <PasswordInput
        label="Confirm Password"
        name="password_confirmation"
        value={formData.password_confirmation}
        onChange={handleChange}
        onBlur={handleBlur}
        error={errors.password_confirmation}
        placeholder="Enter your password"
        required={false}
        autoComplete="new-password"
        icon={LockIcon}
      />

      {/* Submit button */}
      <button
        type="submit"
        disabled={isLoading}
        className={`w-full py-3.5 px-4 mt-4 bg-gradient-to-r from-[#3A9BD5] to-[#1A6FA0] text-white font-bold rounded-xl shadow-md transition-all duration-300 transform hover:-translate-y-0.5 ${
          isLoading
            ? "opacity-70 cursor-not-allowed"
            : "hover:shadow-lg hover:from-[#3290C8] hover:to-[#175E8B]"
        }`}>
        {isLoading ? (
          <span className="flex items-center justify-center gap-2">
            <SpinnerIcon />
            Creating account...
          </span>
        ) : (
          "Sign up"
        )}
      </button>

      {/* Footer Links */}
      <div className="text-center space-y-3 pt-5">
        <p className="text-sm text-gray-500">
          Already have an account?{" "}
          <Link
            to="/login"
            className="text-gray-900 font-bold hover:underline transition-colors duration-200">
            Log In
          </Link>
        </p>

        <div>
          <Link
            to="/"
            className="inline-flex items-center text-sm text-gray-400 hover:text-gray-600 transition-colors">
            <span className="mr-1 text-lg">‹</span> Back to the main page
          </Link>
        </div>
      </div>
    </form>
  );
};

export default RegisterForm;
