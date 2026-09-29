/**
 * Environment Utility
 * Reads environment variables exclusively from the global `window.APP_CONFIG` object
 * (injected at runtime via public/config.js).
 */
export const getEnv = (key) => {
  return window.APP_CONFIG ? window.APP_CONFIG[key] : undefined;
};
