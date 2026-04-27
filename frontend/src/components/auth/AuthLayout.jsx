import acmLogo from "../../assets/logo/acm-logo-no-bg.png";

/**
 * Shared layout for all auth pages.
 * Pure Tailwind — no external CSS file.
 *
 * Props:
 *  - title        : string — form heading
 *  - subtitle     : string — small description below heading
 *  - panelTagline : node   — JSX tagline for the left panel
 *  - panelSub     : string — supporting text for the left panel
 *  - activeDot    : number — which bottom indicator dot is active (0-based)
 *  - children     : node   — form content
 */
const AuthLayout = ({
  title,
  subtitle,
  panelTagline,
  panelSub,
  activeDot = 0,
  isReversed = false,
  children,
}) => {
  return (
    <div key={isReversed ? "register" : "login"} className="min-h-screen w-full flex items-center justify-center px-4 py-12 bg-gray-50 relative overflow-hidden">

      {/* Subtle background blobs — site palette colours */}
      <div
        aria-hidden="true"
        className="pointer-events-none fixed -top-32 -left-28 w-[480px] h-[480px] rounded-full bg-[#4B98C8]/20 blur-3xl animate-[drift_18s_ease-in-out_infinite]"
      />
      <div
        aria-hidden="true"
        className="pointer-events-none fixed -bottom-24 -right-24 w-[380px] h-[380px] rounded-full bg-[#205E85]/15 blur-3xl animate-[drift-r_22s_ease-in-out_infinite]"
      />

      {/* Card */}
      <div
        className={`
          relative z-10 w-full max-w-[860px] flex ${isReversed ? "flex-row-reverse" : "flex-row"} rounded-2xl overflow-hidden
          shadow-2xl border border-gray-200
          animate-[${isReversed ? "cardSlideLeft" : "cardSlideRight"}_0.7s_cubic-bezier(0.22,1,0.36,1)_both]
        `}
      >

        {/* ── Side decorative panel (left or right) ── */}
        <aside
          aria-hidden="true"
          className="
            hidden md:flex flex-col justify-between
            w-[42%] shrink-0
            bg-gradient-to-br from-[#4B98C8] to-[#205E85]
            px-10 py-10 relative overflow-hidden
          "
        >
          {/* Grid texture */}
          <div
            className="absolute inset-0 pointer-events-none"
            style={{
              backgroundImage:
                "linear-gradient(rgba(255,255,255,0.07) 1px,transparent 1px),linear-gradient(90deg,rgba(255,255,255,0.07) 1px,transparent 1px)",
              backgroundSize: "36px 36px",
            }}
          />
          {/* Soft radial glow */}
          <div className="absolute inset-0 pointer-events-none bg-[radial-gradient(ellipse_70%_50%_at_30%_20%,rgba(255,255,255,0.12),transparent)]" />

          {/* Logo */}
          <div
            className="relative z-10 flex items-center gap-3"
            style={{ animation: `${isReversed ? "slideRight" : "slideLeft"} 0.7s cubic-bezier(0.22,1,0.36,1) 0.2s both` }}
          >
            <img src={acmLogo} alt="ACM Logo" className="h-11 w-auto" />
            <div className="leading-tight">
              <p className="text-white font-extrabold text-[15px] tracking-tight">ACM ALEXANDRIA</p>
              <p className="text-blue-100/80 text-[10px] font-bold tracking-[0.12em] uppercase">Student Chapter</p>
            </div>
          </div>

          {/* Central copy */}
          <div
            className="relative z-10"
            style={{ animation: `${isReversed ? "slideRight" : "slideLeft"} 0.7s cubic-bezier(0.22,1,0.36,1) 0.35s both` }}
          >
            <div className="text-white font-extrabold text-[1.85rem] leading-[1.2] tracking-tight mb-4">
              {typeof panelTagline === 'string' ? (
                panelTagline.split(' ').map((word, i) => (
                  <span
                    key={i}
                    className="inline-block mr-[0.25em]"
                    style={{
                      animation: `revealWord 0.5s cubic-bezier(0.22,1,0.36,1) ${0.5 + i * 0.1}s both`
                    }}
                  >
                    {word}
                  </span>
                ))
              ) : (
                panelTagline
              )}
            </div>
            <p
              className="text-blue-50/70 text-[15px] leading-relaxed font-medium"
              style={{ animation: "fadeIn 0.8s ease 1.2s both" }}
            >
              {panelSub}
            </p>
          </div>

          {/* Dots */}
          <div
            className="relative z-10 flex gap-1.5"
            style={{ animation: `${isReversed ? "slideRight" : "slideLeft"} 0.7s cubic-bezier(0.22,1,0.36,1) 0.45s both` }}
          >
            {[0, 1, 2].map((i) => (
              <div
                key={i}
                className={`rounded-full transition-all duration-300 ${
                  i === activeDot
                    ? "w-4 h-2 bg-white"
                    : "w-2 h-2 bg-white/30"
                }`}
              />
            ))}
          </div>
        </aside>

        {/* ── Right form panel ── */}
        <main className="flex-1 bg-white px-8 py-10 sm:px-12 sm:py-14 flex flex-col justify-center">

          {/* Mobile logo */}
          <div className="md:hidden flex items-center gap-2.5 mb-8 animate-[floatIn_0.5s_cubic-bezier(0.22,1,0.36,1)_0.05s_both]">
            <img src={acmLogo} alt="ACM Logo" className="h-9 w-auto" />
            <div className="leading-tight">
              <p className="text-slate-900 font-extrabold text-sm tracking-tight">ACM ALEXANDRIA</p>
              <p className="text-slate-500 text-[9px] font-bold tracking-widest uppercase">Student Chapter</p>
            </div>
          </div>

          {/* Heading */}
          <div className="mb-9 animate-[floatIn_0.6s_cubic-bezier(0.22,1,0.36,1)_0.15s_both]">
            <h1 className="text-[32px] font-extrabold text-slate-900 tracking-[-0.03em] mb-2 leading-tight">{title}</h1>
            <p className="text-base text-slate-500 leading-relaxed font-medium">{subtitle}</p>
          </div>

          {/* Form */}
          <div className="animate-[floatIn_0.6s_cubic-bezier(0.22,1,0.36,1)_0.25s_both]">
            {children}
          </div>
        </main>
      </div>

      {/* Keyframe definitions via inline style tag */}
      <style>{`
        @keyframes floatIn {
          from { opacity: 0; transform: translateY(24px) scale(0.97); }
          to   { opacity: 1; transform: translateY(0)    scale(1);    }
        }
        @keyframes cardSlideRight {
          from { opacity: 0; transform: translateX(-40px) scale(0.98); }
          to   { opacity: 1; transform: translateX(0)     scale(1);    }
        }
        @keyframes cardSlideLeft {
          from { opacity: 0; transform: translateX(40px) scale(0.98); }
          to   { opacity: 1; transform: translateX(0)    scale(1);    }
        }
        @keyframes slideLeft {
          from { opacity: 0; transform: translateX(-28px); }
          to   { opacity: 1; transform: translateX(0);     }
        }
        @keyframes slideRight {
          from { opacity: 0; transform: translateX(28px); }
          to   { opacity: 1; transform: translateX(0);    }
        }
        @keyframes drift {
          0%,100% { transform: translate(0,0)        scale(1);    }
          33%      { transform: translate(40px,-30px) scale(1.07); }
          66%      { transform: translate(-20px,20px) scale(0.95); }
        }
        @keyframes drift-r {
          0%,100% { transform: translate(0,0)         scale(1);    }
          33%      { transform: translate(-35px,25px)  scale(1.05); }
          66%      { transform: translate(25px,-20px)  scale(0.97); }
        }
        @keyframes shimmer {
          0%   { background-position: -200% center; }
          100% { background-position:  200% center; }
        }
        @keyframes spin {
          to { transform: rotate(360deg); }
        }
        @keyframes successPop {
          0%   { transform: scale(0)    rotate(-15deg); opacity: 0; }
          60%  { transform: scale(1.15) rotate(5deg);  opacity: 1; }
          100% { transform: scale(1)    rotate(0deg);  opacity: 1; }
        }
        @keyframes shake {
          0%,100% { transform: translateX(0);   }
          20%      { transform: translateX(-6px); }
          40%      { transform: translateX(6px);  }
          60%      { transform: translateX(-4px); }
          80%      { transform: translateX(4px);  }
        }
        @keyframes fadeIn {
          from { opacity: 0; }
          to   { opacity: 1; }
        }
        @keyframes revealWord {
          from { opacity: 0; transform: translateY(10px); }
          to   { opacity: 1; transform: translateY(0);    }
        }
      `}</style>
    </div>
  );
};

export default AuthLayout;
