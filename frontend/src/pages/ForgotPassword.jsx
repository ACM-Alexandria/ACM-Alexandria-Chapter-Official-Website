import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { forgotPassword } from '../utils/api';
import { validateEmail } from '../utils/validation';
import Toast from '../components/Toast';
import './ForgotPassword.css';

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
    <div className="auth-container">
      {/* Toast notification for errors */}
      {toastMessage && (
        <Toast
          message={toastMessage}
          onClose={() => setToastMessage('')}
          duration={5000}
        />
      )}

      <div className="auth-card">
        {!isSuccess ? (
          <>
            {/* Form Header */}
            <h1>Forgot Password?</h1>
            <p className="auth-subtitle">
              Enter your email address and we'll send you a link to reset your password.
            </p>

            {/* Forgot Password Form */}
            <form onSubmit={handleSubmit} className="auth-form" noValidate>
              <div className="form-group">
                <label htmlFor="email">Email Address</label>
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
                />

                {/* Real-time validation error message */}
                {touched && emailError && (
                  <span
                    id="email-error"
                    className="error-message"
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
                className="auth-button"
                disabled={!isFormValid || isLoading}
                aria-busy={isLoading}
              >
                {isLoading ? (
                  <>
                    <span className="spinner" aria-hidden="true"></span>
                    Sending...
                  </>
                ) : (
                  'Send Reset Link'
                )}
              </button>
            </form>

            {/* Back to Login Link */}
            <div className="auth-links">
              <Link to="/login" className="back-link">
                ← Back to Login
              </Link>
            </div>
          </>
        ) : (
          <>
            {/* Success State */}
            <div className="success-message" role="status" aria-live="polite">
              <div className="success-icon" aria-hidden="true">✓</div>
              <h2>Check Your Email</h2>
              <p>
                If an account with this email exists, a password reset link has been sent.
                Please check your inbox.
              </p>
              <div className="auth-links">
                <Link to="/login" className="back-link">
                  ← Back to Login
                </Link>
              </div>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default ForgotPassword;
