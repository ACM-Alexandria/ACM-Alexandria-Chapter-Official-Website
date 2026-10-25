import { initials } from "../../utils/format";

// A committee or club logo, or its initials on soft blue when it has no logo.
const TeamLogo = ({ src, name, className = "" }) => (src
  ? <img src={src} alt="" className={`object-cover ${className}`} loading="lazy" />
  : <span className={`flex shrink-0 items-center justify-center bg-[#E2F0FA] text-[0.6rem] font-semibold text-[#1D6A9C] dark:bg-[#16364C] dark:text-[#7DBBE0] ${className}`} aria-hidden="true">{initials(name)}</span>);

export default TeamLogo;
