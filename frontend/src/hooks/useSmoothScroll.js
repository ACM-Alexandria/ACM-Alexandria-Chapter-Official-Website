import { useEffect, useRef } from "react";
import { useLocation } from "react-router-dom";
import Lenis from "lenis";
import "lenis/dist/lenis.css";

// Smooth wheel scrolling with Lenis, using the same settings as uchicagoacm.github.io. Scrolling inside an open
// panel or dialog stays inside it.
// In-page "#section" links glide to their target below the floating navbar. Arriving from another page with a
// hash (/#events) jumps straight to that section once `hashReady` says the page's content is in place.
// Smooth scrolling is skipped for reduced motion.
export const useSmoothScroll = (offset = -88, hashReady = false) => {
  const lenisRef = useRef(null);
  const handledHash = useRef("");
  const { hash } = useLocation();

  useEffect(() => {
    if (window.matchMedia("(prefers-reduced-motion: reduce)").matches) return undefined;
    // Panels and dialogs scroll natively: anything inside [data-lenis-prevent] or a modal is left alone.
    const lenis = new Lenis({
      duration: 1.15,
      smoothWheel: true,
      prevent: (node) => Boolean(node.closest?.("[data-lenis-prevent], [aria-modal='true']")),
    });
    lenisRef.current = lenis;
    let frame;
    const raf = (time) => {
      lenis.raf(time);
      frame = requestAnimationFrame(raf);
    };
    frame = requestAnimationFrame(raf);

    const onClick = (event) => {
      const link = event.target.closest?.('a[href^="#"]');
      if (!link) return;
      const id = link.getAttribute("href");
      const target = id === "#top" ? 0 : document.querySelector(id);
      if (target === null) return;
      event.preventDefault();
      lenis.scrollTo(target, { offset: target === 0 ? 0 : offset });
    };
    document.addEventListener("click", onClick);

    return () => {
      cancelAnimationFrame(frame);
      document.removeEventListener("click", onClick);
      lenis.destroy();
      lenisRef.current = null;
    };
  }, [offset]);

  useEffect(() => {
    if (!hashReady || !hash || handledHash.current === hash) return undefined;
    const frame = requestAnimationFrame(() => {
      const target = document.getElementById(decodeURIComponent(hash.slice(1)));
      if (!target) return;
      handledHash.current = hash;
      if (lenisRef.current) lenisRef.current.scrollTo(target, { offset, immediate: true, force: true });
      else window.scrollTo({ top: target.getBoundingClientRect().top + window.scrollY + offset });
    });
    return () => cancelAnimationFrame(frame);
  }, [hashReady, hash, offset]);
};
