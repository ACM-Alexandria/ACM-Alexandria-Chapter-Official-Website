import Navbar from "../components/HomePage/Navbar";
import Hero from "../components/HomePage/Hero";
import Footer from "../components/HomePage/Footer";

const HomePage = () => {
  return (
    <div className="min-h-screen flex flex-col">
      <Navbar />
      <main className="flex-1">
        <Hero />
      </main>
      <Footer />
    </div>
  );
};

export default HomePage;
