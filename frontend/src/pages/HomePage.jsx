import { useState, useEffect } from "react";
import Navbar from "../components/HomePage/Navbar";
import Hero from "../components/HomePage/Hero";
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
      <main className="flex-1">
        <Hero />
        {/* Data will be stored in state and can be used here */}
        {/* clubs: {JSON.stringify(clubs)} */}
        {/* committee: {JSON.stringify(committee)} */}
        {/* events: {JSON.stringify(events)} */}
      </main>
      <Footer />
    </div>
  );
};

export default HomePage;
