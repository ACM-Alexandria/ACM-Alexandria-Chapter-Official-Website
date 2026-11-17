import logo from "../../assets/logo/acm-logo-no-bg.png";
import { TALL_STRIPS } from "./confetti";

// The backdrop of the badge card on phones (the wide layout is BadgeBanner): a few confetti strips in the
// margins, a thin gold frame, and the ACM logo in a white disc in the corner. Decorative only; put it first
// inside a `relative overflow-hidden` card.
const BadgeBackdrop = () => (
  <>
    <svg aria-hidden="true" viewBox="0 0 400 760" preserveAspectRatio="xMidYMid slice" className="pointer-events-none absolute inset-0 h-full w-full">
      {TALL_STRIPS.map(([x, y, rotation, length, color, opacity], i) => (
        <rect key={i} x={-length / 2} y={-length * 0.225} width={length} height={length * 0.45} rx="1.5" fill={color} opacity={opacity} transform={`translate(${x} ${y}) rotate(${rotation})`} />
      ))}
    </svg>
    <span aria-hidden="true" className="pointer-events-none absolute inset-3 rounded-[1.5rem] border border-gold-518/55" />
    <span aria-hidden="true" className="absolute right-5 top-5 flex h-14 w-14 items-center justify-center rounded-full bg-white shadow-[0_10px_26px_-10px_rgba(0,0,0,0.5)] sm:right-8 sm:top-8 sm:h-16 sm:w-16">
      <img src={logo} alt="" className="h-[86%] w-[86%] object-contain" />
    </span>
  </>
);

export default BadgeBackdrop;
