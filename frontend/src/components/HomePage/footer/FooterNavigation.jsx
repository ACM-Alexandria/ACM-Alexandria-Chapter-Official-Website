import React from "react";
import { getEnv } from "../../../utils/env";

const FooterNavigation = ({ onNavigate }) => {
  const isEnabled = (envVal) => envVal !== "false";

  const navItems = [
    isEnabled(getEnv("VITE_ENABLE_ABOUT")) && { id: "about", label: "About Us" },
    isEnabled(getEnv("VITE_ENABLE_CLUBS")) && { id: "clubs", label: "Our Clubs" },
    isEnabled(getEnv("VITE_ENABLE_EVENTS")) && { id: "events", label: "Events" },
    isEnabled(getEnv("VITE_ENABLE_PROGRAMS")) && { id: "programs", label: "Programs" },
    isEnabled(getEnv("VITE_ENABLE_RADIO")) && { id: "radio", label: "Radio" },
    isEnabled(getEnv("VITE_ENABLE_SERVICES")) && { id: "services", label: "Services" },
    isEnabled(getEnv("VITE_ENABLE_PARTNERS")) && { id: "partners", label: "Partners" },
  ].filter(Boolean);

  return (
    <div className="space-y-8 text-center md:text-left">
      <h4 className="text-sm font-black uppercase tracking-[0.15em] text-white">
        Navigation
      </h4>
      <ul className="space-y-4 text-blue-100/70 font-bold text-[11px] uppercase tracking-widest font-sans list-none p-0 m-0">
        {navItems.map((item, i) => (
          <li key={i}>
            <button
              type="button"
              onClick={() => onNavigate(item.id)}
              className="hover:text-white transition-colors duration-300 inline-flex items-center gap-2 group bg-transparent border-none p-0 cursor-pointer uppercase text-left font-bold text-[11px] tracking-widest text-blue-100/70"
            >
              <div className="w-1 h-1 rounded-full bg-[#4B98C8] scale-0 group-hover:scale-100 transition-transform" />
              {item.label}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default FooterNavigation;
