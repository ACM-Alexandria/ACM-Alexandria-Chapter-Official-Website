import { useState } from "react";
import { useNavigate } from "react-router-dom";
import InputField from "./InputField";
import PasswordInput from "./PasswordInput";
import { login } from "../../services/authService";
import { validateEmail, validatePassword } from "../../utils/validation";

/**
 * Login Form Component
 * Handles user authentication with email and password
 */
const LoginForm = () => {
  const navigate = useNavigate();
  
  // Form state
  const [formData, setFormData] = useState({
    email: "",
    password: "",
  });

  // Error state for each field
  const [errors, setErrors] = useState({
    email: "",
    password: "",
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

    const newErrors = {
      email: emailValidation.isValid ? "" : emailValidation.message,
      password: passwordValidation.isValid ? "" : passwordValidation.message,
      general: "",
    };

    setErrors(newErrors);

    return emailValidation.isValid && passwordValidation.isValid;
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
      // Call login API
      const response = await login({
        email: formData.email,
        password: formData.password,
      });

      // Store user data (you can customize this based on your needs)
      if (response.id && response.email) {
        localStorage.setItem("user", JSON.stringify(response));
        setSuccessMessage("Logged in successfully!");

        // Clear form
        setFormData({ email: "", password: "" });

        // Redirect to dashboard after a short delay
        setTimeout(() => {
          navigate("/"); // Change to your desired route
        }, 1500);
      }
    } catch (error) {
      // Handle backend errors
      const errorMessage = error.message || "Incorrect email or password";
      
      // Clear password field on error (as per requirements)
      setFormData((prev) => ({
        ...prev,
        password: "",
      }));

      // Set general error or specific field error based on error message
      if (errorMessage.toLowerCase().includes("email")) {
        setErrors((prev) => ({
          ...prev,
          email: errorMessage,
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
    <div className="w-full max-w-md">
      <form onSubmit={handleSubmit} noValidate>
        {/* General error message */}
        {errors.general && (
          <div
            className="mb-4 p-4 bg-red-50 border border-red-200 rounded-lg flex items-start gap-3"
            role="alert"
          >
            <svg
              className="w-5 h-5 text-red-600 mt-0.5"
              fill="currentColor"
              viewBox="0 0 20 20"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z"
                clipRule="evenodd"
              />
            </svg>
            <p className="text-sm text-red-800">{errors.general}</p>
          </div>
        )}

        {/* Success message */}
        {successMessage && (
          <div
            className="mb-4 p-4 bg-green-50 border border-green-200 rounded-lg flex items-start gap-3"
            role="alert"
          >
            <svg
              className="w-5 h-5 text-green-600 mt-0.5"
              fill="currentColor"
              viewBox="0 0 20 20"
              xmlns="http://www.w3.org/2000/svg"
            >
              <path
                fillRule="evenodd"
                d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z"
                clipRule="evenodd"
              />
            </svg>
            <p className="text-sm text-green-800">{successMessage}</p>
          </div>
        )}

        {/* Email field */}
        <InputField
          label="Email"
          type="email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          onBlur={handleBlur}
          error={errors.email}
          placeholder="example@mail.com"
          required={true}
          autoComplete="email"
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
          required={true}
          autoComplete="current-password"
        />

        {/* Forgot password link */}
        <div className="mb-6 text-right">
          <a
            href="/forgot-password"
            className="text-sm text-blue-600 hover:text-blue-800 hover:underline transition-colors duration-200"
          >
            Forgot password?
          </a>
        </div>

        {/* Submit button */}
        <button
          type="submit"
          disabled={isLoading}
          className={`w-full py-3 px-4 bg-gradient-to-r from-[#4B98C8] to-[#205E85] text-white font-semibold rounded-lg transition-all duration-300 ${
            isLoading
              ? "opacity-70 cursor-not-allowed"
              : "hover:opacity-90 hover:shadow-lg"
          }`}
        >
          {isLoading ? (
            <span className="flex items-center justify-center gap-2">
              <svg
                className="animate-spin h-5 w-5 text-white"
                xmlns="http://www.w3.org/2000/svg"
                fill="none"
                viewBox="0 0 24 24"
              >
                <circle
                  className="opacity-25"
                  cx="12"
                  cy="12"
                  r="10"
                  stroke="currentColor"
                  strokeWidth="4"
                />
                <path
                  className="opacity-75"
                  fill="currentColor"
                  d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"
                />
              </svg>
              Logging in...
            </span>
          ) : (
            "Log In"
          )}
        </button>

        {/* Sign up link */}
        <div className="mt-6 text-center">
          <p className="text-sm text-gray-600">
            Don't have an account?{" "}
            <a
              href="/register"
              className="text-blue-600 hover:text-blue-800 font-semibold hover:underline transition-colors duration-200"
            >
              Sign up
            </a>
          </p>
        </div>
      </form>
    </div>
  );
};

export default LoginForm;