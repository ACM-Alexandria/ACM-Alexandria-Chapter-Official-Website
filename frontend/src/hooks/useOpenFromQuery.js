import { useEffect } from "react";
import { useSearchParams } from "react-router-dom";

// Reopens a details panel after a login redirect: reads ?<param>=<id> once, calls `open(id)`, and clears it.
const useOpenFromQuery = (param, open) => {
  const [searchParams, setSearchParams] = useSearchParams();
  const value = searchParams.get(param);
  useEffect(() => {
    if (!value) return;
    open(Number(value));
    const next = new URLSearchParams(searchParams);
    next.delete(param);
    setSearchParams(next, { replace: true });
  }, [value, param, open, searchParams, setSearchParams]);
};

export default useOpenFromQuery;
