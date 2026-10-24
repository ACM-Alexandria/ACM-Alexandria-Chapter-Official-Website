import { useEffect, useState } from "react";
import { Link, useSearchParams } from "react-router-dom";
import { FiArrowLeft, FiRefreshCw } from "react-icons/fi";
import AOS from "aos";
import "aos/dist/aos.css";
import Navbar from "../components/HomePage/Navbar";
import Footer from "../components/HomePage/Footer";
import HighBoardCard from "../components/HomePage/cards/HighBoardCard";
import CommitteeCard from "../components/HomePage/cards/CommitteeCard";
import ClubCard from "../components/HomePage/cards/ClubCard";
import { normalizeBoards } from "../components/PastBoards/normalizeBoards";
import SpinnerIcon from "../components/icons/SpinnerIcon";
import { fetchBoardSeasons, fetchPastBoards } from "../services/pastBoardsService";
import { fetchHighBoard, fetchCommittee, fetchAllClubs } from "../services/homePageService";
import { formatBoardSeason } from "../utils/boardSeasons";
import { fieldClass, hintClass, labelClass, noticeClass, secondaryButtonClass, tagBrandClass } from "../components/AdminPage/SharedComponents/adminStyles";

const BoardsPage = () => {
  const [params, setParams] = useSearchParams();
  const requested = params.get("season");
  const [seasons, setSeasons] = useState(null);
  const [boards, setBoards] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [retry, setRetry] = useState(0);
  const selected = requested === null ? seasons?.currentSeason : Number(requested);

  useEffect(() => {
    AOS.init({ duration: 600, once: true });
  }, []);

  useEffect(() => {
    let active = true;
    const load = async () => {
      setLoading(true);
      setError("");
      setBoards(null);
      try {
        const available = await fetchBoardSeasons();
        if (!active) return;
        setSeasons(available);
        const season = requested === null ? available.currentSeason : Number(requested);
        if ((requested !== null && !/^\d{4}$/.test(requested)) ||
            ![available.currentSeason, ...available.pastSeasons].includes(season)) {
          throw new Error("This season is unavailable. Choose one of the listed seasons.");
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
          const [highBoard, committees, clubs] = await Promise.all([
            fetchHighBoard(), fetchCommittee(), fetchAllClubs(),
          ]);
          result = { highBoard, committees, clubs };
        } else {
          result = await fetchPastBoards(season);
        }
        if (active) setBoards(normalizeBoards(result));
      } catch (failure) {
        if (active) setError(failure.response?.data?.message || failure.message || "Could not load boards.");
      } finally {
        if (active) setLoading(false);
      }
    };
    load();
    return () => { active = false; };
  }, [requested, retry, setParams]);

  useEffect(() => {
    if (boards) AOS.refresh();
  }, [boards]);

  const availableSeasons = seasons ? [seasons.currentSeason, ...seasons.pastSeasons] : [];
  const validSelection = availableSeasons.includes(selected) && (requested === null || /^\d{4}$/.test(requested));

  return (
    <div className="flex min-h-screen flex-col bg-[#F3F7FB] text-slate-800 dark:bg-[#0B1520] dark:text-slate-100">
      <Navbar activeSection="" />
      <main className="admin-ui flex-1 px-4 pb-16 pt-[106px] sm:px-6 md:px-12 max-w-[1600px] mx-auto w-full">
        <div className="flex flex-col gap-8">
          <header className="flex flex-col gap-4">
            <Link to="/" className="w-fit -ml-1 inline-flex items-center gap-1.5 rounded px-1 text-sm font-medium text-slate-600 transition-colors hover:text-slate-900 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8]/40 dark:text-slate-400 dark:hover:text-slate-100">
              <FiArrowLeft aria-hidden="true" className="h-4 w-4" />Back to home
            </Link>
            <div className="flex flex-col gap-4 sm:flex-row sm:items-end sm:justify-between">
              <div className="min-w-0">
                <h1 className="text-2xl font-semibold tracking-tight text-slate-900 sm:text-3xl dark:text-slate-100">Meet our boards</h1>
                <p className="mt-1 max-w-2xl text-sm text-slate-600 dark:text-slate-400">Get to know the people who lead our chapter today and those who helped shape its past.</p>
              </div>
              <div className="flex flex-col gap-1.5 sm:w-64">
                <label htmlFor="board-season" className={labelClass}>Season</label>
                <select
                  id="board-season"
                  value={validSelection ? String(selected) : ""}
                  disabled={!seasons}
                  onChange={(event) => setParams((previous) => {
                    const next = new URLSearchParams(previous);
                    next.set("season", event.target.value);
                    return next;
                  })}
                  className={fieldClass}
                >
                  {!validSelection && <option value="" disabled>{seasons ? "Choose a season" : "Loading seasons…"}</option>}
                  {availableSeasons.map((season) => <option key={season} value={season}>{formatBoardSeason(season)}{season === seasons.currentSeason ? " · Current" : ""}</option>)}
                </select>
              </div>
            </div>
          </header>

          <div aria-busy={loading}>
            {loading ? (
              <p role="status" className="flex items-center justify-center gap-3 py-16 text-sm font-medium text-slate-600 dark:text-slate-400"><SpinnerIcon aria-hidden="true" className="h-5 w-5 animate-spin text-[#4B98C8]" />Loading boards…</p>
            ) : error ? (
              <div role="alert" className={`flex flex-col items-start gap-3 ${noticeClass.error}`}>
                <p>{error}</p>
                <button type="button" onClick={() => setRetry((value) => value + 1)} className={secondaryButtonClass}><FiRefreshCw aria-hidden="true" className="h-4 w-4" />Retry</button>
              </div>
            ) : boards && (
              <div key={selected} className="flex flex-col gap-12">
                <section aria-labelledby="high-board-heading" className="flex flex-col gap-6">
                  <div className="flex items-center gap-3">
                    <h2 id="high-board-heading" className="text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-100">High board</h2>
                    <span className={tagBrandClass}>{formatBoardSeason(selected)}</span>
                  </div>
                  {boards.highBoard.length ? (
                    <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
                      {boards.highBoard.map((member, index) => <HighBoardCard key={member.id} member={member} index={index} />)}
                    </div>
                  ) : <EmptyBoard text="No high board members listed for this season." />}
                </section>
                {[ ["Committees", boards.committees], ["Clubs", boards.clubs] ].map(([label, groups]) => (
                  <section key={label} aria-label={label} className="flex flex-col gap-6">
                    <h2 className="text-lg font-semibold tracking-tight text-slate-900 dark:text-slate-100">{label}</h2>
                    {groups.length ? (
                      <div className="grid grid-cols-1 gap-8 md:grid-cols-2">
                        {groups.map((group) => (
                          <div key={group.id} className={group.boardRoles.length > 2 ? "min-w-0 md:col-span-2" : "min-w-0"}>
                            {label === "Committees"
                              ? <CommitteeCard committee={group} readOnly />
                              : <ClubCard club={group} />}
                          </div>
                        ))}
                      </div>
                    ) : <EmptyBoard text={`No ${label.toLowerCase()} listed for this season.`} />}
                  </section>
                ))}
              </div>
            )}
          </div>
        </div>
      </main>
      <Footer />
    </div>
  );
};

const EmptyBoard = ({ text }) => (
  <div className="rounded-2xl border border-dashed border-slate-300 px-6 py-10 text-center dark:border-slate-700">
    <p className={hintClass}>{text}</p>
  </div>
);

export default BoardsPage;
