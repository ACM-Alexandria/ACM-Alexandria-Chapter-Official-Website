import { Link, useLocation, useNavigate } from "react-router-dom";
import logo from "../../assets/logo/acm-logo-no-bg.png";
import { useState, useEffect, useRef } from "react";
import { useAuth } from "../../contexts/AuthContext";
import LogoutConfirmModal from "../auth/LogoutConfirmModal";
import { HiOutlineLogout } from "react-icons/hi";
import { FaUserCircle } from "react-icons/fa";


const Navbar = ({ activeSection }) => {
  const [indicatorStyle, setIndicatorStyle] = useState({ left: 0, width: 0 });
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const navListRef = useRef(null);
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, isLoading, logout } = useAuth();

  const navLinkClass =
    "no-underline text-white text-[22px] font-semibold hover:opacity-80 transition-all duration-300 px-6 flex items-center h-full relative z-10";
  const activeLinkClass =
    "no-underline text-white text-[22px] font-semibold hover:opacity-80 transition-all duration-300 px-6 flex items-center h-full relative z-10";

  const navItems = [
    { id: "about", label: "About Us" },
    { id: "clubs", label: "Clubs" },
    { id: "events", label: "Events" },
    { id: "programs", label: "Programs" },
    { id: "services", label: "Services" },
  ];

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

  // Update indicator position when active section changes
  useEffect(() => {
    if (!navListRef.current) return;

    const activeLink = navListRef.current.querySelector(
      `[data-section="${activeSection}"]`,
    );

    if (activeLink) {
      const { offsetLeft, offsetWidth } = activeLink;
      setIndicatorStyle({
        left: offsetLeft,
        width: offsetWidth,
      });
    }
  }, [activeSection]);

  return (
    <nav className="w-full h-[70px] bg-gradient-to-r from-[#4B98C8] to-[#205E85] flex items-center justify-between px-10 text-white fixed top-0 z-10 gap-12">
      <Link to="/" className="flex items-center" aria-label="Go to home page">
        <img src={logo} className="h-20" alt="ACM Logo" />
      </Link>

      <ul
        className="list-none flex ml-auto h-full items-stretch relative"
        ref={navListRef}
      >
        {/* Sliding background indicator */}
        <div
          className="absolute top-0 h-full bg-white/20 transition-all duration-300 ease-out"
          style={{
            left: `${indicatorStyle.left}px`,
            width: `${indicatorStyle.width}px`,
          }}
        />

        {navItems.map((item) => {
          const isActive = activeSection === item.id;
          return (
            <li key={item.id} className="h-full">
              <button
                type="button"
                data-section={item.id}
                onClick={() => handleSectionNavigation(item.id)}
                className={isActive ? activeLinkClass : navLinkClass}
              >
                {item.label}
              </button>
            </li>
          );
        })}
      </ul>

      {isLoading ? (
        <div className="bg-white/40 text-white py-2 px-[18px] rounded-md font-bold animate-pulse">
          Loading...
        </div>
      ) : isAuthenticated ? (
        <div className="flex items-center gap-2">
          <button
            type="button"
            aria-label="View profile"
            title="Profile"
            className="text-white/90 p-3 rounded-full transition-colors hover:bg-white/10 hover:text-white inline-flex items-center justify-center"
            onClick={() => navigate("/profile")}
          >
            <FaUserCircle className="h-6 w-6" />
          </button>
          <button
            type="button"
            aria-label="Log out"
            title="Log out"
            className="text-white/90 p-3 rounded-full transition-colors hover:bg-white/10 hover:text-white inline-flex items-center justify-center"
            onClick={() => setShowLogoutModal(true)}
          >
            <HiOutlineLogout className="h-6 w-6" />
          </button>
        </div>
      ) : (
        <Link
          className="bg-white text-[#2c4a72] py-2 px-[18px] rounded-md no-underline font-bold hover:bg-gray-200"
          to="/login"
        >
          Sign In
        </Link>
      )}

      <LogoutConfirmModal
        open={showLogoutModal}
        onCancel={() => setShowLogoutModal(false)}
        onConfirm={async () => {
          setShowLogoutModal(false);
          await logout();
          navigate("/login");
        }}
      />
    </nav>
  );
};

export default Navbar;
