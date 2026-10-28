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

  // The circle starts from the sun or moon icon of the control that was pressed (the menu's theme row is as wide
  // as the whole menu, so its own middle would be far from the icon).
  const anchor = event.currentTarget.querySelector("svg") || event.currentTarget;
  const box = anchor.getBoundingClientRect();
  const x = box.left + box.width / 2;
  const y = box.top + box.height / 2;
  const root = document.documentElement;
  // Everything is a percentage of the page, so the circle lands on the icon however the browser scales or offsets
  // the captured page (device emulation, zoom, a collapsing address bar). A circle's percentage radius is relative
  // to sqrt((width^2 + height^2) / 2).
  const width = window.innerWidth;
  const height = window.innerHeight;
  const reach = Math.hypot(Math.max(x, width - x), Math.max(y, height - y));
  root.style.setProperty("--theme-x", `${(x / width) * 100}%`);
  root.style.setProperty("--theme-y", `${(y / height) * 100}%`);
  root.style.setProperty("--theme-r", `${(reach / Math.hypot(width, height)) * Math.SQRT2 * 100}%`);
  root.dataset.themeDir = nextDark ? "grow" : "shrink";
  const transition = document.startViewTransition(toggle);
  // A skipped transition (the tab is in the background) rejects; the theme has switched anyway.
  transition.finished.finally(() => { delete root.dataset.themeDir; }).catch(() => {});
};
