import React from "react";

const FooterNewsletter = () => {
  return (
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
            disabled
          />
          <button
            className="w-full bg-[#4B98C8]/60 text-white/60 font-bold py-3 rounded-xl shadow-lg shadow-black/20 uppercase text-[10px] tracking-[0.2em] cursor-not-allowed"
            disabled
          >
            Join Mailing List (Paused)
          </button>
        </div>
      </div>
    </div>
  );
};

export default FooterNewsletter;
