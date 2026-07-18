import { Link, useLocation, useNavigate } from "react-router-dom";
import logo from "../../assets/logo/acm-logo-no-bg.png";
import { useState, useEffect, useRef } from "react";
import { useAuth } from "../../contexts/AuthContext";
import LogoutConfirmModal from "../auth/LogoutConfirmModal";
import { HiOutlineArrowRightOnRectangle, HiOutlineChevronDown, HiOutlineUser, HiOutlineShieldCheck } from "react-icons/hi2";



const Navbar = ({ activeSection }) => {
  const [indicatorStyle, setIndicatorStyle] = useState({ left: 0, width: 0 });
  const [showLogoutModal, setShowLogoutModal] = useState(false);
  const [showProfileMenu, setShowProfileMenu] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const navListRef = useRef(null);
  const profileMenuRef = useRef(null);
  const navigate = useNavigate();
  const location = useLocation();
  const { isAuthenticated, isLoading, logout, user } = useAuth();



  const isEnabled = (envVal) => envVal !== "false";

  const navItems = [
    isEnabled(import.meta.env.VITE_ENABLE_ABOUT) && { id: "about", label: "About Us" },
    isEnabled(import.meta.env.VITE_ENABLE_CLUBS) && { id: "clubs", label: "Clubs" },
    isEnabled(import.meta.env.VITE_ENABLE_EVENTS) && { id: "events", label: "Events" },
    isEnabled(import.meta.env.VITE_ENABLE_PROGRAMS) && { id: "programs", label: "Programs" },
    isEnabled(import.meta.env.VITE_ENABLE_RADIO) && { id: "radio", label: "Radio" },
    isEnabled(import.meta.env.VITE_ENABLE_SERVICES) && { id: "services", label: "Services" },
  ].filter(Boolean);

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

  useEffect(() => {
    const handleOutsideClick = (event) => {
      if (
        profileMenuRef.current &&
        !profileMenuRef.current.contains(event.target)
      ) {
        setShowProfileMenu(false);
      }
    };

    window.addEventListener("mousedown", handleOutsideClick);
    return () => window.removeEventListener("mousedown", handleOutsideClick);
  }, []);

  // Prevent scroll and close when mobile menu is open on Escape key
  useEffect(() => {
    if (isMobileMenuOpen) {
      document.body.style.overflow = "hidden";
    } else {
      document.body.style.overflow = "";
    }

    const handleKeyDown = (e) => {
      if (e.key === "Escape") {
        setIsMobileMenuOpen(false);
      }
    };

    if (isMobileMenuOpen) {
      window.addEventListener("keydown", handleKeyDown);
    }

    return () => {
      document.body.style.overflow = "";
      window.removeEventListener("keydown", handleKeyDown);
    };
  }, [isMobileMenuOpen]);

  return (
    <>
    <nav className="w-full h-[74px] bg-white/80 backdrop-blur-md border-b border-slate-100 flex items-center justify-between px-6 md:px-12 fixed top-0 z-50 gap-8">
      <Link 
        to="/" 
        onClick={(e) => {
          e.preventDefault();
          handleSectionNavigation("greeting");
        }}
        className="flex items-center group transition-transform duration-300 hover:scale-105" 
        aria-label="Go to home page"
      >
        <img src={logo} className="h-14 w-auto" alt="ACM Logo" />
        <div className="ml-3 hidden sm:block">
          <p className="text-slate-900 font-extrabold text-sm tracking-tight leading-tight uppercase">ACM Alexandria</p>
          <p className="text-[#4B98C8] text-[9px] font-bold tracking-[0.15em] uppercase">Student Chapter</p>
        </div>
      </Link>

      <ul
        className="list-none hidden lg:flex ml-auto h-full items-stretch relative"
        ref={navListRef}
      >
        {/* Sliding background indicator */}
        <div
          className="absolute bottom-0 h-[3px] bg-gradient-to-r from-[#4B98C8] to-[#205E85] transition-all duration-300 ease-out rounded-t-full"
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
                className={`
                  no-underline text-sm font-bold tracking-wide transition-all duration-300 px-5 flex items-center h-full relative z-10
                  ${isActive ? "text-[#205E85]" : "text-slate-500 hover:text-[#4B98C8]"}
                `}
              >
                {item.label}
              </button>
            </li>
          );
        })}
      </ul>

      <div className="flex items-center gap-4">
        {isLoading ? (
          <div className="w-8 h-8 rounded-full border-2 border-slate-200 border-t-[#4B98C8] animate-spin" />
        ) : isAuthenticated ? (
          <div className="relative" ref={profileMenuRef}>
            <button
              type="button"
              className="flex items-center gap-2 p-1.5 rounded-full hover:bg-slate-50 transition-colors group"
              onClick={() => setShowProfileMenu((prev) => !prev)}
            >
              <div className="w-8 h-8 rounded-full bg-gradient-to-br from-[#4B98C8] to-[#205E85] flex items-center justify-center text-white text-sm font-bold shadow-sm">
                {user?.email?.[0].toUpperCase() || "U"}
              </div>
              <HiOutlineChevronDown className={`w-4 h-4 text-slate-400 transition-transform duration-300 ${showProfileMenu ? "rotate-180" : ""}`} />
            </button>

            {showProfileMenu && (
              <div 
                className="absolute right-0 mt-3 w-64 rounded-2xl bg-white text-slate-900 shadow-2xl ring-1 ring-black/5 overflow-hidden z-50 origin-top-right transition-all duration-200"
                style={{ animation: "floatIn 0.3s cubic-bezier(0.22,1,0.36,1) both" }}
              >
                <div className="px-5 py-4 border-b border-slate-100 bg-slate-50/50">
                  <p className="text-[10px] font-bold uppercase tracking-widest text-slate-400">Signed in as</p>
                  <p className="mt-0.5 text-sm font-bold text-slate-700 truncate">
                    {user?.email}
                  </p>
                </div>
                <div className="p-2">
                  <button
                    type="button"
                    className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-bold text-slate-700 hover:bg-slate-50 transition-colors"
                    onClick={() => {
                      setShowProfileMenu(false);
                      navigate("/profile");
                    }}
                  >
                    <HiOutlineUser className="h-5 w-5 text-slate-500" />
                    My Profile
                  </button>
                  {user?.role === 'ADMIN' && (
                    <button
                      type="button"
                      className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-bold text-slate-700 hover:bg-slate-50 transition-colors"
                      onClick={() => {
                        setShowProfileMenu(false);
                        navigate("/admin");
                      }}
                    >
                      <HiOutlineShieldCheck className="h-5 w-5 text-slate-500" />
                      Admin Tools
                    </button>
                  )}
                  <button
                    type="button"
                    className="flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-bold text-red-600 hover:bg-red-50 transition-colors"
                    onClick={() => {
                      setShowProfileMenu(false);
                      setShowLogoutModal(true);
                    }}
                  >
                    <HiOutlineArrowRightOnRectangle className="h-5 w-5" />
                    Log out
                  </button>
                </div>
              </div>
            )}
          </div>
        ) : (
          <Link
            className="px-6 py-2.5 bg-gradient-to-r from-[#4B98C8] to-[#205E85] text-white text-sm font-bold rounded-xl shadow-md shadow-blue-200/50 hover:-translate-y-0.5 hover:shadow-lg transition-all duration-300 active:scale-95"
            to="/login"
          >
            Sign In
          </Link>
        )}

        {/* Mobile Hamburger Button */}
        <button
          type="button"
          className="flex lg:hidden p-2 rounded-xl text-slate-500 hover:bg-slate-50 hover:text-slate-900 transition-colors cursor-pointer"
          onClick={() => setIsMobileMenuOpen(true)}
          aria-label="Open navigation menu"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M4 6h16M4 12h16M4 18h16" />
          </svg>
        </button>
      </div>
    </nav>

    {/* Mobile Drawer Backdrop */}
    <div
      className={`fixed inset-0 z-50 bg-slate-900/40 backdrop-blur-sm transition-opacity duration-300 lg:hidden ${
        isMobileMenuOpen ? "opacity-100 pointer-events-auto" : "opacity-0 pointer-events-none"
      }`}
      onClick={() => setIsMobileMenuOpen(false)}
    />

    {/* Mobile Drawer Content */}
    <div
      className={`fixed top-0 right-0 bottom-0 z-50 w-full max-w-[320px] bg-white shadow-2xl transition-transform duration-300 ease-[cubic-bezier(0.22,1,0.36,1)] transform lg:hidden flex flex-col ${
        isMobileMenuOpen ? "translate-x-0" : "translate-x-full"
      }`}
    >
      {/* Drawer Header */}
      <div className="flex items-center justify-between p-6 border-b border-slate-100">
        <div className="flex items-center">
          <img src={logo} className="h-10 w-auto" alt="ACM Logo" />
          <div className="ml-2.5">
            <p className="text-slate-900 font-extrabold text-xs tracking-tight uppercase">ACM Alexandria</p>
            <p className="text-[#4B98C8] text-[8px] font-bold tracking-widest uppercase">Student Chapter</p>
          </div>
        </div>
        <button
          type="button"
          className="p-2 rounded-xl text-slate-500 hover:bg-slate-50 hover:text-slate-900 transition-colors cursor-pointer"
          onClick={() => setIsMobileMenuOpen(false)}
          aria-label="Close navigation menu"
        >
          <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2.5} d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
      </div>

      {/* Drawer Links */}
      <ul className="flex-1 list-none p-6 space-y-4 overflow-y-auto">
        {navItems.map((item) => {
          const isActive = activeSection === item.id;
          return (
            <li key={item.id}>
              <button
                type="button"
                onClick={() => {
                  setIsMobileMenuOpen(false);
                  handleSectionNavigation(item.id);
                }}
                className={`w-full text-left py-3 px-4 rounded-xl text-sm font-bold tracking-wide transition-all duration-200 cursor-pointer ${
                  isActive
                    ? "bg-slate-50 text-[#205E85]"
                    : "text-slate-500 hover:bg-slate-50/50 hover:text-[#4B98C8]"
                }`}
              >
                {item.label}
              </button>
            </li>
          );
        })}
      </ul>

      {/* Drawer Footer (Auth & User Info) */}
      <div className="p-6 border-t border-slate-100 bg-slate-50/50">
        {isLoading ? (
          <div className="flex justify-center py-2">
            <div className="w-6 h-6 rounded-full border-2 border-slate-200 border-t-[#4B98C8] animate-spin" />
          </div>
        ) : isAuthenticated ? (
          <div className="space-y-4">
            <div className="flex items-center gap-3">
              <div className="w-10 h-10 rounded-full bg-gradient-to-br from-[#4B98C8] to-[#205E85] flex items-center justify-center text-white text-sm font-bold shadow-sm">
                {user?.email?.[0].toUpperCase() || "U"}
              </div>
              <div className="truncate">
                <p className="text-[10px] font-bold uppercase tracking-widest text-slate-400">Signed in as</p>
                <p className="text-xs font-bold text-slate-700 truncate">{user?.email}</p>
              </div>
            </div>
            <div className="space-y-2 pt-2">
              <button
                type="button"
                className="flex w-full items-center gap-3 rounded-xl px-4 py-2.5 text-xs font-bold text-slate-700 hover:bg-slate-100 transition-colors"
                onClick={() => {
                  setIsMobileMenuOpen(false);
                  navigate("/profile");
                }}
              >
                <HiOutlineUser className="h-4 w-4 text-slate-500" />
                My Profile
              </button>
              {user?.role === 'ADMIN' && (
                <button
                  type="button"
                  className="flex w-full items-center gap-3 rounded-xl px-4 py-2.5 text-xs font-bold text-slate-700 hover:bg-slate-100 transition-colors"
                  onClick={() => {
                    setIsMobileMenuOpen(false);
                    navigate("/admin");
                  }}
                >
                  <HiOutlineShieldCheck className="h-4 w-4 text-slate-500" />
                  Admin Tools
                </button>
              )}
              <button
                type="button"
                className="flex w-full items-center gap-3 rounded-xl px-4 py-2.5 text-xs font-bold text-red-600 hover:bg-red-50 transition-colors"
                onClick={() => {
                  setIsMobileMenuOpen(false);
                  setShowLogoutModal(true);
                }}
              >
                <HiOutlineArrowRightOnRectangle className="h-4 w-4" />
                Log out
              </button>
            </div>
          </div>
        ) : (
          <Link
            to="/login"
            onClick={() => setIsMobileMenuOpen(false)}
            className="flex w-full items-center justify-center py-3 bg-gradient-to-r from-[#4B98C8] to-[#205E85] text-white text-sm font-bold rounded-xl shadow-md transition-all duration-300 active:scale-95"
          >
            Sign In
          </Link>
        )}
      </div>
    </div>

    <LogoutConfirmModal
      open={showLogoutModal}
      onCancel={() => setShowLogoutModal(false)}
      onConfirm={async () => {
        setShowProfileMenu(false);
        setShowLogoutModal(false);
        await logout();
      }}
    />
  </>
);
};

export default Navbar;
