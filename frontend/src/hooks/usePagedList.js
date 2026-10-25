import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";

// One page of a Spring-paged list, with the page number kept in the URL (?page=2, one-based).
const usePagedList = (load, failMessage) => {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = Math.max(0, Number(searchParams.get("page") || 1) - 1);
  const [result, setResult] = useState({ page: null, items: [], totalPages: 1, error: null });

  useEffect(() => {
    let active = true;
    load(page)
      .then((data) => {
        if (!active) return;
        const items = Array.isArray(data) ? data : data?.content ?? [];
        setResult({ page, items, totalPages: data?.totalPages ?? 1, error: null });
      })
      .catch((err) => {
        console.error("Failed to load list:", err);
        if (active) setResult({ page, items: [], totalPages: 1, error: failMessage });
      });
    return () => { active = false; };
  }, [page, load, failMessage]);

  const setPage = (next) => {
    const params = new URLSearchParams(searchParams);
    if (next <= 0) params.delete("page");
    else params.set("page", String(next + 1));
    setSearchParams(params);
    window.scrollTo({ top: 0 });
  };

  return { ...result, page, loading: result.page !== page, setPage };
};

export default usePagedList;
