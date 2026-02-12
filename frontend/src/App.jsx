import { Routes, Route, Navigate } from "react-router-dom";
import ResetPassword from "./pages/ResetPassword";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import ForgotPassword from "./pages/ForgotPassword";

/**
 * Main App component with routing configuration
 */
function App() {
  return (
    <Routes>
      <Route path="/" element={<HomePage />} />
      <Route path="/login" element={<LoginPage />} />
      <Route path="/forgot-password" element={<ForgotPassword />} />
      <Route path="/reset-password" element={<ResetPassword />} />
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

export default App;
