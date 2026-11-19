import { FiEdit2, FiMail } from "react-icons/fi";
import { FaStar } from "react-icons/fa";
import Avatar from "../ui/Avatar";
import BadgeMedal from "../badges/BadgeMedal";
import { card, focusRing, muted, secondaryButton } from "../ui/uiStyles";
import { canShowPhoto, getRoleLabel } from "../UserProfile/profileUtils";

const Tag = ({ children }) => (
  <span className="rounded-full bg-ocean-67 px-3 py-1 text-xs font-semibold text-ocean-637 dark:bg-ocean-808 dark:text-ocean-222">{children}</span>
);

// Who you are, always in view beside the tabs: photo (board members and admins only), name, email, role,
// committee and, for members, this season's points and their badges. On large screens it stays in place
// while the tab content scrolls. `onEdit` is left out while editing.
const ProfileSideCard = ({ profile, email, onEdit, badges = [], onShowBadges }) => (
  <aside className={`rounded-[2rem] p-6 text-center sm:p-8 lg:sticky lg:top-28 ${card}`}>
    <Avatar name={profile.name || email} src={canShowPhoto(profile.role) ? profile.profileImageUrl : ""} className="mx-auto h-24 w-24 text-3xl sm:h-28 sm:w-28 sm:text-4xl" />
    <h2 className="mt-5 break-words text-2xl font-semibold tracking-tight text-slate-900 dark:text-white" style={{ textWrap: "balance" }}>{profile.name || "Unnamed member"}</h2>
    <p className={`mt-1.5 flex items-center justify-center gap-1.5 break-all text-sm ${muted}`}><FiMail className="h-4 w-4 shrink-0" aria-hidden="true" />{email}</p>
    <div className="mt-4 flex flex-wrap justify-center gap-2">
      <Tag>{getRoleLabel(profile.role)}</Tag>
      {profile.committeeName && <Tag>{profile.committeeName}</Tag>}
    </div>
    {profile.role !== "USER" && (
      <p className="mt-5 flex items-center justify-center gap-2 border-t border-ocean-69 pt-5 dark:border-white/10">
        <FaStar className="h-5 w-5 text-gold-518" aria-hidden="true" />
        <span className="text-2xl font-semibold tabular-nums text-slate-900 dark:text-white">{profile.seasonPoints || 0}</span>
        <span className={`text-sm ${muted}`}>points this season</span>
      </p>
    )}
    {badges.length > 0 && (
      <button
        type="button"
        onClick={onShowBadges || undefined}
        disabled={!onShowBadges}
        className={`mt-4 flex w-full items-center justify-center gap-3 rounded-2xl px-2 py-2 transition-colors enabled:hover:bg-ocean-31 dark:enabled:hover:bg-white/5 ${focusRing}`}
      >
        <span className="flex -space-x-2" aria-hidden="true">
          {badges.slice(0, 4).map((b) => <BadgeMedal key={b.id} periodType={b.periodType} periodLabel={b.periodLabel} icon={b.icon} size={32} />)}
        </span>
        <span className="text-sm font-semibold text-slate-900 dark:text-white">
          {badges.length} {badges.length === 1 ? "badge" : "badges"}
        </span>
      </button>
    )}
    {onEdit && (
      <button type="button" onClick={onEdit} className={`mt-6 w-full ${secondaryButton}`}><FiEdit2 className="h-4 w-4" aria-hidden="true" />Edit profile</button>
    )}
  </aside>
);

export default ProfileSideCard;
