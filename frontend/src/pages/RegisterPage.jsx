import RegisterForm from "../components/auth/RegisterForm";

const RegisterPage = () => {
  return (
    <div className="min-h-screen w-full bg-gradient-to-br from-blue-50 via-white to-blue-50 flex items-center justify-center px-4 py-12">
      <div className="bg-white w-full max-w-[480px] rounded-2xl shadow-xl p-10 sm:p-12 border border-gray-100">
        {/* Header Section */}
        <div className="text-center mb-8">
          <h1 className="text-3xl font-extrabold text-gray-900 mb-2">
            Create Member Account
          </h1>
          <p className="text-gray-500 text-sm leading-relaxed">
            Join us today!
            <br />
            Our Admin will approve your account shortly
          </p>
        </div>

        {/* Form Section */}
        <RegisterForm />
      </div>
    </div>
  );
};

export default RegisterPage;
