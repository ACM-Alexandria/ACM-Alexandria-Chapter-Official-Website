import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import AOS from "aos";
import "aos/dist/aos.css";
import Navbar from "../components/HomePage/Navbar";
import GreetingSection from "../components/HomePage/sections/GreetingSection";
import AboutSection from "../components/HomePage/sections/AboutSection";
import ClubsSection from "../components/HomePage/sections/ClubsSection";
import EventsSection from "../components/HomePage/sections/EventsSection";
import ProgramsSection from "../components/HomePage/sections/ProgramsSection";
import ServicesSection from "../components/HomePage/sections/ServicesSection";
import EventDetailsSidebar from "../components/HomePage/EventDetailsSidebar";
import ClubDetailsSidebar from "../components/HomePage/ClubDetailsSidebar";
import RegistrationModal from "../components/registration/RegistrationModal";
import Footer from "../components/HomePage/Footer";
import { fetchHomePageData } from "../services/homePageService";

const HomePage = () => {
  const [clubs, setClubs] = useState([]);
  const [committee, setCommittee] = useState([]);
  const [highBoard, setHighBoard] = useState([]);
  const [events, setEvents] = useState([]);
  const [programs, setPrograms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeSection, setActiveSection] = useState("greeting");
  const [selectedEventId, setSelectedEventId] = useState(null);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [selectedClubId, setSelectedClubId] = useState(null);
  const [isClubSidebarOpen, setIsClubSidebarOpen] = useState(false);
  const [selectedCommitteeIdForReg, setSelectedCommitteeIdForReg] = useState(null);
  const [isCommitteeRegOpen, setIsCommitteeRegOpen] = useState(false);

  const handleApplyClick = (committeeId) => {
    setSelectedCommitteeIdForReg(committeeId);
    setIsCommitteeRegOpen(true);
  };

  const handleShowEventDetails = (eventId) => {
    setSelectedEventId(eventId);
    setIsSidebarOpen(true);
  };

  const handleCloseSidebar = () => {
    setIsSidebarOpen(false);
  };

  const handleShowClubDetails = (clubId) => {
    setSelectedClubId(clubId);
    setIsClubSidebarOpen(true);
  };

  const handleCloseClubSidebar = () => {
    setIsClubSidebarOpen(false);
  };

  const [searchParams, setSearchParams] = useSearchParams();

  // Automatically restores sidebar state if returning from dynamic login redirects (User Request)
  useEffect(() => {
    const openEventId = searchParams.get("openEventId");
    const openClubId = searchParams.get("openClubId");
    const openCommitteeId = searchParams.get("openCommitteeId");

    if (openEventId) {
      handleShowEventDetails(Number(openEventId));
      const newParams = new URLSearchParams(searchParams);
      newParams.delete("openEventId");
      setSearchParams(newParams, { replace: true });
    } else if (openClubId) {
      handleShowClubDetails(Number(openClubId));
      const newParams = new URLSearchParams(searchParams);
      newParams.delete("openClubId");
      setSearchParams(newParams, { replace: true });
    } else if (openCommitteeId) {
      setSelectedCommitteeIdForReg(Number(openCommitteeId));
      setIsCommitteeRegOpen(true);
      const newParams = new URLSearchParams(searchParams);
      newParams.delete("openCommitteeId");
      setSearchParams(newParams, { replace: true });
    }
  }, [searchParams]);

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        const data = await fetchHomePageData();

        setClubs(data.clubs || []);
        setCommittee(data.committee || []);
        setHighBoard(data.highBoard || []);
        setEvents(data.events || []);
        setPrograms(data.programs || []);
      } catch (err) {
        console.error("Error loading home page data:", err);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  // Initialize AOS animations
  useEffect(() => {
    AOS.init({
      disable: true,
    });
  }, []);

  // Track active section based on scroll position
  useEffect(() => {
    let debounceTimer;

    const handleScroll = () => {
      clearTimeout(debounceTimer);
      debounceTimer = setTimeout(() => {
        const sections = document.querySelectorAll("section[id]");
        const navHeight = 70; // navbar height

        let closestSection = "greeting";
        let closestDistance = Infinity;

        sections.forEach((section) => {
          const rect = section.getBoundingClientRect();
          // Calculate distance from top of viewport minus navbar
          const distance = Math.abs(rect.top - navHeight);

          if (distance < closestDistance) {
            closestDistance = distance;
            closestSection = section.id;
          }
        });

        setActiveSection(closestSection);
      }, 0); // Debounce every 50ms
    };

    window.addEventListener("scroll", handleScroll);
    return () => {
      window.removeEventListener("scroll", handleScroll);
      clearTimeout(debounceTimer);
    };
  }, []);

  const isEnabled = (envVal) => envVal !== "false";

  return (
    <div className="min-h-screen flex flex-col">
      <Navbar activeSection={activeSection} />
      <main className="flex-1 pt-[74px]">
        <GreetingSection />
        {isEnabled(import.meta.env.VITE_ENABLE_ABOUT) && (
          <AboutSection 
            loading={loading} 
            highBoard={highBoard} 
            committees={committee} 
            onApplyClick={handleApplyClick} 
            isRegistrationModalOpen={isCommitteeRegOpen}
          />
        )}
        {isEnabled(import.meta.env.VITE_ENABLE_CLUBS) && (
          <ClubsSection loading={loading} clubs={clubs} onShowClubDetails={handleShowClubDetails} />
        )}
        {isEnabled(import.meta.env.VITE_ENABLE_EVENTS) && (
          <EventsSection
            loading={loading}
            events={events}
            onShowEventDetails={handleShowEventDetails}
          />
        )}
        {isEnabled(import.meta.env.VITE_ENABLE_PROGRAMS) && (
          <ProgramsSection loading={loading} programs={programs} />
        )}
        {isEnabled(import.meta.env.VITE_ENABLE_SERVICES) && (
          <ServicesSection />
        )}
      </main>
      <Footer />

      <EventDetailsSidebar
        eventId={selectedEventId}
        isOpen={isSidebarOpen}
        onClose={handleCloseSidebar}
      />

      <ClubDetailsSidebar
        clubId={selectedClubId}
        isOpen={isClubSidebarOpen}
        onClose={handleCloseClubSidebar}
      />

      <RegistrationModal
        isOpen={isCommitteeRegOpen}
        onClose={() => setIsCommitteeRegOpen(false)}
        entityId={selectedCommitteeIdForReg}
        type="committee"
        entityName={committee.find(c => c.id === selectedCommitteeIdForReg)?.name || ""}
      />
    </div>
  );
};

export default HomePage;
