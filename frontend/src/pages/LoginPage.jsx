import LoginForm from "../components/auth/LoginForm";
import logo from "../assets/acm-logo.png";

const LoginPage = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 flex items-center justify-center px-4 py-8">
      <div className="w-full max-w-md">
        {/* Logo and Header */}
        <div className="text-center mb-8">
          <div className="flex justify-center mb-6">
            <img
              src={logo}
              alt="ACM Logo"
              className="h-32 w-32 object-contain"
            />
          </div>
          <h1 className="text-3xl font-bold text-gray-800 mb-2">
            Welcome Back
          </h1>
          <p className="text-gray-600">
            Sign in to access your ACM Alexandria account
          </p>
        </div>

        {/* Login Form Card */}
        <div className="bg-white rounded-2xl shadow-xl p-8">
          <LoginForm />
        </div>

        {/* Back to Home Link */}
        <div className="mt-6 text-center">
          <a
            href="/"
            className="text-sm text-gray-600 hover:text-gray-800 hover:underline transition-colors duration-200"
          >
            ← Back to Home
          </a>
        </div>
      </div>
    </div>
  );
};

export default LoginPage;