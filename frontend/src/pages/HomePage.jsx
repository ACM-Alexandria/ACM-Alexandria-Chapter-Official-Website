import { useState, useEffect } from "react";
import AOS from "aos";
import "aos/dist/aos.css";
import Navbar from "../components/HomePage/Navbar";
import GreetingSection from "../components/HomePage/sections/GreetingSection";
import AboutSection from "../components/HomePage/sections/AboutSection";
import ClubsSection from "../components/HomePage/sections/ClubsSection";
import EventsSection from "../components/HomePage/sections/EventsSection";
import ProgramsSection from "../components/HomePage/sections/ProgramsSection";
import ServicesSection from "../components/HomePage/sections/ServicesSection";
import Footer from "../components/HomePage/Footer";
import { fetchHomePageData } from "../services/homePageService";

const HomePage = () => {
  const [clubs, setClubs] = useState([]);
  const [committee, setCommittee] = useState([]);
  const [highBoard, setHighBoard] = useState([]);
  const [events, setEvents] = useState([]);
  const [programs, setPrograms] = useState([]);
  const [_, setLoading] = useState(true);
  const [activeSection, setActiveSection] = useState("greeting");

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
      duration: 1000,
      once: true,
      offset: 100,
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

  return (
    <div className="min-h-screen flex flex-col">
      <Navbar activeSection={activeSection} />
      <main className="flex-1 pt-[70px]">
        <GreetingSection />
        <AboutSection highBoard={highBoard} committees={committee} />
        <ClubsSection clubs={clubs} />
        <EventsSection events={events} />
        <ProgramsSection programs={programs} />
        <ServicesSection />
      </main>
      <Footer />
    </div>
  );
};

export default HomePage;
