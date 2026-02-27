import { useState } from "react";
import { Link } from "react-router-dom";
import { forgotPassword } from "../services/authService";
import { validateEmail } from "../utils/validation";
import { SpinnerIcon, EnvelopeIcon } from "../components/icons";
import InputField from "../components/auth/InputField";
import Toast from "../components/Toast";

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
    <div className="min-h-screen w-full bg-gradient-to-br from-blue-50 via-white to-blue-50 flex items-center justify-center px-4 py-12">
      {toastMessage && (
        <Toast
          message={toastMessage}
          onClose={() => setToastMessage("")}
          duration={5000}
        />
      )}

      <div className="bg-white w-full max-w-[480px] rounded-2xl shadow-xl p-10 sm:p-12 border border-gray-100">
        {!isSuccess ? (
          <>
            {/* Header */}
            <div className="text-center mb-8">
              <h1 className="text-3xl font-extrabold text-gray-900 mb-2">
                Request Password Reset
              </h1>
              <p className="text-gray-500 text-sm leading-relaxed">
                Enter your email address and we'll send you a link to
                <br />
                reset your password
              </p>
            </div>

            {/* Form */}
            <form onSubmit={handleSubmit} noValidate>
              <InputField
                label="Email Address"
                type="email"
                name="email"
                value={email}
                onChange={handleEmailChange}
                onBlur={handleEmailBlur}
                error={touched ? emailError : ""}
                placeholder="Enter your email"
                required={false}
                autoComplete="email"
                icon={EnvelopeIcon}
              />

              {/* Submit Button */}
              <button
                type="submit"
                disabled={isLoading}
                aria-busy={isLoading}
                className={`w-full py-3.5 px-4 mt-4 bg-gradient-to-r from-[#3A9BD5] to-[#1A6FA0] text-white font-bold rounded-xl shadow-md transition-all duration-300 transform hover:-translate-y-0.5 flex items-center justify-center gap-2 ${
                  isLoading
                    ? "opacity-70 cursor-not-allowed"
                    : "hover:shadow-lg hover:from-[#3290C8] hover:to-[#175E8B]"
                }`}>
                {isLoading ? (
                  <>
                    <SpinnerIcon />
                    Sending...
                  </>
                ) : (
                  "Send Reset Link"
                )}
              </button>
            </form>

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
          </>
        ) : (
          /* Success State */
          <div className="text-center py-4" role="status" aria-live="polite">
            <div className="w-16 h-16 bg-green-500 rounded-full flex items-center justify-center text-white text-3xl mx-auto mb-6 animate-bounce">
              ✓
            </div>
            <h2 className="text-xl font-extrabold text-gray-900 mb-3">
              Check Your Email
            </h2>
            <p className="text-gray-500 text-sm leading-relaxed mb-6">
              If an account with this email exists, a password reset link has
              been sent. Please check your inbox.
            </p>
            <div className="text-center space-y-3">
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
        )}
      </div>
    </div>
  );
};

export default ForgotPassword;
