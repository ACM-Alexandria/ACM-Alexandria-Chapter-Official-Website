import { Navigate } from "react-router-dom";
import { useAuth } from "../../contexts/AuthContext";

/**
 * A wrapper component to guard routes that require authentication.
 * Displays a loading spinner while hydration takes place to avoid race condition redirects.
 */
const ProtectedRoute = ({ children }) => {
  const { isAuthenticated, isLoading } = useAuth();

  // Prevent flickering redirects while auth system is verifying user tokens
  if (isLoading) {
    return (
      <div className="min-h-screen bg-[#f8fafc] flex items-center justify-center">
        <div className="w-12 h-12 rounded-full border-4 border-slate-200 border-t-[#4B98C8] animate-spin" />
      </div>
    );
  }

  // If user is not authenticated, safely redirect them back to the login gate
  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  // Render protected components
  return children;
};

export default ProtectedRoute;
