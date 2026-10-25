import { useEffect, useState } from "react";

// Loads one item's details while a panel is open. `loading` is true until the item asked for has arrived;
// the last item stays available after closing so the panel can slide out with its content.
const useDetails = (open, id, load, failMessage) => {
  const [result, setResult] = useState({ key: null, data: null, error: null });
  const key = open && id ? id : null;

  useEffect(() => {
    if (key === null) return undefined;
    let active = true;
    load(key)
      .then((data) => { if (active) setResult({ key, data, error: null }); })
      .catch((err) => {
        console.error("Failed to load details:", err);
        if (active) setResult({ key, data: null, error: failMessage });
      });
    return () => { active = false; };
  }, [key, load, failMessage]);

  const current = key === null || result.key === key;
  return { data: result.data, loading: !current, error: current ? result.error : null };
};

export default useDetails;
