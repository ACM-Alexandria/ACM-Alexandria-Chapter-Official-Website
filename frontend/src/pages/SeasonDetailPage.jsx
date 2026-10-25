import { Link, useParams } from "react-router-dom";
import { FiAlertCircle, FiArrowLeft, FiMic, FiPlay, FiUser } from "react-icons/fi";
import SiteLayout from "../components/Layout/SiteLayout";
import PageHero from "../components/Layout/PageHero";
import EmptyState from "../components/ui/EmptyState";
import Reveal from "../components/ui/Reveal";
import { fetchSeasonById } from "../services/homePageService";
import useDetails from "../hooks/useDetails";
import { card, muted, secondaryButton, skeleton } from "../components/ui/uiStyles";

const backLink = "inline-flex h-11 items-center gap-2 rounded-xl px-4 text-sm font-semibold text-white ring-1 ring-white/25 transition-colors hover:bg-white/10";

// One episode: the thumbnail opens it (on YouTube or wherever it's published), with its number, host, and guest.
const EpisodeCard = ({ episode, index }) => (
  <Reveal delay={(index % 4) * 70} className="h-full">
    <a href={episode.url} target="_blank" rel="noopener noreferrer" className={`group flex h-full flex-col overflow-hidden rounded-3xl transition-transform duration-300 hover:-translate-y-1 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-[#4B98C8] ${card}`}>
      <div className="relative aspect-video overflow-hidden bg-[#E2F0FA] dark:bg-[#16364C]">
        {episode.imageUrl && <img src={episode.imageUrl} alt="" className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-[1.05]" loading="lazy" />}
        <span className="absolute inset-0 flex items-center justify-center bg-[#06121D]/35 opacity-0 transition-opacity group-hover:opacity-100">
          <span className="flex h-12 w-12 items-center justify-center rounded-full bg-white text-[#205E85] shadow-lg"><FiPlay className="ml-0.5 h-5 w-5 fill-current" aria-hidden="true" /></span>
        </span>
        <span className="absolute bottom-3 right-3 rounded-full bg-[#06121D]/75 px-2.5 py-0.5 text-xs font-semibold text-white">Episode {episode.episodeNumber}</span>
      </div>
      <div className="flex flex-1 flex-col p-5">
        <h3 className="line-clamp-2 text-base font-semibold tracking-tight text-slate-900 dark:text-white">{episode.title}</h3>
        <dl className={`mt-auto grid gap-1.5 pt-4 text-sm ${muted}`}>
          <div className="flex items-center gap-2"><FiUser className="h-4 w-4 shrink-0" aria-hidden="true" /><dt className="sr-only">Host</dt><dd>Hosted by <span className="font-semibold text-slate-800 dark:text-slate-100">{episode.host || "the chapter"}</span></dd></div>
          {episode.guest && <div className="flex items-center gap-2"><FiMic className="h-4 w-4 shrink-0" aria-hidden="true" /><dt className="sr-only">Guest</dt><dd>With <span className="font-semibold text-slate-800 dark:text-slate-100">{episode.guest}</span></dd></div>}
        </dl>
      </div>
    </a>
  </Reveal>
);

// A season of ACM Radio with all its episodes in order.
const SeasonDetailPage = () => {
  const { id } = useParams();
  const { data: season, loading, error } = useDetails(true, id, fetchSeasonById, "We couldn't load this season. Please try again later.");
  const episodes = season?.episodes ? [...season.episodes].sort((a, b) => a.episodeNumber - b.episodeNumber) : [];
  const ready = !loading && season;

  return (
    <SiteLayout>
      <PageHero
        eyebrow="ACM Radio"
        title={ready ? `Season ${season.seasonNumber}` : loading ? "Loading season…" : "Season"}
        text={ready ? `${episodes.length} ${episodes.length === 1 ? "episode" : "episodes"} of conversations with members, guests, and alumni.` : undefined}
        media={ready && season.imageUrl ? <img src={season.imageUrl} alt="" className="h-40 w-40 rounded-[2rem] object-cover shadow-[0_24px_60px_-24px_rgba(0,0,0,0.6)] ring-1 ring-white/15 sm:h-48 sm:w-48" /> : undefined}
      >
        <Link to="/radio" className={backLink}><FiArrowLeft className="h-4 w-4" aria-hidden="true" />All seasons</Link>
      </PageHero>
      <div className="mx-auto max-w-[1280px] px-5 py-16 sm:px-8 sm:py-20">
        {loading ? (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {Array.from({ length: 4 }, (_, i) => <div key={i} className={`aspect-[4/5] rounded-3xl ${skeleton}`} />)}
          </div>
        ) : error ? (
          <EmptyState icon={FiAlertCircle} tone="error" title="Something went wrong" text={error}>
            <Link to="/radio" className={secondaryButton}>Back to all seasons</Link>
          </EmptyState>
        ) : episodes.length === 0 ? (
          <EmptyState icon={FiMic} title="No episodes yet" text="This season is being recorded. Check back soon." />
        ) : (
          <div className="grid gap-6 sm:grid-cols-2 lg:grid-cols-4">
            {episodes.map((episode, i) => <EpisodeCard key={episode.id} episode={episode} index={i} />)}
          </div>
        )}
      </div>
    </SiteLayout>
  );
};

export default SeasonDetailPage;
