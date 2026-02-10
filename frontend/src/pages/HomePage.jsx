import { useState, useEffect } from "react";
import Navbar from "../components/HomePage/Navbar";
import GreetingSection from "../components/HomePage/GreetingSection";
import AboutSection from "../components/HomePage/AboutSection";
import ClubsSection from "../components/HomePage/ClubsSection";
import EventsSection from "../components/HomePage/EventsSection";
import ProgramsSection from "../components/HomePage/ProgramsSection";
import ServicesSection from "../components/HomePage/ServicesSection";
import Footer from "../components/HomePage/Footer";
import { fetchHomePageData } from "../services/homePageService";

const HomePage = () => {
  const [clubs, setClubs] = useState([]);
  const [committee, setCommittee] = useState([]);
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const loadData = async () => {
      try {
        setLoading(true);
        const data = await fetchHomePageData();

        setClubs(data.clubs);
        setCommittee(data.committee);
        setEvents(data.events);
      } catch (err) {
        setError(err);
        console.error("Error loading home page data:", err);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, []);

  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <main className="flex-1 pt-[70px]">
        <GreetingSection />
        <AboutSection />
        <ClubsSection clubs={clubs} />
        <EventsSection events={events} />
        <ProgramsSection />
        <ServicesSection />
      </main>
      <Footer />
    </div>
  );
};

export default HomePage;
