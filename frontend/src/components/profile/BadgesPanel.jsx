import BadgeCard from "../badges/BadgeCard";
import { card, muted } from "../ui/uiStyles";

// The Badges tab of the member's own profile: every badge, newest first, with the ones they haven't
// seen yet marked "New". `newIds` holds the badges that were new when the tab was opened.
// Each badge opens its own public page, the one shared links point to.
const BadgesPanel = ({ badges, newIds, error }) => (
  <div className="flex flex-col gap-6">
    <h2 className="text-2xl font-semibold tracking-tight text-slate-900 dark:text-white">Your badges</h2>

    {error ? (
      <p role="alert" className="rounded-xl bg-red-50 px-4 py-3 text-sm font-medium text-red-700 dark:bg-red-950/40 dark:text-red-200">{error}</p>
    ) : badges.length === 0 ? (
      <div className={`rounded-3xl px-6 py-12 text-center ${card}`}>
        <p className="font-semibold text-slate-900 dark:text-white">No badges yet</p>
        <p className={`mx-auto mt-1.5 max-w-sm text-sm ${muted}`}>MVP badges from your committee will show up here.</p>
      </div>
    ) : (
      <div className="grid gap-4 sm:grid-cols-2">
        {badges.map((b) => <BadgeCard key={b.id} badge={b} isNew={newIds.has(b.id)} />)}
      </div>
    )}
  </div>
);

export default BadgesPanel;
