import { FiBookOpen, FiCalendar, FiExternalLink, FiLinkedin, FiPhone, FiUser } from "react-icons/fi";
import { InfoTile } from "../ui/SidePanel";
import { muted } from "../ui/uiStyles";
import { DEPARTMENT_LABELS } from "../UserProfile/profileUtils";

// Strip protocol, "www." and LinkedIn tracking parameters so only the readable part shows.
const displayUrl = (url) => url.replace(/^https?:\/\/(www\.)?/, "").split(/[?#]/)[0].replace(/\/$/, "");

const Missing = ({ children = "Not added" }) => <span className={`font-normal ${muted}`}>{children}</span>;

// The read-only details: contact, then university. Batch and department show only for Alexandria Engineering students.
const ProfileDetails = ({ profile }) => {
  const student = profile.isAlexEngStudent === true;
  const known = profile.isAlexEngStudent !== null && profile.isAlexEngStudent !== undefined;
  return (
    <div className="grid gap-4 sm:grid-cols-2">
      <InfoTile icon={FiPhone} label="Phone number">{profile.phoneNumber ? <span className="tabular-nums">{profile.phoneNumber}</span> : <Missing />}</InfoTile>
      <InfoTile icon={FiLinkedin} label="LinkedIn">
        {profile.linkedinUrl ? (
          <a href={profile.linkedinUrl} target="_blank" rel="noreferrer" title={profile.linkedinUrl} className="flex max-w-full items-center gap-1 text-ocean-573 hover:underline dark:text-ocean-316">
            <span className="truncate">{displayUrl(profile.linkedinUrl)}</span><FiExternalLink className="h-4 w-4 shrink-0" aria-hidden="true" />
          </a>
        ) : <Missing />}
      </InfoTile>
      <InfoTile icon={FiUser} label="Alexandria Engineering">{!known ? <Missing>Not specified</Missing> : student ? "Student" : "Not a student"}</InfoTile>
      {student && <InfoTile icon={FiCalendar} label="Batch">{profile.batch || <Missing />}</InfoTile>}
      {student && <InfoTile icon={FiBookOpen} label="Department">{DEPARTMENT_LABELS[profile.department] || <Missing />}</InfoTile>}
    </div>
  );
};

export default ProfileDetails;
