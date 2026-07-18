import { useState, useEffect } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { fetchSocialLinks } from "../../services/homePageService";
import { useAuth } from "../../contexts/AuthContext";

import FooterBrand from "./footer/FooterBrand";
import FooterNavigation from "./footer/FooterNavigation";
import FooterResources from "./footer/FooterResources";
import FooterNewsletter from "./footer/FooterNewsletter";
import FooterBottom from "./footer/FooterBottom";
import HelpUsGrowModal from "./footer/HelpUsGrowModal";

const Footer = () => {
  const currentYear = new Date().getFullYear();
  const navigate = useNavigate();
  const location = useLocation();
  const [socialLinks, setSocialLinks] = useState([]);
  const [showGrowModal, setShowGrowModal] = useState(false);
  const { isAuthenticated } = useAuth();

  useEffect(() => {
    const loadSocialLinks = async () => {
      try {
        const data = await fetchSocialLinks();
        setSocialLinks(data);
      } catch (error) {
        console.error("Error loading social links in footer:", error);
      }
    };
    loadSocialLinks();
  }, []);

  const scrollToSection = (sectionId) => {
    const section = document.getElementById(sectionId);
    if (section) {
      section.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  };

  const handleSectionNavigation = (sectionId) => {
    if (location.pathname !== "/") {
      navigate("/");
      setTimeout(() => scrollToSection(sectionId), 120);
      return;
    }

    scrollToSection(sectionId);
  };

  const handleResourceClick = (item) => {
    if (item === "Join Community") {
      navigate("/register");
    } else if (item === "Contact") {
      scrollToSection("footer");
    } else if (item === "Help Us Grow") {
      if (!isAuthenticated) {
        navigate("/login");
      } else {
        setShowGrowModal(true);
      }
    } else {
      // Do nothing for Sponsors and Partners (no proper navigation targets)
    }
  };

  return (
    <footer id="footer" className="w-full relative overflow-hidden bg-gradient-to-br from-[#205E85] to-[#1a4563] text-white">
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
          <FooterBrand socialLinks={socialLinks} onNavigate={handleSectionNavigation} />
          <FooterNavigation onNavigate={handleSectionNavigation} />
          <FooterResources onResourceClick={handleResourceClick} />
          <FooterNewsletter onHelpUsGrowClick={() => handleResourceClick("Help Us Grow")} />
        </div>

        {/* Divider */}
        <div className="h-px bg-white/5 w-full my-16" />

        <FooterBottom currentYear={currentYear} onNavigate={handleSectionNavigation} />
      </div>

      <HelpUsGrowModal open={showGrowModal} onClose={() => setShowGrowModal(false)} />
    </footer>
  );
};

export default Footer;
