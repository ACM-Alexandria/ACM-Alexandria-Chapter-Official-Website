import { useState, useEffect } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { resetPassword } from "../services/authService";
import {
  validatePassword as validatePasswordStrength,
  validatePasswordMatch,
  getPasswordRules,
} from "../utils/validation";
import {
  LockIcon,
  ErrorCircleIcon,
  SuccessCircleIcon,
  SpinnerIcon,
  EyeSlashIcon,
  EyeIcon,
  CheckIcon,
  MinusCircleIcon,
  ArrowLeftIcon,
  ChevronRightIcon,
} from "../components/icons";

const ResetPassword = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");

  const [formData, setFormData] = useState({
    new_password: "",
    new_password_confirm: "",
  });

  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  // Check for token on mount
  useEffect(() => {
    if (!token) {
      setApiError(
        "Invalid or missing reset link. Please request a new password reset.",
      );
    }
  }, [token]);

  const validateForm = () => {
    const newErrors = {};

    if (!formData.new_password) {
      newErrors.new_password = "Password is required";
    } else {
      const validation = validatePasswordStrength(formData.new_password);
      if (!validation.isValid) {
        newErrors.new_password = validation.message;
      }
    }

    const matchValidation = validatePasswordMatch(
      formData.new_password,
      formData.new_password_confirm,
    );
    if (!matchValidation.isValid) {
      newErrors.new_password_confirm = matchValidation.message;
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

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
      }));
    }

    // Clear API error when user starts typing
    if (apiError) {
      setApiError("");
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    // Clear previous messages
    setApiError("");
    setSuccessMessage("");

    // Validate form
    if (!validateForm()) {
      return;
    }

    setIsLoading(true);

    try {
      await resetPassword({
        token,
        new_password: formData.new_password,
        new_password_confirm: formData.new_password_confirm,
      });

      // Show success message
      setSuccessMessage("Password reset successfully! Redirecting to login...");

      // Redirect to login after 3 seconds
      setTimeout(() => {
        navigate("/login");
      }, 3000);
    } catch (error) {
      // Handle API errors
      if (error.response?.status === 400) {
        setApiError(
          "This password reset link is invalid or has expired. Please request a new one.",
        );
      } else if (error.response?.data?.message) {
        setApiError(error.response.data.message);
      } else {
        setApiError("An error occurred. Please try again later.");
      }
    } finally {
      setIsLoading(false);
    }
  };

  // Check if form is valid (for button state)
  const isFormValid = () => {
    return (
      formData.new_password &&
      formData.new_password_confirm &&
      formData.new_password === formData.new_password_confirm &&
      validatePasswordStrength(formData.new_password).isValid
    );
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-gray-50 via-blue-50 to-indigo-50 flex items-center justify-center p-4">
      {/* Background decorative elements */}
      <div className="absolute inset-0 overflow-hidden pointer-events-none">
        <div className="absolute top-0 right-0 w-96 h-96 bg-blue-200 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob"></div>
        <div className="absolute bottom-0 left-0 w-96 h-96 bg-indigo-200 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob animation-delay-2000"></div>
        <div className="absolute top-1/2 left-1/2 w-96 h-96 bg-purple-200 rounded-full mix-blend-multiply filter blur-3xl opacity-30 animate-blob animation-delay-4000"></div>
      </div>

      <div className="w-full max-w-md relative">
        <div className="bg-white rounded-3xl shadow-2xl overflow-hidden border border-gray-100">
          {/* Header */}
          <div className="bg-gradient-to-r from-blue-600 to-indigo-600 p-8 text-center relative overflow-hidden">
            <div className="absolute inset-0 bg-black opacity-5"></div>
            <div className="relative z-10">
              <div className="w-16 h-16 bg-white/20 backdrop-blur-sm rounded-2xl flex items-center justify-center mx-auto mb-4 shadow-lg">
                <LockIcon className="w-8 h-8 text-white" />
              </div>
              <h1 className="text-3xl font-bold text-white mb-2">
                Reset Password
              </h1>
              <p className="text-blue-100">
                Create a new secure password for your account
              </p>
            </div>
          </div>

          <div className="p-8">
            {/* Error: No token */}
            {!token && (
              <div className="bg-red-50 border-l-4 border-red-500 rounded-lg p-4 mb-6">
                <div className="flex items-start">
                  <ErrorCircleIcon className="w-5 h-5 text-red-500 mt-0.5 mr-3 flex-shrink-0" />
                  <div className="flex-1">
                    <p className="text-red-800 text-sm font-medium">
                      {apiError}
                    </p>
                    <button
                      onClick={() => navigate("/forgot-password")}
                      className="text-red-700 hover:text-red-900 text-sm font-semibold mt-2 inline-flex items-center transition-colors underline">
                      Request a new reset link
                      <ChevronRightIcon className="w-4 h-4 ml-1" />
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* Success Message */}
            {successMessage && (
              <div className="bg-green-50 border-l-4 border-green-500 rounded-lg p-4 mb-6 animate-fadeIn">
                <div className="flex items-start">
                  <SuccessCircleIcon className="w-5 h-5 text-green-500 mt-0.5 mr-3 flex-shrink-0" />
                  <p className="text-green-800 text-sm font-medium">
                    {successMessage}
                  </p>
                </div>
              </div>
            )}

            {/* API Error Message */}
            {apiError && token && (
              <div className="bg-red-50 border-l-4 border-red-500 rounded-lg p-4 mb-6">
                <div className="flex items-start">
                  <ErrorCircleIcon className="w-5 h-5 text-red-500 mt-0.5 mr-3 flex-shrink-0" />
                  <div className="flex-1">
                    <p className="text-red-800 text-sm font-medium">
                      {apiError}
                    </p>
                    <button
                      onClick={() => navigate("/forgot-password")}
                      className="text-red-700 hover:text-red-900 text-sm font-semibold mt-2 inline-flex items-center transition-colors underline">
                      Request a new reset link
                      <ChevronRightIcon className="w-4 h-4 ml-1" />
                    </button>
                  </div>
                </div>
              </div>
            )}

            {/* Form */}
            {token && !successMessage && (
              <form onSubmit={handleSubmit} className="space-y-6">
                {/* New Password Field */}
                <div>
                  <label
                    htmlFor="new_password"
                    className="block text-sm font-semibold text-gray-700 mb-2">
                    New Password
                  </label>
                  <div className="relative">
                    <input
                      type={showPassword ? "text" : "password"}
                      id="new_password"
                      name="new_password"
                      value={formData.new_password}
                      onChange={handleChange}
                      className={`w-full px-4 py-3 bg-gray-50 border-2 ${
                        errors.new_password
                          ? "border-red-300 focus:border-red-500"
                          : "border-gray-200 focus:border-blue-500"
                      } rounded-xl text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-4 focus:ring-blue-100 transition-all`}
                      placeholder="Enter your new password"
                      disabled={isLoading}
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword(!showPassword)}
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors p-1"
                      tabIndex={-1}>
                      {showPassword ? <EyeSlashIcon /> : <EyeIcon />}
                    </button>
                  </div>
                  {errors.new_password && (
                    <p className="mt-2 text-sm text-red-600 font-medium">
                      {errors.new_password}
                    </p>
                  )}
                  {!errors.new_password && formData.new_password && (
                    <div className="mt-3 space-y-2">
                      <p className="text-xs font-semibold text-gray-600 uppercase tracking-wide">
                        Password Requirements:
                      </p>
                      <div className="flex flex-wrap gap-2">
                        {getPasswordRules(formData.new_password).map(
                          (rule, idx) => (
                            <span
                              key={idx}
                              className={`text-xs px-3 py-1.5 rounded-full font-medium transition-all ${
                                rule.passed
                                  ? "bg-green-100 text-green-700 border border-green-200"
                                  : "bg-gray-100 text-gray-500 border border-gray-200"
                              }`}>
                              {rule.passed ? (
                                <span className="inline-flex items-center">
                                  <CheckIcon className="w-3 h-3 mr-1" />
                                  {rule.label}
                                </span>
                              ) : (
                                <span className="inline-flex items-center">
                                  <MinusCircleIcon className="w-3 h-3 mr-1" />
                                  {rule.label}
                                </span>
                              )}
                            </span>
                          ),
                        )}
                      </div>
                    </div>
                  )}
                </div>

                {/* Confirm Password Field */}
                <div>
                  <label
                    htmlFor="new_password_confirm"
                    className="block text-sm font-semibold text-gray-700 mb-2">
                    Confirm New Password
                  </label>
                  <div className="relative">
                    <input
                      type={showConfirmPassword ? "text" : "password"}
                      id="new_password_confirm"
                      name="new_password_confirm"
                      value={formData.new_password_confirm}
                      onChange={handleChange}
                      className={`w-full px-4 py-3 bg-gray-50 border-2 ${
                        errors.new_password_confirm
                          ? "border-red-300 focus:border-red-500"
                          : "border-gray-200 focus:border-blue-500"
                      } rounded-xl text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-4 focus:ring-blue-100 transition-all`}
                      placeholder="Confirm your new password"
                      disabled={isLoading}
                    />
                    <button
                      type="button"
                      onClick={() =>
                        setShowConfirmPassword(!showConfirmPassword)
                      }
                      className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors p-1"
                      tabIndex={-1}>
                      {showConfirmPassword ? <EyeSlashIcon /> : <EyeIcon />}
                    </button>
                  </div>
                  {errors.new_password_confirm && (
                    <p className="mt-2 text-sm text-red-600 font-medium">
                      {errors.new_password_confirm}
                    </p>
                  )}
                  {!errors.new_password_confirm &&
                    formData.new_password_confirm &&
                    formData.new_password === formData.new_password_confirm && (
                      <p className="mt-2 text-sm text-green-600 font-medium flex items-center">
                        <SuccessCircleIcon className="w-4 h-4 mr-1.5" />
                        Passwords match perfectly
                      </p>
                    )}
                </div>

                {/* Submit Button */}
                <button
                  type="submit"
                  disabled={!isFormValid() || isLoading}
                  className={`w-full py-3.5 px-4 rounded-xl font-semibold text-white transition-all duration-300 ${
                    !isFormValid() || isLoading
                      ? "bg-gray-300 cursor-not-allowed"
                      : "bg-gradient-to-r from-blue-600 to-indigo-600 hover:from-blue-700 hover:to-indigo-700 shadow-lg hover:shadow-xl transform hover:-translate-y-0.5 active:translate-y-0"
                  }`}>
                  {isLoading ? (
                    <span className="flex items-center justify-center">
                      <SpinnerIcon className="animate-spin -ml-1 mr-3 h-5 w-5 text-white" />
                      Resetting Password...
                    </span>
                  ) : (
                    "Set New Password"
                  )}
                </button>

                {/* Back to Login Link */}
                <div className="text-center pt-2">
                  <button
                    type="button"
                    onClick={() => navigate("/login")}
                    className="text-sm text-gray-600 hover:text-gray-900 transition-colors inline-flex items-center font-medium group">
                    <ArrowLeftIcon className="w-4 h-4 mr-1.5 group-hover:-translate-x-1 transition-transform" />
                    Back to Login
                  </button>
                </div>
              </form>
            )}
          </div>
        </div>
      </div>

      <style>{`
        @keyframes fadeIn {
          from {
            opacity: 0;
            transform: translateY(-10px);
          }
          to {
            opacity: 1;
            transform: translateY(0);
          }
        }

        @keyframes blob {
          0%,
          100% {
            transform: translate(0, 0) scale(1);
          }
          33% {
            transform: translate(30px, -50px) scale(1.1);
          }
          66% {
            transform: translate(-20px, 20px) scale(0.9);
          }
        }

        .animate-fadeIn {
          animation: fadeIn 0.3s ease-out;
        }

        .animate-blob {
          animation: blob 7s infinite;
        }

        .animation-delay-2000 {
          animation-delay: 2s;
        }

        .animation-delay-4000 {
          animation-delay: 4s;
        }
      `}</style>
    </div>
  );
};

export default ResetPassword;
