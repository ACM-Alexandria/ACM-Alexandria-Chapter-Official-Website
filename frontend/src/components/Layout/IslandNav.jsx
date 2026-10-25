import { useEffect, useRef, useState } from "react";
import { Link, NavLink, useNavigate } from "react-router-dom";
import { FiMenu, FiMoon, FiSun, FiX } from "react-icons/fi";
import { HiOutlineArrowRightOnRectangle, HiOutlineChevronDown, HiOutlineShieldCheck, HiOutlineUser } from "react-icons/hi2";
import logo from "../../assets/logo/acm-logo-no-bg.png";
import { useAuth } from "../../contexts/useAuth";
import LogoutConfirmModal from "../auth/LogoutConfirmModal";
import useScrollLock from "../../hooks/useScrollLock";
import HRFolderIcon from "../icons/HRFolderIcon";

const prefersDark = () => {
  const saved = localStorage.getItem("theme");
  if (saved) return saved === "dark";
  return window.matchMedia("(prefers-color-scheme: dark)").matches;
};

// A nav item: "#id" glides to a section of this page, "/#id" opens the home page at that section, and
// "/path" opens another page (highlighted when current).
const NavItem = ({ href, className, activeClassName = "", onClick, children }) => {
  if (href.startsWith("#")) return <a href={href} onClick={onClick} className={className}>{children}</a>;
  if (href.includes("#")) return <Link to={href} onClick={onClick} className={className}>{children}</Link>;
  return <NavLink to={href} end={href === "/"} onClick={onClick} className={({ isActive }) => `${className} ${isActive ? activeClassName : ""}`}>{children}</NavLink>;
};

