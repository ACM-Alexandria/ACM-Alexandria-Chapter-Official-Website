import LoginForm from "../components/auth/LoginForm";

const LoginPage = () => {
  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 via-white to-blue-50 flex items-center justify-center px-4 py-8">
      {/* Main Card - Increased padding to p-10 or p-12 */}
      <div className="bg-white w-full max-w-[450px] rounded-2xl shadow-xl p-8 sm:p-12 border border-gray-100">
        {/* Header Section */}
        <div className="text-center mb-10">
          <h1 className="text-3xl font-extrabold text-gray-900 mb-3">
            Member Log In
          </h1>
          <p className="text-gray-500 text-sm leading-relaxed">
            Welcome back!
            <br />
            Please enter your credentials to log in
          </p>
        </div>

        {/* Form Section */}
        <LoginForm />
      </div>
    </div>
  );
};

export default LoginPage;
