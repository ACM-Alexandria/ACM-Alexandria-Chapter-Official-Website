import { useState, useEffect } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { resetPassword } from "../services/authService";
import {
  validatePassword as validatePasswordStrength,
  validatePasswordMatch,
} from "../utils/validation";
import PasswordInput from "../components/auth/PasswordInput";
import { LockIcon, ErrorCircleIcon, SpinnerIcon } from "../components/icons";

const ResetPassword = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");

  const [formData, setFormData] = useState({
    new_password: "",
    new_password_confirm: "",
  });

  const [errors, setErrors] = useState({
    new_password: "",
    new_password_confirm: "",
  });
  const [apiError, setApiError] = useState("");
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);

  useEffect(() => {
    if (!token) {
      setApiError(
        "Invalid or missing reset link. Please request a new password reset.",
      );
    }
  }, [token]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
    if (errors[name]) {
      setErrors((prev) => ({ ...prev, [name]: "" }));
    }
    if (apiError) setApiError("");
  };

  const handleBlur = (e) => {
    const { name, value } = e.target;
    let fieldError = "";
    if (name === "new_password") {
      const validation = validatePasswordStrength(value);
      if (!validation.isValid) fieldError = validation.message;
    } else if (name === "new_password_confirm") {
      const validation = validatePasswordMatch(formData.new_password, value);
      if (!validation.isValid) fieldError = validation.message;
    }
    setErrors((prev) => ({ ...prev, [name]: fieldError }));
  };

  const validateForm = () => {
    const pwValidation = validatePasswordStrength(formData.new_password);
    const matchValidation = validatePasswordMatch(
      formData.new_password,
      formData.new_password_confirm,
    );
    const newErrors = {
      new_password: pwValidation.isValid ? "" : pwValidation.message,
      new_password_confirm: matchValidation.isValid
        ? ""
        : matchValidation.message,
    };
    setErrors(newErrors);
    return pwValidation.isValid && matchValidation.isValid;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setApiError("");

    if (!validateForm()) return;

    setIsLoading(true);

    try {
      await resetPassword({
        token,
        new_password: formData.new_password,
        new_password_confirm: formData.new_password_confirm,
      });
      setIsSuccess(true);
    } catch (error) {
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

  return (
    <div className="min-h-screen w-full bg-gradient-to-br from-blue-50 via-white to-blue-50 flex items-center justify-center px-4 py-12">
      <div className="bg-white w-full max-w-[480px] rounded-2xl shadow-xl p-10 sm:p-12 border border-gray-100">
        {/* Header */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-extrabold text-gray-900 mb-2">
            Reset Password
          </h1>
          <p className="text-gray-500 text-sm leading-relaxed">
            Enter your new password and confirm it to log in
            <br />
            using it next time
          </p>
        </div>

        {/* No token error */}
        {!token && (
          <div className="mb-6 p-4 bg-red-50 border border-red-100 rounded-lg flex items-start gap-3">
            <ErrorCircleIcon className="w-5 h-5 text-red-600 mt-0.5" />
            <div>
              <p className="text-sm text-red-800">{apiError}</p>
              <button
                onClick={() => navigate("/forgot-password")}
                className="text-sm text-red-700 font-bold hover:underline mt-1">
                Request a new reset link
              </button>
            </div>
          </div>
        )}

        {/* API error */}
        {apiError && token && (
          <div className="mb-6 p-4 bg-red-50 border border-red-100 rounded-lg flex items-start gap-3">
            <ErrorCircleIcon className="w-5 h-5 text-red-600 mt-0.5" />
            <div>
              <p className="text-sm text-red-800">{apiError}</p>
              <button
                onClick={() => navigate("/forgot-password")}
                className="text-sm text-red-700 font-bold hover:underline mt-1">
                Request a new reset link
              </button>
            </div>
          </div>
        )}

        {/* Form */}
        {token && (
          <form onSubmit={handleSubmit} noValidate>
            <PasswordInput
              label="Password"
              name="new_password"
              value={formData.new_password}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.new_password}
              placeholder="Enter your password"
              required={false}
              autoComplete="new-password"
              icon={LockIcon}
            />

            <PasswordInput
              label="Confirm Password"
              name="new_password_confirm"
              value={formData.new_password_confirm}
              onChange={handleChange}
              onBlur={handleBlur}
              error={errors.new_password_confirm}
              placeholder="Enter your password"
              required={false}
              autoComplete="new-password"
              icon={LockIcon}
            />

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isLoading}
              className={`w-full py-3.5 px-4 mt-4 bg-gradient-to-r from-[#3A9BD5] to-[#1A6FA0] text-white font-bold rounded-xl shadow-md transition-all duration-300 transform hover:-translate-y-0.5 flex items-center justify-center gap-2 ${
                isLoading
                  ? "opacity-70 cursor-not-allowed"
                  : "hover:shadow-lg hover:from-[#3290C8] hover:to-[#175E8B]"
              }`}>
              {isLoading ? (
                <>
                  <SpinnerIcon />
                  Resetting...
                </>
              ) : (
                "Reset Password"
              )}
            </button>
          </form>
        )}

        {/* Footer Links */}
        <div className="text-center space-y-3 pt-5">
          <p className="text-sm text-gray-500">
            Return to{" "}
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
      </div>

      {/* Success Modal Overlay */}
      {isSuccess && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/30 backdrop-blur-sm px-4">
          <div className="bg-white w-full max-w-[480px] rounded-2xl shadow-2xl p-10 sm:p-12 border border-gray-100 animate-[fadeIn_0.3s_ease-out]">
            <div className="text-center">
              <h2 className="text-3xl font-extrabold text-gray-900 mb-2">
                Password Reset
              </h2>
              <p className="text-gray-500 text-sm leading-relaxed mb-8">
                Your password has been reset successfully!
                <br />
                You can now use it to log in
              </p>

              <div className="border-t border-gray-100 pt-5 space-y-3">
                <p className="text-sm text-gray-500">
                  Return to{" "}
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
                    <span className="mr-1 text-lg">‹</span> Back to the main
                    page
                  </Link>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ResetPassword;