// Full-width and transparent over the hero; once the page has scrolled half a screen it shrinks into a
// floating, rounded bar (the acmuobd.com navbar). Every change is a 300ms transition. It also carries the
// theme switch and the account menu, and below `lg` a menu button opens a panel with the links and actions.
// `links` are [href, label] pairs; on pages other than home the logo links back home. `solid` keeps the floating
// bar from the start, for pages that begin on a light background instead of a PageHero.
const IslandNav = ({ links, home = true, solid: alwaysSolid = false }) => {
  const [scrolled, setScrolled] = useState(false);
  const [menuOpen, setMenuOpen] = useState(false);
  const [profileOpen, setProfileOpen] = useState(false);
  const [logoutOpen, setLogoutOpen] = useState(false);
  const [dark, setDark] = useState(prefersDark);
  const headerRef = useRef(null);
  const navigate = useNavigate();
  const { isAuthenticated, isLoading, logout, user } = useAuth();
  const solid = alwaysSolid || scrolled || menuOpen;

  const showAdminLink = ["SUPER_ADMIN", "ACM_HIGH_BOARD", "ACM_COMMITTEE_BOARD", "ACM_CLUB_BOARD"].includes(user?.role);
  const showHRLink = user?.role === "ACM_MEMBER" && user?.committeeName === "HR Committee";
  const accountLinks = [
    { to: "/profile", label: "My Profile", icon: <HiOutlineUser className="h-5 w-5 text-slate-500 dark:text-[#7DBBE0]" /> },
    showAdminLink && { to: "/admin", label: "Admin Tools", icon: <HiOutlineShieldCheck className="h-5 w-5 text-slate-500 dark:text-[#7DBBE0]" /> },
    showHRLink && { to: "/hr-dashboard", label: "HR Dashboard", icon: <HRFolderIcon className="h-5 w-5 text-slate-500 dark:text-[#7DBBE0]" /> },
  ].filter(Boolean);

  useEffect(() => {
    // Home waits for half a screen (its hero is a whole pinned scene); shorter page headers switch sooner.
    const onScroll = () => setScrolled(window.scrollY > (home ? window.innerHeight * 0.5 : 120));
    onScroll();
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, [home]);

  useEffect(() => {
    document.documentElement.classList.toggle("dark", dark);
    localStorage.setItem("theme", dark ? "dark" : "light");
  }, [dark]);

  // Close the menus on Escape or a click outside the bar; lock the page behind the open phone menu.
  useEffect(() => {
    const onKey = (event) => {
      if (event.key === "Escape") {
        setMenuOpen(false);
        setProfileOpen(false);
      }
    };
    const onDown = (event) => {
      if (headerRef.current && !headerRef.current.contains(event.target)) {
        setMenuOpen(false);
        setProfileOpen(false);
      }
    };
    window.addEventListener("keydown", onKey);
    window.addEventListener("mousedown", onDown);
    return () => {
      window.removeEventListener("keydown", onKey);
      window.removeEventListener("mousedown", onDown);
    };
  }, []);

  useScrollLock(menuOpen);

  const go = (to) => {
    setMenuOpen(false);
    setProfileOpen(false);
    navigate(to);
  };

  const iconButton = `flex h-10 w-10 items-center justify-center rounded-full transition-colors ${solid ? "text-slate-600 hover:bg-[#F3F7FB] dark:text-[#A9C7DB] dark:hover:bg-white/5" : "text-white hover:bg-white/10"}`;
  const menuItem = "flex w-full items-center gap-3 rounded-xl px-4 py-3 text-sm font-semibold text-slate-700 transition-colors hover:bg-[#F3F7FB] dark:text-slate-200 dark:hover:bg-white/5";

  return (
    <>
      <header ref={headerRef} className={`fixed inset-x-0 top-0 z-40 flex flex-col items-center px-3 transition-[padding] duration-300 ease-out ${solid ? "pt-2.5" : "pt-0"}`}>
        <div
          className={`flex w-full items-center justify-between gap-4 transition-all duration-300 ease-out sm:gap-6 ${solid
            ? "h-14 max-w-[1080px] rounded-2xl bg-white/95 px-3 text-slate-900 shadow-[0_14px_40px_-18px_rgba(8,24,38,0.5)] ring-1 ring-black/5 backdrop-blur-md dark:bg-[#0F2233]/95 dark:text-white dark:ring-white/10"
            : "h-[72px] max-w-[1280px] rounded-none bg-transparent px-2 text-white sm:px-5"}`}
        >
          <NavItem href={home ? "#top" : "/"} className="flex items-center gap-3 whitespace-nowrap text-base font-semibold">
            <span className={`flex items-center justify-center rounded-full bg-white transition-all duration-300 ${solid ? "h-9 w-9 ring-1 ring-[#D6E6F2]" : "h-10 w-10"}`}><img src={logo} alt="" className="h-3/4 w-3/4 object-contain" /></span>
            ACM Alexandria
          </NavItem>
          <nav aria-label="Sections" className="hidden gap-1 text-sm font-medium lg:flex">
            {links.map(([href, label]) => (
              <NavItem key={href} href={href} activeClassName={solid ? "!text-[#205E85] bg-[#F3F7FB] dark:bg-white/5 dark:!text-white" : "!text-white"} className={`rounded-lg px-3 py-2 transition-colors ${solid ? "text-slate-600 hover:bg-[#F3F7FB] hover:text-[#205E85] dark:text-[#A9C7DB] dark:hover:bg-white/5 dark:hover:text-white" : "text-[#BFE0F5] hover:text-white"}`}>{label}</NavItem>
            ))}
          </nav>
          <div className="flex items-center gap-1.5 sm:gap-2">
            <button type="button" onClick={() => setDark((value) => !value)} aria-label={dark ? "Switch to light mode" : "Switch to dark mode"} className={`hidden sm:flex ${iconButton}`}>
              {dark ? <FiSun className="h-[18px] w-[18px]" aria-hidden="true" /> : <FiMoon className="h-[18px] w-[18px]" aria-hidden="true" />}
            </button>
            {isLoading ? (
              <span className="hidden h-8 w-8 animate-spin rounded-full border-2 border-[#D6E6F2] border-t-[#4B98C8] sm:block" aria-label="Loading account" />
            ) : isAuthenticated ? (
              <div className="relative hidden sm:block">
                <button type="button" onClick={() => setProfileOpen((value) => !value)} aria-expanded={profileOpen} aria-haspopup="menu" className={`flex items-center gap-1.5 rounded-full p-1 pr-2 transition-colors ${solid ? "hover:bg-[#F3F7FB] dark:hover:bg-white/5" : "hover:bg-white/10"}`}>
                  <span className="flex h-8 w-8 items-center justify-center rounded-full bg-[#205E85] text-sm font-semibold text-white dark:bg-[#4B98C8] dark:text-[#081826]">{user?.email?.[0]?.toUpperCase() || "U"}</span>
                  <HiOutlineChevronDown className={`h-4 w-4 transition-transform duration-300 ${profileOpen ? "rotate-180" : ""}`} aria-hidden="true" />
                </button>
                {profileOpen && (
                  <div role="menu" className="absolute right-0 mt-3 w-64 overflow-hidden rounded-2xl bg-white text-slate-900 shadow-[0_24px_60px_-24px_rgba(8,24,38,0.55)] ring-1 ring-[#D6E6F2] dark:bg-[#0F2233] dark:text-slate-100 dark:ring-white/10" style={{ animation: "hp-rise 0.25s ease-out both" }}>
                    <div className="border-b border-[#E6EEF5] px-5 py-4 dark:border-white/10">
                      <p className="text-xs font-semibold text-slate-500 dark:text-[#A9C7DB]">Signed in as</p>
                      <p className="mt-0.5 truncate text-sm font-semibold">{user?.email}</p>
                    </div>
                    <div className="p-2">
                      {accountLinks.map(({ to, label, icon }) => (
                        <button key={to} type="button" role="menuitem" onClick={() => go(to)} className={menuItem}>{icon}{label}</button>
                      ))}
                      <button type="button" role="menuitem" onClick={() => { setProfileOpen(false); setLogoutOpen(true); }} className={`${menuItem} text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-950/40`}>
                        <HiOutlineArrowRightOnRectangle className="h-5 w-5" />Log out
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ) : (
              <>
                <Link to="/login" className={`hidden rounded-xl px-3 py-2 text-sm font-semibold transition-colors sm:inline-flex ${solid ? "text-slate-700 hover:text-[#205E85] dark:text-slate-200 dark:hover:text-white" : "text-[#BFE0F5] hover:text-white"}`}>Sign in</Link>
                <Link
                  to="/register"
                  className={`hidden shrink-0 whitespace-nowrap rounded-xl px-4 py-2 text-sm font-semibold transition-colors duration-300 sm:inline-flex ${solid ? "bg-[#205E85] text-white hover:bg-[#16364C] dark:bg-[#4B98C8] dark:text-[#081826]" : "bg-white text-[#205E85] dark:bg-[#4B98C8] dark:text-[#081826]"}`}
                >
                  Join us
                </Link>
              </>
            )}
            <button type="button" onClick={() => setMenuOpen((value) => !value)} aria-expanded={menuOpen} aria-controls="home-menu" aria-label={menuOpen ? "Close menu" : "Open menu"} className={`lg:hidden ${iconButton}`}>
              {menuOpen ? <FiX className="h-5 w-5" aria-hidden="true" /> : <FiMenu className="h-5 w-5" aria-hidden="true" />}
            </button>
          </div>
        </div>

        {menuOpen && (
          <div id="home-menu" className="mt-2 max-h-[calc(100svh-5.5rem)] w-full max-w-[1080px] overflow-y-auto rounded-2xl bg-white p-3 text-slate-900 shadow-[0_24px_60px_-24px_rgba(8,24,38,0.55)] ring-1 ring-black/5 lg:hidden dark:bg-[#0F2233] dark:text-white dark:ring-white/10" style={{ animation: "hp-rise 0.25s ease-out both" }}>
            <nav aria-label="Sections" className="grid gap-1">
              {links.map(([href, label]) => (
                <NavItem key={href} href={href} onClick={() => setMenuOpen(false)} activeClassName="bg-[#F3F7FB] !text-[#205E85] dark:bg-white/5 dark:!text-white" className="rounded-xl px-4 py-3 text-base font-semibold text-slate-700 transition-colors hover:bg-[#F3F7FB] hover:text-[#205E85] dark:text-slate-200 dark:hover:bg-white/5 dark:hover:text-white">{label}</NavItem>
              ))}
            </nav>
            <div className="mt-3 border-t border-[#E6EEF5] pt-3 dark:border-white/10">
              <button type="button" onClick={() => setDark((value) => !value)} className={menuItem}>
                {dark ? <FiSun className="h-5 w-5 text-slate-500 dark:text-[#7DBBE0]" aria-hidden="true" /> : <FiMoon className="h-5 w-5 text-slate-500" aria-hidden="true" />}
                {dark ? "Light mode" : "Dark mode"}
              </button>
              {!isLoading && isAuthenticated && (
                <>
                  {accountLinks.map(({ to, label, icon }) => (
                    <button key={to} type="button" onClick={() => go(to)} className={menuItem}>{icon}{label}</button>
                  ))}
                  <button type="button" onClick={() => { setMenuOpen(false); setLogoutOpen(true); }} className={`${menuItem} text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-950/40`}>
                    <HiOutlineArrowRightOnRectangle className="h-5 w-5" />Log out
                  </button>
                </>
              )}
            </div>
            {!isLoading && !isAuthenticated && (
              <div className="mt-3 grid grid-cols-2 gap-2 sm:hidden">
                <Link to="/login" onClick={() => setMenuOpen(false)} className="flex h-11 items-center justify-center rounded-xl text-sm font-semibold text-[#205E85] ring-1 ring-[#D6E6F2] dark:text-[#9FD0EE] dark:ring-white/15">Sign in</Link>
                <Link to="/register" onClick={() => setMenuOpen(false)} className="flex h-11 items-center justify-center rounded-xl bg-[#205E85] text-sm font-semibold text-white dark:bg-[#4B98C8] dark:text-[#081826]">Join us</Link>
              </div>
            )}
          </div>
        )}
      </header>

      <LogoutConfirmModal
        open={logoutOpen}
        onCancel={() => setLogoutOpen(false)}
        onConfirm={async () => {
          setLogoutOpen(false);
          await logout();
        }}
      />
    </>
  );
};

export default IslandNav;
