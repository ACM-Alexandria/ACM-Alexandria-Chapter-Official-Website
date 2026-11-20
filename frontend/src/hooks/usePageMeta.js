import { useEffect } from "react";
import { defaultDescription, defaultImage, pageMeta, siteName, siteUrl } from "../seo/pageMeta";

// Sets or creates one <meta>/<link> tag in <head>, found by its key attribute (name, property or rel).
const setTag = (tag, key, keyValue, attr, value) => {
  let el = document.head.querySelector(`${tag}[${key}="${keyValue}"]`);
  if (!el) {
    el = document.createElement(tag);
    el.setAttribute(key, keyValue);
    document.head.appendChild(el);
  }
  el.setAttribute(attr, value);
};

// Gives the current page its title, description, canonical URL, social preview text and robots rule.
// `page` is a key of pageMeta, or an object with the same fields; `overrides` adds what's only known once data
// loads (a season's name, a member's badge). A page without a `path` uses the current URL's path.
const usePageMeta = (page, overrides) => {
  const base = typeof page === "string" ? pageMeta[page] : page;
  const loaded = Object.fromEntries(Object.entries(overrides ?? {}).filter(([, value]) => value != null && value !== ""));
  const meta = { ...base, ...loaded };
  const { title, description = defaultDescription, noindex = false, image = defaultImage } = meta;
  const path = meta.path ?? window.location.pathname;
  const fullTitle = !title || title.includes(siteName) ? title || siteName : `${title} | ${siteName}`;

  useEffect(() => {
    const url = `${siteUrl}${path === "/" ? "/" : path.replace(/\/$/, "")}`;
    document.title = fullTitle;
    setTag("meta", "name", "description", "content", description);
    setTag("meta", "name", "robots", "content", noindex ? "noindex, nofollow" : "index, follow");
    setTag("link", "rel", "canonical", "href", url);
    setTag("meta", "property", "og:title", "content", fullTitle);
    setTag("meta", "property", "og:description", "content", description);
    setTag("meta", "property", "og:url", "content", url);
    setTag("meta", "name", "twitter:title", "content", fullTitle);
    setTag("meta", "name", "twitter:description", "content", description);
    setTag("meta", "property", "og:image", "content", image);
    setTag("meta", "name", "twitter:image", "content", image);
  }, [fullTitle, description, noindex, path, image]);
};

export default usePageMeta;
