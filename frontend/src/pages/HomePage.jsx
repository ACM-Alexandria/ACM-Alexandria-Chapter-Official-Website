import { useState, useEffect } from "react";
import { useSearchParams } from "react-router-dom";
import AOS from "aos";
import "aos/dist/aos.css";
import { getEnv } from "../utils/env";
import Navbar from "../components/HomePage/Navbar";
import GreetingSection from "../components/HomePage/sections/GreetingSection";
import AboutSection from "../components/HomePage/sections/AboutSection";
import ClubsSection from "../components/HomePage/sections/ClubsSection";
import EventsSection from "../components/HomePage/sections/EventsSection";
import ProgramsSection from "../components/HomePage/sections/ProgramsSection";
import RadioSection from "../components/HomePage/sections/RadioSection";
import ServicesSection from "../components/HomePage/sections/ServicesSection";
import EventDetailsSidebar from "../components/HomePage/EventDetailsSidebar";
import ClubDetailsSidebar from "../components/HomePage/ClubDetailsSidebar";
import ProgramDetailsSidebar from "../components/HomePage/ProgramDetailsSidebar";
import ExclusiveFormDetailsSidebar from "../components/HomePage/ExclusiveFormDetailsSidebar";
import RegistrationModal from "../components/registration/RegistrationModal";
import ActiveFormsSection from "../components/HomePage/sections/ActiveFormsSection";
import Footer from "../components/HomePage/Footer";
import { fetchHomePageData, fetchActiveExclusiveForms } from "../services/homePageService";
import { FiClock } from "react-icons/fi";

