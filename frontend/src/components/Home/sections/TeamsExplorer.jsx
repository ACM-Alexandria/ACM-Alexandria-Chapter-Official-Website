import { useState } from "react";
import { FiArrowUpRight } from "react-icons/fi";
import OrgTree from "../../ui/OrgTree";
import Portrait from "../../ui/Portrait";
import TeamLogo from "../../ui/TeamLogo";
import CommitteeActions from "./CommitteeActions";
import { shortName } from "../../../utils/format";
import { muted, secondaryButton } from "../../ui/uiStyles";

const OpenTag = ({ open }) => (
  <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${open ? "bg-emerald-50 text-emerald-700 dark:bg-emerald-950/50 dark:text-emerald-300" : "bg-slate-100 text-slate-600 dark:bg-white/5 dark:text-ocean-239"}`}>{open ? "Open" : "Closed"}</span>
);

const tabs = [["committees", "Committees"], ["clubs", "Clubs"]];

// Pick a committee or club and see its card with its heads branching under it. A committee shows what the
// visitor can do with it (apply, follow); a club opens its details, where its own registration lives.
// A tab with no teams is left out.
const TeamsExplorer = ({ groups, myCommitteeId = null, onApply, onShowClub, isRegistrationModalOpen = false }) => {
  const available = tabs.filter(([id]) => groups[id]?.length > 0);
  const [chosen, setChosen] = useState(null);
  const [index, setIndex] = useState(0);
  if (available.length === 0) return null;
  const tab = available.some(([id]) => id === chosen) ? chosen : available[0][0];
  const list = groups[tab];
  const team = list[Math.min(index, list.length - 1)];
  const isCommittee = tab === "committees";

  return (
    <div className="overflow-hidden rounded-[2.25rem] bg-white ring-1 ring-ocean-106 dark:bg-ocean-871 dark:ring-white/10">
      <div className="flex flex-col gap-5 border-b border-ocean-69 p-6 sm:p-8 lg:flex-row lg:items-center lg:justify-between dark:border-white/10">
        <div role="tablist" aria-label="Teams" className="inline-flex w-fit rounded-xl bg-ocean-31 p-1 dark:bg-white/5">
          {available.map(([id, label]) => (
            <button key={id} type="button" role="tab" aria-selected={tab === id} onClick={() => { setChosen(id); setIndex(0); }}
              className={`h-10 rounded-lg px-4 text-sm font-semibold transition-colors ${tab === id ? "bg-white text-ocean-676 shadow-sm dark:bg-ocean-461 dark:text-ocean-910" : "text-slate-600 hover:text-slate-900 dark:text-ocean-239 dark:hover:text-white"}`}>
              {label} <span className="ml-1 tabular-nums opacity-70">{groups[id].length}</span>
            </button>
          ))}
        </div>
        <div className="-mx-1 flex gap-2 overflow-x-auto px-1 pb-1">
          {list.map((g, i) => (
            <button key={g.id} type="button" onClick={() => setIndex(i)} aria-pressed={team.id === g.id}
              className={`flex shrink-0 items-center gap-2 rounded-full py-1 pl-1 pr-4 text-sm font-semibold ring-1 transition-colors ${team.id === g.id ? "bg-ocean-676 text-white ring-ocean-676 dark:bg-ocean-461 dark:text-ocean-910 dark:ring-ocean-461" : "bg-white text-slate-700 ring-ocean-106 hover:ring-ocean-461 dark:bg-transparent dark:text-slate-200 dark:ring-white/15"}`}>
              <TeamLogo src={g.image} name={g.name} className="h-8 w-8 rounded-full" />{shortName(g.name)}
            </button>
          ))}
        </div>
      </div>

      <div key={`${tab}-${team.id}`} className="grid gap-10 p-6 sm:p-10 lg:grid-cols-[0.9fr_1.1fr] lg:items-center">
        <div style={{ animation: "hp-rise 0.35s cubic-bezier(0.22,1,0.36,1) both" }}>
          <TeamLogo src={team.image} name={team.name} className="h-24 w-24 rounded-3xl !text-2xl shadow-[0_20px_40px_-20px_color-mix(in_srgb,var(--color-ocean-676)_60%,transparent)]" />
          <div className="mt-6 flex flex-wrap items-center gap-3">
            <h3 className="text-3xl font-semibold tracking-tight text-slate-900 dark:text-white">{team.name}</h3>
            <OpenTag open={team.open} />
          </div>
          <p className={`mt-4 max-w-md text-lg leading-relaxed ${muted}`}>{team.description}</p>
          {isCommittee ? (
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
        {team.board.length > 0 && (
          <OrgTree
            members={team.board}
            className="mx-auto w-full max-w-md"
            root={<span className="inline-flex items-center gap-2 rounded-full bg-ocean-67 px-4 py-2 text-sm font-semibold text-ocean-637 dark:bg-ocean-808 dark:text-ocean-222"><TeamLogo src={team.image} name={team.name} className="h-6 w-6 rounded-full" />{shortName(team.name)} board</span>}
            renderMember={(m) => <Portrait member={m} />}
          />
        )}
      </div>
    </div>
  );
};

export default TeamsExplorer;
