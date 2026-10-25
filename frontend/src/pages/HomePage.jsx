import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { FiBookOpen, FiCalendar, FiClock, FiMapPin, FiRadio } from "react-icons/fi";
import { useAuth } from "../contexts/useAuth";
import { fetchActiveExclusiveForms, fetchPartners } from "../services/homePageService";
import { useSmoothScroll } from "../hooks/useSmoothScroll";
import IslandNav from "../components/Layout/IslandNav";
import SiteFooter from "../components/Layout/SiteFooter";
import HeroPharos from "../components/Home/scene/HeroPharos";
import GallerySection from "../components/Home/sections/GallerySection";
import ExclusiveFormsSection from "../components/Home/sections/ExclusiveFormsSection";
import PeopleSection from "../components/Home/sections/PeopleSection";
import ActivitySection from "../components/Home/sections/ActivitySection";
import PartnersSection from "../components/Home/sections/PartnersSection";
import FeatureCard from "../components/Home/cards/FeatureCard";
import SmallCard from "../components/Home/cards/SmallCard";
import EventDetailsSidebar from "../components/details/EventDetailsSidebar";
import ClubDetailsSidebar from "../components/details/ClubDetailsSidebar";
import ProgramDetailsSidebar from "../components/details/ProgramDetailsSidebar";
import ExclusiveFormDetailsSidebar from "../components/details/ExclusiveFormDetailsSidebar";
import RegistrationModal from "../components/registration/RegistrationModal";
import { emptyHomeData, fetchHomeData } from "../components/Home/homeData";
import { joinCta, profileCta } from "../components/Home/homeContent";
import { eventDate, summary } from "../utils/format";
import { muted } from "../components/ui/uiStyles";

