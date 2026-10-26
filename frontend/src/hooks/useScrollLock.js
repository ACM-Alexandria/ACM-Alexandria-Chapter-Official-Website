import { useEffect } from "react";

// Locks page scrolling while `active` is true. Counted, so overlapping dialogs/panels
// (e.g. a dialog opened over a side panel) keep the lock until the last one closes.
// Both <html> and <body> are locked: the site sets overflow-x: clip on <html>, which stops
// a body-only lock from reaching the real scroll area.
let lockCount = 0;
let saved = null;

const useScrollLock = (active = true) => {
  useEffect(() => {
    if (!active) return undefined;
    const html = document.documentElement;
    const body = document.body;

    if (lockCount === 0) saved = { html: html.style.overflow, body: body.style.overflow };
    lockCount += 1;
    html.style.overflow = "hidden";
    body.style.overflow = "hidden";

    return () => {
      lockCount -= 1;
      if (lockCount === 0 && saved) {
        html.style.overflow = saved.html;
        body.style.overflow = saved.body;
        saved = null;
      }
    };
  }, [active]);
};

export default useScrollLock;
