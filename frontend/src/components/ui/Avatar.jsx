import { useState } from "react";
import { initials } from "../../utils/format";

// Profile photo with an initials fallback when the image is missing or fails to load.
const Avatar = ({ name, src, className = "h-12 w-12 text-sm" }) => {
  const [failed, setFailed] = useState(false);
  if (!src || failed) {
    return <span className={`flex shrink-0 items-center justify-center rounded-full bg-[#E2F0FA] font-semibold text-[#1D6A9C] dark:bg-[#16364C] dark:text-[#7DBBE0] ${className}`}>{initials(name)}</span>;
  }
  return <img src={src} alt={name} loading="lazy" onError={() => setFailed(true)} className={`shrink-0 rounded-full object-cover ${className}`} />;
};

export default Avatar;
