import React from "react";

const FooterBottom = ({ currentYear, onNavigate }) => {
  return (
    <div className="flex flex-col md:flex-row items-center justify-between text-[10px] font-bold uppercase tracking-[0.2em] text-white/30 gap-6 text-center">
      <p>© {currentYear} ACM Alexandria Student Chapter.</p>
      <div className="flex space-x-8">
        {['Privacy', 'Terms', 'Cookies'].map((item) => (
          <button 
            key={item}
            type="button" 
            className="bg-transparent border-none p-0 cursor-pointer hover:text-white transition-colors text-[10px] font-bold uppercase tracking-[0.2em] text-white/30"
          >
            {item}
          </button>
        ))}
      </div>
    </div>
  );
};

export default FooterBottom;
