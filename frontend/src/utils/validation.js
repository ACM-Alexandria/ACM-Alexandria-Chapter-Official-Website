/**
 * Validation utility functions
 */

/**
 * Validates email format
 */
export const isValidEmail = (email) => {
  if (!email) return false;
  const emailRegex = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;
  return emailRegex.test(email);
};

/**
 * Validates if a field is not empty
 */
export const isNotEmpty = (value) => {
  return value && value.trim().length > 0;
};

/**
 * Validates password strength
 */
export const validatePassword = (password) => {
  if (!password || password.length === 0) {
    return { isValid: false, message: "Password is required" };
  }

  if (password.length < 8) {
    return { isValid: false, message: "Password must be at least 8 characters" };
  }

  return { isValid: true, message: "" };
};

/**
 * Validates that password and confirmation match
 */
export const validatePasswordMatch = (password, passwordConfirmation) => {
  if (!passwordConfirmation || passwordConfirmation.length === 0) {
    return { isValid: false, message: "Password confirmation is required" };
  }

  if (password !== passwordConfirmation) {
    return { isValid: false, message: "Passwords do not match" };
  }

  return { isValid: true, message: "" };
};

/**
 * Validates email field - returns { isValid, error, message }
 * Both 'error' and 'message' contain the same value for compatibility
 * with ForgotPassword (.error) and LoginForm (.message)
 * @param {string} email - Email address to validate
 * @returns {Object} - { isValid: boolean, error: string, message: string }
 */
export const validateEmail = (email) => {
  if (!email || email.trim().length === 0) {
    return { isValid: false, error: "Email is required", message: "Email is required" };
  }

  if (!isValidEmail(email)) {
    return { isValid: false, error: "Please enter a valid email address", message: "Please enter a valid email address" };
  }

  return { isValid: true, error: "", message: "" };
};

