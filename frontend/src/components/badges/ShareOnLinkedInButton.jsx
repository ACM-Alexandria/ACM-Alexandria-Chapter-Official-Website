import { useState } from "react";
import { FaLinkedinIn } from "react-icons/fa";
import api from "../../services/api";
import { getEnv } from "../../utils/env";
import { focusRing } from "../ui/uiStyles";
import { badgeGroupLabel } from "./badgeGroup";

// The link a post shares: the backend's share page, which carries the LinkedIn preview (title and image)
// and sends visitors on to the badge page. VITE_SHARE_BASE_URL is the public address those pages are served
// from (a separate proxy in production); without it the API's address is used.
const shareBase = () => (getEnv("VITE_SHARE_BASE_URL") || api.defaults.baseURL).replace(/\/$/, "");
const badgeShareUrl = (publicId) => `${shareBase()}/share/badges/${publicId}`;

// LinkedIn turns these into clickable hashtags. (A real @mention of the chapter's page can't be filled in
// from a link; the member can add one by typing @ in the post box.)
const HASHTAGS = "#ACMAlex #ACMAlexandria";

const shareText = (badge) => `I'm happy to share that I was named ${badge.name} of the ${badgeGroupLabel(badge)} `
  + `for ${badge.periodLabel} at ACM Alexandria Student Chapter.\n\n${badgeShareUrl(badge.publicId)}\n\n${HASHTAGS}`;

// On a computer: LinkedIn's post box on the web, with the message already written.
const linkedInUrl = (badge) => `https://www.linkedin.com/feed/?shareActive=true&text=${encodeURIComponent(shareText(badge))}`;

// On a phone the LinkedIn app can't take a ready-made message (it ignores the text in the web link above), so the
// button copies the post instead and tells the member to paste it into a new LinkedIn post. The badge link in the
// text gives the post its preview.
const isPhone = () => typeof window !== "undefined" && window.matchMedia?.("(pointer: coarse)").matches;

// The clipboard API, or the older select-and-copy where it isn't available.
const copyText = async (text) => {
  try {
    await navigator.clipboard.writeText(text);
    return true;
  } catch {
    const area = document.createElement("textarea");
    area.value = text;
    area.setAttribute("readonly", "");
    area.style.position = "fixed";
    area.style.opacity = "0";
    document.body.appendChild(area);
    area.select();
    const ok = document.execCommand("copy");
    area.remove();
    return ok;
  }
};

const variants = {
  // A quiet text link, for the profile's badge cards.
  link: "text-sm -mx-2 h-9 rounded-lg px-2 text-ocean-573 hover:bg-ocean-31 hover:text-ocean-676 dark:text-ocean-316 dark:hover:bg-white/5 dark:hover:text-ocean-222",
  // An outline button on the deep-blue badge card on phones.
  dark: "text-sm h-10 rounded-xl px-4 text-white ring-1 ring-white/30 hover:bg-white/10",
  // An outline button inside the badge banner: sized in em, so it scales with the banner's font size.
  banner: "rounded-[0.7em] px-[1.1em] py-[0.65em] text-white ring-1 ring-white/35 hover:bg-white/10",
};

const noteTone = {
  link: "text-slate-600 dark:text-ocean-239",
  dark: "text-white/80",
  banner: "text-white/80",
};

// On a computer: opens LinkedIn's post box with a ready-made message and the badge's link; the member can edit it.
// On a phone: copies that message and asks the member to paste it into a new post in the LinkedIn app.
const ShareOnLinkedInButton = ({ badge, variant = "link", className = "" }) => {
  const [phone] = useState(isPhone);
  const [note, setNote] = useState("");

  const copyPost = async (event) => {
    if (!phone) return;
    event.preventDefault();
    const copied = await copyText(shareText(badge));
    setNote(copied
      ? "Post copied. Open LinkedIn, start a new post and paste it."
      : "Couldn't copy the post. Try again.");
  };

  return (
    <span className={`inline-flex flex-col items-start gap-1 ${className}`}>
      <a
        href={linkedInUrl(badge)}
        onClick={copyPost}
        target="_blank"
        rel="noopener noreferrer"
        className={`inline-flex items-center gap-[0.5em] font-semibold transition-colors ${variants[variant]} ${focusRing}`}
      >
        <FaLinkedinIn className="h-[1.15em] w-[1.15em]" aria-hidden="true" />
        {phone ? "Copy post for LinkedIn" : "Share on LinkedIn"}
      </a>
      <span role="status" aria-live="polite" className={`text-[0.85em] empty:hidden ${noteTone[variant]}`}>{note}</span>
    </span>
  );
};

export default ShareOnLinkedInButton;
