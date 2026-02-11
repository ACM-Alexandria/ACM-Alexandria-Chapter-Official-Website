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
 * Validates email field
 */
export const validateEmail = (email) => {
  if (!email || email.trim().length === 0) {
    return { isValid: false, message: "Email is required" };
  }

  if (!isValidEmail(email)) {
    return { isValid: false, message: "Please enter a valid email address" };
  }

  return { isValid: true, message: "" };
};
