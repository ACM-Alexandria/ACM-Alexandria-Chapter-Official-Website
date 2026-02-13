import { useState } from "react";
import { Link } from "react-router-dom";
import LoginForm from "../components/auth/LoginForm";

const LoginPage = () => {
  const [view, setView] = useState("selection");

  return (
    <div className="min-h-screen w-full bg-gradient-to-br from-blue-50 via-white to-blue-50 flex items-center justify-center px-4 py-12">
      <div className="bg-white w-full max-w-[480px] rounded-2xl shadow-xl p-10 sm:p-12 border border-gray-100 transition-all duration-300">
        {view === "selection" && (
          <div className="animate-fadeIn">
            <div className="text-center mb-8">
              <h1 className="text-3xl font-extrabold text-gray-900 mb-2">
                Log In
              </h1>
              <p className="text-gray-500 text-sm leading-relaxed">
                Select your account type to continue
              </p>
            </div>

            <div className="space-y-4">
              <button
                onClick={() => setView("Member")}
                className="w-full py-4 px-6 bg-gradient-to-r from-[#3A9BD5] to-[#1A6FA0] text-white font-bold rounded-xl shadow-md transition-all duration-300 transform hover:-translate-y-0.5 hover:shadow-lg hover:from-[#3290C8] hover:to-[#175E8B]">
                Log in as Member
              </button>

              {/* Admin Selection Button (Dark Theme) */}
              <button
                onClick={() => setView("Admin")}
                className="w-full py-4 px-6 bg-white text-black border border-[#0F172A] font-bold rounded-xl shadow-md transition-all duration-300 transform hover:-translate-y-0.5 hover:shadow-lg hover:bg-white/90">
                Log in as Admin
              </button>
            </div>

            <div className="mt-8 text-center">
              <Link
                to="/"
                className="inline-flex items-center text-sm text-gray-400 hover:text-gray-600 transition-colors">
                <span className="mr-1 text-lg">‹</span> Back to the main page
              </Link>
            </div>
          </div>
        )}
        {view !== "selection" && (
          <div className="animate-fadeIn">
            {/* Dynamic Header */}
            <div className="text-center mb-8">
              <h1 className="text-3xl font-extrabold text-gray-900 mb-2">
                {view} Log In
              </h1>
              <p className="text-gray-500 text-sm leading-relaxed">
                Welcome back!
                <br />
                Please enter your credentials to log in
              </p>
            </div>
            <LoginForm
              loginType={view}
              onBack={() => setView("selection")}
              onSwitchToMember={() => setView("Member")}
            />
          </div>
        )}
      </div>
    </div>
  );
};

export default LoginPage;
