import { 
  FaFacebookF, 
  FaLinkedinIn, 
  FaInstagram, 
  FaTwitter, 
  FaYoutube, 
  FaGithub, 
  FaDiscord, 
  FaGlobe,
  FaSlack,
  FaTelegram,
  FaReddit,
  FaTiktok,
  FaMedium,
  FaTwitch,
  FaWhatsapp,
  FaPinterest,
  FaSnapchat,
  FaDribbble,
  FaBehance,
  FaStackOverflow
} from "react-icons/fa";
import { FiShare2 } from "react-icons/fi";

export const getSocialIcon = (url) => {
  if (!url) return <FiShare2 />;
  const normalizedUrl = url.toLowerCase().trim();
  if (normalizedUrl.includes("facebook.com") || normalizedUrl.includes("fb.com")) {
    return <FaFacebookF />;
  }
  if (normalizedUrl.includes("linkedin.com")) {
    return <FaLinkedinIn />;
  }
  if (normalizedUrl.includes("instagram.com")) {
    return <FaInstagram />;
  }
  if (normalizedUrl.includes("twitter.com") || normalizedUrl.includes("x.com")) {
    return <FaTwitter />;
  }
  if (normalizedUrl.includes("youtube.com") || normalizedUrl.includes("youtu.be")) {
    return <FaYoutube />;
  }
  if (normalizedUrl.includes("github.com")) {
    return <FaGithub />;
  }
  if (normalizedUrl.includes("discord.gg") || normalizedUrl.includes("discord.com")) {
    return <FaDiscord />;
  }
  if (normalizedUrl.includes("slack.com")) {
    return <FaSlack />;
  }
  if (normalizedUrl.includes("t.me") || normalizedUrl.includes("telegram.org")) {
    return <FaTelegram />;
  }
  if (normalizedUrl.includes("reddit.com")) {
    return <FaReddit />;
  }
  if (normalizedUrl.includes("tiktok.com")) {
    return <FaTiktok />;
  }
  if (normalizedUrl.includes("medium.com")) {
    return <FaMedium />;
  }
  if (normalizedUrl.includes("twitch.tv") || normalizedUrl.includes("twitch.com")) {
    return <FaTwitch />;
  }
  if (normalizedUrl.includes("whatsapp.com") || normalizedUrl.includes("wa.me")) {
    return <FaWhatsapp />;
  }
  if (normalizedUrl.includes("pinterest.com")) {
    return <FaPinterest />;
  }
  if (normalizedUrl.includes("snapchat.com")) {
    return <FaSnapchat />;
  }
  if (normalizedUrl.includes("dribbble.com")) {
    return <FaDribbble />;
  }
  if (normalizedUrl.includes("behance.net") || normalizedUrl.includes("behance.com")) {
    return <FaBehance />;
  }
  if (normalizedUrl.includes("stackoverflow.com")) {
    return <FaStackOverflow />;
  }
  return <FaGlobe />;
};
