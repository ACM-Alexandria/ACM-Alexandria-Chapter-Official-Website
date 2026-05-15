import { FaFacebookF, FaLinkedinIn, FaInstagram, FaTwitter } from "react-icons/fa";

const Footer = () => {
  const currentYear = new Date().getFullYear();

  return (
    <footer className="w-full relative overflow-hidden bg-gradient-to-br from-[#205E85] to-[#1a4563] text-white">
      {/* Decorative patterns */}
      <div 
        className="absolute inset-0 opacity-5 pointer-events-none"
        style={{
          backgroundImage: "radial-gradient(circle at 2px 2px, white 1px, transparent 0)",
          backgroundSize: "32px 32px"
        }}
      />
      
      <div className="max-w-7xl mx-auto px-6 md:px-12 py-20 relative z-10">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-16 lg:gap-12">
          {/* Brand Section */}
          <div className="space-y-8 text-center md:text-left">
            <div className="space-y-2">
              <h3 className="text-2xl font-black tracking-tight text-white uppercase">
                ACM <span className="text-[#4B98C8]">Alexandria</span>
              </h3>
              <p className="text-blue-100/60 text-[10px] font-bold tracking-[0.2em] uppercase">Student Chapter</p>
            </div>
            <p className="text-blue-50/80 text-base leading-relaxed font-medium max-w-xs mx-auto md:mx-0">
              Empowering students through technology, innovation, and
              professional growth.
            </p>
            {/* Social Links */}
            <div className="flex justify-center md:justify-start space-x-3">
              {[
                { icon: <FaFacebookF />, label: "Facebook" },
                { icon: <FaLinkedinIn />, label: "LinkedIn" },
                { icon: <FaInstagram />, label: "Instagram" },
                { icon: <FaTwitter />, label: "Twitter" }
              ].map((social, i) => (
                <a
                  key={i}
                  href="#"
                  className="w-10 h-10 rounded-xl bg-white/5 hover:bg-[#4B98C8] text-white flex items-center justify-center transition-all duration-300 hover:-translate-y-1 border border-white/10"
                  aria-label={social.label}
                >
                  {social.icon}
                </a>
              ))}
            </div>
          </div>

          {/* Navigation Links */}
          <div className="space-y-8 text-center md:text-left">
            <h4 className="text-sm font-black uppercase tracking-[0.15em] text-white">
              Navigation
            </h4>
            <ul className="space-y-4 text-blue-100/70 font-bold text-[11px] uppercase tracking-widest">
              {['About Us', 'Our Clubs', 'Events', 'Programs', 'Services'].map((item, i) => (
                <li key={i}>
                  <a
                    href={`#${item.toLowerCase().replace(' ', '')}`}
                    className="hover:text-white transition-colors duration-300 inline-flex items-center gap-2 group"
                  >
                    <div className="w-1 h-1 rounded-full bg-[#4B98C8] scale-0 group-hover:scale-100 transition-transform" />
                    {item}
                  </a>
                </li>
              ))}
            </ul>
          </div>

          {/* Resources */}
          <div className="space-y-8 text-center md:text-left">
            <h4 className="text-sm font-black uppercase tracking-[0.15em] text-white">
              Resources
            </h4>
            <ul className="space-y-4 text-blue-100/70 font-bold text-[11px] uppercase tracking-widest">
              {['Join Community', 'Our Sponsors', 'Partners', 'Contact'].map((item, i) => (
                <li key={i}>
                  <a
                    href="#"
                    className="hover:text-white transition-colors duration-300 inline-flex items-center gap-2 group"
                  >
                    <div className="w-1 h-1 rounded-full bg-[#4B98C8] scale-0 group-hover:scale-100 transition-transform" />
                    {item}
                  </a>
                </li>
              ))}
            </ul>
          </div>

          {/* Newsletter Section */}
          <div className="space-y-8 text-center md:text-left">
            <h4 className="text-sm font-black uppercase tracking-[0.15em] text-white">
              Newsletter
            </h4>
            <div className="space-y-5">
              <p className="text-blue-100/80 text-sm font-medium leading-relaxed">
                Stay updated with our latest news and upcoming technical events.
              </p>
              <div className="space-y-3">
                <input 
                  type="email" 
                  placeholder="Your Email" 
                  className="w-full bg-white/5 border border-white/10 rounded-xl px-4 py-3 text-sm font-medium placeholder:text-white/30 focus:bg-white/10 transition-all text-white outline-none"
                />
                <button
                  className="w-full bg-[#4B98C8] text-white font-bold py-3 rounded-xl shadow-lg shadow-black/20 hover:bg-[#3679a3] transition-all duration-300 active:scale-[0.98] uppercase text-[10px] tracking-[0.2em]"
                >
                  Join Mailing List
                </button>
              </div>
            </div>
          </div>
        </div>

        {/* Divider */}
        <div className="h-px bg-white/5 w-full my-16" />

        {/* Footer Bottom */}
        <div className="flex flex-col md:flex-row items-center justify-between text-[10px] font-bold uppercase tracking-[0.2em] text-white/30 gap-6 text-center">
          <p>© {currentYear} ACM Alexandria Student Chapter.</p>
          <div className="flex space-x-8">
            <a href="#" className="hover:text-white transition-colors">Privacy</a>
            <a href="#" className="hover:text-white transition-colors">Terms</a>
            <a href="#" className="hover:text-white transition-colors">Cookies</a>
          </div>
        </div>
      </div>
    </footer>
  );
};

export default Footer;
