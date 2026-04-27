import { useState, useEffect } from "react";
import { useNavigate, useSearchParams, Link } from "react-router-dom";
import { resetPassword } from "../services/authService";
import {
  validatePassword as validatePasswordStrength,
  validatePasswordMatch,
} from "../utils/validation";
import PasswordInput from "../components/auth/PasswordInput";
import { LockIcon, ErrorCircleIcon } from "../components/icons";
import AuthLayout from "../components/auth/AuthLayout";

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
      new_password_confirm: matchValidation.isValid ? "" : matchValidation.message,
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
    <AuthLayout
      title={isSuccess ? "Password reset!" : "Reset password"}
      subtitle={
        isSuccess
          ? "Your password has been updated — you can sign in now"
          : "Enter your new password and confirm it below"
      }
      panelTagline="New password, fresh start."
      panelSub="Create a strong new password to keep your account secure."
      activeDot={2}
    >
      {isSuccess ? (
        /* Success View */
        <div
          className="text-center py-4"
          style={{ animation: "floatIn 0.5s cubic-bezier(0.22,1,0.36,1) both" }}
        >
          <div
            className="w-16 h-16 rounded-full bg-gradient-to-br from-[#4B98C8] to-[#205E85] flex items-center justify-center text-white text-2xl mx-auto mb-5 shadow-lg"
            style={{ animation: "successPop 0.5s cubic-bezier(0.22,1,0.36,1) both" }}
          >
            ✓
          </div>
          <h2 className="text-xl font-extrabold text-gray-800 mb-2 tracking-tight">All done!</h2>
          <p className="text-gray-500 text-sm leading-relaxed mb-6">
            Your password has been reset successfully.
            <br />
            You can now sign in with your new password.
          </p>

          <div className="flex flex-col items-center gap-2.5 text-center">
            <p className="text-sm text-gray-500">
              Return to{" "}
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
        </div>
      ) : (
        <>
          {/* No token error */}
          {!token && (
            <div className="mb-5 flex items-start gap-2.5 p-3.5 bg-red-50 border border-red-200 rounded-xl text-sm text-red-700">
              <ErrorCircleIcon className="w-5 h-5 shrink-0 mt-0.5 text-red-500" />
              <div>
                <p>{apiError}</p>
                <button
                  onClick={() => navigate("/forgot-password")}
                  className="mt-1 text-red-600 font-semibold hover:underline text-xs"
                >
                  Request a new reset link
                </button>
              </div>
            </div>
          )}

          {/* API error */}
          {apiError && token && (
            <div className="mb-5 flex items-start gap-2.5 p-3.5 bg-red-50 border border-red-200 rounded-xl text-sm text-red-700">
              <ErrorCircleIcon className="w-5 h-5 shrink-0 mt-0.5 text-red-500" />
              <div>
                <p>{apiError}</p>
                <button
                  onClick={() => navigate("/forgot-password")}
                  className="mt-1 text-red-600 font-semibold hover:underline text-xs"
                >
                  Request a new reset link
                </button>
              </div>
            </div>
          )}

          {/* Form */}
          {token && (
            <form onSubmit={handleSubmit} noValidate>
              <PasswordInput
                label="New Password"
                name="new_password"
                value={formData.new_password}
                onChange={handleChange}
                onBlur={handleBlur}
                error={errors.new_password}
                placeholder="Enter your new password"
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
                placeholder="Repeat your new password"
                required={false}
                autoComplete="new-password"
                icon={LockIcon}
              />

              <button
                type="submit"
                id="reset-password-submit-btn"
                disabled={isLoading}
                className={`
                  relative w-full mt-2 py-3 px-6 flex items-center justify-center gap-2
                  bg-gradient-to-r from-[#4B98C8] to-[#205E85]
                  text-white font-bold text-sm rounded-xl
                  shadow-md overflow-hidden transition-all duration-300
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
                    Resetting…
                  </>
                ) : (
                  "Reset Password"
                )}
              </button>

              <div className="mt-5 flex flex-col items-center gap-2.5 text-center">
                <p className="text-sm text-gray-500">
                  Return to{" "}
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
          )}
        </>
      )}
    </AuthLayout>
  );
};

export default ResetPassword;
