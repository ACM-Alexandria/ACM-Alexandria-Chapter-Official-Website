/**
 * Validation utility functions
 * Shared across all forms (login, register, forgot-password, reset-password).
 */

const EMAIL_REGEX = /^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$/;

export const isValidEmail = (email) => {
  if (!email) return false;
  return EMAIL_REGEX.test(email);
};

export const isNotEmpty = (value) => {
  return value && value.trim().length > 0;
};

/**
 * Validate email — returns { isValid, error, message }.
 * Both `error` and `message` carry the same string for compatibility.
 */
export const validateEmail = (email) => {
  if (!email || email.trim().length === 0) {
    return {
      isValid: false,
      error: "Email is required",
      message: "Email is required",
    };
  }
  if (!isValidEmail(email)) {
    return {
      isValid: false,
      error: "Please enter a valid email address",
      message: "Please enter a valid email address",
    };
  }
  return { isValid: true, error: "", message: "" };
};

/**
 * Password rules — matches backend RegisterDTO validation.
 * Each rule has a label (for UI badges) and a test function.
 */
export const PASSWORD_RULES = [
  { label: "8+ characters", test: (p) => p.length >= 8 },
  { label: "Uppercase", test: (p) => /[A-Z]/.test(p) },
  { label: "Lowercase", test: (p) => /[a-z]/.test(p) },
  { label: "Number", test: (p) => /\d/.test(p) },
  {
    label: "Special character",
    test: (p) => /[!@#$%^&*(),.?":{}|<>]/.test(p),
  },
];

/**
 * Returns per-rule results: [{ label, passed }]
 * Useful for rendering individual requirement badges in the UI.
 */
export const getPasswordRules = (password) =>
  PASSWORD_RULES.map((rule) => ({
    label: rule.label,
    passed: rule.test(password),
  }));

/**
 * Validate password strength — matches backend RegisterDTO rules:
 *   >= 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special character.
 */
export const validatePassword = (password) => {
  if (!password || password.length === 0) {
    return { isValid: false, message: "Password is required" };
  }

  const failing = PASSWORD_RULES.filter((r) => !r.test(password)).map((r) =>
    r.label.toLowerCase(),
  );

  if (failing.length > 0) {
    return {
      isValid: false,
      message: `Password must contain: ${failing.join(", ")}`,
    };
  }

  return { isValid: true, message: "" };
};

export const validatePasswordMatch = (password, passwordConfirmation) => {
  if (!passwordConfirmation || passwordConfirmation.length === 0) {
    return { isValid: false, message: "Password confirmation is required" };
  }
  if (password !== passwordConfirmation) {
    return { isValid: false, message: "Passwords do not match" };
  }
  return { isValid: true, message: "" };
};
