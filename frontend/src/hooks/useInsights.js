import { useEffect, useState } from "react";
import { fetchInsights } from "../services/adminService";

// Results already loaded this session, so switching back to a tab or period is instant.
const cache = new Map();

// Loads one part of the Insights tab ("overview" or a section) for a period. reload() fetches it again.
const useInsights = (path, range) => {
  const key = `${path}:${range}`;
  const [result, setResult] = useState({ key: null, data: null, error: null });
  const [reloadCount, setReloadCount] = useState(0);

  useEffect(() => {
    let cancelled = false;
    fetchInsights(path, range)
      .then((data) => {
        cache.set(key, data);
        if (!cancelled) setResult({ key, data, error: null });
      })
      .catch((err) => {
        if (!cancelled) setResult({ key, data: null, error: err.message || "Unable to load insights." });
      });
    return () => {
      cancelled = true;
    };
  }, [key, path, range, reloadCount]);

  const current = result.key === key;
  return {
    data: current ? result.data : cache.get(key) ?? null,
    error: current ? result.error : null,
    loading: !current,
    reload: () => setReloadCount((count) => count + 1),
  };
};

export default useInsights;
