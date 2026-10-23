import { useEffect, useState } from "react";
import { fetchCommittee, fetchEvents, fetchClubs, fetchPrograms } from "../services/homePageService";
import { fetchExclusiveForms } from "../services/adminService";

// Tabs that have a list (users and the other sidebar tabs load their own data)
const LIST_TABS = ["committees", "events", "clubs", "programs", "exclusiveForms"];

// Loads the list behind one resource tab, the same way on both dashboards. Events, clubs and
// programs come back as Spring pages; committees and exclusive forms come back as arrays.
export const loadResourceList = async (tab, page = 0) => {
  switch (tab) {
    case "committees": {
      const data = await fetchCommittee();
      return (data || []).map((committee) => ({
        ...committee,
        boardRoles: (committee.boardRoles || []).slice().sort((a, b) => (a.order ?? 99) - (b.order ?? 99)),
      }));
    }
    case "events": {
      const data = await fetchEvents(page);
      return { ...data, content: (data.content || []).slice().sort((a, b) => new Date(b.eventTime) - new Date(a.eventTime)) };
    }
    case "clubs": {
      const data = await fetchClubs(page);
      return { ...data, content: (data.content || []).slice().sort((a, b) => a.name.localeCompare(b.name)) };
    }
    case "programs":
      return fetchPrograms(page);
    case "exclusiveForms":
      return (await fetchExclusiveForms()) || [];
    default:
      throw new Error(`No resource list for tab ${tab}.`);
  }
};

// Current list for a resource tab, with paging. Stale results from a previous tab or page are ignored.
const useResourceList = (tab) => {
  const [pager, setPager] = useState({ tab, page: 0 });
  const [reloadCount, setReloadCount] = useState(0);
  const [result, setResult] = useState({ key: null, data: null, error: null });

  // Going to another tab starts from its first page
  const page = pager.tab === tab ? pager.page : 0;
  const requestKey = `${tab}:${page}:${reloadCount}`;
  const hasList = LIST_TABS.includes(tab);

  useEffect(() => {
    if (!LIST_TABS.includes(tab)) return undefined;
    let cancelled = false;
    const key = `${tab}:${page}:${reloadCount}`;
    loadResourceList(tab, page)
      .then((data) => {
        if (!cancelled) setResult({ key, data, error: null });
      })
      .catch((err) => {
        console.error(err);
        if (!cancelled) setResult({ key, data: null, error: "Failed to fetch resource data." });
      });
    return () => {
      cancelled = true;
    };
  }, [tab, page, reloadCount]);

  const loading = hasList && result.key !== requestKey;

  return {
    data: loading ? null : result.data,
    error: loading ? null : result.error,
    loading,
    page,
    setPage: (nextPage) => setPager({ tab, page: nextPage }),
    reload: () => setReloadCount((count) => count + 1),
  };
};

export default useResourceList;
