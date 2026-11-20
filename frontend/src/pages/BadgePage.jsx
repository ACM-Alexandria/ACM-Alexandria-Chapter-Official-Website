import { Link, useParams } from "react-router-dom";
import { FiAlertCircle } from "react-icons/fi";
import SiteLayout from "../components/Layout/SiteLayout";
import EmptyState from "../components/ui/EmptyState";
import BadgeMedal from "../components/badges/BadgeMedal";
import BadgeBanner from "../components/badges/BadgeBanner";
import BadgeBackdrop from "../components/badges/BadgeBackdrop";
import logo from "../assets/logo/acm-logo-outline.svg";
import useDetails from "../hooks/useDetails";
import usePageMeta from "../hooks/usePageMeta";
import { fetchPublicBadge } from "../services/badgesService";
import { useAuth } from "../contexts/useAuth";
import ShareOnLinkedInButton from "../components/badges/ShareOnLinkedInButton";
import { secondaryButton, skeleton } from "../components/ui/uiStyles";
import { badgeGroupLabel } from "../components/badges/badgeGroup";

const awardedOn = (iso) =>
  iso ? new Date(iso).toLocaleDateString("en-GB", { day: "numeric", month: "long", year: "numeric" }) : "";

// One awarded badge, public: the page shared links (LinkedIn posts) open. On wide screens it is the banner, laid
// out exactly like the share image (BadgeBanner); on phones the medal sits above the text. The badge's
// description is on the banner when it has one, and so is a Share on LinkedIn button for the member who owns it.
const BadgePage = () => {
  const { publicId } = useParams();
  const { data: badge, loading, error } = useDetails(true, publicId, fetchPublicBadge,
    "This badge isn't available. It may have been removed.");
  const { user } = useAuth();
  const isOwner = Boolean(user?.id && badge?.member?.id === user.id);
  const awarded = badge ? awardedOn(badge.awardedAt) : "";
  usePageMeta("badge", badge ? {
    title: `${badge.member.name}: ${badge.name}, ${badge.periodLabel}`,
    description: `${badge.member.name} earned the ${badge.name} badge (${badge.periodLabel}) at the ACM Alexandria Student Chapter.`,
  } : undefined);

  return (
    <SiteLayout solidNav>
      <div className="mx-auto flex max-w-[1280px] justify-center px-5 pb-16 pt-28 sm:px-8 sm:pb-24 sm:pt-36">
        {loading ? (
          <div className={`h-[32rem] w-full max-w-5xl rounded-[2rem] md:aspect-[1200/627] md:h-auto ${skeleton}`} aria-busy="true" aria-label="Loading badge" />
        ) : error || !badge ? (
          <div className="w-full max-w-xl">
            <EmptyState icon={FiAlertCircle} tone="error" title="Badge not found" text={error}>
              <Link to="/" className={secondaryButton}>Go to the homepage</Link>
            </EmptyState>
          </div>
        ) : (
          <div className="w-full max-w-5xl">
            <BadgeBanner badge={badge} awardedOn={awarded} className="hidden md:block">
              {isOwner && <ShareOnLinkedInButton badge={badge} variant="banner" />}
            </BadgeBanner>

            <article
              className="relative flex flex-col items-center gap-8 overflow-hidden rounded-[2rem] px-6 py-10 text-center text-white sm:px-10 md:hidden"
              style={{ background: "linear-gradient(135deg, #0B2E49 0%, #0F3A5A 55%, #15507A 100%)" }}
            >
              <BadgeBackdrop />
              <div className="relative shrink-0">
                <BadgeMedal
                  periodType={badge.periodType}
                  periodLabel={badge.periodLabel}
                  icon={badge.icon}
                  onDark
                  size={240}
                  label={`${badge.name}, ${badge.periodLabel}`}
                  className="relative h-40 w-40 sm:h-52 sm:w-52"
                />
              </div>
              <div className="relative min-w-0">
                <div className="flex flex-col items-center justify-center gap-2 sm:flex-row sm:gap-3">
                  <img src={logo} alt="" className="h-14 w-auto sm:h-16" />
                  <p className="text-base font-semibold text-gold-180 sm:text-lg">ACM Alexandria Student Chapter</p>
                </div>
                <h1 className="mt-4 break-words text-4xl font-extrabold leading-[1.05] tracking-[-0.02em] sm:text-5xl">{badge.member.name}</h1>
                <p className="mt-3 text-2xl font-bold text-ocean-114 sm:text-3xl">{badge.name}</p>
                <p className="mt-2 text-lg text-ocean-114 sm:text-xl">{badge.periodLabel} · {badgeGroupLabel(badge)}</p>
                {badge.note && (
                  <p className="mx-auto mt-6 max-w-xl text-lg leading-relaxed text-white/90">
                    <span className="sr-only">Description: </span>“{badge.note}”
                  </p>
                )}
                <p className="mt-8 flex items-center justify-center gap-3 text-base text-ocean-145">
                  <span aria-hidden="true" className="h-[3px] w-9 bg-gold-518" />
                  Awarded {awarded}
                </p>
                {isOwner && <ShareOnLinkedInButton badge={badge} variant="dark" className="mt-6" />}
              </div>
            </article>
          </div>
        )}
      </div>
    </SiteLayout>
  );
};

export default BadgePage;
