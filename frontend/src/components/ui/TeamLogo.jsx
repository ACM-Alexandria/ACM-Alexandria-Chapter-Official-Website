import { initials } from "../../utils/format";

// A committee or club logo, or its initials on soft blue when it has no logo.
const TeamLogo = ({ src, name, className = "" }) => (src
  ? <img src={src} alt="" className={`object-cover ${className}`} loading="lazy" />
  : <span className={`flex shrink-0 items-center justify-center bg-ocean-67 text-[0.6rem] font-semibold text-ocean-637 dark:bg-ocean-808 dark:text-ocean-316 ${className}`} aria-hidden="true">{initials(name)}</span>);

export default TeamLogo;