// The public home page: the hero and lighthouse scene, the gallery, the chapter's people, then events,
// programs, and ACM Radio, with the exclusive forms and partners when there are any. A section without content
// is left out. Design: docs/home-page-redesign/DESIGN.md.
const HomePage = () => {
  const { isAuthenticated, user } = useAuth();
  const navigate = useNavigate();
  const [searchParams, setSearchParams] = useSearchParams();
  const [data, setData] = useState(emptyHomeData);
  const [loading, setLoading] = useState(true);
  const [forms, setForms] = useState([]);
  const [formsLoading, setFormsLoading] = useState(true);
  const [partners, setPartners] = useState([]);
  const [partnersLoading, setPartnersLoading] = useState(true);
  const [eventId, setEventId] = useState(null);
  const [clubId, setClubId] = useState(null);
  const [programId, setProgramId] = useState(null);
  const [formId, setFormId] = useState(null);
  const [committeeId, setCommitteeId] = useState(null);
  const [panel, setPanel] = useState(null);
  const [showFormsButton, setShowFormsButton] = useState(true);
  useSmoothScroll(-88, !loading);

  const openEvent = (id) => { setEventId(id); setPanel("event"); };
  const openClub = (id) => { setClubId(id); setPanel("club"); };
  const openProgram = (id) => { setProgramId(id); setPanel("program"); };
  const openForm = (id) => { setFormId(id); setPanel("form"); };
  const openApplication = (id) => { setCommitteeId(id); setPanel("apply"); };
  const closePanel = () => setPanel(null);

  useEffect(() => {
    let active = true;
    fetchHomeData()
      .then((result) => { if (active) setData(result); })
      .catch((err) => console.error("Error loading home page data:", err))
      .finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, []);

  useEffect(() => {
    let active = true;
    fetchActiveExclusiveForms()
      .then((result) => { if (active) setForms(result || []); })
      .catch((err) => console.error("Error loading active forms:", err))
      .finally(() => { if (active) setFormsLoading(false); });
    return () => { active = false; };
  }, []);

  useEffect(() => {
    let active = true;
    fetchPartners()
      .then((result) => { if (active) setPartners(result || []); })
      .catch((err) => console.error("Error loading partners:", err))
      .finally(() => { if (active) setPartnersLoading(false); });
    return () => { active = false; };
  }, []);

  // Reopens the sidebar or application a visitor was on before a login redirect, then clears the parameter.
  useEffect(() => {
    const targets = [
      ["openEventId", (id) => { setEventId(id); setPanel("event"); }],
      ["openClubId", (id) => { setClubId(id); setPanel("club"); }],
      ["openProgramId", (id) => { setProgramId(id); setPanel("program"); }],
      ["openCommitteeId", (id) => { setCommitteeId(id); setPanel("apply"); }],
      ["openExclusiveFormId", (id) => { setFormId(id); setPanel("form"); }],
    ];
    const match = targets.find(([key]) => searchParams.get(key));
    if (!match) return;
    const [key, open] = match;
    open(Number(searchParams.get(key)));
    const next = new URLSearchParams(searchParams);
    next.delete(key);
    setSearchParams(next, { replace: true });
  }, [searchParams, setSearchParams]);

  useEffect(() => {
    const onScroll = () => setShowFormsButton(window.scrollY < 300);
    window.addEventListener("scroll", onScroll, { passive: true });
    return () => window.removeEventListener("scroll", onScroll);
  }, []);

  const myCommitteeId = isAuthenticated && ["ACM_MEMBER", "ACM_COMMITTEE_BOARD"].includes(user?.role) ? user?.committeeId : null;
  const { committees, clubs } = data;
  // A section shows while loading and then only if it has something to show.
  const has = (key) => loading || data[key].length > 0;
  const links = [
    ["#people", "People"],
    has("events") && ["#events", "Events"],
    has("programs") && ["#programs", "Programs"],
    has("radio") && ["#radio", "Radio"],
  ].filter(Boolean);

  const [featuredEvent, ...otherEvents] = data.events;
  const [featuredProgram, ...otherPrograms] = data.programs;
  const [featuredSeason, ...otherSeasons] = data.radio;
  const episodes = data.radio.reduce((total, season) => total + season.episodes, 0);

  return (
    <div className="hp admin-ui min-h-screen bg-[#F3F7FB] text-slate-800 dark:bg-[#0A1724] dark:text-slate-100">
      <IslandNav links={links} />

      <HeroPharos committees={committees} clubs={clubs} stats={data.stats} cta={isAuthenticated ? profileCta : joinCta} />

      {data.gallery.length > 0 && <GallerySection images={data.gallery} />}

      <main>
        <ExclusiveFormsSection loading={formsLoading} forms={forms} onShowDetails={openForm} />

        <PeopleSection
          loading={loading}
          highBoard={data.highBoard}
          committees={committees}
          clubs={clubs}
          currentSeason={data.currentSeason}
          myCommitteeId={myCommitteeId}
          onApply={openApplication}
          onShowClub={openClub}
          isRegistrationModalOpen={panel === "apply"}
        />

        {has("events") && (
          <ActivitySection id="events" icon={FiCalendar} label="Events" count={data.counts.events} countLabel={data.counts.events === 1 ? "event this year" : "events this year"} text="Workshops, talks, and competitions open to every student." to="/events" cta={`See all ${data.counts.events} events`} tone="white" loading={loading}
            featured={featuredEvent && (
              <FeatureCard image={featuredEvent.imageUrl} badge="Latest" title={featuredEvent.name} meta={eventDate(featuredEvent.time).full} text={summary(featuredEvent.description, 190)} onClick={() => openEvent(featuredEvent.id)}>
                {featuredEvent.location && <p className={`mt-4 flex items-center gap-1.5 text-sm font-medium ${muted}`}><FiMapPin className="h-4 w-4" aria-hidden="true" />Room {featuredEvent.location}</p>}
              </FeatureCard>
            )}
            more={otherEvents.slice(0, 3).map((e, i) => <SmallCard key={e.id} image={e.imageUrl} title={e.name} meta={eventDate(e.time).full} onClick={() => openEvent(e.id)} index={i} />)}
          />
        )}

        {has("programs") && (
          <ActivitySection id="programs" icon={FiBookOpen} label="Programs" count={data.counts.programs} countLabel={data.counts.programs === 1 ? "training program" : "training programs"} text="Longer tracks that meet every week and build real skills." to="/programs" cta="See all programs" flip loading={loading}
            featured={featuredProgram && (
              <FeatureCard image={featuredProgram.imageUrl} badge={featuredProgram.open ? "Registration open" : "Registration closed"} title={featuredProgram.name} meta={featuredProgram.time} text={summary(featuredProgram.description, 190)} onClick={() => openProgram(featuredProgram.id)}>
                {featuredProgram.startDate && <p className={`mt-4 text-sm font-medium ${muted}`}>{eventDate(featuredProgram.startDate).day} {eventDate(featuredProgram.startDate).month} to {eventDate(featuredProgram.endDate).day} {eventDate(featuredProgram.endDate).month}</p>}
              </FeatureCard>
            )}
            more={otherPrograms.slice(0, 3).map((p, i) => <SmallCard key={p.id} image={p.imageUrl} title={p.name} meta={p.time} onClick={() => openProgram(p.id)} index={i} />)}
          />
        )}

        {has("radio") && (
          <ActivitySection id="radio" icon={FiRadio} label="ACM Radio" count={data.counts.radio} countLabel={`seasons, ${episodes} episodes`} text="Conversations with members, guests, and alumni about tech and student life." to="/radio" cta="Listen to ACM Radio" tone="white" loading={loading}
            featured={featuredSeason && (
              <FeatureCard image={featuredSeason.imageUrl} badge="Newest season" title={`Season ${featuredSeason.season}`} meta={`${featuredSeason.episodes} ${featuredSeason.episodes === 1 ? "episode" : "episodes"} so far`} text="The latest season of ACM Radio, recorded by the chapter's own members." onClick={() => navigate(`/radio/seasons/${featuredSeason.id}`)} />
            )}
            more={otherSeasons.slice(0, 3).map((r, i) => <SmallCard square key={r.id} image={r.imageUrl} title={`Season ${r.season}`} meta={`${r.episodes} episodes`} to={`/radio/seasons/${r.id}`} index={i} />)}
          />
        )}

        {(partnersLoading || partners.length > 0) && <PartnersSection loading={partnersLoading} partners={partners} />}
      </main>

      <SiteFooter social={data.social} links={links} showPartners={partners.length > 0} />

      <EventDetailsSidebar eventId={eventId} isOpen={panel === "event"} onClose={closePanel} />
      <ClubDetailsSidebar clubId={clubId} isOpen={panel === "club"} onClose={closePanel} />
      <ProgramDetailsSidebar programId={programId} isOpen={panel === "program"} onClose={closePanel} />
      <ExclusiveFormDetailsSidebar formId={formId} isOpen={panel === "form"} onClose={closePanel} />
      <RegistrationModal
        isOpen={panel === "apply"}
        onClose={closePanel}
        entityId={committeeId}
        type="committee"
        entityName={data.committees.find((c) => c.id === committeeId)?.name || ""}
      />

      {forms.length > 0 && (
        <a
          href="#exclusive-forms"
          className={`fixed bottom-6 right-6 z-30 inline-flex h-12 items-center gap-2 rounded-xl bg-[#205E85] px-5 text-sm font-semibold text-white shadow-[0_14px_40px_-12px_rgba(8,24,38,0.6)] transition-all duration-500 hover:bg-[#16364C] dark:bg-[#4B98C8] dark:text-[#081826] ${showFormsButton ? "translate-y-0 opacity-100" : "pointer-events-none translate-y-16 opacity-0"}`}
        >
          <FiClock className="h-4 w-4" aria-hidden="true" />Exclusive opportunities
        </a>
      )}
    </div>
  );
};

export default HomePage;
