import { FaLinkedinIn } from "react-icons/fa";
import api from "../../services/api";
import { focusRing } from "../ui/uiStyles";
import { badgeGroupLabel } from "./badgeGroup";

// The link a post shares: the backend's share page, which carries the LinkedIn preview (title and image)
// and sends visitors on to the badge page.
const badgeShareUrl = (publicId) => `${api.defaults.baseURL}/share/badges/${publicId}`;

// LinkedIn turns these into clickable hashtags. (A real @mention of the chapter's page can't be filled in
// from a link; the member can add one by typing @ in the post box.)
const HASHTAGS = "#ACMAlex #ACMAlexandria";

const linkedInUrl = (badge) => {
  const text = `I'm happy to share that I was named ${badge.name} of the ${badgeGroupLabel(badge)} `
    + `for ${badge.periodLabel} at ACM Alexandria Student Chapter.\n\n${badgeShareUrl(badge.publicId)}\n\n${HASHTAGS}`;
  return `https://www.linkedin.com/feed/?shareActive=true&text=${encodeURIComponent(text)}`;
};

const variants = {
  // A quiet text link, for the profile's badge cards.
  link: "text-sm -mx-2 h-9 rounded-lg px-2 text-ocean-573 hover:bg-ocean-31 hover:text-ocean-676 dark:text-ocean-316 dark:hover:bg-white/5 dark:hover:text-ocean-222",
  // An outline button on the deep-blue badge card on phones.
  dark: "text-sm h-10 rounded-xl px-4 text-white ring-1 ring-white/30 hover:bg-white/10",
  // An outline button inside the badge banner: sized in em, so it scales with the banner's font size.
  banner: "rounded-[0.7em] px-[1.1em] py-[0.65em] text-white ring-1 ring-white/35 hover:bg-white/10",
};

// Opens LinkedIn's post box with a ready-made message and the badge's link; the member can edit it first.
const ShareOnLinkedInButton = ({ badge, variant = "link", className = "" }) => (
  <a
    href={linkedInUrl(badge)}
    target="_blank"
    rel="noopener noreferrer"
    className={`inline-flex items-center gap-[0.5em] font-semibold transition-colors ${variants[variant]} ${focusRing} ${className}`}
  >
    <FaLinkedinIn className="h-[1.15em] w-[1.15em]" aria-hidden="true" />
    Share on LinkedIn
  </a>
);

export default ShareOnLinkedInButton;
