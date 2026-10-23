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
    <div className="flex min-h-screen flex-col bg-slate-50 text-slate-800 transition-colors dark:bg-slate-950 dark:text-slate-100">
      <Navbar activeSection="" />
      <main className="flex-1 px-4 pb-16 pt-28 sm:px-6">
        <div className="mx-auto max-w-7xl space-y-12">
          <header className="space-y-6">
            <Link to="/" className="inline-flex items-center gap-2 text-sm font-bold text-[#205E85] hover:underline dark:text-sky-300"><FiArrowLeft aria-hidden="true" />Back to home</Link>
            <div>
              <p className="mb-2 text-xs font-bold uppercase tracking-widest text-[#4B98C8]">Our chapter, through the years</p>
              <h1 className="text-4xl font-black tracking-tight sm:text-5xl">Meet our boards</h1>
              <p className="mt-4 max-w-2xl text-slate-500 dark:text-slate-300">Get to know the people who lead our chapter today and those who helped shape its past.</p>
            </div>
            <div className="flex flex-wrap items-center gap-3">
              <label htmlFor="board-season" className="text-sm font-bold">Academic season</label>
              <select
                id="board-season"
                value={validSelection ? String(selected) : ""}
                disabled={!seasons}
                onChange={(event) => setParams((previous) => {
                  const next = new URLSearchParams(previous);
                  next.set("season", event.target.value);
                  return next;
                })}
                className="rounded-xl border border-slate-300 bg-white px-4 py-3 text-sm font-bold outline-none focus:border-[#4B98C8] focus:ring-2 focus:ring-[#4B98C8]/30 disabled:opacity-50 dark:border-slate-700 dark:bg-slate-900"
              >
                {!validSelection && <option value="" disabled>{seasons ? "Choose a season" : "Loading seasons…"}</option>}
                {availableSeasons.map((season) => <option key={season} value={season}>{formatBoardSeason(season)}{season === seasons.currentSeason ? " · Current" : ""}</option>)}
              </select>
            </div>
          </header>

          <div aria-busy={loading}>
            {loading ? (
              <p role="status" className="flex items-center justify-center gap-3 py-16 text-sm font-semibold text-slate-500 dark:text-slate-300"><SpinnerIcon aria-hidden="true" className="h-5 w-5 animate-spin text-[#4B98C8]" />Loading boards…</p>
            ) : error ? (
              <div role="alert" className="rounded-2xl border border-red-200 bg-red-50 p-6 text-red-700 dark:border-red-900 dark:bg-red-950/20 dark:text-red-300">
                <p>{error}</p>
                <button type="button" onClick={() => setRetry((value) => value + 1)} className="mt-4 inline-flex items-center gap-2 rounded-xl border border-red-300 px-4 py-2 text-sm font-bold dark:border-red-800"><FiRefreshCw aria-hidden="true" />Retry</button>
              </div>
            ) : boards && (
              <div key={selected} className="space-y-14">
                <section aria-labelledby="high-board-heading">
                  <h2 id="high-board-heading" className="mb-6 text-2xl font-extrabold">High board · {formatBoardSeason(selected)}</h2>
                  {boards.highBoard.length ? (
                    <div className="grid grid-cols-1 gap-6 md:grid-cols-2 lg:grid-cols-4">
                      {boards.highBoard.map((member, index) => <HighBoardCard key={member.id} member={member} index={index} />)}
                    </div>
                  ) : <p className="text-sm text-slate-500 dark:text-slate-400">No high board members listed for this season.</p>}
                </section>
                {[ ["Committees", boards.committees], ["Clubs", boards.clubs] ].map(([label, groups]) => (
                  <section key={label} aria-label={label}>
                    <h2 className="mb-6 text-2xl font-extrabold">{label}</h2>
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
                    ) : <p className="text-sm text-slate-500 dark:text-slate-400">No {label.toLowerCase()} listed for this season.</p>}
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

export default BoardsPage;
