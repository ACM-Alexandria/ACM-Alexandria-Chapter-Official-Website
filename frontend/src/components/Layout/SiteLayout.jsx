import IslandNav from "./IslandNav";
import SiteFooter from "./SiteFooter";
import { useSmoothScroll } from "../../hooks/useSmoothScroll";

// The same sections as the home page's navbar; each opens the home page at that section.
const sections = [["/#people", "People"], ["/#events", "Events"], ["/#programs", "Programs"], ["/#radio", "Radio"]];

// The frame of every public page other than home: the island navbar, smooth scrolling, the page, and the footer.
// Pages start with a PageHero so the transparent navbar sits on deep blue, as it does over the home hero.
const SiteLayout = ({ children }) => {
  useSmoothScroll();
  return (
    <div className="hp admin-ui flex min-h-screen flex-col bg-[#F3F7FB] text-slate-800 dark:bg-[#0A1724] dark:text-slate-100">
      <IslandNav links={sections} home={false} />
      <main className="flex-1">{children}</main>
      <SiteFooter links={sections} />
    </div>
  );
};

export default SiteLayout;
