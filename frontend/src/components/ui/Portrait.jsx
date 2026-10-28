import { FaLinkedinIn } from "react-icons/fa";
import Avatar from "./Avatar";

// A tall photo card with the name and role laid over the bottom of the photo, and a LinkedIn button when the
// member has one. The caption sizes itself to the card's own width, so small cards in a row stay readable.
const Portrait = ({ member, aspect = "aspect-[3/4]" }) => (
  <figure className={`@container group relative overflow-hidden rounded-3xl bg-ocean-67 dark:bg-ocean-808 ${aspect}`}>
    {member.image
      ? <img src={member.image} alt={member.name} loading="lazy" className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-[1.04]" />
      : <Avatar name={member.name} className="h-full w-full rounded-none text-4xl" />}
    {member.linkedin && (
      <a href={member.linkedin} target="_blank" rel="noopener noreferrer" aria-label={`${member.name} on LinkedIn`} className="absolute right-3 top-3 flex h-9 w-9 items-center justify-center rounded-full bg-white/90 text-linkedin shadow-md transition-transform hover:scale-105">
        <FaLinkedinIn className="h-4 w-4" aria-hidden="true" />
      </a>
    )}
    <figcaption className="absolute inset-x-0 bottom-0 bg-gradient-to-t from-ocean-927/90 via-ocean-927/45 to-transparent p-3.5 pt-14 text-left text-white @[180px]:p-5 @[180px]:pt-16">
      <p className="text-sm font-semibold leading-tight @[180px]:text-base">{member.name}</p>
      <p className="mt-0.5 text-xs font-medium text-ocean-222 @[180px]:text-sm">{member.role}</p>
    </figcaption>
  </figure>
);

export default Portrait;
