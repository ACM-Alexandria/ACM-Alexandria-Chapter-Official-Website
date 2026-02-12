import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { forgotPassword } from '../utils/api';
import { validateEmail } from '../utils/validation';
import Toast from '../components/Toast';
import logo from '../assets/acm-logo.png';

/**
 * Forgot Password page component
 * Allows users to request a password reset link via email
 * Includes real-time validation, accessibility features, and error handling
 */
const ForgotPassword = () => {
  // Form state
  const [email, setEmail] = useState('');
  const [emailError, setEmailError] = useState('');
  const [touched, setTouched] = useState(false);

  // UI state
  const [isLoading, setIsLoading] = useState(false);
  const [isSuccess, setIsSuccess] = useState(false);
  const [toastMessage, setToastMessage] = useState('');

  /**
   * Validates email field and updates error state
   * @param {string} value - Email value to validate
   */
  const handleEmailValidation = (value) => {
    const validation = validateEmail(value);
    setEmailError(validation.error);
  };

  /**
   * Handles email input change with real-time validation
   */
  const handleEmailChange = (e) => {
    const value = e.target.value;
    setEmail(value);

    // Only show errors after user has touched the field
    if (touched) {
      handleEmailValidation(value);
    }
  };

  /**
   * Handles email input blur event
   * Marks field as touched and validates
   */
  const handleEmailBlur = () => {
    setTouched(true);
    handleEmailValidation(email);
  };

  /**
   * Handles form submission
   * Sends password reset request to API
   */
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Final validation check
    const validation = validateEmail(email);
    if (!validation.isValid) {
      setEmailError(validation.error);
      setTouched(true);
      return;
    }

    setIsLoading(true);
    setToastMessage(''); // Clear any existing toast

    try {
      // Call forgot password API
      await forgotPassword(email);

      // Show success state (regardless of whether email exists - security best practice)
      setIsSuccess(true);
    } catch (error) {
      // Handle server errors (5xx) or network errors
      if (error.response && error.response.status >= 500) {
        setToastMessage('An error occurred. Please try again.');
      } else if (!error.response) {
        // Network error
        setToastMessage('An error occurred. Please try again.');
      } else {
        // For other errors, still show generic message to prevent user enumeration
        setIsSuccess(true);
      }
    } finally {
      setIsLoading(false);
    }
  };

  // Check if form is valid
  const isFormValid = validateEmail(email).isValid;

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 flex items-center justify-center px-4 py-8">
      {/* Toast notification for errors */}
      {toastMessage && (
        <Toast
          message={toastMessage}
          onClose={() => setToastMessage('')}
          duration={5000}
        />
      )}

      <div className="w-full max-w-md">
        {/* Logo and Header */}
        <div className="text-center mb-8">
          <div className="flex justify-center mb-6">
            <img
              src={logo}
              alt="ACM Logo"
              className="h-32 w-32 object-contain"
            />
          </div>
        </div>

        {/* Card */}
        <div className="bg-white rounded-2xl shadow-xl p-8">
          {!isSuccess ? (
            <>
              {/* Form Header */}
              <h1 className="text-2xl font-bold text-gray-800 mb-2 text-center">
                Forgot Password?
              </h1>
              <p className="text-gray-600 text-center mb-6 text-sm">
                Enter your email address and we'll send you a link to reset your password.
              </p>

              {/* Forgot Password Form */}
              <form onSubmit={handleSubmit} noValidate>
                <div className="mb-4">
                  <label
                    htmlFor="email"
                    className="block text-sm font-medium text-gray-700 mb-1"
                  >
                    Email Address
                  </label>
                  <input
                    type="email"
                    id="email"
                    value={email}
                    onChange={handleEmailChange}
                    onBlur={handleEmailBlur}
                    placeholder="your.email@example.com"
                    disabled={isLoading}
                    aria-required="true"
                    aria-invalid={emailError ? 'true' : 'false'}
                    aria-describedby={emailError ? 'email-error' : undefined}
                    className={`w-full px-4 py-3 border rounded-lg text-gray-800 placeholder-gray-400 focus:outline-none focus:ring-2 transition-all duration-200 disabled:opacity-60 disabled:cursor-not-allowed ${touched && emailError
                        ? 'border-red-400 focus:ring-red-200 focus:border-red-400'
                        : 'border-gray-300 focus:ring-blue-200 focus:border-blue-400'
                      }`}
                  />

                  {/* Real-time validation error message */}
                  {touched && emailError && (
                    <span
                      id="email-error"
                      className="text-red-500 text-xs mt-1 block"
                      role="alert"
                      aria-live="polite"
                    >
                      {emailError}
                    </span>
                  )}
                </div>

                {/* Submit Button */}
                <button
                  type="submit"
                  disabled={!isFormValid || isLoading}
                  aria-busy={isLoading}
                  className={`w-full py-3 px-4 bg-gradient-to-r from-[#4B98C8] to-[#205E85] text-white font-semibold rounded-lg transition-all duration-300 mt-2 flex items-center justify-center gap-2 ${!isFormValid || isLoading
                      ? 'opacity-50 cursor-not-allowed'
                      : 'hover:opacity-90 hover:shadow-lg'
                    }`}
                >
                  {isLoading ? (
                    <>
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
                      Sending...
                    </>
                  ) : (
                    'Send Reset Link'
                  )}
                </button>
              </form>

              {/* Back to Login Link */}
              <div className="mt-6 text-center">
                <Link
                  to="/login"
                  className="text-sm text-gray-600 hover:text-gray-800 hover:underline transition-colors duration-200"
                >
                  ← Back to Login
                </Link>
              </div>
            </>
          ) : (
            <>
              {/* Success State */}
              <div className="text-center py-4" role="status" aria-live="polite">
                <div className="w-16 h-16 bg-green-500 rounded-full flex items-center justify-center text-white text-3xl mx-auto mb-6 animate-bounce">
                  ✓
                </div>
                <h2 className="text-xl font-bold text-gray-800 mb-3">
                  Check Your Email
                </h2>
                <p className="text-gray-600 text-sm leading-relaxed mb-6">
                  If an account with this email exists, a password reset link has been sent.
                  Please check your inbox.
                </p>
                <div className="mt-4">
                  <Link
                    to="/login"
                    className="text-sm text-gray-600 hover:text-gray-800 hover:underline transition-colors duration-200"
                  >
                    ← Back to Login
                  </Link>
                </div>
              </div>
            </>
          )}
        </div>

        {/* Back to Home Link */}
        <div className="mt-6 text-center">
          <a
            href="/"
            className="text-sm text-gray-600 hover:text-gray-800 hover:underline transition-colors duration-200"
          >
            ← Back to Home
          </a>
        </div>
      </div>
    </div>
  );
};

export default ForgotPassword;
