/**
 * Environment Utility
 * Reads environment variables from the global `window.APP_CONFIG` object (injected at runtime via public/config.js),
 * falling back to Vite build-time `import.meta.env` variables if window.APP_CONFIG is undefined.
 */
export const getEnv = (key) => {
  if (window.APP_CONFIG && window.APP_CONFIG[key] !== undefined) {
    return window.APP_CONFIG[key];
  }
  return import.meta.env[key];
};
