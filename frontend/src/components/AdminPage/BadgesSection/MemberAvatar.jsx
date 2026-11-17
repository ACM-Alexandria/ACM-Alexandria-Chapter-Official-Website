import { initials } from "./badgeUtils";

// A member's photo, or their initials on a brand tint when they have none.
const MemberAvatar = ({ name, imageUrl, size = "w-8 h-8 text-xs" }) =>
  imageUrl ? (
    <img src={imageUrl} alt="" className={`${size} shrink-0 rounded-full object-cover`} />
  ) : (
    <span
      aria-hidden="true"
      className={`${size} shrink-0 rounded-full inline-flex items-center justify-center font-semibold bg-ocean-67 text-ocean-676 dark:bg-ocean-808 dark:text-ocean-316`}
    >
      {initials(name)}
    </span>
  );

export default MemberAvatar;
