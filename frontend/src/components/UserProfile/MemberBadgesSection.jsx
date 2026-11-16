import { useEffect, useState } from "react";
import { fetchMemberBadges } from "../../services/badgesService";
import BadgeMedal from "../badges/BadgeMedal";
import { Panel } from "./ProfileViewCard";

// A member's badges in the admin member view, read-only. Badges are awarded from the Badges tab.
const MemberBadgesSection = ({ userId }) => {
  const [badges, setBadges] = useState(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    if (!userId) return undefined;
    let ignore = false;
    fetchMemberBadges(userId)
      .then((data) => { if (!ignore) { setBadges(data.badges || []); setFailed(false); } })
      .catch(() => { if (!ignore) setFailed(true); });
    return () => { ignore = true; };
  }, [userId]);

  return (
    <Panel title="Badges">
      {failed ? (
        <p className="px-5 py-6 text-sm text-rose-700 dark:text-rose-300">Couldn't load this member's badges.</p>
      ) : badges === null ? (
        <p className="px-5 py-6 text-sm text-slate-600 dark:text-slate-400">Loading…</p>
      ) : badges.length === 0 ? (
        <p className="px-5 py-6 text-sm text-slate-600 dark:text-slate-400">No badges yet.</p>
      ) : (
        <ul className="divide-y divide-slate-200 dark:divide-ocean-800">
          {badges.map((b) => (
            <li key={b.id}>
              <a href={`/badges/${b.publicId}`} target="_blank" rel="noreferrer" className="flex items-center gap-3 px-5 py-3 hover:bg-slate-50 dark:hover:bg-ocean-845/40">
                <BadgeMedal periodType={b.periodType} icon={b.icon} size={36} />
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium text-slate-900 dark:text-slate-100">{b.name}</p>
                  <p className="truncate text-xs text-slate-600 dark:text-slate-400">{b.periodLabel} · {b.committeeName}</p>
                </div>
              </a>
            </li>
          ))}
        </ul>
      )}
    </Panel>
  );
};

export default MemberBadgesSection;
