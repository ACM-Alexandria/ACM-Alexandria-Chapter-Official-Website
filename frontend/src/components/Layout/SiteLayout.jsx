import IslandNav from "./IslandNav";
import SiteFooter from "./SiteFooter";
import { useSmoothScroll } from "../../hooks/useSmoothScroll";
import { siteSections as sections } from "./siteSections";

// The frame of every public page other than home: the island navbar, smooth scrolling, the page, and the footer.
// Pages start with a PageHero so the transparent navbar sits on deep blue, as it does over the home hero;
// a page without one passes `solidNav` so the navbar stays solid on the light background.
const SiteLayout = ({ children, solidNav = false }) => {
  useSmoothScroll();
  return (
    <div className="hp admin-ui flex min-h-screen flex-col bg-ocean-31 text-slate-800 dark:bg-ocean-911 dark:text-slate-100">
      <IslandNav links={sections} home={false} solid={solidNav} />
      <main className="flex-1">{children}</main>
      <SiteFooter links={sections} />
    </div>
  );
};

export default SiteLayout;
