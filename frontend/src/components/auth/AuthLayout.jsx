import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import { FiArrowLeft, FiMoon, FiSun } from "react-icons/fi";
import acmLogo from "../../assets/logo/acm-logo-no-bg.png";
import NightSky from "../ui/NightSky";
import { switchTheme } from "../../utils/themeTransition";

const stars = Array.from({ length: 40 }, (_, i) => ({ x: (i * 41.3) % 100, y: (i * 23.7) % 70, size: i % 6 === 0 ? 2.5 : 1.5, delay: (i % 5) * 0.8 }));

/**
 * Shared layout for the auth pages, in the site's design: the night sky with the chapter's tagline on
 * one side (from `md` up) and the form on the other.
 *
 * Props:
 *  - title        : string — form heading
 *  - subtitle     : string — small description below heading
 *  - panelTagline : node   — the line on the night-sky panel
 *  - panelSub     : string — supporting text on the night-sky panel
 *  - isReversed   : bool   — put the night-sky panel on the right (sign up)
 *  - children     : node   — form content
 */
const AuthLayout = ({ title, subtitle, panelTagline, panelSub, isReversed = false, children }) => {
  const [isDark, setIsDark] = useState(() => {
    const savedTheme = localStorage.getItem("theme");
    if (savedTheme) return savedTheme === "dark";
    return window.matchMedia("(prefers-color-scheme: dark)").matches;
  });
  useEffect(() => {
    document.documentElement.classList.toggle("dark", isDark);
    localStorage.setItem("theme", isDark ? "dark" : "light");
  }, [isDark]);

  return (
    <div className={`hp admin-ui flex min-h-screen bg-[#F3F7FB] text-slate-800 dark:bg-[#0A1724] dark:text-slate-100 ${isReversed ? "flex-row-reverse" : ""}`}>
      <aside aria-hidden="true" className="relative hidden w-[44%] shrink-0 overflow-hidden bg-[linear-gradient(180deg,#081826_0%,#06121D_45%,#0B2236_78%,#0F3A5A_100%)] md:block">
        <div className="absolute inset-0">
          {stars.map((s, i) => (
            <span key={i} className="absolute rounded-full bg-[#D3E9F7]" style={{ left: `${s.x}%`, top: `${s.y}%`, width: s.size, height: s.size, animation: `hp-twinkle ${3.5 + s.delay}s ease-in-out ${s.delay}s infinite` }} />
          ))}
        </div>
        <NightSky />
        <div className="relative z-10 flex h-full flex-col p-10 lg:p-12">
          <Link to="/" tabIndex={-1} className="flex items-center gap-3 text-white">
            <span className="flex h-11 w-11 items-center justify-center rounded-full bg-white"><img src={acmLogo} alt="" className="h-8 w-8 object-contain" /></span>
            <span className="text-base font-semibold">ACM Alexandria</span>
          </Link>
          <div className="my-auto max-w-sm pb-12">
            <p className="text-3xl font-semibold leading-tight tracking-tight text-[#FFF4CC]" style={{ animation: "hp-rise 0.7s cubic-bezier(0.22,1,0.36,1) 0.1s both" }}>{panelTagline}</p>
            {panelSub && <p className="mt-4 text-base leading-relaxed text-[#CFE6F5]" style={{ animation: "hp-rise 0.7s ease-out 0.2s both" }}>{panelSub}</p>}
          </div>
        </div>
      </aside>

      <div className="flex min-h-screen flex-1 flex-col">
        <header className="flex items-center justify-between gap-4 px-5 py-5 sm:px-8">
          <Link to="/" className="inline-flex items-center gap-2 rounded-xl px-2 py-2 text-sm font-semibold text-slate-600 transition-colors hover:text-[#205E85] dark:text-[#A9C7DB] dark:hover:text-white">
            <FiArrowLeft className="h-4 w-4" aria-hidden="true" />
            <span className="md:hidden"><img src={acmLogo} alt="" className="mr-1 inline h-6 w-6 object-contain align-[-6px]" />ACM Alexandria</span>
            <span className="hidden md:inline">Back to home</span>
          </Link>
          <button type="button" onClick={(event) => switchTheme(event, !isDark, () => setIsDark(!isDark))} aria-label={isDark ? "Switch to light mode" : "Switch to dark mode"} className="flex h-10 w-10 items-center justify-center rounded-full text-slate-600 transition-colors hover:bg-white dark:text-[#A9C7DB] dark:hover:bg-white/5">
            {isDark ? <FiSun className="h-[18px] w-[18px]" aria-hidden="true" /> : <FiMoon className="h-[18px] w-[18px]" aria-hidden="true" />}
          </button>
        </header>
        <main className="flex flex-1 items-center justify-center px-5 pb-12 sm:px-8">
          <div className="w-full max-w-md">
            <div className="mb-8" style={{ animation: "hp-rise 0.6s cubic-bezier(0.22,1,0.36,1) both" }}>
              <h1 className="text-3xl font-semibold tracking-[-0.03em] text-slate-900 sm:text-4xl dark:text-white">{title}</h1>
              {subtitle && <p className="mt-2 text-base leading-relaxed text-slate-600 dark:text-[#A9C7DB]">{subtitle}</p>}
            </div>
            <div className="rounded-[2rem] bg-white p-6 shadow-[0_24px_60px_-32px_rgba(15,58,90,0.35)] ring-1 ring-[#D6E6F2] sm:p-8 dark:bg-[#0F2233] dark:ring-white/10" style={{ animation: "hp-rise 0.6s cubic-bezier(0.22,1,0.36,1) 0.08s both" }}>
              {children}
            </div>
          </div>
        </main>
      </div>
    </div>
  );
};

export default AuthLayout;
