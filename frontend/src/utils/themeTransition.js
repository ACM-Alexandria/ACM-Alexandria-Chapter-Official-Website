import { flushSync } from "react-dom";

// Switches the theme with a circle from the clicked button. Going dark, the new colours grow from the button until
// they cover the page; going light, the dark page shrinks back into the button and uncovers the light one.
// The circle itself is a CSS animation (styles/motion.css) driven by the variables set here, so it starts in the
// very frame the browser swaps the pages, with no gap. `apply` sets the theme state; the `dark` class is flipped
// here too, inside the transition, so the browser captures the new page at the right moment. Without view
// transitions (or with reduced motion) it just switches.
export const switchTheme = (event, nextDark, apply) => {
  const toggle = () => {
    flushSync(apply);
    document.documentElement.classList.toggle("dark", nextDark);
  };
  const reduced = window.matchMedia("(prefers-reduced-motion: reduce)").matches;
  if (!document.startViewTransition || reduced) return toggle();

  const box = event.currentTarget.getBoundingClientRect();
  const x = box.left + box.width / 2;
  const y = box.top + box.height / 2;
  const root = document.documentElement;
  root.style.setProperty("--theme-x", `${x}px`);
  root.style.setProperty("--theme-y", `${y}px`);
  root.style.setProperty("--theme-r", `${Math.hypot(Math.max(x, window.innerWidth - x), Math.max(y, window.innerHeight - y))}px`);
  root.dataset.themeDir = nextDark ? "grow" : "shrink";
  const transition = document.startViewTransition(toggle);
  // A skipped transition (the tab is in the background) rejects; the theme has switched anyway.
  transition.finished.finally(() => { delete root.dataset.themeDir; }).catch(() => {});
};