const HomePage = () => {
  const [clubs, setClubs] = useState([]);
  const [committee, setCommittee] = useState([]);
  const [highBoard, setHighBoard] = useState([]);
  const [events, setEvents] = useState([]);
  const [programs, setPrograms] = useState([]);
  const [seasons, setSeasons] = useState([]);
  const [activeForms, setActiveForms] = useState([]);
  const [loading, setLoading] = useState(true);
  const [activeFormsLoading, setActiveFormsLoading] = useState(true);
  const [activeSection, setActiveSection] = useState("greeting");
  const [showFloatingButton, setShowFloatingButton] = useState(true);
  const [selectedEventId, setSelectedEventId] = useState(null);
  const [isSidebarOpen, setIsSidebarOpen] = useState(false);
  const [selectedClubId, setSelectedClubId] = useState(null);
  const [isClubSidebarOpen, setIsClubSidebarOpen] = useState(false);
  const [selectedProgramId, setSelectedProgramId] = useState(null);
  const [isProgramSidebarOpen, setIsProgramSidebarOpen] = useState(false);
  const [selectedCommitteeIdForReg, setSelectedCommitteeIdForReg] = useState(null);
  const [isCommitteeRegOpen, setIsCommitteeRegOpen] = useState(false);
  const [selectedExclusiveFormId, setSelectedExclusiveFormId] = useState(null);
  const [isExclusiveFormSidebarOpen, setIsExclusiveFormSidebarOpen] = useState(false);

  const handleApplyClick = (committeeId) => {
    setSelectedCommitteeIdForReg(committeeId);
    setIsCommitteeRegOpen(true);
  };

  const handleShowExclusiveFormDetails = (formId) => {
    setSelectedExclusiveFormId(formId);
    setIsExclusiveFormSidebarOpen(true);
  };

  const handleCloseExclusiveFormSidebar = () => {
    setIsExclusiveFormSidebarOpen(false);
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

  const handleShowProgramDetails = (programId) => {
    setSelectedProgramId(programId);
    setIsProgramSidebarOpen(true);
  };

  const handleCloseProgramSidebar = () => {
    setIsProgramSidebarOpen(false);
  };

  const [searchParams, setSearchParams] = useSearchParams();

  // Automatically restores sidebar state if returning from dynamic login redirects (User Request)
  useEffect(() => {
    const openEventId = searchParams.get("openEventId");
    const openClubId = searchParams.get("openClubId");
    const openCommitteeId = searchParams.get("openCommitteeId");
    const openProgramId = searchParams.get("openProgramId");
    const openExclusiveFormId = searchParams.get("openExclusiveFormId");

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
    } else if (openProgramId) {
      handleShowProgramDetails(Number(openProgramId));
      const newParams = new URLSearchParams(searchParams);
      newParams.delete("openProgramId");
      setSearchParams(newParams, { replace: true });
    } else if (openCommitteeId) {
      setSelectedCommitteeIdForReg(Number(openCommitteeId));
      setIsCommitteeRegOpen(true);
      const newParams = new URLSearchParams(searchParams);
      newParams.delete("openCommitteeId");
      setSearchParams(newParams, { replace: true });
    } else if (openExclusiveFormId) {
      handleShowExclusiveFormDetails(Number(openExclusiveFormId));
      const newParams = new URLSearchParams(searchParams);
      newParams.delete("openExclusiveFormId");
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
        setSeasons(data.seasons || []);
      } catch (err) {
        console.error("Error loading home page data:", err);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  useEffect(() => {
    const loadActiveForms = async () => {
        if (!isEnabled(getEnv("VITE_ENABLE_EXCLUSIVE_FORMS"))) {
            setActiveFormsLoading(false);
            return;
        }
        try {
            setActiveFormsLoading(true);
            const forms = await fetchActiveExclusiveForms();
            setActiveForms(forms || []);
        } catch (err) {
            console.error("Error loading active forms:", err);
        } finally {
            setActiveFormsLoading(false);
        }
    };
    loadActiveForms();
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
        setShowFloatingButton(window.scrollY < 300);

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
        {isEnabled(getEnv("VITE_ENABLE_ABOUT")) && (
          <AboutSection 
            loading={loading} 
            highBoard={highBoard} 
            committees={committee} 
            onApplyClick={handleApplyClick} 
            isRegistrationModalOpen={isCommitteeRegOpen}
            activeFormsLoading={activeFormsLoading}
            activeForms={activeForms}
            onShowExclusiveFormDetails={handleShowExclusiveFormDetails}
          />
        )}
        {isEnabled(getEnv("VITE_ENABLE_CLUBS")) && (
          <ClubsSection loading={loading} clubs={clubs} onShowClubDetails={handleShowClubDetails} />
        )}
        {isEnabled(getEnv("VITE_ENABLE_EVENTS")) && (
          <EventsSection
            loading={loading}
            events={events}
            onShowEventDetails={handleShowEventDetails}
          />
        )}
        {isEnabled(getEnv("VITE_ENABLE_PROGRAMS")) && (
          <ProgramsSection loading={loading} programs={programs} onShowProgramDetails={handleShowProgramDetails} />
        )}
        {isEnabled(getEnv("VITE_ENABLE_RADIO")) && (
          <RadioSection loading={loading} seasons={seasons} />
        )}
        {isEnabled(getEnv("VITE_ENABLE_SERVICES")) && (
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

      <ProgramDetailsSidebar
        programId={selectedProgramId}
        isOpen={isProgramSidebarOpen}
        onClose={handleCloseProgramSidebar}
      />

      <RegistrationModal
        isOpen={isCommitteeRegOpen}
        onClose={() => setIsCommitteeRegOpen(false)}
        entityId={selectedCommitteeIdForReg}
        type="committee"
        entityName={committee.find(c => c.id === selectedCommitteeIdForReg)?.name || ""}
      />

      <ExclusiveFormDetailsSidebar
        formId={selectedExclusiveFormId}
        isOpen={isExclusiveFormSidebarOpen}
        onClose={handleCloseExclusiveFormSidebar}
      />

      {/* Floating Button for Exclusive Opportunities */}
      {isEnabled(getEnv("VITE_ENABLE_EXCLUSIVE_FORMS")) && activeForms.length > 0 && (
        <div
          className={`fixed bottom-8 right-8 z-40 transition-all duration-500 transform ${
            showFloatingButton ? "translate-y-0 opacity-100" : "translate-y-16 opacity-0 pointer-events-none"
          }`}
        >
          <button
            onClick={() => {
              const element = document.getElementById("exclusive-forms");
              if (element) {
                const navHeight = 73;
                const elementPosition = element.getBoundingClientRect().top;
                const offsetPosition = elementPosition + window.scrollY - navHeight-10;
                window.scrollTo({
                  top: offsetPosition,
                  behavior: "smooth"
                });
              }
            }}
            className="flex items-center gap-2 px-6 py-3 bg-[#205E85] hover:bg-[#4B98C8] text-white font-bold rounded-full shadow-lg shadow-[#205E85]/30 hover:shadow-xl hover:scale-105 transition-all duration-300"
          >
            <FiClock className="w-5 h-5 animate-pulse" />
            Exclusive Opportunities
          </button>
        </div>
      )}
    </div>
  );
};

export default HomePage;
