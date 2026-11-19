import { FiArrowUpRight } from "react-icons/fi";
import logo from "../../../assets/logo/acm-logo-no-bg.png";
import { FaLinkedinIn } from "react-icons/fa";
import Avatar from "../../ui/Avatar";
import TeamLogo from "../../ui/TeamLogo";
import CommitteeActions from "./CommitteeActions";
import { summary } from "../../../utils/format";
import { card, muted, secondaryButton } from "../../ui/uiStyles";

const line = "bg-ocean-255 dark:bg-ocean-678";
// Half the gap between the two columns, where the trunk runs.
const HALF_GAP = "1.25rem";
// How far down a card its branch leaves the trunk.
const BRANCH_Y = "2rem";

const OpenTag = ({ open }) => (
  <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${open ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-300" : "bg-slate-100 text-slate-600 dark:bg-white/5 dark:text-ocean-239"}`}>{open ? "Open" : "Closed"}</span>
);

// The root of a tree: the chapter's logo with a title and a line under it (the season).
export const TreeRoot = ({ title, subtitle }) => (
  <div className="flex items-center gap-4 rounded-full bg-ocean-794 py-2 pl-2 pr-6 text-white shadow-[0_18px_40px_-20px_color-mix(in_srgb,var(--color-ocean-794)_80%,transparent)] dark:bg-ocean-820">
    <span className="flex h-12 w-12 items-center justify-center rounded-full bg-white"><img src={logo} alt="" className="h-9 w-9 object-contain" /></span>
    <div className="text-left"><p className="text-base font-semibold">{title}</p><p className="text-xs text-ocean-145">{subtitle}</p></div>
  </div>
);

// One committee or club: logo, name and status, a short description, its heads, and what a visitor can do.
const TeamCard = ({ team, committee, myCommitteeId, onApply, onShowClub, isRegistrationModalOpen }) => (
  <article className={`flex h-full flex-col rounded-3xl p-5 sm:p-6 ${card}`}>
    <div className="flex items-center gap-4">
      <div className="flex min-w-0 flex-1 items-center gap-4">
        <TeamLogo src={team.image} name={team.name} className="h-12 w-12 shrink-0 rounded-2xl !text-base" />
        <div className="min-w-0">
          <h3 className="text-lg font-semibold leading-snug tracking-tight text-slate-900 dark:text-white">{team.name}</h3>
          {team.open !== undefined && <div className="mt-1"><OpenTag open={team.open} /></div>}
        </div>
      </div>
    </div>
    <div className="mt-5 flex flex-wrap items-stretch gap-x-6 gap-y-4">
    {team.board.length > 0 && (
      <ul className="flex flex-wrap gap-4">
        {team.board.map((member) => (
          <li key={member.name} className="w-24 sm:w-28">
            <div className="relative aspect-[4/5] overflow-hidden rounded-2xl bg-ocean-67 dark:bg-ocean-808">
              {member.image
                ? <img src={member.image} alt={member.name} loading="lazy" className="h-full w-full object-cover" />
                : <Avatar name={member.name} className="h-full w-full rounded-none text-2xl" />}
              {member.linkedin && (
                <a href={member.linkedin} target="_blank" rel="noopener noreferrer" aria-label={`${member.name} on LinkedIn`} className="absolute bottom-1.5 right-1.5 flex h-7 w-7 items-center justify-center rounded-full bg-white/90 text-linkedin shadow transition-transform hover:scale-105">
                  <FaLinkedinIn className="h-3 w-3" aria-hidden="true" />
                </a>
              )}
            </div>
            <p className="mt-2 text-sm font-semibold leading-tight text-slate-900 dark:text-white">{member.name}</p>
            <p className={`mt-0.5 text-xs ${muted}`}>{member.role}</p>
          </li>
        ))}
      </ul>
    )}
      <p className={`min-w-[12rem] flex-1 text-sm leading-relaxed ${muted} ${team.board.length > 0 ? "border-ocean-106 xl:border-l xl:pl-6 dark:border-white/10" : ""}`}>{summary(team.description || "", 170)}</p>
    </div>
    {(committee || onShowClub) && <div className="mt-auto pt-5">
      <div>
        {committee ? (
          <CommitteeActions
            committee={team}
            isMyCommittee={myCommitteeId != null && String(team.id) === String(myCommitteeId)}
            onApply={onApply}
            isRegistrationModalOpen={isRegistrationModalOpen}
          />
        ) : onShowClub && (
          <button type="button" onClick={() => onShowClub(team.id)} className={`mt-6 ${secondaryButton}`}>
            Club details <FiArrowUpRight className="h-4 w-4" aria-hidden="true" />
          </button>
        )}
      </div>
    </div>}
  </article>
);

// Teams hanging from a root as a tree, two to a row: a trunk runs down the middle and a short branch reaches each
// card. A team left over in the last row sits in the middle, on the trunk. On phones the cards stack in one column
// with a short line between them.
const TeamTree = ({ root, teams, committee = false, myCommitteeId = null, onApply, onShowClub, isRegistrationModalOpen = false, className = "" }) => {
  const lastRow = Math.floor((teams.length - 1) / 2);
  const props = { committee, myCommitteeId, onApply, onShowClub, isRegistrationModalOpen };

  return (
    <div className={`flex flex-col items-center ${className}`}>
      {root}
      <span className={`h-8 w-px ${line}`} aria-hidden="true" />
      <ul className="grid w-full grid-cols-1 gap-y-6 sm:grid-cols-2 sm:gap-x-10">
        {teams.map((team, i) => {
          const row = Math.floor(i / 2);
          const left = i % 2 === 0;
          const alone = left && i === teams.length - 1;
          const top = row === 0 ? "0" : "-1.5rem";
          // The trunk segment a left card draws: to the next row, or down to its branch when it is the last row.
          const trunk = row === lastRow
            ? { top, height: row === 0 ? BRANCH_Y : `calc(${BRANCH_Y} + 1.5rem)` }
            : { top, bottom: "-1.5rem" };

          return (
            <li key={team.id} className={`relative ${alone ? "sm:col-span-2" : ""}`}>
              {i > 0 && <span className={`absolute -top-6 left-1/2 h-6 w-px sm:hidden ${line}`} aria-hidden="true" />}
              {alone ? (
                <div className="relative mx-auto sm:w-[calc(50%-1.25rem)]">
                  {row > 0 && <span className={`absolute -top-6 left-1/2 hidden h-6 w-px sm:block ${line}`} aria-hidden="true" />}
                  <TeamCard team={team} {...props} />
                </div>
              ) : (
                <>
                  <span className={`absolute hidden h-px sm:block ${line}`} style={left ? { left: "100%", width: HALF_GAP, top: BRANCH_Y } : { right: "100%", width: HALF_GAP, top: BRANCH_Y }} aria-hidden="true" />
                  {left && <span className={`absolute hidden w-px sm:block ${line}`} style={{ left: `calc(100% + ${HALF_GAP})`, ...trunk }} aria-hidden="true" />}
                  <TeamCard team={team} {...props} />
                </>
              )}
            </li>
          );
        })}
      </ul>
    </div>
  );
};

export default TeamTree;
