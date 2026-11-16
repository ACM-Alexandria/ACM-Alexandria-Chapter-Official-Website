import { Link, useParams } from "react-router-dom";
import { FiAlertCircle } from "react-icons/fi";
import SiteLayout from "../components/Layout/SiteLayout";
import EmptyState from "../components/ui/EmptyState";
import BadgeMedal from "../components/badges/BadgeMedal";
import useDetails from "../hooks/useDetails";
import { fetchPublicBadge } from "../services/badgesService";
import { useAuth } from "../contexts/useAuth";
import ShareOnLinkedInButton from "../components/badges/ShareOnLinkedInButton";
import { secondaryButton, skeleton } from "../components/ui/uiStyles";

const awardedOn = (iso) =>
  iso ? new Date(iso).toLocaleDateString("en-GB", { day: "numeric", month: "long", year: "numeric" }) : "";

// One awarded badge, public: the page shared links (LinkedIn posts) open. It looks like the share image:
// a wide deep-blue card with the medal beside the member's name (the largest text), the badge, the period
// and committee, and the award date. On phones the medal sits above the text. The member who owns the
// badge also gets a Share on LinkedIn button.
const BadgePage = () => {
  const { publicId } = useParams();
  const { data: badge, loading, error } = useDetails(true, publicId, fetchPublicBadge,
    "This badge isn't available. It may have been removed.");
  const { user } = useAuth();
  const isOwner = Boolean(user?.id && badge?.member?.id === user.id);

  return (
    <SiteLayout solidNav>
      <div className="mx-auto flex max-w-[1280px] justify-center px-5 pb-16 pt-28 sm:px-8 sm:pb-24 sm:pt-36">
        {loading ? (
          <div className={`h-[32rem] w-full max-w-5xl rounded-[2rem] md:h-[26rem] ${skeleton}`} aria-busy="true" aria-label="Loading badge" />
        ) : error || !badge ? (
          <div className="w-full max-w-xl">
            <EmptyState icon={FiAlertCircle} tone="error" title="Badge not found" text={error}>
              <Link to="/" className={secondaryButton}>Go to the homepage</Link>
            </EmptyState>
          </div>
        ) : (
          <article className="relative flex w-full max-w-5xl flex-col items-center gap-8 overflow-hidden rounded-[2rem] bg-ocean-794 px-6 py-10 text-center text-white sm:px-10 md:flex-row md:gap-14 md:px-16 md:py-14 md:text-left">
            <span aria-hidden="true" className="pointer-events-none absolute -right-28 -top-28 h-80 w-80 rounded-full ring-1 ring-white/10" />
            <span aria-hidden="true" className="pointer-events-none absolute -right-10 -top-10 h-48 w-48 rounded-full ring-1 ring-white/10" />
            <BadgeMedal
              periodType={badge.periodType}
              icon={badge.icon}
              size={240}
              label={`${badge.name}, ${badge.periodLabel}`}
              className="relative h-40 w-40 sm:h-52 sm:w-52 md:h-60 md:w-60"
            />
            <div className="relative min-w-0">
              <p className="text-base font-semibold text-gold-180 sm:text-lg">ACM Alexandria Student Chapter</p>
              <h1 className="mt-4 break-words text-4xl font-bold leading-[1.05] tracking-[-0.025em] sm:text-5xl md:text-6xl">{badge.member.name}</h1>
              <p className="mt-3 text-2xl font-semibold text-ocean-114 sm:text-3xl">{badge.name}</p>
              <p className="mt-2 text-lg text-ocean-114 sm:text-xl">{badge.periodLabel} · {badge.committeeName}</p>
              <p className="mt-8 flex items-center justify-center gap-3 text-base text-ocean-145 md:justify-start">
                <span aria-hidden="true" className="h-[3px] w-9 bg-gold-518" />
                Awarded {awardedOn(badge.awardedAt)}
              </p>
              {isOwner && <ShareOnLinkedInButton badge={badge} variant="dark" className="mt-8" />}
            </div>
          </article>
        )}
      </div>
    </SiteLayout>
  );
};

export default BadgePage;
