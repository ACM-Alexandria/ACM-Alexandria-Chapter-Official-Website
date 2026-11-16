import { useCallback, useEffect, useState } from "react";
import { FiAlertTriangle } from "react-icons/fi";
import { fetchBadgeTypes, messageOf } from "../../../services/badgesAdminService";
import { fetchPeriods } from "../../../services/seasonsAdminService";
import { noticeClass, secondaryButtonClass } from "../SharedComponents/adminStyles";
import AwardPanel from "./AwardPanel";

// The Badges area of the admin page, open to every role that can reach the page.
// Seasons and quarters are set up in Season settings (super admin).
const BadgesTab = () => {
  const [types, setTypes] = useState([]);
  const [periods, setPeriods] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const load = useCallback(async () => {
    setError("");
    try {
      const [t, p] = await Promise.all([fetchBadgeTypes(), fetchPeriods()]);
      setTypes(t);
      setPeriods(p);
    } catch (e) {
      setError(messageOf(e));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => { load(); }, [load]);

  return (
    <div className="flex flex-col gap-5">
      {error ? (
        <div role="alert" className={`flex flex-wrap items-center gap-3 ${noticeClass.error}`}>
          <FiAlertTriangle className="w-5 h-5 shrink-0" aria-hidden="true" />
          <p className="flex-1">{error}</p>
          <button type="button" onClick={load} className={secondaryButtonClass}>Try again</button>
        </div>
      ) : loading ? (
        <p className="text-sm text-slate-600 dark:text-slate-400">Loading badges…</p>
      ) : (
        <AwardPanel types={types} periods={periods} />
      )}
    </div>
  );
};

export default BadgesTab;
