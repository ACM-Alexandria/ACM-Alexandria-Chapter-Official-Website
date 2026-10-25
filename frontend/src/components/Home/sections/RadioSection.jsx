import { Link } from "react-router-dom";
import { FiArrowUpRight, FiHeadphones, FiPlay, FiRadio } from "react-icons/fi";
import SectionIntro from "./SectionIntro";
import NightSky from "../../ui/NightSky";
import Reveal from "../../ui/Reveal";

// Bar heights for the waveform, as a share of its height; fixed so the shape is the same on every render.
const bars = Array.from({ length: 36 }, (_, i) => 0.3 + (((i * 37) % 23) / 23) * 0.7);

const Cover = ({ season, className }) => (
  <span className={`relative block overflow-hidden bg-[#16364C] ring-1 ring-white/10 ${className}`}>
    {season.imageUrl
      ? <img src={season.imageUrl} alt="" className="h-full w-full object-cover" loading="lazy" />
      : <span className="flex h-full w-full items-center justify-center text-2xl font-semibold text-[#7DBBE0]">S{season.season}</span>}
  </span>
);

// ACM Radio as a night studio: a dark band with stars, like the lighthouse scene at night. The newest season sits
// in a player with a moving waveform; its play button opens the latest episode (or the season when there is no
// link). The older seasons follow as a row of covers.
const RadioSection = ({ loading, seasons, count }) => {
  const [featured, ...older] = seasons;
  const episodes = seasons.reduce((total, season) => total + season.episodes, 0);
  const latest = featured?.latest;
  const seasonPath = featured && `/radio/seasons/${featured.id}`;
  const play = "flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-white text-[#0F3A5A] transition-transform hover:scale-105 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#7DBBE0] focus-visible:ring-offset-2 focus-visible:ring-offset-[#0B2236]";

  return (
    <section id="radio" className="relative scroll-mt-20 overflow-hidden bg-[#0B2236] py-16 text-white sm:py-24 dark:bg-[#06121D]">
      <NightSky always />
      <div className="relative mx-auto grid max-w-[1280px] gap-10 px-5 sm:gap-12 sm:px-8 lg:grid-cols-[0.7fr_1.3fr] lg:items-start">
        <div className="lg:sticky lg:top-28">
          <SectionIntro dark icon={FiRadio} label="ACM Radio" count={count} countLabel={`seasons, ${episodes} episodes`} text="Conversations with members, guests, and alumni about tech and student life." to="/radio" cta="Listen to ACM Radio" />
        </div>

        {loading ? <div className="h-[420px] animate-pulse rounded-[2rem] bg-white/5" /> : featured && (
          <div className="flex flex-col gap-5">
            <Reveal className="rounded-[2rem] bg-white/5 p-5 ring-1 ring-white/10 sm:p-8">
              <div className="flex flex-col gap-6 sm:flex-row sm:items-center sm:gap-8">
                <Link to={seasonPath} className="w-40 shrink-0 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#7DBBE0] sm:w-52" aria-label={`Open season ${featured.season}`}>
                  <Cover season={featured} className="aspect-square rounded-3xl" />
                </Link>
                <div className="min-w-0 flex-1">
                  <span className="rounded-full bg-white/10 px-3 py-1 text-xs font-semibold text-[#9FD0EE]">Newest season</span>
                  <h3 className="mt-4 text-3xl font-semibold tracking-tight sm:text-4xl">Season {featured.season}</h3>
                  <p className="mt-1 font-medium text-[#7DBBE0]">{featured.episodes} {featured.episodes === 1 ? "episode" : "episodes"} so far</p>
                  {latest && (
                    <p className="mt-4 text-[#CFE6F5]">
                      <span className="text-sm font-semibold text-[#7DBBE0]">Latest{latest.number ? `, episode ${latest.number}` : ""}</span>
                      <span className="mt-1 block line-clamp-2 text-lg font-semibold text-white">{latest.title}</span>
                      {latest.guest && <span className="mt-0.5 block text-sm">with {latest.guest}</span>}
                    </p>
                  )}
                </div>
              </div>
              <div className="mt-7 flex items-center gap-5">
                {latest?.url
                  ? <a href={latest.url} target="_blank" rel="noopener noreferrer" className={play} aria-label={`Play ${latest.title}`}><FiPlay className="ml-0.5 h-6 w-6" aria-hidden="true" /></a>
                  : <Link to={seasonPath} className={play} aria-label={`Open season ${featured.season}`}><FiHeadphones className="h-6 w-6" aria-hidden="true" /></Link>}
                <div aria-hidden="true" className="flex h-12 min-w-0 flex-1 items-center gap-[3px] overflow-hidden">
                  {bars.map((h, i) => (
                    <span
                      key={i}
                      className="w-1 shrink-0 rounded-full bg-[#4B98C8] sm:w-1.5"
                      style={{ height: `${Math.round(h * 100)}%`, animation: `hp-bar ${0.9 + (i % 4) * 0.22}s ease-in-out ${(i % 6) * 0.11}s infinite` }}
                    />
                  ))}
                </div>
                <Link to={seasonPath} className="hidden shrink-0 items-center gap-1.5 text-sm font-semibold text-[#9FD0EE] hover:text-white sm:inline-flex">All episodes <FiArrowUpRight className="h-4 w-4" aria-hidden="true" /></Link>
              </div>
            </Reveal>

            {older.length > 0 && (
              <div className="-mx-5 flex snap-x snap-mandatory gap-4 overflow-x-auto px-5 pb-2 [scrollbar-width:none] sm:mx-0 sm:grid sm:grid-cols-3 sm:overflow-visible sm:px-0 sm:pb-0">
                {older.slice(0, 3).map((season, i) => (
                  <Reveal key={season.id} delay={i * 80} className="w-[58%] shrink-0 snap-start sm:w-auto">
                    <Link to={`/radio/seasons/${season.id}`} className="group flex items-center gap-3 rounded-3xl bg-white/5 p-3 ring-1 ring-white/10 transition-colors hover:bg-white/10 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#7DBBE0]">
                      <Cover season={season} className="h-16 w-16 shrink-0 rounded-2xl" />
                      <span className="min-w-0">
                        <span className="block font-semibold">Season {season.season}</span>
                        <span className="block text-sm text-[#7DBBE0]">{season.episodes} {season.episodes === 1 ? "episode" : "episodes"}</span>
                      </span>
                    </Link>
                  </Reveal>
                ))}
              </div>
            )}
          </div>
        )}
      </div>
    </section>
  );
};

export default RadioSection;
