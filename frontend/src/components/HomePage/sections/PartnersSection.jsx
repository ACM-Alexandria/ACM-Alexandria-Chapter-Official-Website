import { useState } from "react";
import { FiGlobe } from "react-icons/fi";

const PartnerCard = ({ partner }) => {
  const [imageError, setImageError] = useState(false);

  const handleClick = () => {
    if (partner.website) {
      window.open(partner.website, "_blank", "noopener,noreferrer");
    }
  };

  return (
    <div
      onClick={partner.website ? handleClick : undefined}
      className={`group overflow-hidden bg-white rounded-2xl border border-slate-200/70 shadow-sm hover:shadow-md hover:border-slate-300 hover:-translate-y-1 transition-all duration-300 w-[200px] h-[120px] p-4 flex items-center justify-center shrink-0 ${
        partner.website ? "cursor-pointer" : ""
      }`}
    >
      {partner.imageUrl && !imageError ? (
        <img
          src={partner.imageUrl}
          alt={partner.name}
          className="w-full h-full object-contain transition-transform duration-300 group-hover:scale-105"
          onError={() => setImageError(true)}
        />
      ) : (
        <div className="w-full h-full flex flex-col items-center justify-center gap-1.5 bg-gradient-to-br from-slate-50 to-slate-100">
          <FiGlobe className="w-7 h-7 text-slate-300 group-hover:text-[#4B98C8] transition-colors" />
          <span className="text-[11px] font-bold text-slate-400 group-hover:text-slate-600 transition-colors text-center leading-tight px-2">
            {partner.name}
          </span>
        </div>
      )}
    </div>
  );
};

const PartnersSection = ({ loading, partners }) => {
  return (
    <section
      id="partners"
      className="w-full py-24 px-6 bg-white relative overflow-hidden"
    >
      <div className="max-w-7xl mx-auto w-full relative z-10">
        {/* Header */}
        <div className="text-center mb-16">
          <h2
            className="text-4xl md:text-5xl font-extrabold text-slate-900 tracking-tight mb-6"
            data-aos="fade-up"
          >
            Our <span className="text-[#205E85]">Partners</span>
          </h2>
          <p
            className="text-slate-500 text-lg max-w-2xl mx-auto font-medium"
            data-aos="fade-up"
            data-aos-delay="100"
          >
            We're proud to collaborate with organisations that share our
            commitment to technology, education, and community growth.
          </p>
        </div>

        {/* Logo strip */}
        {loading ? (
          <div className="flex flex-wrap justify-center gap-4">
            {[...Array(5)].map((_, i) => (
              <div
                key={i}
                className="animate-pulse bg-slate-100 rounded-2xl h-24 w-44 border border-slate-200/50"
              />
            ))}
          </div>
        ) : (
          <div
            className="flex flex-wrap justify-center gap-4"
            data-aos="fade-up"
            data-aos-delay="150"
          >
            {partners.map((partner) => (
              <PartnerCard key={partner.id} partner={partner} />
            ))}
          </div>
        )}
      </div>
    </section>
  );
};

export default PartnersSection;
