import React from "react";

const FooterResources = ({ onResourceClick }) => {
  const resourceItems = ['Join Community', 'Our Sponsors', 'Partners'];

  return (
    <div className="space-y-8 text-center md:text-left">
      <h4 className="text-sm font-black uppercase tracking-[0.15em] text-white">
        Resources
      </h4>
      <ul className="space-y-4 text-blue-100/70 font-bold text-[11px] uppercase tracking-widest font-sans list-none p-0 m-0">
        {resourceItems.map((item, i) => (
          <li key={i}>
            <button
              type="button"
              onClick={() => onResourceClick(item)}
              className="hover:text-white transition-colors duration-300 inline-flex items-center gap-2 group bg-transparent border-none p-0 cursor-pointer uppercase text-left font-bold text-[11px] tracking-widest text-blue-100/70"
            >
              <div className="w-1 h-1 rounded-full bg-[#4B98C8] scale-0 group-hover:scale-100 transition-transform" />
              {item}
            </button>
          </li>
        ))}
      </ul>
    </div>
  );
};

export default FooterResources;
