import { Link } from "react-router-dom";
import LoginForm from "../components/auth/LoginForm";

const LoginPage = () => {
  return (
    <div className="min-h-screen w-full bg-gradient-to-br from-blue-50 via-white to-blue-50 flex items-center justify-center px-4 py-12">
      <div className="bg-white w-full max-w-[480px] rounded-2xl shadow-xl p-10 sm:p-12 border border-gray-100 transition-all duration-300">
        <div className="animate-fadeIn">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-extrabold text-gray-900 mb-2">
              Log In
            </h1>
            <p className="text-gray-500 text-sm leading-relaxed">
              Welcome back!
              <br />
              Please enter your credentials to log in
            </p>
          </div>
          <LoginForm />
        </div>
      </div>
    </div>
  );
};

export default LoginPage;

