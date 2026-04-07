import { Link } from "react-router-dom";
import logo from "../../assets/logo/acm-logo-no-bg.png";
import { useState, useEffect, useRef } from "react";

const Navbar = ({ activeSection }) => {
  const [indicatorStyle, setIndicatorStyle] = useState({ left: 0, width: 0 });
  const navListRef = useRef(null);

  const navLinkClass =
    "no-underline text-white text-[22px] font-semibold hover:opacity-80 transition-all duration-300 px-6 flex items-center h-full relative z-10";
  const activeLinkClass =
    "no-underline text-white text-[22px] font-semibold hover:opacity-80 transition-all duration-300 px-6 flex items-center h-full relative z-10";

  const navItems = [
    { href: "#about", label: "About Us" },
    { href: "#clubs", label: "Clubs" },
    { href: "#events", label: "Events" },
    { href: "#programs", label: "Programs" },
    { href: "#services", label: "Services" },
  ];

  // Update indicator position when active section changes
  useEffect(() => {
    if (!navListRef.current) return;

    const activeLink = navListRef.current.querySelector(
      `Link[href="#${activeSection}"]`,
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
          const isActive = activeSection === item.href.slice(1); // Remove # from href
          return (
            <li key={item.href} className="h-full">
              <Link
                href={item.href}
                className={isActive ? activeLinkClass : navLinkClass}
              >
                {item.label}
              </Link>
            </li>
          );
        })}
      </ul>

      <Link
        className="bg-white text-[#2c4a72] py-2 px-[18px] rounded-md no-underline font-bold hover:bg-gray-200"
        to="/login">
        Sign In
      </Link>
    </nav>
  );
};

export default Navbar;
