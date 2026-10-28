import { useEffect, useState } from "react";
import { useSearchParams } from "react-router-dom";
import { FiAlertCircle, FiChevronDown, FiRefreshCw, FiUsers } from "react-icons/fi";
import logo from "../assets/logo/acm-logo-no-bg.png";
import SiteLayout from "../components/Layout/SiteLayout";
import PageHero from "../components/Layout/PageHero";
import OrgTree from "../components/ui/OrgTree";
import Portrait from "../components/ui/Portrait";
import Reveal from "../components/ui/Reveal";
import TeamLogo from "../components/ui/TeamLogo";
import EmptyState from "../components/ui/EmptyState";
import { normalizeBoards } from "../utils/normalizeBoards";
import { fetchBoardSeasons, fetchPastBoards } from "../services/pastBoardsService";
import { fetchAllClubs, fetchCommittee, fetchHighBoard } from "../services/homePageService";
import { formatBoardSeason } from "../utils/boardSeasons";
import { shortName } from "../utils/format";
import { card, muted, secondaryButton, skeleton } from "../components/ui/uiStyles";

const toPortrait = (member) => ({ name: member.userName, role: member.role, image: member.profileImageUrl, linkedin: member.linkedinUrl });

// One committee or club with its board as a small photo tree.
const TeamBoard = ({ team, index }) => (
  <Reveal delay={(index % 2) * 80} className={team.boardRoles.length > 2 ? "lg:col-span-2" : ""}>
    <article className={`h-full rounded-[2rem] p-6 sm:p-8 ${card}`}>
      <div className="flex items-center gap-4">
        <TeamLogo src={team.logoUrl} name={team.name} className="h-14 w-14 rounded-2xl !text-base" />
        <div className="min-w-0">
          <h3 className="text-xl font-semibold tracking-tight text-slate-900 dark:text-white">{team.name}</h3>
          {team.description && <p className={`mt-1 line-clamp-2 text-sm ${muted}`}>{team.description}</p>}
        </div>
      </div>
      {team.boardRoles.length > 0 ? (
        <OrgTree
          className="mt-8"
          members={team.boardRoles.map(toPortrait)}
          root={<span className="inline-flex items-center gap-2 rounded-full bg-ocean-67 px-4 py-2 text-sm font-semibold text-ocean-637 dark:bg-ocean-808 dark:text-ocean-222"><TeamLogo src={team.logoUrl} name={team.name} className="h-6 w-6 rounded-full" />{shortName(team.name)} board</span>}
          renderMember={(member) => <Portrait member={member} />}
        />
      ) : <p className={`mt-6 text-sm ${muted}`}>No board listed for this season.</p>}
    </article>
  </Reveal>
);

