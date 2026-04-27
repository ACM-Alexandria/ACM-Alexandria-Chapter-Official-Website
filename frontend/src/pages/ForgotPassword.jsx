import { useState } from "react";
import { Link } from "react-router-dom";
import { forgotPassword } from "../services/authService";
import { validateEmail } from "../utils/validation";
import { EnvelopeIcon } from "../components/icons";
import InputField from "../components/auth/InputField";
import Toast from "../components/Toast";
import AuthLayout from "../components/auth/AuthLayout";

const ForgotPassword = () => {
  const [email, setEmail] = useState("");
  const [emailError, setEmailError] = useState("");
  const [touched, setTouched] = useState(false);

  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [toastMessage, setToastMessage] = useState("");

  const handleEmailChange = (e) => {
    const value = e.target.value;
    setEmail(value);
    if (touched) {
      const validation = validateEmail(value);
      setEmailError(validation.isValid ? "" : validation.message);
    }
  };

  const handleEmailBlur = () => {
    setTouched(true);
    const validation = validateEmail(email);
    setEmailError(validation.isValid ? "" : validation.message);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    const validation = validateEmail(email);
    if (!validation.isValid) {
      setEmailError(validation.message);
      setTouched(true);
      return;
    }

    setIsLoading(true);
    setToastMessage("");

    try {
      await forgotPassword(email);
      setIsSuccess(true);
    } catch (error) {
      if (error.response && error.response.status >= 500) {
        setToastMessage("An error occurred. Please try again.");
      } else if (!error.response) {
        setToastMessage("An error occurred. Please try again.");
      } else {
        setIsSuccess(true);
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <AuthLayout
      title={!isSuccess ? "Forgot password?" : "Check your inbox"}
      subtitle={
        !isSuccess
          ? "Enter your email and we'll send you a reset link"
          : "A password reset link has been sent if an account exists"
      }
      panelTagline="Reset and get back in."
      panelSub="We'll send a secure link to your email so you can create a new password."
      activeDot={2}
    >
      {toastMessage && (
        <Toast
          message={toastMessage}
          onClose={() => setToastMessage("")}
          duration={5000}
        />
      )}

      {!isSuccess ? (
        <form onSubmit={handleSubmit} noValidate>
          <InputField
            label="Email Address"
            type="email"
            name="email"
            value={email}
            onChange={handleEmailChange}
            onBlur={handleEmailBlur}
            error={touched ? emailError : ""}
            placeholder="you@example.com"
            required={false}
            autoComplete="email"
            icon={EnvelopeIcon}
          />

          <button
            type="submit"
            id="forgot-password-submit-btn"
            disabled={isLoading}
            aria-busy={isLoading}
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
                Sending…
              </>
            ) : (
              "Send Reset Link"
            )}
          </button>

          <div className="mt-5 flex flex-col items-center gap-2.5 text-center">
            <p className="text-sm text-gray-500">
              Remember your password?{" "}
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
      ) : (
        /* Success State */
        <div
          className="text-center py-4"
          role="status"
          aria-live="polite"
          style={{ animation: "floatIn 0.5s cubic-bezier(0.22,1,0.36,1) both" }}
        >
          <div
            className="w-16 h-16 rounded-full bg-gradient-to-br from-[#4B98C8] to-[#205E85] flex items-center justify-center text-white text-2xl mx-auto mb-5 shadow-lg"
            style={{ animation: "successPop 0.5s cubic-bezier(0.22,1,0.36,1) both" }}
          >
            ✓
          </div>
          <h2 className="text-xl font-extrabold text-gray-800 mb-2 tracking-tight">Email sent!</h2>
          <p className="text-gray-500 text-sm leading-relaxed mb-6">
            If an account with this email exists, a password reset link has been sent.
            <br />
            Please check your inbox and spam folder.
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
      )}
    </AuthLayout>
  );
};

export default ForgotPassword;
