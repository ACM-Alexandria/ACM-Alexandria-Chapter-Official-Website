import { lazy, Suspense } from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import ResetPassword from "./pages/ResetPassword";
import HomePage from "./pages/HomePage";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import EventsPage from "./pages/EventsPage";
import ProgramsPage from "./pages/ProgramsPage";
import ForgotPassword from "./pages/ForgotPassword";
import RadioPage from "./pages/RadioPage";
import SeasonDetailPage from "./pages/SeasonDetailPage";
import ProtectedRoute from "./components/auth/ProtectedRoute";
import AdminProtectedRoute from "./components/auth/AdminProtectedRoute";
import EmailConfirmationPage from "./pages/EmailConfirmationPage";
import RulesPage from "./pages/RulesPage";
import BoardsPage from "./pages/BoardsPage";
import FAQPage from "./pages/FAQPage";
import BadgePage from "./pages/BadgePage";
import EventPage from "./pages/EventPage";
import ProgramPage from "./pages/ProgramPage";

// Member and admin pages load on first visit, so the public pages (what search engines and new visitors open)
// don't download the dashboards and the rich-text editor.
const UserProfilePage = lazy(() => import("./pages/UserProfilePage"));
const AdminPage = lazy(() => import("./pages/AdminPage"));
const AdminUserProfileView = lazy(() => import("./pages/AdminUserProfileView"));
const HRDashboardPage = lazy(() => import("./pages/HRDashboardPage"));

/**
 * Main App component with routing configuration
 */
function App() {
  return (
    <Suspense fallback={<div className="min-h-screen bg-ocean-31 dark:bg-ocean-911" aria-busy="true" />}>
      <Routes>
        <Route path="/" element={<HomePage />} />
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<RegisterPage />} />
        <Route path="/forgot-password" element={<ForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/events" element={<EventsPage />} />
        <Route path="/events/:slug" element={<EventPage />} />
        <Route path="/programs" element={<ProgramsPage />} />
        <Route path="/programs/:slug" element={<ProgramPage />} />
        <Route path="/radio" element={<RadioPage />} />
        <Route path="/radio/seasons/:id" element={<SeasonDetailPage />} />
        <Route path="/rules" element={<RulesPage />} />
        <Route path="/boards" element={<BoardsPage />} />
        <Route path="/faq" element={<FAQPage />} />
        <Route path="/badges/:publicId" element={<BadgePage />} />
        <Route path="/email-confirmation/:token" element={<EmailConfirmationPage />} />
        <Route
          path="/profile"
          element={
            <ProtectedRoute>
              <UserProfilePage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/admin"
          element={
            <AdminProtectedRoute>
              <AdminPage />
            </AdminProtectedRoute>
          }
        />
        <Route
          path="/admin/user-profile"
          element={
            <AdminProtectedRoute>
              <AdminUserProfileView />
            </AdminProtectedRoute>
          }
        />
        <Route
          path="/hr-dashboard"
          element={
            <ProtectedRoute>
              <HRDashboardPage />
            </ProtectedRoute>
          }
        />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Suspense>
  );
}

export default App;
