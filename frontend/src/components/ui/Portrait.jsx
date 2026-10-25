import { FaLinkedinIn } from "react-icons/fa";
import Avatar from "./Avatar";

// A tall photo card with the name and role laid over the bottom of the photo, and a LinkedIn button when the
// member has one.
const Portrait = ({ member, aspect = "aspect-[3/4]" }) => (
  <figure className={`group relative overflow-hidden rounded-[1.75rem] bg-[#E2F0FA] dark:bg-[#16364C] ${aspect}`}>
    {member.image
      ? <img src={member.image} alt={member.name} loading="lazy" className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-[1.04]" />
      : <Avatar name={member.name} className="h-full w-full rounded-none text-4xl" />}
    {member.linkedin && (
      <a href={member.linkedin} target="_blank" rel="noopener noreferrer" aria-label={`${member.name} on LinkedIn`} className="absolute right-3 top-3 flex h-9 w-9 items-center justify-center rounded-full bg-white/90 text-[#0A66C2] shadow-md transition-transform hover:scale-105">
        <FaLinkedinIn className="h-4 w-4" aria-hidden="true" />
      </a>
    )}
    <figcaption className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-[#06131F]/90 via-[#06131F]/45 to-transparent p-4 pt-14 text-left text-white sm:p-5 sm:pt-16">
      <p className="text-sm font-semibold sm:text-base">{member.name}</p>
      <p className="text-xs font-medium text-[#9FD0EE] sm:text-sm">{member.role}</p>
    </figcaption>
  </figure>
);

export default Portrait;
