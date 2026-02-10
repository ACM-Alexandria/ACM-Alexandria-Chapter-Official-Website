import logo from "../../../assets/logo/acm-logo-no-bg.png";

const GreetingSection = () => {
  return (
    <section
      id="greeting"
      className="w-full py-24 px-10 bg-gray-50 h-96 flex items-center justify-center"
    >
      <div className="max-w-6xl mx-auto flex items-center justify-center gap-12">
        {/* Logo */}
        <img
          src={logo}
          alt="ACM Logo"
          className="h-72 w-72 object-contain"
          data-aos="zoom-in"
        />

        {/* Text Content */}
        <div className="text-center" data-aos="flip-up">
          <h1 className="text-6xl font-bold text-gray-800 mb-2">Welcome to</h1>
          <h1 className="text-6xl font-bold text-gray-800 mb-6">
            ACM Alexandria
          </h1>
          <p className="text-xl text-gray-600 max-w-md mx-auto">
            Discover the world of technology, innovation, and community
          </p>
        </div>
      </div>
    </section>
  );
};

export default GreetingSection;
