import logo from "../../../assets/logo/acm-logo-no-bg.png";

const GreetingSection = () => {
  const tagline = "Discover the world of technology, innovation, and community";

  return (
    <section
      id="greeting"
      className="w-full py-24 md:py-36 px-4 bg-white relative overflow-hidden flex items-center justify-center min-h-[80vh]"
    >
      {/* Background Blobs */}
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -top-24 -left-24 w-[500px] h-[500px] rounded-full bg-[#4B98C8]/10 blur-3xl animate-[drift_20s_ease-in-out_infinite]"
      />
      <div
        aria-hidden="true"
        className="pointer-events-none absolute -bottom-24 -right-24 w-[400px] h-[400px] rounded-full bg-[#205E85]/10 blur-3xl animate-[drift-r_25s_ease-in-out_infinite]"
      />

      <div className="max-w-7xl mx-auto flex flex-col md:flex-row items-center justify-center gap-12 md:gap-20 relative z-10">
        {/* Logo Container */}
        <div className="relative group" style={{ animation: "floatIn 1s cubic-bezier(0.22,1,0.36,1) both" }}>
          <div className="absolute -inset-4 bg-gradient-to-tr from-[#4B98C8]/20 to-[#205E85]/20 rounded-full blur-2xl opacity-0 group-hover:opacity-100 transition-opacity duration-700" />
          <img
            src={logo}
            alt="ACM Logo"
            className="h-64 w-64 md:h-80 md:w-80 object-contain relative transition-transform duration-700 group-hover:scale-105"
          />
        </div>

        {/* Text Content */}
        <div className="text-center md:text-left max-w-2xl">
          <h2 
            className="text-lg md:text-xl font-bold text-[#4B98C8] uppercase tracking-[0.2em] mb-4"
            style={{ animation: "slideLeft 0.8s cubic-bezier(0.22,1,0.36,1) 0.2s both" }}
          >
            Welcome to
          </h2>
          <h1 
            className="text-5xl md:text-7xl font-extrabold text-slate-900 tracking-tight mb-8 leading-[1.1]"
            style={{ animation: "slideLeft 0.8s cubic-bezier(0.22,1,0.36,1) 0.4s both" }}
          >
            ACM <span className="text-transparent bg-clip-text bg-gradient-to-r from-[#4B98C8] to-[#205E85]">Alexandria</span>
          </h1>
          
          <div className="text-xl md:text-2xl text-slate-500 font-medium leading-relaxed mb-10">
            {tagline.split(' ').map((word, i) => (
              <span
                key={i}
                className="inline-block mr-[0.25em]"
                style={{
                  animation: `revealWord 0.5s cubic-bezier(0.22,1,0.36,1) ${0.8 + i * 0.08}s both`
                }}
              >
                {word}
              </span>
            ))}
          </div>

          <div className="flex flex-wrap items-center justify-center md:justify-start gap-4" style={{ animation: "fadeIn 1s ease 1.8s both" }}>
            <button
              onClick={() => document.getElementById('about').scrollIntoView({ behavior: 'smooth' })}
              className="px-8 py-4 bg-gradient-to-r from-[#4B98C8] to-[#205E85] text-white font-bold rounded-xl shadow-lg shadow-blue-200/50 hover:-translate-y-1 hover:shadow-xl transition-all duration-300 active:scale-95"
            >
              Get Started
            </button>
          </div>
        </div>
      </div>
    </section>
  );
};

export default GreetingSection;
