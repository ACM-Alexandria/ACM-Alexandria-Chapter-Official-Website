import { FaLinkedinIn } from "react-icons/fa";
import api from "../../services/api";
import { focusRing } from "../ui/uiStyles";

// The link a post shares: the backend's share page, which carries the LinkedIn preview (title and image)
// and sends visitors on to the badge page.
const badgeShareUrl = (publicId) => `${api.defaults.baseURL}/share/badges/${publicId}`;

const linkedInUrl = (badge) => {
  const text = `I'm happy to share that I was named ${badge.name} of the ${badge.committeeName} `
    + `for ${badge.periodLabel} at ACM Alexandria Student Chapter.\n\n${badgeShareUrl(badge.publicId)}`;
  return `https://www.linkedin.com/feed/?shareActive=true&text=${encodeURIComponent(text)}`;
};

const variants = {
  // A quiet text link, for the profile's badge cards.
  link: "-mx-2 h-9 rounded-lg px-2 text-ocean-573 hover:bg-ocean-31 hover:text-ocean-676 dark:text-ocean-316 dark:hover:bg-white/5 dark:hover:text-ocean-222",
  // An outline button on the deep-blue badge card.
  dark: "h-10 rounded-xl px-4 text-white ring-1 ring-white/30 hover:bg-white/10",
};

// Opens LinkedIn's post box with a ready-made message and the badge's link; the member can edit it first.
const ShareOnLinkedInButton = ({ badge, variant = "link", className = "" }) => (
  <a
    href={linkedInUrl(badge)}
    target="_blank"
    rel="noopener noreferrer"
    className={`inline-flex items-center gap-2 text-sm font-semibold transition-colors ${variants[variant]} ${focusRing} ${className}`}
  >
    <FaLinkedinIn className="h-4 w-4" aria-hidden="true" />
    Share on LinkedIn
  </a>
);

export default ShareOnLinkedInButton;
