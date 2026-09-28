import { useEffect, useState } from "react";
import { useParams, Link } from "react-router-dom";
import { confirmEmail, resendConfirmationEmail } from "../services/authService";
import { validateEmail } from "../utils/validation";
import AuthLayout from "../components/auth/AuthLayout";
import InputField from "../components/auth/InputField";
import { ErrorCircleIcon, EnvelopeIcon } from "../components/icons";

const EmailConfirmationPage = () => {
  const { token } = useParams();

  const [isLoading, setIsLoading] = useState(true);
  const [isSuccess, setIsSuccess] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  const [resendEmail, setResendEmail] = useState("");
  const [resendEmailError, setResendEmailError] = useState("");
  const [resendLoading, setResendLoading] = useState(false);
  const [resendSuccess, setResendSuccess] = useState(false);

  const [isAlreadyConfirmed, setIsAlreadyConfirmed] = useState(false);

  useEffect(() => {
    let isMounted = true;

    const performConfirmation = async () => {
      if (!token) {
        setErrorMessage("Missing or invalid confirmation link.");
        setIsLoading(false);
        return;
      }

      try {
        await confirmEmail(token);
        if (isMounted) {
          setIsSuccess(true);
        }
      } catch (error) {
        if (isMounted) {
          setIsAlreadyConfirmed(error.status === 409);
          setErrorMessage(
            error.status === 409
              ? "This email has already been confirmed. You can sign in."
              : error.message || "This confirmation link is invalid or expired.",
          );
        }
      }finally {
        if (isMounted) {
          setIsLoading(false);
        }
      }
    };

    performConfirmation();

    return () => {
      isMounted = false;
    };
  }, [token]);

  const handleResend = async (e) => {
    e.preventDefault();
    const emailValidation = validateEmail(resendEmail);
    if (!emailValidation.isValid) {
      setResendEmailError(emailValidation.message);
      return;
    }

    setResendEmailError("");
    setResendLoading(true);

    try {
      await resendConfirmationEmail(resendEmail);
      setResendSuccess(true);
    } catch (error) {
      setResendEmailError(error.message || "Failed to resend confirmation link.");
    } finally {
      setResendLoading(false);
    }
  };

  return (
    <AuthLayout
      title={
        isLoading
          ? "Verifying Email..."
          : isSuccess
          ? "Email Confirmed!"
          : isAlreadyConfirmed
          ? "Email Already Confirmed"
          : "Verification Failed"
      }
      subtitle={
        isLoading
          ? "Please wait while we confirm your email address"
          : isSuccess
          ? "Your email has been verified. You can now log in."
          : isAlreadyConfirmed
          ? "This email is already confirmed."
          : "We were unable to confirm your email address."
      }
      panelTagline="One step closer to the chapter."
      panelSub="Confirming your email ensures the security of your ACM Alexandria account and grants access to member events."
      activeDot={1}
    >
      {/* 1. Loading State */}
      {isLoading && (
        <div className="py-10 flex flex-col items-center justify-center gap-4 text-center">
          <span
            aria-hidden="true"
            className="w-10 h-10 rounded-full border-4 border-[#4B98C8]/30 border-t-[#205E85] shrink-0"
            style={{ animation: "spin 0.8s linear infinite" }}
          />
          <p className="text-sm font-medium text-gray-600 dark:text-gray-300">
            Validating confirmation token...
          </p>
        </div>
      )}

      {/* 2. Success State */}
      {!isLoading && isSuccess && (
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
          <h2 className="text-xl font-extrabold text-gray-800 dark:text-gray-100 mb-2 tracking-tight">
            Account Activated!
          </h2>
          <p className="text-gray-500 dark:text-gray-300 text-sm leading-relaxed mb-6">
            Your email has been successfully confirmed.
            <br />
            You can now sign in with your account credentials.
          </p>

          <Link
            to="/login"
            className="
              inline-block w-full py-3 px-6 text-center
              bg-gradient-to-r from-[#4B98C8] to-[#205E85]
              text-white font-bold text-sm rounded-xl shadow-md
              hover:-translate-y-0.5 hover:shadow-lg transition-all duration-300
            "
          >
            Sign In Now
          </Link>
        </div>
      )}

      {/* 3. Error State with Resend Form */}
      {!isLoading && !isSuccess && (
        <div className="space-y-5" style={{ animation: "floatIn 0.5s cubic-bezier(0.22,1,0.36,1) both" }}>
          <div
            className="flex items-start gap-2.5 p-3.5 bg-red-50 dark:bg-red-950/40 border border-red-200 dark:border-red-800 rounded-xl text-sm text-red-700 dark:text-red-200"
            role="alert"
          >
            <ErrorCircleIcon className="w-5 h-5 shrink-0 mt-0.5 text-red-500" />
            <p>{errorMessage}</p>
          </div>

          {isAlreadyConfirmed ? (
            <Link
              to="/login"
              className="
                inline-block w-full py-3 px-6 text-center
                bg-gradient-to-r from-[#4B98C8] to-[#205E85]
                text-white font-bold text-sm rounded-xl shadow-md
                hover:-translate-y-0.5 hover:shadow-lg transition-all duration-300
              "
            >
              Sign In
            </Link>
          ) : resendSuccess ? (
            <div className="p-4 bg-green-50 dark:bg-green-950/40 border border-green-200 dark:border-green-800 rounded-xl text-sm text-green-700 dark:text-green-200 text-center">
              <p className="font-semibold mb-1">New confirmation link sent!</p>
              <p className="text-xs text-green-600 dark:text-green-300">
                If an unconfirmed account exists for {resendEmail}, a fresh confirmation link has been sent to your inbox.
              </p>
            </div>
          ) : (
            <form onSubmit={handleResend} noValidate className="space-y-4">
              <p className="text-xs text-gray-500 dark:text-gray-400">
                Need a new confirmation link? Enter your email address below:
              </p>

              <InputField
                label="Email Address"
                type="email"
                name="resendEmail"
                value={resendEmail}
                onChange={(e) => {
                  setResendEmail(e.target.value);
                  if (resendEmailError) setResendEmailError("");
                }}
                error={resendEmailError}
                placeholder="you@example.com"
                required={true}
                autoComplete="email"
                icon={EnvelopeIcon}
              />

              <button
                type="submit"
                disabled={resendLoading}
                className={`
                  relative w-full py-3 px-6 flex items-center justify-center gap-2
                  bg-gradient-to-r from-[#4B98C8] to-[#205E85]
                  text-white font-bold text-sm rounded-xl
                  shadow-md overflow-hidden transition-all duration-300
                  ${resendLoading ? "opacity-70 cursor-not-allowed" : "hover:-translate-y-0.5 hover:shadow-lg"}
                `}
              >
                {resendLoading ? "Sending..." : "Resend Confirmation Link"}
              </button>
            </form>
          )}

          <div className="pt-2 flex flex-col items-center gap-2.5 text-center">
            <Link
              to="/login"
              className="text-sm font-semibold text-[#205E85] dark:text-blue-300 hover:text-[#4B98C8] transition-colors"
            >
              Back to Sign In
            </Link>
            <Link
              to="/"
              className="inline-flex items-center gap-1 text-xs text-gray-400 dark:text-gray-300 hover:text-gray-600 dark:hover:text-gray-100 transition-colors"
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

export default EmailConfirmationPage;