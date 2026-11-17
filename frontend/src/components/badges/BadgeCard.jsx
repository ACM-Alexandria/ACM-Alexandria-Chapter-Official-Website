import { Link } from "react-router-dom";
import BadgeMedal from "./BadgeMedal";
import { card, focusRing, muted } from "../ui/uiStyles";

const awardedOn = (iso) =>
  iso ? new Date(iso).toLocaleDateString("en-GB", { day: "numeric", month: "short", year: "numeric" }) : "";

// One badge on the member's profile: the medal, its name, the period and committee, and when it was awarded.
// `isNew` adds the gold "New" marker. The card links to the badge's public page; `footer` holds actions.
const BadgeCard = ({ badge, isNew = false, footer }) => (
  <article className={`relative flex flex-col gap-4 rounded-3xl p-5 sm:p-6 ${isNew ? "bg-white ring-2 ring-gold-518 dark:bg-ocean-871" : card}`}>
    {isNew && (
      <span className="absolute right-4 top-4 rounded-full bg-gold-116 px-2.5 py-0.5 text-xs font-bold text-gold-851">New</span>
    )}
    <Link to={`/badges/${badge.publicId}`} className={`flex items-center gap-4 rounded-2xl ${focusRing}`}>
      <BadgeMedal periodType={badge.periodType} periodLabel={badge.periodLabel} icon={badge.icon} size={88} label={`${badge.name}, ${badge.periodLabel}`} />
      <span className="min-w-0">
        <span className="block text-lg font-semibold text-slate-900 dark:text-white">{badge.name}</span>
        <span className={`mt-0.5 block text-sm ${muted}`}>{badge.periodLabel} · {badge.committeeName}</span>
        <span className={`mt-0.5 block text-xs ${muted}`}>Awarded {awardedOn(badge.awardedAt)}</span>
      </span>
    </Link>
    {badge.note && (
      <p className="rounded-2xl bg-ocean-31 px-4 py-3 text-sm leading-relaxed text-slate-700 dark:bg-white/5 dark:text-ocean-114">
        <span className="sr-only">Description: </span>{badge.note}
      </p>
    )}
    {footer && <div className="-mb-2 border-t border-ocean-69 pt-2 dark:border-white/10">{footer}</div>}
  </article>
);

export default BadgeCard;
