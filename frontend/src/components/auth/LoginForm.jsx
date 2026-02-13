import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
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
  const { login } = useAuth();

  const [formData, setFormData] = useState({ email: "", password: "" });
  const [errors, setErrors] = useState({
    email: "",
    password: "",
    general: "",
  });
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
      setTimeout(() => navigate("/"), 1500);
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
      {/* General error message */}
      {errors.general && (
        <div className="mb-6 p-4 bg-red-50 border border-red-100 rounded-lg flex items-start gap-3">
          <ErrorCircleIcon className="w-5 h-5 text-red-600 mt-0.5" />
          <p className="text-sm text-red-800">{errors.general}</p>
        </div>
      )}

      {/* Success message */}
      {successMessage && (
        <div className="mb-6 p-4 bg-green-50 border border-green-100 rounded-lg flex items-start gap-3">
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
        placeholder="example@mail.com"
        required={true}
        autoComplete="email"
        icon={EnvelopeIcon} // Passing the new icon
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
        icon={LockIcon} // Passing the new icon
        // Passing the link to be rendered inside the component label area
        forgotPasswordLink={<Link to="/forgot-password">Forgot Password?</Link>}
      />

      {/* Submit button */}
      <button
        type="submit"
        disabled={isLoading}
        className={`w-full py-3.5 px-4 mt-2 bg-gradient-to-r from-[#4B98C8] to-[#205E85] text-white font-bold rounded-lg shadow-md transition-all duration-300 transform hover:-translate-y-0.5 ${
          isLoading
            ? "opacity-70 cursor-not-allowed"
            : "hover:shadow-lg hover:opacity-95"
        }`}>
        {isLoading ? (
          <span className="flex items-center justify-center gap-2">
            <SpinnerIcon />
            Logging in...
          </span>
        ) : (
          "Log In"
        )}
      </button>

      {/* Footer Links */}
      <div className="text-center space-y-4 pt-6">
        <p className="text-sm text-gray-500">
          Don't have an account?{" "}
          <Link
            to="/register"
            className="text-gray-900 font-bold hover:underline transition-colors duration-200">
            Sign Up
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

export default LoginForm;
