import RegisterForm from "../components/auth/RegisterForm";
import AuthLayout from "../components/auth/AuthLayout";

const RegisterPage = () => {
  return (
    <AuthLayout
      title="Join the Chapter"
      subtitle="Create your account to participate in workshops and exclusive events"
      panelTagline="Be part of something bigger."
      panelSub="Register today and gain access to exclusive events, workshops, and a vibrant tech community."
      activeDot={1}
      isReversed={true}
    >
      <RegisterForm />
    </AuthLayout>
  );
};

export default RegisterPage;
