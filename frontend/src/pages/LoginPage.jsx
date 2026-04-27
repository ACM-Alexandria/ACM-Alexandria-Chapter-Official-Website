import LoginForm from "../components/auth/LoginForm";
import AuthLayout from "../components/auth/AuthLayout";

const LoginPage = () => {
  return (
    <AuthLayout
      title="Welcome Back"
      subtitle="Sign in to your ACM account to continue"
      panelTagline="Your community awaits, let's go."
      panelSub="Connect, learn, and grow with the ACM Alexandria Chapter — your hub for tech events, workshops, and community."
      activeDot={0}
    >
      <LoginForm />
    </AuthLayout>
  );
};

export default LoginPage;
