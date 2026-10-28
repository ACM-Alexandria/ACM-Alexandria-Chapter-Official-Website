import { useEffect, useRef, useState } from "react";
import useScrollLock from "./useScrollLock";

// Open overlays, newest last. Escape only closes the topmost one, so a dialog over a side panel closes alone.
const stack = [];

// Shared behaviour of side panels and dialogs: locks page scrolling while open, closes the topmost overlay
// on Escape, and reports `visible` one frame after opening so the enter transition runs. Overlays carry
// `data-lenis-prevent`, so wheel and touch scrolling inside them never reach the smooth-scrolled page.
const useOverlay = (open, onClose) => {
  const [visible, setVisible] = useState(false);
  const id = useRef(Symbol("overlay"));
  const close = useRef(onClose);
  useScrollLock(open);

  useEffect(() => {
    close.current = onClose;
  }, [onClose]);

  useEffect(() => {
    if (!open) {
      setVisible(false);
      return undefined;
    }
    const key = id.current;
    stack.push(key);
    const frame = requestAnimationFrame(() => setVisible(true));
    const onKey = (event) => {
      if (event.key === "Escape" && stack[stack.length - 1] === key) close.current?.();
    };
    window.addEventListener("keydown", onKey);
    return () => {
      cancelAnimationFrame(frame);
      window.removeEventListener("keydown", onKey);
      const index = stack.indexOf(key);
      if (index !== -1) stack.splice(index, 1);
    };
  }, [open]);

  return visible;
};

export default useOverlay;