// The chapter's boards by season: the current one by default, past ones from the season picker. The season
// lives in the URL (?season=2026) so a past board can be shared.
const BoardsPage = () => {
  const [params, setParams] = useSearchParams();
  const requested = params.get("season");
  const [seasons, setSeasons] = useState(null);
  const [state, setState] = useState({ key: null, boards: null, error: "" });
  const [retry, setRetry] = useState(0);
  const selected = requested === null ? seasons?.currentSeason : Number(requested);
  const key = `${requested}-${retry}`;

  useEffect(() => {
    let active = true;
    const load = async () => {
      try {
        const available = await fetchBoardSeasons();
        if (!active) return;
        setSeasons(available);
        const season = requested === null ? available.currentSeason : Number(requested);
        if ((requested !== null && !/^\d{4}$/.test(requested)) || ![available.currentSeason, ...available.pastSeasons].includes(season)) {
          throw new Error("This season isn't available. Choose one of the listed seasons.");
        }
        if (requested === null) {
          setParams((previous) => {
            const next = new URLSearchParams(previous);
            next.set("season", String(season));
            return next;
          }, { replace: true });
          return;
        }
        let result;
        if (season === available.currentSeason) {
          const [highBoard, committees, clubs] = await Promise.all([fetchHighBoard(), fetchCommittee(), fetchAllClubs()]);
          result = { highBoard, committees, clubs: Array.isArray(clubs) ? clubs : clubs?.content };
        } else {
          result = await fetchPastBoards(season);
        }
        if (active) setState({ key, boards: normalizeBoards(result), error: "" });
      } catch (failure) {
        if (active) setState({ key, boards: null, error: failure.response?.data?.message || failure.message || "We couldn't load the boards." });
      }
    };
    load();
    return () => { active = false; };
  }, [requested, retry, key, setParams]);

  const loading = state.key !== key;
  const available = seasons ? [seasons.currentSeason, ...seasons.pastSeasons] : [];
  const validSelection = available.includes(selected);
  const boards = state.boards;
  const isCurrent = seasons && selected === seasons.currentSeason;

  return (
    <SiteLayout>
      <PageHero eyebrow="Boards" title="The people who lead the chapter" text="Meet the high board and the heads of every committee and club, this season and in past ones.">
        <label htmlFor="board-season" className="sr-only">Season</label>
        <div className="relative">
          <select
            id="board-season"
            value={validSelection ? String(selected) : ""}
            disabled={!seasons}
            onChange={(event) => setParams((previous) => {
              const next = new URLSearchParams(previous);
              next.set("season", event.target.value);
              return next;
            })}
            className="h-12 min-w-56 cursor-pointer appearance-none rounded-xl bg-white pl-4 pr-10 text-sm font-semibold text-ocean-676 shadow-sm focus:outline-none focus:ring-2 focus:ring-ocean-316 disabled:opacity-70 dark:bg-ocean-871 dark:text-ocean-222"
          >
            {!validSelection && <option value="" disabled>{seasons ? "Choose a season" : "Loading seasons…"}</option>}
            {available.map((season) => <option key={season} value={season}>{formatBoardSeason(season)} season{season === seasons.currentSeason ? " (current)" : ""}</option>)}
          </select>
          <FiChevronDown className="pointer-events-none absolute right-4 top-1/2 h-4 w-4 -translate-y-1/2 text-ocean-676 dark:text-ocean-222" aria-hidden="true" />
        </div>
      </PageHero>

      <div className="mx-auto max-w-[1280px] px-5 py-16 sm:px-8 sm:py-20" aria-busy={loading}>
        {loading ? (
          <div className="grid grid-cols-2 gap-5 sm:grid-cols-4">
            {[0, 1, 2, 3].map((i) => <div key={i} className={`aspect-[4/5] rounded-[1.75rem] ${skeleton}`} />)}
          </div>
        ) : state.error ? (
          <EmptyState icon={FiAlertCircle} tone="error" title="Something went wrong" text={state.error}>
            <button type="button" onClick={() => setRetry((value) => value + 1)} className={secondaryButton}><FiRefreshCw className="h-4 w-4" aria-hidden="true" />Try again</button>
          </EmptyState>
        ) : boards && (
          <div key={selected} className="space-y-20">
            <section aria-labelledby="high-board-heading">
              <h2 id="high-board-heading" className="sr-only">High board</h2>
              {boards.highBoard.length ? (
                <OrgTree
                  members={boards.highBoard.map(toPortrait)}
                  root={(
                    <div className="flex items-center gap-4 rounded-full bg-ocean-794 py-2 pl-2 pr-6 text-white shadow-[0_18px_40px_-20px_color-mix(in_srgb,var(--color-ocean-794)_80%,transparent)] dark:bg-ocean-820">
                      <span className="flex h-12 w-12 items-center justify-center rounded-full bg-white"><img src={logo} alt="" className="h-9 w-9 object-contain" /></span>
                      <div className="text-left"><p className="text-base font-semibold">High board</p><p className="text-xs text-ocean-145">ACM Alexandria · {formatBoardSeason(selected)} season{isCurrent ? " (current)" : ""}</p></div>
                    </div>
                  )}
                  renderMember={(member) => <Portrait member={member} aspect="aspect-[4/5]" />}
                />
              ) : <EmptyState icon={FiUsers} title="No high board listed" text="There's no high board recorded for this season." />}
            </section>
            {[["Committees", boards.committees], ["Clubs", boards.clubs]].map(([label, groups]) => (
              <section key={label} aria-labelledby={`${label}-heading`}>
                <Reveal className="mb-8">
                  <h2 id={`${label}-heading`} className="text-3xl font-semibold tracking-tight text-slate-900 sm:text-4xl dark:text-white">{label}</h2>
                  <p className={`mt-2 ${muted}`}>{groups.length} {label.toLowerCase()} in the {formatBoardSeason(selected)} season.</p>
                </Reveal>
                {groups.length ? (
                  <div className="grid gap-6 lg:grid-cols-2">
                    {groups.map((team, i) => <TeamBoard key={team.id} team={team} index={i} />)}
                  </div>
                ) : <EmptyState icon={FiUsers} title={`No ${label.toLowerCase()} listed`} text={`There are no ${label.toLowerCase()} recorded for this season.`} />}
              </section>
            ))}
          </div>
        )}
      </div>
    </SiteLayout>
  );
};

export default BoardsPage;
