import { useEffect } from "react";

// Adds a page's schema.org data (an Event, a Course) to <head> as JSON-LD while the page is open, so search
// engines can show it as a rich result. `data` is null until the item has loaded.
const useStructuredData = (data) => {
  const json = data ? JSON.stringify(data) : null;
  useEffect(() => {
    if (!json) return undefined;
    const script = document.createElement("script");
    script.type = "application/ld+json";
    script.dataset.page = "true";
    script.textContent = json;
    document.head.appendChild(script);
    return () => script.remove();
  }, [json]);
};

export default useStructuredData;
