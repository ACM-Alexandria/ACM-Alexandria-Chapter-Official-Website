const ServicesSection = () => {
  return (
    <section
      id="services"
      className="w-full py-20 px-10 flex items-center bg-gray-50"
    >
      <div className="max-w-6xl mx-auto w-full">
        {/* Header Section */}
        <div className="text-center py-12" data-aos="fade-up">
          <h2
            className="text-4xl font-bold text-gray-800 mb-6"
            data-aos="fade-down"
          >
            Our Services
          </h2>
          <div
            className="w-20 h-1 bg-gradient-to-r from-[#4B98C8] to-[#205E85] mx-auto mb-12"
            data-aos="fade-up"
          ></div>

          {/* Coming Soon Content */}

          <p className="text-2xl font-bold text-gray-800">Coming Soon</p>
          <div data-aos="fade-up" data-aos-delay="100">
            {/* Loading Animation */}
            <div className="flex justify-center items-center gap-2 h-8 my-5">
              <span
                className="w-3 h-3 bg-gradient-to-r from-[#4B98C8] to-[#205E85] rounded-full animate-bounce"
                style={{ animationDelay: "0s" }}
              ></span>
              <span
                className="w-3 h-3 bg-gradient-to-r from-[#4B98C8] to-[#205E85] rounded-full animate-bounce"
                style={{ animationDelay: "0.2s" }}
              ></span>
              <span
                className="w-3 h-3 bg-gradient-to-r from-[#4B98C8] to-[#205E85] rounded-full animate-bounce"
                style={{ animationDelay: "0.4s" }}
              ></span>
            </div>

            <p className="text-gray-500">Something amazing is on the way...</p>
          </div>
        </div>
      </div>
    </section>
  );
};

export default ServicesSection;
