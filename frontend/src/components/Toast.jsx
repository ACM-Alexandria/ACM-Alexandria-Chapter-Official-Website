import { useEffect } from "react";
import "./Toast.css";

/**
 * Toast notification component for displaying error messages
 * @param {Object} props
 * @param {string} props.message - Message to display
 * @param {Function} props.onClose - Callback when toast closes
 * @param {number} props.duration - Auto-dismiss duration in milliseconds (default: 5000)
 */
const Toast = ({ message, onClose, duration = 5000 }) => {
  useEffect(() => {
    // Auto-dismiss after specified duration
    const timer = setTimeout(() => {
      onClose();
    }, duration);

    // Cleanup timer on unmount
    return () => clearTimeout(timer);
  }, [duration, onClose]);

  return (
    <div
      className="toast toast-error"
      role="alert"
      aria-live="assertive"
      aria-atomic="true">
      <div className="toast-content">
        <span className="toast-icon">⚠️</span>
        <span className="toast-message">{message}</span>
        <button
          className="toast-close"
          onClick={onClose}
          aria-label="Close notification">
          ×
        </button>
      </div>
    </div>
  );
};

export default Toast;
