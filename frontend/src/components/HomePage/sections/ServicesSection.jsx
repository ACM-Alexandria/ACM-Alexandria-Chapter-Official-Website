const ServicesSection = () => {
  return (
    <section
      id="services"
      className="w-full py-24 px-6 bg-gray-50/50 relative overflow-hidden"
    >
      
      <div className="max-w-7xl mx-auto w-full relative z-10">
        {/* Header Section */}
        <div className="text-center mb-16">
          <h2 
            className="text-4xl md:text-5xl font-extrabold text-slate-900 tracking-tight"
            data-aos="fade-up"
          >
            Premium <span className="text-[#205E85]">Solutions</span>
          </h2>
        </div>

        <div className="max-w-3xl mx-auto" data-aos="fade-up" data-aos-delay="200">
          <div className="bg-white p-12 md:p-20 rounded-[3rem] shadow-2xl shadow-slate-200/50 border-t-4 border-t-[#4B98C8]/50 border-x border-b border-slate-100 text-center relative overflow-hidden group">
            
            <div className="relative z-10">
              <div className="w-20 h-20 bg-slate-50 rounded-3xl flex items-center justify-center mx-auto mb-8 transition-transform duration-500 group-hover:scale-110 group-hover:rotate-3">
                <svg xmlns="http://www.w3.org/2000/svg" className="h-10 w-10 text-[#4B98C8]" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 10V3L4 14h7v7l9-11h-7z" />
                </svg>
              </div>

              <h4 className="text-3xl font-black text-slate-900 tracking-tight mb-4">Coming Soon</h4>
              <p className="text-slate-500 text-lg font-medium mb-10 max-w-sm mx-auto">
                We're currently building something extraordinary for our members. Stay tuned for professional services and technical solutions.
              </p>

              {/* Enhanced Loading Animation */}
              <div className="flex justify-center items-center gap-3 h-4">
                <div 
                  className="w-2.5 h-2.5 bg-[#4B98C8] rounded-full animate-bounce"
                  style={{ animationDelay: "0s" }}
                />
                <div 
                  className="w-2.5 h-2.5 bg-[#4B98C8]/60 rounded-full animate-bounce"
                  style={{ animationDelay: "0.2s" }}
                />
                <div 
                  className="w-2.5 h-2.5 bg-[#4B98C8]/30 rounded-full animate-bounce"
                  style={{ animationDelay: "0.4s" }}
                />
              </div>
            </div>
          </div>
        </div>
      </div>
    </section>
  );
};

export default ServicesSection;
