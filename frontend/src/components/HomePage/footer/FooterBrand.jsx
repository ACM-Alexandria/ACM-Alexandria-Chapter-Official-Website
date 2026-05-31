import React from "react";
import { getSocialIcon } from "./SocialIconResolver";

const FooterBrand = ({ socialLinks, onNavigate }) => {
  return (
    <div className="space-y-8 text-center md:text-left">
      <div className="space-y-2">
        <h3 
          className="text-2xl font-black tracking-tight text-white uppercase cursor-pointer hover:opacity-80 transition-opacity inline-block"
          onClick={() => onNavigate("greeting")}
        >
          ACM <span className="text-[#4B98C8]">Alexandria</span>
        </h3>
        <p className="text-blue-100/60 text-[10px] font-bold tracking-[0.2em] uppercase">Student Chapter</p>
      </div>
      <p className="text-blue-50/80 text-base leading-relaxed font-medium max-w-xs mx-auto md:mx-0">
        Empowering students through technology, innovation, and
        professional growth.
      </p>
      {/* Social Links */}
      {socialLinks && socialLinks.length > 0 && (
        <div className="flex justify-center md:justify-start space-x-3">
          {socialLinks.map((social) => (
            <a
              key={social.id}
              href={social.url}
              target="_blank"
              rel="noopener noreferrer"
              className="w-10 h-10 rounded-xl bg-white/5 hover:bg-[#4B98C8] text-white flex items-center justify-center transition-all duration-300 hover:-translate-y-1 border border-white/10"
              aria-label={social.platform || "Social Link"}
            >
              {getSocialIcon(social.url)}
            </a>
          ))}
        </div>
      )}
    </div>
  );
};

export default FooterBrand;
