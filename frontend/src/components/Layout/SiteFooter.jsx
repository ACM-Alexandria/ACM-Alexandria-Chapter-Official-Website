import { useEffect, useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { FiFacebook, FiGlobe, FiInstagram, FiLinkedin, FiMail, FiTwitter, FiYoutube } from "react-icons/fi";
import logo from "../../assets/logo/acm-logo-no-bg.png";
import { useAuth } from "../../contexts/useAuth";
import { getEnv } from "../../utils/env";
import UnsubscribeConfirmModal from "../UnsubscribeConfirmModal";
import HelpUsGrowModal from "../feedback/HelpUsGrowModal";
import HRFeedbackModal from "../feedback/HRFeedbackModal";
import { fetchNewsSubscriptionStatus, fetchSocialLinks, subscribeToNews, unsubscribeFromNews } from "../../services/homePageService";
import { contactEmail as fallbackEmail } from "../Home/homeContent";

const icons = { Facebook: FiFacebook, Instagram: FiInstagram, LinkedIn: FiLinkedin, Linkedin: FiLinkedin, YouTube: FiYoutube, Youtube: FiYoutube, X: FiTwitter, Twitter: FiTwitter };
const linkClass = "text-sm text-ocean-145 transition-colors hover:text-white";
const heading = "text-sm font-semibold text-white";

// The chapter's logo, email and social links on the deep blue, then the section links, resources, and the
// newsletter. Keeps every action of the previous footer: news subscription, "Help us grow", and suggestions
// and complaints for members. `links` are [href, label] pairs ("#id" on this page or "/path"). Without a
// `social` list it loads the chapter's social links itself.
const SiteFooter = ({ social, links = [], showPartners = false }) => {
  const [loadedSocial, setLoadedSocial] = useState([]);
  const navigate = useNavigate();
  const { isAuthenticated, user } = useAuth();
  const [subscribed, setSubscribed] = useState(false);
  const [busy, setBusy] = useState(false);
  const [newsOpen, setNewsOpen] = useState(false);
  const [growOpen, setGrowOpen] = useState(false);
  const [feedbackOpen, setFeedbackOpen] = useState(false);
  const canSubmitFeedback = isAuthenticated && user?.role !== "USER";
  const email = getEnv("VITE_CONTACT_EMAIL") || fallbackEmail;

  useEffect(() => {
    let active = true;
    if (!isAuthenticated) {
      setSubscribed(false);
      return undefined;
    }
    setBusy(true);
    fetchNewsSubscriptionStatus()
      .then((data) => { if (active) setSubscribed(Boolean(data?.subscribed)); })
      .catch((err) => console.error("Error fetching news status:", err))
      .finally(() => { if (active) setBusy(false); });
    return () => { active = false; };
  }, [isAuthenticated]);

  useEffect(() => {
    if (social) return undefined;
    let active = true;
    fetchSocialLinks()
      .then((data) => { if (active) setLoadedSocial(Array.isArray(data) ? data : []); })
      .catch((err) => console.error("Error loading social links:", err));
    return () => { active = false; };
  }, [social]);
  const socialLinks = social || loadedSocial;

  const askNews = () => (isAuthenticated ? setNewsOpen(true) : navigate("/login"));
  const toggleNews = async () => {
    setBusy(true);
    try {
      if (subscribed) await unsubscribeFromNews();
      else await subscribeToNews();
      setSubscribed(!subscribed);
      setNewsOpen(false);
    } catch (err) {
      console.error("Error toggling news subscription:", err);
    } finally {
      setBusy(false);
    }
  };

  return (
    <footer id="footer" className="bg-ocean-794 text-ocean-145 dark:bg-ocean-910">
      <div className="mx-auto max-w-[1280px] px-5 py-14 sm:px-8">
        <div className="flex flex-col gap-8 md:flex-row md:items-center md:justify-between">
          <div className="flex items-center gap-4">
            <span className="flex h-12 w-12 items-center justify-center rounded-full bg-white"><img src={logo} alt="" className="h-9 w-9 object-contain" /></span>
            <div>
              <p className="text-lg font-semibold text-white">ACM Alexandria</p>
              <a href={`mailto:${email}`} className="mt-0.5 inline-flex items-center gap-1.5 text-sm hover:text-white"><FiMail className="h-4 w-4" aria-hidden="true" />{email}</a>
            </div>
          </div>
          {socialLinks.length > 0 && (
            <ul className="flex gap-3">
              {socialLinks.map((s) => {
                const Icon = icons[s.platform] || FiGlobe;
                return (
                  <li key={s.url}>
                    <a href={s.url} target="_blank" rel="noopener noreferrer" aria-label={s.platform} title={s.platform} className="flex h-10 w-10 items-center justify-center rounded-full border border-white/15 transition-colors hover:bg-white hover:text-ocean-676">
                      <Icon className="h-4 w-4" aria-hidden="true" />
                    </a>
                  </li>
                );
              })}
            </ul>
          )}
        </div>

        <div className="mt-12 grid gap-10 border-t border-white/10 pt-12 sm:grid-cols-2 lg:grid-cols-[1fr_1fr_1.3fr]">
          {links.length > 0 && (
            <nav aria-label="Footer">
              <p className={heading}>Explore</p>
              <ul className="mt-4 grid gap-3">
                {links.map(([href, label]) => <li key={href}>{href.startsWith("#") ? <a href={href} className={linkClass}>{label}</a> : <Link to={href} className={linkClass}>{label}</Link>}</li>)}
                <li><Link to="/boards" className={linkClass}>Past boards</Link></li>
              </ul>
            </nav>
          )}
          <div>
            <p className={heading}>Resources</p>
            <ul className="mt-4 grid gap-3">
              <li><Link to="/register" className={linkClass}>Join the community</Link></li>
              <li><Link to="/rules" className={linkClass}>Rules</Link></li>
              {showPartners && <li><a href="#partners" className={linkClass}>Partners</a></li>}
              <li><button type="button" onClick={() => (isAuthenticated ? setGrowOpen(true) : navigate("/login"))} className={linkClass}>Help us grow</button></li>
              {canSubmitFeedback && <li><button type="button" onClick={() => setFeedbackOpen(true)} className={linkClass}>Suggestions and complaints</button></li>}
            </ul>
          </div>
          <div className="rounded-3xl bg-white/5 p-6 ring-1 ring-white/10">
            <p className={heading}>Newsletter</p>
            <p className="mt-2 text-sm leading-relaxed">Get the chapter's news and upcoming events in your inbox.</p>
            <button type="button" onClick={askNews} disabled={busy} className={`mt-5 inline-flex h-11 items-center justify-center rounded-xl px-5 text-sm font-semibold transition-colors disabled:opacity-60 ${subscribed ? "text-white ring-1 ring-white/25 hover:bg-white/10" : "bg-white text-ocean-676 hover:bg-ocean-67"}`}>
              {busy ? "One moment…" : subscribed ? "Unsubscribe from news" : "Subscribe to news"}
            </button>
          </div>
        </div>

        <div className="mt-12 flex flex-col gap-3 border-t border-white/10 pt-6 text-xs text-ocean-145/70 sm:flex-row sm:items-center sm:justify-between">
          <p>© {new Date().getFullYear()} ACM Alexandria Student Chapter</p>
        </div>
      </div>

      <UnsubscribeConfirmModal open={newsOpen} onClose={() => setNewsOpen(false)} onConfirm={toggleNews} topicName="ACM Newsletter Updates" loading={busy} isSubscribe={!subscribed} />
      <HelpUsGrowModal open={growOpen} onClose={() => setGrowOpen(false)} />
      <HRFeedbackModal open={feedbackOpen} onClose={() => setFeedbackOpen(false)} />
    </footer>
  );
};

export default SiteFooter;
