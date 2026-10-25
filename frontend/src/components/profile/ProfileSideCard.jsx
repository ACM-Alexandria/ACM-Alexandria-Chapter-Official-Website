import { FiEdit2, FiMail } from "react-icons/fi";
import { FaStar } from "react-icons/fa";
import Avatar from "../ui/Avatar";
import { card, muted, secondaryButton } from "../ui/uiStyles";
import { canShowPhoto, getRoleLabel } from "../UserProfile/profileUtils";

const Tag = ({ children }) => (
  <span className="rounded-full bg-[#E2F0FA] px-3 py-1 text-xs font-semibold text-[#1D6A9C] dark:bg-[#16364C] dark:text-[#9FD0EE]">{children}</span>
);

// Who you are, always in view beside the tabs: photo (board members and admins only), name, email, role,
// committee and, for members, this season's points. On large screens it stays in place while the tab content scrolls. `onEdit` is left out while editing.
const ProfileSideCard = ({ profile, email, onEdit }) => (
  <aside className={`rounded-[2rem] p-6 text-center sm:p-8 lg:sticky lg:top-28 ${card}`}>
    <Avatar name={profile.name || email} src={canShowPhoto(profile.role) ? profile.profileImageUrl : ""} className="mx-auto h-24 w-24 text-3xl sm:h-28 sm:w-28 sm:text-4xl" />
    <h2 className="mt-5 break-words text-2xl font-semibold tracking-tight text-slate-900 dark:text-white" style={{ textWrap: "balance" }}>{profile.name || "Unnamed member"}</h2>
    <p className={`mt-1.5 flex items-center justify-center gap-1.5 break-all text-sm ${muted}`}><FiMail className="h-4 w-4 shrink-0" aria-hidden="true" />{email}</p>
    <div className="mt-4 flex flex-wrap justify-center gap-2">
      <Tag>{getRoleLabel(profile.role)}</Tag>
      {profile.committeeName && <Tag>{profile.committeeName}</Tag>}
    </div>
    {profile.role !== "USER" && (
      <p className="mt-5 flex items-center justify-center gap-2 border-t border-[#E6EEF5] pt-5 dark:border-white/10">
        <FaStar className="h-5 w-5 text-[#F5B301]" aria-hidden="true" />
        <span className="text-2xl font-semibold tabular-nums text-slate-900 dark:text-white">{profile.seasonPoints || 0}</span>
        <span className={`text-sm ${muted}`}>points this season</span>
      </p>
    )}
    {onEdit && (
      <button type="button" onClick={onEdit} className={`mt-6 w-full ${secondaryButton}`}><FiEdit2 className="h-4 w-4" aria-hidden="true" />Edit profile</button>
    )}
  </aside>
);

export default ProfileSideCard;
