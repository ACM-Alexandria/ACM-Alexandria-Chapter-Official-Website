import { FiAlertCircle, FiMic } from "react-icons/fi";
import SiteLayout from "../components/Layout/SiteLayout";
import PageHero from "../components/Layout/PageHero";
import MediaCard from "../components/ui/MediaCard";
import Pagination from "../components/ui/Pagination";
import EmptyState from "../components/ui/EmptyState";
import { fetchSeasons } from "../services/homePageService";
import usePagedList from "../hooks/usePagedList";
import { skeleton } from "../components/ui/uiStyles";

// Every season of ACM Radio; a season opens its episodes.
const RadioPage = () => {
  const list = usePagedList(fetchSeasons, "We couldn't load ACM Radio. Please try again later.");

  return (
    <SiteLayout>
      <PageHero eyebrow="ACM Radio" title="Conversations about tech and student life" text="Members, guests, and alumni on tech and student life. Pick a season to see its episodes." />
      <div className="mx-auto max-w-[1280px] px-5 py-16 sm:px-8 sm:py-20">
        {list.loading ? (
          <div className="grid grid-cols-2 gap-5 sm:gap-6 lg:grid-cols-4">
            {Array.from({ length: 4 }, (_, i) => <div key={i} className={`aspect-[4/5] rounded-3xl ${skeleton}`} />)}
          </div>
        ) : list.error ? (
          <EmptyState icon={FiAlertCircle} tone="error" title="Something went wrong" text={list.error} />
        ) : list.items.length === 0 ? (
          <EmptyState icon={FiMic} title="No seasons yet" text="The first season of ACM Radio is on its way." />
        ) : (
          <>
            <div className="grid grid-cols-2 gap-5 sm:gap-6 lg:grid-cols-4">
              {list.items.map((season, i) => {
                const count = season.episodes?.length || 0;
                return <MediaCard key={season.id} index={i} square image={season.imageUrl} meta={`${count} ${count === 1 ? "episode" : "episodes"}`} title={`Season ${season.seasonNumber}`} to={`/radio/seasons/${season.id}`} />;
              })}
            </div>
            <Pagination page={list.page} totalPages={list.totalPages} onChange={list.setPage} />
          </>
        )}
      </div>
    </SiteLayout>
  );
};

export default RadioPage;
