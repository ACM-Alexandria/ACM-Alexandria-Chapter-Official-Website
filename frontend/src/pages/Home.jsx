import React from "react";
import Navbar from "../components/Navbar";
import Hero from "../components/Hero";
import About from "../sections/About";

/**
 * Home page component - Landing page with hero and about sections
 */
const Home = () => {
  return (
    <>
      <Navbar />
      <Hero />
      <About />
    </>
  );
};

export default Home;
